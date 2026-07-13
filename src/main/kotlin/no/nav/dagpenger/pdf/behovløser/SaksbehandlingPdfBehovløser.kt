package no.nav.dagpenger.pdf.behovløser

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import io.ktor.util.decodeBase64String
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.pdf.generator.PdfBuilder
import no.nav.dagpenger.pdf.lagring.Lagring
import no.nav.dagpenger.pdf.lagring.PdfDokument
import no.nav.dagpenger.pdf.observability.PdfFlyt
import no.nav.dagpenger.pdf.observability.PdfSteg
import no.nav.dagpenger.pdf.observability.konteksttype
import no.nav.dagpenger.pdf.observability.målPdfGenerering
import no.nav.dagpenger.pdf.observability.målPdfLagring
import no.nav.dagpenger.pdf.observability.målPdfRendering
import no.nav.dagpenger.pdf.observability.registrerHtmlStørrelse
import no.nav.dagpenger.pdf.observability.registrerPdfFeil
import no.nav.dagpenger.pdf.observability.registrerPdfStørrelse

internal class SaksbehandlingPdfBehovløser(
    rapidsConnection: RapidsConnection,
    private val lagring: Lagring,
) : River.PacketListener {
    companion object {
        private val logg = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall")
        const val BEHOV = "SaksbehandlingPdfBehov"
    }

    init {
        River(rapidsConnection)
            .apply {
                precondition {
                    validate { it.requireValue("@event_name", "behov") }
                    validate { it.requireAll("@behov", listOf(BEHOV)) }
                    validate { it.forbid("@løsning") }
                }
                validate { it.requireKey("ident", "htmlBase64", "dokumentNavn", "kontekst") }
                validate { it.interestedIn("@id") }
            }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val kontekst = packet["kontekst"].asText()
        val flyt = PdfFlyt.SAKSBEHANDLING_PDF_BEHOV
        var steg = PdfSteg.DEKODING

        withLoggingContext("id" to packet["@id"].asText(), "kontekst" to kontekst) {
            logg.info { "Mottok behov for å lage pdf" }
            val startTid = System.nanoTime()
            try {
                målPdfGenerering(flyt) {
                    val ident = packet["ident"].asText()
                    val html =
                        packet["htmlBase64"].asText().decodeBase64String().also {
                            sikkerlogg.info { "Skal lage pdf av HTML: $it" }
                        }
                    registrerHtmlStørrelse(flyt, html.toByteArray().size)
                    val dokumentNavn = packet["dokumentNavn"].asText()

                    steg = PdfSteg.RENDERING
                    logg.info { "Starter pdf-rendering" }
                    val pdf = målPdfRendering(flyt) { PdfBuilder.lagPdf(html = html) }
                    registrerPdfStørrelse(flyt, pdf.size)
                    logg.info { "Fullførte pdf-rendering, størrelse ${pdf.size} bytes" }

                    val pdfDokument =
                        PdfDokument(
                            navn = dokumentNavn,
                            eier = ident,
                            pdf = pdf,
                        )

                    steg = PdfSteg.LAGRING
                    logg.info { "Lagrer pdf hos dp-mellomlagring" }
                    val lagretDokument =
                        målPdfLagring(flyt, konteksttype(kontekst)) {
                            runBlocking { lagring.lagre(kontekst, pdfDokument).first() }
                        }
                    logg.info { "Lagret pdf, urn=${lagretDokument.urn}" }

                    steg = PdfSteg.PUBLISERING
                    packet["@løsning"] =
                        mapOf(
                            BEHOV to
                                mapOf(
                                    "metainfo" to
                                        mapOf(
                                            "dokumentNavn" to lagretDokument.filnavn,
                                            "dokumentType" to "PDF",
                                        ),
                                    "urn" to lagretDokument.urn,
                                ),
                        )
                    context.publish(ident, packet.toJson())

                    val varighetMs = (System.nanoTime() - startTid) / 1_000_000
                    logg.info { "Fullførte pdf-generering på ${varighetMs}ms, størrelse ${pdf.size} bytes" }
                }
            } catch (e: Exception) {
                registrerPdfFeil(flyt, steg)
                sikkerlogg.error(e) { "Feil ved generering av pdf i steg ${steg.label}" }
                throw e
            }
        }
    }
}

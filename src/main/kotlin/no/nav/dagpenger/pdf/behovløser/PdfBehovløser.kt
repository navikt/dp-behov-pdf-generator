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
import no.nav.dagpenger.pdf.html.lagHtml
import no.nav.dagpenger.pdf.lagring.Lagring
import no.nav.dagpenger.pdf.lagring.PdfDokument

internal class PdfBehovløser(
    rapidsConnection: RapidsConnection,
    private val lagring: Lagring,
) : River.PacketListener {
    companion object {
        private val logg = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall")
        const val BEHOV = "PdfBehov"
    }

    init {
        River(rapidsConnection)
            .apply {
                precondition {
                    validate { it.requireValue("@event_name", "behov") }
                    validate { it.requireAll("@behov", listOf(BEHOV)) }
                    validate { it.forbid("@løsning") }
                }
                validate { it.requireKey("ident", "htmlBase64", "dokumentNavn", "kontekst", "sak") }
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
        withLoggingContext("id" to packet["@id"].asText(), "kontekst" to kontekst) {
            val ident = packet["ident"].asText()
            val html = packet["htmlBase64"].asText().decodeBase64String()
            val dokumentNavn = packet["dokumentNavn"].asText()
            val sakId = packet["sak"]["id"].asText()
            val pdf = PdfBuilder.lagPdf(html = lagHtml(sakId = sakId, htmlBody = html))

            val pdfDokument =
                PdfDokument(
                    navn = dokumentNavn,
                    eier = ident,
                    pdf = pdf,
                )

            runBlocking {
                try {
                    val lagretDokument = lagring.lagre(kontekst, pdfDokument).first()
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
                    context.publish(packet.toJson())
                } catch (e: Exception) {
                    sikkerlogg.error(e) { "Feil ved generering av pdf. Html: $html" }
                    throw e
                }
            }
        }
    }
}

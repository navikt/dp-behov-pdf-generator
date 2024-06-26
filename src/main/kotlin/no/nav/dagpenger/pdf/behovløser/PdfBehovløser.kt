package no.nav.dagpenger.pdf.behovløser

import io.ktor.util.decodeBase64String
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.pdf.generator.PdfBuilder
import no.nav.dagpenger.pdf.lagring.Lagring
import no.nav.dagpenger.pdf.lagring.PdfDokument
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class PdfBehovløser(
    rapidsConnection: RapidsConnection,
    private val lagring: Lagring,
) : River.PacketListener {
    companion object {
        private val logg = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall")
        const val BEHOV = "PdfBehov"

        val rapidFilter: River.() -> Unit = {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.demandAll("@behov", listOf(BEHOV)) }
            validate { it.requireKey("ident", "htmlBase64", "dokumentNavn", "kontekst") }
            validate { it.rejectKey("@løsning") }
            validate { it.interestedIn("@id") }
        }
    }

    init {
        River(rapidsConnection).apply {
            rapidFilter()
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        withLoggingContext("id" to packet["@id"].asText()) {
            val ident = packet["ident"].asText()
            val html = packet["htmlBase64"].asText().decodeBase64String()
            val dokumentNavn = packet["dokumentNavn"].asText()
            val kontekst = packet["kontekst"].asText()
            val pdf = PdfBuilder.lagPdf(html)

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
                    sikkerlogg.error(e) { "Feil ved generering av pdf" }
                }
            }
        }
    }
}

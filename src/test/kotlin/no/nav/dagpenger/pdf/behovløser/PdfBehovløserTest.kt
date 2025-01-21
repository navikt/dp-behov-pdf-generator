package no.nav.dagpenger.pdf.behovløser

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.kotest.assertions.json.shouldEqualSpecifiedJsonIgnoringOrder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.util.encodeBase64
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import no.nav.dagpenger.pdf.lagring.Lagring
import no.nav.dagpenger.pdf.lagring.PdfDokument
import no.nav.dagpenger.pdf.lagring.URNResponse
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class PdfBehovløserTest {
    private val testRapid = TestRapid()

    @Test
    fun `skal lage pdf og lagre fila i skyen`() {
        //language=HTML
        val htmlBrevAsBase64 =
            """
          <html><body><h1>Hei</h1></body></html>
        """.encodeBase64()

        val ident = "12345"
        val dokumentNavn = "vedtaksbrev.pdf"
        val kontekst = "opppgave/oppgaveId"

        val slot = slot<PdfDokument>()
        val pdfLagring =
            mockk<Lagring>().also {
                coEvery { it.lagre(kontekst, capture(slot)) } returns
                    listOf(
                        URNResponse(
                            filnavn = dokumentNavn,
                            urn = "urn:kontekt:enuuid",
                            filsti = "filstil",
                            storrelse = 5,
                            tidspunkt = ZonedDateTime.now(),
                        ),
                    )
            }

        PdfBehovløser(testRapid, pdfLagring)

        testRapid.sendTestMessage(
            testMelding(
                htmlBrevAsBase64 = htmlBrevAsBase64,
                dokumentNavn = dokumentNavn,
                ident = ident,
                kontekst = kontekst,
                løsning = null,
                sakId = "saksnummer",
            ),
        )

        testRapid.inspektør.size shouldBe 1
        slot.captured.let {
            it.navn shouldBe dokumentNavn
            it.eier shouldBe ident
        }

        testRapid.inspektør.message(0)["@løsning"].toString() shouldEqualSpecifiedJsonIgnoringOrder
            //language=JSON
            """
                {
                  "PdfBehov": {
                      "metainfo": {
                        "dokumentNavn": "$dokumentNavn",
                        "dokumentType": "PDF"
                      },
                      "urn": "urn:kontekt:enuuid"
                  }
                }
                """
    }

    @Test
    fun `Skal ikke håndtere pakker der en løsning allerede finnes`() {
        val mock = mockk<Lagring>()
        PdfBehovløser(testRapid, mock)
        testRapid.sendTestMessage(
            testMelding(
                htmlBrevAsBase64 = "base64",
                dokumentNavn = "dokumentNAvn",
                ident = "123",
                kontekst = "kontekst",
                løsning = true,
            ),
        )

        coVerify(exactly = 0) { mock.lagre(any(), any()) }
    }

    @Test
    fun `Skal ikke håndtere pakker der event name ikke er behov`() {
        val mock = mockk<Lagring>()
        PdfBehovløser(testRapid, mock)
        testRapid.sendTestMessage(
            testMelding(
                eventName = "ikke_behov",
                htmlBrevAsBase64 = "base64",
                dokumentNavn = "dokumentNAvn",
                ident = "123",
                kontekst = "kontekst",
            ),
        )

        coVerify(exactly = 0) { mock.lagre(any(), any()) }
    }

    @Test
    fun `Skal ikke håndtere pakker der  behov ikke er PdfBehov`() {
        val mock = mockk<Lagring>()
        PdfBehovløser(testRapid, mock)
        testRapid.sendTestMessage(
            testMelding(
                behov = "ikke_PdfBehov",
                htmlBrevAsBase64 = "base64",
                dokumentNavn = "dokumentNAvn",
                ident = "123",
                kontekst = "kontekst",
            ),
        )

        coVerify(exactly = 0) { mock.lagre(any(), any()) }
    }

    @Test
    fun `Skal kaste feil når det oppstår feil ved generering eller lagring av pdf`() {
        val htmlBrevAsBase64 =
            """
          tull
        """.encodeBase64()

        val ident = "12345"
        val dokumentNavn = "vedtaksbrev.pdf"
        val kontekst = "opppgave/oppgaveId"

        val pdfLagring =
            mockk<Lagring>().also {
                coEvery { it.lagre(any(), any()) } throws RuntimeException("Feil ved lagring")
            }

        PdfBehovløser(testRapid, pdfLagring)
        shouldThrow<RuntimeException> {
            testRapid.sendTestMessage(
                testMelding(
                    htmlBrevAsBase64 = htmlBrevAsBase64,
                    dokumentNavn = dokumentNavn,
                    ident = ident,
                    kontekst = kontekst,
                    løsning = null,
                    sakId = "saksnummer",
                ),
            )
        }
    }

    private fun testMelding(
        eventName: String = "behov",
        behov: String = "PdfBehov",
        htmlBrevAsBase64: String,
        dokumentNavn: String,
        ident: String,
        kontekst: String,
        løsning: Boolean? = null,
        sakId: String = "saksnummer",
    ): String {
        return """
            {
               "@event_name": "$eventName",
               "@behov": [
                 "$behov"
               ],
               "htmlBase64": "$htmlBrevAsBase64",
               "dokumentNavn": "$dokumentNavn",
               "ident": "$ident",
               "kontekst": "$kontekst",
               "sak": {
                 "id": "$sakId",
                 "kontekst": "kontekst"
                 }
                ${løsning?.let { ""","@løsning": $it""" } ?: ""}
            }
            """.trimIndent()
    }
}

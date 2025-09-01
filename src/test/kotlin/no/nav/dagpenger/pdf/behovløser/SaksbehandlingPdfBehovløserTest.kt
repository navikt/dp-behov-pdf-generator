package no.nav.dagpenger.pdf.behovløser

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.kotest.assertions.json.shouldEqualSpecifiedJsonIgnoringOrder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.util.encodeBase64
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import no.nav.dagpenger.pdf.lagring.Lagring
import no.nav.dagpenger.pdf.lagring.PdfDokument
import no.nav.dagpenger.pdf.lagring.URNResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class SaksbehandlingPdfBehovløserTest {
    private val testRapid = TestRapid()
    private val lagringMock = mockk<Lagring>()
    private val behovløser = SaksbehandlingPdfBehovløser(testRapid, lagringMock)
    private val slot = slot<PdfDokument>()
    private val ident = "12345"
    private val dokumentNavn = "vedtaksbrev.pdf"
    private val kontekst = "behandling/behandlingId"
    private val htmlBrevAsBase64 =
        """
          <html><body><h1>Hei</h1></body></html>
        """.encodeBase64()

    @BeforeEach
    fun setup() {
        testRapid.reset()
        clearAllMocks()
    }

    @Test
    fun `skal lage pdf og lagre fila i skyen`() {
        //language=HTML
        coEvery { lagringMock.lagre(kontekst, capture(slot)) } returns
            listOf(
                URNResponse(
                    filnavn = dokumentNavn,
                    urn = "urn:kontekt:enuuid",
                    filsti = "filstil",
                    storrelse = 5,
                    tidspunkt = ZonedDateTime.now(),
                ),
            )

        testRapid.sendTestMessage(
            testMelding(),
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
                  "SaksbehandlingPdfBehov": {
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
        testRapid.sendTestMessage(
            testMelding(løsning = true),
        )

        coVerify(exactly = 0) { lagringMock.lagre(any(), any()) }
    }

    @Test
    fun `Skal ikke håndtere pakker der event name ikke er behov`() {
        testRapid.sendTestMessage(
            testMelding(eventName = "ikke_behov"),
        )

        coVerify(exactly = 0) { lagringMock.lagre(any(), any()) }
    }

    @Test
    fun `Skal ikke håndtere pakker der behov ikke er PdfBehov`() {
        testRapid.sendTestMessage(testMelding(behov = "ikke_PdfBehov"))
        coVerify(exactly = 0) { lagringMock.lagre(any(), any()) }
    }

    @Test
    fun `Skal kaste feil når det oppstår feil ved generering eller lagring av pdf`() {
        coEvery { lagringMock.lagre(any(), any()) } throws RuntimeException("Feil ved lagring")
        shouldThrow<RuntimeException> {
            testRapid.sendTestMessage(
                testMelding(),
            )
        }
    }

    private fun testMelding(
        eventName: String = "behov",
        behov: String = "SaksbehandlingPdfBehov",
        htmlBrevAsBase64: String = this.htmlBrevAsBase64,
        dokumentNavn: String = this.dokumentNavn,
        ident: String = this.ident,
        kontekst: String = this.kontekst,
        løsning: Boolean? = null,
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
               "kontekst": "$kontekst"
                ${løsning?.let { ""","@løsning": $it""" } ?: ""}
            }
            """.trimIndent()
    }
}

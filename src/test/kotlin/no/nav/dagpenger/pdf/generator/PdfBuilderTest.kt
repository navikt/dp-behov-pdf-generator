package no.nav.dagpenger.pdf.generator

import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeTrue
import no.nav.dagpenger.pdf.html.lagHtml
import no.nav.dagpenger.pdf.les
import no.nav.dagpenger.pdf.skrivTilFil
import org.junit.jupiter.api.Test
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider
import org.verapdf.pdfa.Foundries
import org.verapdf.pdfa.flavours.PDFAFlavour
import org.verapdf.pdfa.results.TestAssertion
import java.io.ByteArrayInputStream

internal class PdfBuilderTest {
    @Test
    fun `Kan lage PDF som møter PdfA og UA-standardene fra enkel HTML`() {
        val htmlBody = "/html/enkel.html".les()
        val html =
            lagHtml(sakId = "149520214", htmlBody = htmlBody).also {
                it.skrivTilFil("build/test.html")
            }
        VeraGreenfieldFoundryProvider.initialise()
        Foundries.defaultInstance().use { foundry ->
            val pdf = PdfBuilder.lagPdf(html)
            val pdfInputStream =
                ByteArrayInputStream(
                    pdf,
                )
            val validator = foundry.createValidator(PDFAFlavour.PDFA_2_U, true)
            foundry.createParser(pdfInputStream, PDFAFlavour.PDFA_2_U).also { parser ->
                val result =
                    validator.validate(parser).testAssertions.filter {
                        it.status == TestAssertion.Status.FAILED
                    }.distinctBy { it.ruleId }
                withClue(
                    "PDF-A verifisering feiler på :\n ${result.map { it.ruleId }}," +
                        " se https://docs.verapdf.org/validation/pdfa-parts-2-and-3/",
                ) {
                    result.isEmpty().shouldBeTrue()
                }
            }

            pdf.skrivTilFil("build/test.pdf")
        }
    }
}

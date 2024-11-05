package no.nav.dagpenger.pdf.generator

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.io.File

internal object PdfBuilder {
    private val sikkerlogg = KotlinLogging.logger("tjenestekall")

    internal fun lagPdf(html: String): ByteArray {
        return try {
            ByteArrayOutputStream().use {
                PdfRendererBuilder()
                    .useFont(File("src/main/resources/OpenSans-Regular.ttf"), "Open Sans")
                    .usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_2_U)
                    .useSVGDrawer(BatikSVGDrawer())
                    .usePdfUaAccessibility(true)
                    .useColorProfile("/sRGB2014.icc".fileAsByteArray())
                    .defaultTextDirection(BaseRendererBuilder.TextDirection.LTR)
                    .withHtmlContent(html, null)
                    .toStream(it)
                    .run()
                it.toByteArray()
            }
        } catch (e: Exception) {
            sikkerlogg.error(e) { "Kunne ikke lage PDF av søknaden. HTML=$html" }
            throw e
        }
    }
}

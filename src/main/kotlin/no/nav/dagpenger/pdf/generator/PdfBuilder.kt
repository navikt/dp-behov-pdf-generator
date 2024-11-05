package no.nav.dagpenger.pdf.generator

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import mu.KotlinLogging
import no.nav.dagpenger.pdf.utils.fileAsByteArray
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

internal object PdfBuilder {
    private val sikkerlogg = KotlinLogging.logger("tjenestekall")

    internal fun lagPdf(html: String): ByteArray {
        return try {
            val font = this::class.java.getResource("/OpenSans-Regular.ttf")!!.readBytes()

            ByteArrayOutputStream().use {
                PdfRendererBuilder()
                    .useFont({ ByteArrayInputStream(font) }, "Open Sans")
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

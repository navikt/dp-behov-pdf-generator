package no.nav.dagpenger.pdf.generator

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import com.openhtmltopdf.pdfboxout.PDFontSupplier
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import mu.KotlinLogging
import no.nav.dagpenger.pdf.utils.fileAsByteArray
import org.apache.fontbox.ttf.TTFParser
import org.apache.pdfbox.io.RandomAccessReadBufferedFile
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.PDType0Font
import java.io.ByteArrayOutputStream

internal object PdfBuilder {
    private val sikkerlogg = KotlinLogging.logger("tjenestekall")

    private data class Font(
        val family: String,
        val path: String,
        val weight: Int,
        val style: BaseRendererBuilder.FontStyle,
        val subset: Boolean,
    )

    private val fonts: List<Font> =
        listOf(
            Font(
                family = "Source Sans 3",
                path = "/fonts/static/SourceSans3-Regular.ttf",
                weight = 400,
                style = BaseRendererBuilder.FontStyle.NORMAL,
                subset = false,
            ),
            Font(
                family = "Source Sans 3",
                path = "/fonts/static/SourceSans3-Italic.ttf",
                weight = 300,
                style = BaseRendererBuilder.FontStyle.ITALIC,
                subset = false,
            ),
            Font(
                family = "Source Sans 3",
                path = "/fonts/static/SourceSans3-Bold.ttf",
                weight = 700,
                style = BaseRendererBuilder.FontStyle.NORMAL,
                subset = false,
            ),
        )

    internal fun lagPdf(html: String): ByteArray {
        return try {
            ByteArrayOutputStream().use {
                PdfRendererBuilder()
                    .apply {
                        for (font in fonts) {
                            val ttf =
                                TTFParser()
                                    .parse(
                                        RandomAccessReadBufferedFile("src/main/resources" + font.path),
                                    )
                                    .also { it.isEnableGsub = false }
                            useFont(
                                PDFontSupplier(PDType0Font.load(PDDocument(), ttf, font.subset)),
                                font.family,
                                font.weight,
                                font.style,
                                font.subset,
                            )
                        }
                    }
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

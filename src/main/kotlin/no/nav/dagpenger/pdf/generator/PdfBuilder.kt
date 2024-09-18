package no.nav.dagpenger.pdf.generator

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.io.InputStream

internal object PdfBuilder {
    private val sikkerlogg = KotlinLogging.logger("tjenestekall")

    private data class Font(
        val family: String,
        val path: String,
        val weight: Int,
        val style: BaseRendererBuilder.FontStyle,
        val subset: Boolean,
    ) {
        fun inputStreamSupplier(): () -> InputStream = { path.fileAsInputStream() }
    }

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
                PdfRendererBuilder().apply {
                    fonts.forEach { font ->
                        useFont(
                            // supplier =
                            font.inputStreamSupplier(),
                            // fontFamily =
                            font.family,
                            // fontWeight =
                            font.weight,
                            // fontStyle =
                            font.style,
                            // subset =
                            font.subset,
                        )
                    }
                }
                    .useDefaultPageSize(157.7F, 223.4F, BaseRendererBuilder.PageSizeUnits.MM)
                    .usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_2_U)
                    .useSVGDrawer(BatikSVGDrawer())
                    .usePdfUaAccessbility(true)
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

package no.nav.dagpenger.pdf.generator

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import com.openhtmltopdf.pdfboxout.PDFontSupplier
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.dagpenger.pdf.html.cleanHtml
import no.nav.dagpenger.pdf.utils.fileAsByteArray
import org.apache.fontbox.ttf.TTFParser
import org.apache.pdfbox.io.RandomAccessReadBuffer
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.PDType0Font
import java.io.ByteArrayOutputStream

internal object PdfBuilder {
    private val logg = KotlinLogging.logger {}
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

    private val ttf =
        fonts.associateWith { front ->
            TTFParser()
                .parse(RandomAccessReadBuffer(front.path.fileAsByteArray()))
                .apply { isEnableGsub = false }
        }

    private val colorProfile = "/sRGB2014.icc".fileAsByteArray()

    internal fun lagPdf(html: String): ByteArray =
        try {
            val cleanHtml = html.cleanHtml()
            PDDocument().use { document ->
                val builder =
                    PdfRendererBuilder()
                        .apply {
                            ttf.forEach { (font, ttf) ->
                                useFont(
                                    PDFontSupplier(PDType0Font.load(document, ttf, font.subset)),
                                    font.family,
                                    font.weight,
                                    font.style,
                                    font.subset,
                                )
                            }
                        }.usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_2_U)
                        .useSVGDrawer(BatikSVGDrawer())
                        .usePdfUaAccessibility(true)
                        .useColorProfile(colorProfile)
                        .defaultTextDirection(BaseRendererBuilder.TextDirection.LTR)
                        .withHtmlContent(cleanHtml, null)

                ByteArrayOutputStream().use {
                    builder
                        .toStream(it)
                        .run()
                    it.toByteArray()
                }
            }
        } catch (e: Exception) {
            logg.error(e) { "Kunne ikke lage PDF" }
            sikkerlogg.error(e) { "Kunne ikke lage PDF. HTML=$html" }
            throw e
        }
}

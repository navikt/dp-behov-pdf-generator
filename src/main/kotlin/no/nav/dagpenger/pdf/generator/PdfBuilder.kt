package no.nav.dagpenger.pdf.generator

import com.openhtmltopdf.extend.impl.FSDefaultCacheStore
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder.CacheStore.PDF_FONT_METRICS
import com.openhtmltopdf.svgsupport.BatikSVGDrawer
import mu.KotlinLogging
import no.nav.dagpenger.pdf.utils.fileAsByteArray
import org.apache.fontbox.ttf.TTFParser
import org.apache.pdfbox.io.RandomAccessReadBuffer
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

internal object PdfBuilder {
    private val logg = KotlinLogging.logger {}
    private val sikkerlogg = KotlinLogging.logger("tjenestekall")

    fun getFileFromClasspath(fileName: String): File {
        val resource = javaClass.getResource(fileName) ?: throw FileNotFoundException("Resource not found: $fileName")
        return File(resource.toURI())
    }

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

    private val ttf =
        fonts.associateWith { front ->
            TTFParser()
                .parse(RandomAccessReadBuffer(front.path.fileAsByteArray()))
                .apply { isEnableGsub = false }
        }

    private val colorProfile = "/sRGB2014.icc".fileAsByteArray()
    private val cache = FSDefaultCacheStore()

    internal fun lagPdf(html: String): ByteArray =
        try {
            PDDocument().use { document ->
                val builder =
                    PdfRendererBuilder()
                        .apply {
                            ttf.forEach { (font, ttf) ->
                                useFont(
                                    font.inputStreamSupplier(),
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
                        .useCacheStore(PDF_FONT_METRICS, cache)
                        .defaultTextDirection(BaseRendererBuilder.TextDirection.LTR)
                        .withHtmlContent(html, null)

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

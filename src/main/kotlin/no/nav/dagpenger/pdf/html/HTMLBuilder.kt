package no.nav.dagpenger.pdf.html

import kotlinx.html.body
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.lang
import kotlinx.html.meta
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe
import no.nav.dagpenger.pdf.utils.fileAsString
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun lagHtml(body: String): String {
    return createHTML(prettyPrint = false, xhtmlCompatible = true).html {
        val css = "/css/styling.css".fileAsString()
        lang = "no"
        head {
            title = "Dokument"
            meta(name = "description", content = "Enkelt html")
            style {
                unsafe {
                    raw(
                        css,
                    )
                }
            }
        }
        body {
            unsafe { raw(body.clean()) }
        }
    }.let {
        val doc = Jsoup.parse(it)
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml)
        doc.html()
    }
}

private fun String.clean() =
    this
        .replace("&nbsp;", " ")
        .replace("\u001d", "") // Group separator character
        .replace("\u001c", "") // File separator character

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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun lagHtml(
    sakId: String,
    htmlBody: String,
): String {
    return createHTML(prettyPrint = false, xhtmlCompatible = true).html {
        val styleTagStart = htmlBody.indexOf("<style>")
        val styleStart = styleTagStart + "<style>".length
        val styleEnd = htmlBody.indexOf("</style>")
        val styleTagEnd = styleEnd + "</style>".length

        var css = css(sakId)
        var html = htmlBody

        if (styleTagStart > -1) {
            css += htmlBody.substring(styleStart, styleEnd)
            html = htmlBody.substring(styleTagEnd)
        }

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
            unsafe { raw(html.clean()) }
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

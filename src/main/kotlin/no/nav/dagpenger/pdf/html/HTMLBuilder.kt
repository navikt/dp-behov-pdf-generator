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
): String =
    createHTML(prettyPrint = false, xhtmlCompatible = true)
        .html {
            val css = css(sakId)
            lang = "no"
            head {
                title("Vedtak fra NAV")
                meta(name = "description", content = "Vedtak fra NAV")
                meta(name = "subject", content = "Vedtak fra NAV")
                meta(name = "author", content = "NAV")
                style {
                    unsafe {
                        raw(
                            css,
                        )
                    }
                }
            }
            body {
                unsafe { raw(htmlBody.clean()) }
            }
        }.let {
            val doc = Jsoup.parse(it)
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml)
            doc.html()
        }

private fun String.clean() =
    this
        .replace("&nbsp;", " ")
        .replace("\u001d", "") // Group separator character
        .replace("\u001c", "") // File separator character

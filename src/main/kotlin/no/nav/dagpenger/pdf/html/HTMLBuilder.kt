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

fun lagHtml(body: String): String {
    return createHTML(prettyPrint = false, xhtmlCompatible = true).html {
        lang = "no"
        head {
            title = "Dokument"
            meta(name = "description", content = "Enkelt html")
            style {
                unsafe {
                    raw(
                        """
                        body {
                            font-family: 'Source Sans Pro';
                            font-style: normal;
                            width: 600px;
                            padding: 0 40px 40px 40px;
                            color: rgb(38, 38, 38);
                        }
                        """,
                    )
                }
            }
        }
        body {
            unsafe { raw(body) }
        }
    }.replace("&nbsp;", " ")
        .replace("\u001d", "") // Group separator character
        .replace("\u001c", "") // File separator character
}

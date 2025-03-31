package no.nav.dagpenger.pdf.generator

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail
import no.nav.dagpenger.pdf.html.lagHtml

fun Application.pdfGeneratorApi() {
    routing {
        post("/convert-html-to-pdf/{sakId}") {
            val sakId = call.parameters.getOrFail("sakId")
            val css = call.request.queryParameters["css"] ?: ""

            // Receive the HTML content as a string
            val htmlContent = call.receiveText()

            if (htmlContent.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "HTML content is empty")
                return@post
            }
            PdfBuilder.lagPdf(lagHtml(sakId = sakId, htmlBody = htmlContent, additionalCss = css)).let {
                call.respond(HttpStatusCode.OK, it)
            }
        }

        post("/convert-html-to-pdf") {
            // Receive the HTML content as a string
            val htmlContent = call.receiveText()

            if (htmlContent.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "HTML content is empty")
                return@post
            }
            PdfBuilder.lagPdf(htmlContent).let {
                call.respond(HttpStatusCode.OK, it)
            }
        }
    }
}

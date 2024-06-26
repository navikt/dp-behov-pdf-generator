package no.nav.dagpenger.pdf.generator

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.pdfGeneratorApi() {
    install(ContentNegotiation) {
        jackson()
    }

    routing {
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

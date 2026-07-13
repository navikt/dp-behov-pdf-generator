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
import no.nav.dagpenger.pdf.observability.PdfFlyt
import no.nav.dagpenger.pdf.observability.målPdfGenerering
import no.nav.dagpenger.pdf.observability.målPdfRendering
import no.nav.dagpenger.pdf.observability.registrerHtmlStørrelse
import no.nav.dagpenger.pdf.observability.registrerPdfStørrelse

fun Application.pdfGeneratorApi() {
    routing {
        post("/convert-html-to-pdf/{sakId}") {
            val sakId = call.parameters.getOrFail("sakId")
            // Receive the HTML content as a string
            val htmlContent = call.receiveText()

            if (htmlContent.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "HTML content is empty")
                return@post
            }
            call.respond(HttpStatusCode.OK, lagPdfOgMål(PdfFlyt.API_MED_SAKID, lagHtml(sakId = sakId, htmlBody = htmlContent)))
        }

        post("/convert-html-to-pdf") {
            // Receive the HTML content as a string
            val htmlContent = call.receiveText()

            if (htmlContent.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "HTML content is empty")
                return@post
            }
            call.respond(HttpStatusCode.OK, lagPdfOgMål(PdfFlyt.API_RÅ, htmlContent))
        }
    }
}

// Måler hele genereringen og selve renderingen, og registrerer størrelsen på HTML og ferdig pdf.
private fun lagPdfOgMål(
    flyt: PdfFlyt,
    html: String,
): ByteArray =
    målPdfGenerering(flyt) {
        registrerHtmlStørrelse(flyt, html.toByteArray().size)
        målPdfRendering(flyt) { PdfBuilder.lagPdf(html) }
            .also { registrerPdfStørrelse(flyt, it.size) }
    }

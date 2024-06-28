package no.nav.dagpenger.pdf.generator

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.server.testing.testApplication
import io.ktor.util.toByteArray
import no.nav.dagpenger.pdf.les
import org.junit.jupiter.api.Test
import java.io.File

class PdfGeneratorApiKtTest {
    @Test
    fun `lage pdf via api`() {
        testApplication {
            application {
                pdfGeneratorApi()
            }

            client.post {
                url(urlString = "/convert-html-to-pdf")
                setBody("/html/enkel.html".les())
            }.bodyAsChannel().toByteArray().let {
                File("build/test.pdf").writeBytes(it)
            }
        }
    }
}

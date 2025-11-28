package no.nav.dagpenger.pdf.lagring

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class LagringImplTest {
    @Test
    fun `Skal kalle lagring med riktig headers og parse json svar`() {
        runBlocking {
            var httpRequestData: HttpRequestData? = null
            val eier = "eier"
            val jsonResponse =
                """
                 [
                     {
                        "filnavn": "navn.pdf",
                        "urn": "urn:konteskst:navn.pdf",
                        "filsti": "filsti",
                        "storrelse": 123,
                        "tidspunkt": "${ZonedDateTime.now()}" 
                    }
                ]
                """.trimIndent()

            val lagringImpl =
                LagringImpl(
                    baseUrl = "http://localhost:8080",
                    tokenSupplier = { "token" },
                    engine =
                        MockEngine { request ->
                            httpRequestData = request
                            respond(jsonResponse, headers = headersOf("Content-Type" to listOf("application/json")))
                        },
                )

            lagringImpl
                .lagre(
                    "kontekst",
                    PdfDokument(
                        navn = "navn",
                        eier = eier,
                        pdf = "pdf".toByteArray(),
                    ),
                ).let { response ->
                    response.size shouldBe 1
                    with(response.first()) {
                        filnavn shouldBe "navn.pdf"
                        urn shouldBe "urn:konteskst:navn.pdf"
                        filsti shouldBe "filsti"
                        storrelse shouldBe 123
                    }
                }

            requireNotNull(httpRequestData)
            httpRequestData?.let {
                it.headers[HttpHeaders.Authorization] shouldBe "Bearer token"
                it.headers[HttpHeaders.Accept] shouldBe "application/json"
                it.headers["X-Eier"] shouldBe eier
            }
        }
    }

    @Test
    fun `Skal kaste feil dersom kall mot lagring feiler`() {
        runBlocking {
            shouldThrow<ClientRequestException> {
                LagringImpl(
                    baseUrl = "http://localhost:8080",
                    tokenSupplier = { "token" },
                    engine =
                        MockEngine {
                            respondBadRequest()
                        },
                ).lagre(
                    kontekst = "ac",
                    pdfDokument =
                        PdfDokument(
                            navn = "sumo",
                            eier = "donec",
                            pdf = "pdf".toByteArray(),
                        ),
                )
            }
        }
    }
}

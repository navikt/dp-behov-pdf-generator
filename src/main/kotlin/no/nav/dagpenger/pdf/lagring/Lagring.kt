package no.nav.dagpenger.pdf.lagring

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson.jackson
import java.time.ZonedDateTime

internal interface Lagring {
    suspend fun lagre(
        kontekst: String,
        pdfDokument: PdfDokument,
    ): List<URNResponse>
}

internal class PdfDokument(
    val navn: String,
    val eier: String,
    val pdf: ByteArray,
)

internal data class URNResponse(
    val filnavn: String,
    val urn: String,
    val filsti: String,
    val storrelse: Long,
    val tidspunkt: ZonedDateTime,
)

internal class LagringImpl(
    private val baseUrl: String,
    tokenSupplier: () -> String,
    engine: HttpClientEngine = CIO.create(),
) : Lagring {
    private val httpKlient: HttpClient =
        HttpClient(engine) {
            defaultRequest {
                header("Authorization", "Bearer ${tokenSupplier.invoke()}")
            }
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                }
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }

    override suspend fun lagre(
        kontekst: String,
        pdfDokument: PdfDokument,
    ): List<URNResponse> {
        return httpKlient.submitFormWithBinaryData(
            url = "$baseUrl/$kontekst",
            formData =
                formData {
                    append(
                        pdfDokument.navn,
                        pdfDokument.pdf,
                        Headers.build {
                            append(HttpHeaders.ContentType, ContentType.Application.Pdf.toString())
                            append(HttpHeaders.ContentDisposition, "filename=${pdfDokument.navn}")
                        },
                    )
                },
        ) {
            this.header("X-Eier", pdfDokument.eier)
        }.body<List<URNResponse>>()
    }
}

package no.nav.dagpenger.pdf

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import mu.KotlinLogging
import no.nav.dagpenger.pdf.behovløser.PdfBehovløser
import no.nav.dagpenger.pdf.generator.pdfGeneratorApi
import no.nav.dagpenger.pdf.lagring.Lagring
import no.nav.dagpenger.pdf.lagring.LagringImpl
import no.nav.helse.rapids_rivers.RapidApplication

internal class ApplicationBuilder(
    config: Map<String, String>,
) : RapidsConnection.StatusListener {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val rapidsConnection: RapidsConnection =
        RapidApplication
            .create(
                env = config,
                builder = {
                    withKtorModule { pdfGeneratorApi() }
                },
            )

    private val lagring: Lagring by lazy {
        LagringImpl(
            baseUrl = Configuration.dpMellomlagringBaseUrl,
            tokenSupplier = Configuration.dpMellomlagringTokenSupplier,
        )
    }

    init {
        rapidsConnection.register(this).also {
            PdfBehovløser(rapidsConnection, lagring)
        }
    }

    fun start() = rapidsConnection.start()

    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        logger.info { "Starter opp dp-behov-pdf-generator" }
    }
}

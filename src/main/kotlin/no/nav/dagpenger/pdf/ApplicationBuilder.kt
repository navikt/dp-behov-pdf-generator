package no.nav.dagpenger.pdf

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.model.registry.PrometheusRegistry
import io.prometheus.metrics.tracer.initializer.SpanContextSupplier
import no.nav.dagpenger.pdf.behovløser.PdfBehovløser
import no.nav.dagpenger.pdf.behovløser.SaksbehandlingPdfBehovløser
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

    // Egendefinert registry (i stedet for rammeverkets default) slik at exemplarer kan kobles
    // til OpenTelemetry sin span-kontekst via SpanContextSupplier, og dukker opp i /metrics.
    private val meterRegistry =
        PrometheusMeterRegistry(
            PrometheusConfig.DEFAULT,
            PrometheusRegistry.defaultRegistry,
            Clock.SYSTEM,
            SpanContextSupplier.getSpanContext(),
        )

    private val rapidsConnection: RapidsConnection =
        RapidApplication
            .create(
                env = config,
                meterRegistry = meterRegistry,
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
            SaksbehandlingPdfBehovløser(rapidsConnection, lagring)
        }
    }

    fun start() = rapidsConnection.start()

    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        logger.info { "Starter opp dp-behov-pdf-generator" }
    }
}

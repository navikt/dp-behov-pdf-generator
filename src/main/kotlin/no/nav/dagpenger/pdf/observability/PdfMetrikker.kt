package no.nav.dagpenger.pdf.observability

import io.prometheus.metrics.core.metrics.Counter
import io.prometheus.metrics.core.metrics.Histogram

// Skiller inngangspunktene appen kan motta pdf-oppdrag fra, brukt som label på alle pdf-metrikker.
internal enum class PdfFlyt(
    val label: String,
) {
    PDF_BEHOV("pdf_behov"),
    SAKSBEHANDLING_PDF_BEHOV("saksbehandling_pdf_behov"),
    API_MED_SAKID("api_med_sakid"),
    API_RÅ("api_rå"),
}

// Stegene en pdf-generering går gjennom, brukt til å plassere feil presist.
internal enum class PdfSteg(
    val label: String,
) {
    DEKODING("dekoding"),
    HTML_BYGGING("html_bygging"),
    RENDERING("rendering"),
    LAGRING("lagring"),
    PUBLISERING("publisering"),
    UKJENT("ukjent"),
}

private enum class Utfall(
    val label: String,
) {
    SUKSESS("suksess"),
    FEIL("feil"),
}

// Alle metrikker registrerer seg selv i PrometheusRegistry.defaultRegistry ved opprettelse
// (Histogram/Counter.builder().register()), samme registry rapids-and-rivers eksponerer på /metrics.
// Ingen registry trenger å sendes rundt i koden.

private val genereringVarighet =
    Histogram
        .builder()
        .name("pdf_generering_varighet_seconds")
        .help("Total varighet for å generere og levere en pdf")
        .labelNames("flyt", "utfall")
        .register()

private val renderingVarighet =
    Histogram
        .builder()
        .name("pdf_rendering_varighet_seconds")
        .help("Varighet for selve pdf-renderingen")
        .labelNames("flyt", "utfall")
        .register()

private val lagringVarighet =
    Histogram
        .builder()
        .name("pdf_lagring_varighet_seconds")
        .help("Varighet for å lagre pdf hos dp-mellomlagring")
        .labelNames("flyt", "konteksttype", "utfall")
        .register()

private val genereringFeil =
    Counter
        .builder()
        .name("pdf_generering_feil_total")
        .help("Antall feilede pdf-genereringer, fordelt på steg")
        .labelNames("flyt", "steg")
        .register()

private val pdfStørrelseHistogram =
    Histogram
        .builder()
        .name("pdf_storrelse_bytes")
        .help("Størrelse på generert pdf")
        .labelNames("flyt")
        .classicUpperBounds(
            100.kilobyte,
            200.kilobyte,
            300.kilobyte,
            400.kilobyte,
            500.kilobyte,
            750.kilobyte,
            1.megabyte,
            5.megabyte,
            10.megabyte,
            30.megabyte,
        ).register()

private val htmlStørrelseHistogram =
    Histogram
        .builder()
        .name("pdf_html_storrelse_bytes")
        .help("Størrelse på HTML som konverteres til pdf")
        .labelNames("flyt")
        .classicUpperBounds(10.kilobyte, 50.kilobyte, 100.kilobyte, 500.kilobyte, 1.megabyte, 5.megabyte, 10.megabyte)
        .register()

/**
 * Måler total varighet for en hel pdf-generering (fra mottak til publisert/returnert løsning),
 * og registrerer utfall (suksess/feil) på tvers av alle steg.
 */
internal fun <T> målPdfGenerering(
    flyt: PdfFlyt,
    block: () -> T,
): T {
    val start = System.nanoTime()
    var utfall = Utfall.SUKSESS
    try {
        return block()
    } catch (e: Exception) {
        utfall = Utfall.FEIL
        throw e
    } finally {
        genereringVarighet.labelValues(flyt.label, utfall.label).observe((System.nanoTime() - start) / 1e9)
    }
}

/** Måler kun selve pdf-renderingen (PdfBuilder.lagPdf), som antas å være det tyngste steget. */
internal fun <T> målPdfRendering(
    flyt: PdfFlyt,
    block: () -> T,
): T {
    val start = System.nanoTime()
    var utfall = Utfall.SUKSESS
    try {
        return block()
    } catch (e: Exception) {
        utfall = Utfall.FEIL
        throw e
    } finally {
        renderingVarighet.labelValues(flyt.label, utfall.label).observe((System.nanoTime() - start) / 1e9)
    }
}

/** Måler kun lagringskallet mot dp-mellomlagring. */
internal fun <T> målPdfLagring(
    flyt: PdfFlyt,
    konteksttype: String,
    block: () -> T,
): T {
    val start = System.nanoTime()
    var utfall = Utfall.SUKSESS
    try {
        return block()
    } catch (e: Exception) {
        utfall = Utfall.FEIL
        throw e
    } finally {
        lagringVarighet.labelValues(flyt.label, konteksttype, utfall.label).observe((System.nanoTime() - start) / 1e9)
    }
}

/** Teller opp én feil for gitt flyt og steg. */
internal fun registrerPdfFeil(
    flyt: PdfFlyt,
    steg: PdfSteg,
) {
    genereringFeil.labelValues(flyt.label, steg.label).inc()
}

/** Registrerer størrelsen på den ferdig genererte pdf-en. */
internal fun registrerPdfStørrelse(
    flyt: PdfFlyt,
    bytes: Int,
) {
    pdfStørrelseHistogram.labelValues(flyt.label).observe(bytes.toDouble())
}

/** Registrerer størrelsen på HTML-en som skal konverteres til pdf. */
internal fun registrerHtmlStørrelse(
    flyt: PdfFlyt,
    bytes: Int,
) {
    htmlStørrelseHistogram.labelValues(flyt.label).observe(bytes.toDouble())
}

/**
 * Slår rå kontekst (f.eks. "behandling/behandlingId") ned til en avgrenset verdi egnet som
 * Prometheus-label, siden hele kontekst-strengen kan inneholde id-er med høy kardinalitet.
 */
internal fun konteksttype(kontekst: String): String = kontekst.substringBefore("/")

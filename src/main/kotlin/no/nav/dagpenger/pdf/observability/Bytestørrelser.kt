package no.nav.dagpenger.pdf.observability

// Små hjelpere for å uttrykke bytestørrelser lesbart, f.eks. 10.megabyte i stedet for 10_000_000.0.
internal val Int.kilobyte: Double get() = this * 1_000.0
internal val Int.megabyte: Double get() = this * 1_000_000.0
internal val Double.megabyte: Double get() = this * 1_000_000.0

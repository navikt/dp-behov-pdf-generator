package no.nav.dagpenger.pdf

private val resourceRetriever = object {}.javaClass

internal fun String.les(): String {
    return resourceRetriever.getResource(this)!!.readText()
}

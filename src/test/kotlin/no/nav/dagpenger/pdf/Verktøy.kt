package no.nav.dagpenger.pdf

import java.io.File

private val resourceRetriever = object {}.javaClass

internal fun String.les(): String = resourceRetriever.getResource(this)!!.readText()

internal fun ByteArray.skrivTilFil(s: String) {
    File(s).writeBytes(this)
}

internal fun String.skrivTilFil(s: String) {
    File(s).writeText(this)
}

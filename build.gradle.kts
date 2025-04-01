plugins {
    id("common")
    application
    alias(libs.plugins.shadow.jar)
}

dependencies {
    implementation(libs.rapids.and.rivers)
    implementation(libs.konfig)
    implementation(libs.kotlin.logging)
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.client)
    implementation("io.ktor:ktor-serialization-jackson:${libs.versions.ktor.get()}")
    implementation("no.nav.dagpenger:oauth2-klient:2025.03.31-22.36.fc954bf09c91")

    implementation("io.github.openhtmltopdf:openhtmltopdf-pdfbox:1.1.22")
    implementation("io.github.openhtmltopdf:openhtmltopdf-svg-support:1.1.22")
    implementation("commons-io:commons-io:2.17.0") // For å fikse CVE-2024-47554 i openhtmltopdf-svg-support:1.1.22
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")
    implementation("org.jsoup:jsoup:1.17.2")

    testImplementation("de.redsix:pdfcompare:1.1.61")
    testImplementation("org.verapdf:validation-model:1.26.1")
    testImplementation("io.ktor:ktor-server-test-host-jvm:${libs.versions.ktor.get()}")
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.mockk)
    testImplementation(libs.bundles.kotest.assertions)
    testImplementation(libs.bundles.naisful.rapid.and.rivers.test)
}

application {
    mainClass.set("no.nav.dagpenger.pdf.AppKt")
}

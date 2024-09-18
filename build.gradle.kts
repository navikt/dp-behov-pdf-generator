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
    implementation(libs.dp.biblioteker.oauth2.klient)

    implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10")
    implementation("com.openhtmltopdf:openhtmltopdf-slf4j:1.0.10")
    implementation("com.openhtmltopdf:openhtmltopdf-svg-support:1.0.10")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")
    implementation("org.jsoup:jsoup:1.17.2")

    testImplementation("de.redsix:pdfcompare:1.1.61")
    testImplementation("org.verapdf:validation-model:1.26.1")
    testImplementation("io.ktor:ktor-server-test-host-jvm:${libs.versions.ktor.get()}")
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.mockk)
    testImplementation(libs.bundles.kotest.assertions)
}

application {
    mainClass.set("no.nav.dagpenger.pdf.AppKt")
}

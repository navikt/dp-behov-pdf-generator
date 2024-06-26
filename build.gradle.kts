plugins {
    id("common")
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    implementation(libs.rapids.and.rivers)
    implementation(libs.konfig)
    implementation(libs.kotlin.logging)
    implementation(libs.bundles.ktor.client)
    implementation("io.ktor:ktor-serialization-jackson:${libs.versions.ktor.get()}")
    implementation(libs.dp.biblioteker.oauth2.klient)

    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.mockk)
    testImplementation(libs.bundles.kotest.assertions)
}

application {
    mainClass.set("no.nav.dagpenger.pdf.AppKt")
}

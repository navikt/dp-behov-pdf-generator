package no.nav.dagpenger.pdf

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import no.nav.dagpenger.oauth2.CachedOauth2Client
import no.nav.dagpenger.oauth2.OAuth2Config

internal object Configuration {
    const val APP_NAME = "dp-behov-distribuering"

    private val defaultProperties =
        ConfigurationMap(
            mapOf(
                "RAPID_APP_NAME" to "dp-behov-distribuering",
                "KAFKA_CONSUMER_GROUP_ID" to "dp-behov-distribuering-v1",
                "KAFKA_RAPID_TOPIC" to "teamdagpenger.rapid.v1",
                "KAFKA_RESET_POLICY" to "latest",
                "DOKDISTFORDELING_URL" to "https://dokdistfordeling-q1.dev-fss-pub.nais.io/rest/v1/distribuerjournalpost",
                "DOKDISTFORDELING_API_SCOPE" to "api://dev-fss.teamdokumenthandtering.saf-q1/.default",
            ),
        )

    val properties =
        ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding defaultProperties

    val distribuerjournalpostUrl = properties[Key("DOKDISTFORDELING_URL", stringType) ]
    val distribuerjournalpostApiScope = properties[Key("DOKDISTFORDELING_API_SCOPE", stringType) ]

    val tokenProvider = {
        azureAdClient.clientCredentials(distribuerjournalpostApiScope).accessToken
    }

    val config: Map<String, String> =
        properties.list().reversed().fold(emptyMap()) { map, pair ->
            map + pair.second
        }
    private val azureAdClient: CachedOauth2Client by lazy {
        val azureAdConfig = OAuth2Config.AzureAd(properties)
        CachedOauth2Client(
            tokenEndpointUrl = azureAdConfig.tokenEndpointUrl,
            authType = azureAdConfig.clientSecret(),
        )
    }
}

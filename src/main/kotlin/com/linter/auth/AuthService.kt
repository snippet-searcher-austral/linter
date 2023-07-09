package com.linter.auth

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import org.springframework.stereotype.Service

@Serializable
data class TokenRequest(val client_id: String, val client_secret: String, val audience: String, val grant_type: String)

@Serializable
data class TokenResponse(val access_token: String, val token_type: String)

@Service
class AuthService {
    private val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private val clientId: String = System.getenv("AUTH0_CLIENT_ID") ?: throw RuntimeException("AUTH0_CLIENT_ID not set")
    private val clientSecret: String = System.getenv("AUTH0_CLIENT_SECRET") ?: throw RuntimeException("AUTH0_CLIENT_SECRET not set")
    private val apiAudience: String = System.getenv("AUTH0_API_AUDIENCE") ?: throw RuntimeException("AUTH0_API_AUDIENCE not set")


    suspend fun getAccessToken(): String {
        val tokenRequest = TokenRequest(clientId, clientSecret, apiAudience, "client_credentials")
        try {
            val tokenResponse: TokenResponse = client.post("https://snippet-authorizer.us.auth0.com/oauth/token") {
                contentType(ContentType.Application.Json)
                body = tokenRequest
            }
            return tokenResponse.access_token;
        }
        catch (e: Exception) {
            println("ERROR: $e")
            throw e
        }
    }
}

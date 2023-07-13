package com.linter.service

import com.linter.auth.AuthService
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.HttpURLConnection
import java.net.URL

@Service
class SnippetManagerHTTPService(private val authService: AuthService) {

    private var baseUrl = System.getenv("SNIPPET_MANAGER_URL") ?: "Not found :("

    suspend fun getSnippet(snippetId: String): Snippet {
        val url = URL(baseUrl + snippetId)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val token = authService.getAccessToken()
        connection.setRequestProperty("Authorization", "Bearer $token")
        val response = connection.inputStream.bufferedReader().use { it.readText() }

        connection.disconnect()
        val jsonResponse = JSONObject(response)
        val content = jsonResponse.getString("content");
        val userId = jsonResponse.getString("userId");
        return Snippet(content, userId)
    }

    suspend fun updateSnippet(snippetId: String, complianceValue: String) {
        val url = URL(baseUrl + snippetId)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json; utf-8")
        val token = authService.getAccessToken()
        connection.setRequestProperty("Authorization", "Bearer $token")

        val jsonInputString = "{\"compliance\": \"$complianceValue\"}"

        connection.outputStream.use { os ->
            val input = jsonInputString.toByteArray(charset("utf-8"))
            os.write(input, 0, input.size)
        }

        connection.inputStream.bufferedReader().use { it.readText() }
        connection.disconnect()
    }
}
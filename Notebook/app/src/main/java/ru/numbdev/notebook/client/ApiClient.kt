package ru.numbdev.notebook.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson
import ru.numbdev.notebook.dto.LoginRequest
import ru.numbdev.notebook.dto.LoginResponse

class ApiClient {
    companion object {
        private const val BASE_URL = "http://192.168.50.214:8081"

        private val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                gson()
            }
        }

        suspend fun login(username: String): LoginResponse {
            return client.post("$BASE_URL/user") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, "null"))
            }.body()
        }
    }
}
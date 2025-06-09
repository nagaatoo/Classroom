package ru.numbdev.notebook.client

import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import ru.numbdev.notebook.dto.command.BaseCommand
import ru.numbdev.notebook.dto.command.PingCommand
import ru.numbdev.notebook.dto.command.PrintCommand
import ru.numbdev.notebook.dto.command.ToPageCommand
import ru.numbdev.notebook.room.RoomStateParams
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

class RoomClient {

    companion object {

        private var session: DefaultClientWebSocketSession? = null

        suspend fun initClient() {
            if (session != null) {
                return
            }

            val client = HttpClient(CIO) {
                install(WebSockets)
            }

            try {
                session = client.webSocketSession(
                    method = HttpMethod.Get,
                    host = "192.168.50.214",
//                    host = "158.160.155.146",
                    port = 8081,
                    path = "/chat",
                    {
                        header("room_id", RoomStateParams.selectedRoomId.toString())
                        header("user_id", RoomStateParams.userId)
                    }
                )
            } catch (e: RuntimeException) {
                println()
            }

            doPingJob()
        }

        private fun doPingJob() {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    val ping = Gson().toJson(PingCommand(timestamp = System.currentTimeMillis()))
                    runBlocking { session?.outgoing?.send(Frame.Text(ping)) }
                    println("ping")
                }
            }, 10000, 1000)
        }

        suspend fun send(line: BaseCommand) {
            if (session == null || session?.isActive == false) {
                initClient()
            }

            val json = Gson().toJson(line)
            println(json)
            session?.outgoing?.send(Frame.Text(json))
        }

        suspend fun changePage(pageNumber: Number) {
            if (session == null || session?.isActive == false) {
                initClient()
            }

            val json = Gson().toJson(
                ToPageCommand(
                    pageNumber
                )
            )
            println(json)
            session?.outgoing?.send(Frame.Text(json))
        }

        suspend fun getSession(): DefaultClientWebSocketSession? {
            if (session == null || session?.isActive == false) {
                initClient()
            }

            return session
        }
    }

}
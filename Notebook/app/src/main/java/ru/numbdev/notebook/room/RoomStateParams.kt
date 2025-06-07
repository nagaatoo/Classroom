package ru.numbdev.notebook.room

import java.util.UUID

class RoomStateParams {
    companion object {
        var userId = UUID.randomUUID().toString()
        var username: String = ""
        var isTeacher: Boolean = false
        var selectedRoomId: UUID? = null
        var currentPage: Int = 0
    }
}
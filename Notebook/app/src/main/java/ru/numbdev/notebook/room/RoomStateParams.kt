package ru.numbdev.notebook.room

import ru.numbdev.notebook.dto.Role
import java.util.UUID

class RoomStateParams {
    companion object {
        lateinit var userId: String
        var username: String = ""
        var isTeacher: Boolean = false
        var selectedRoomId: UUID? = null
        var currentPage: Int = 0
        lateinit var role: Role
    }
}
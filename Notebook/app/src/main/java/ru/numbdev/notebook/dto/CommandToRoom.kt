package ru.numbdev.notebook.dto

import ru.numbdev.notebook.dto.command.Command
import java.util.UUID

data class CommandToRoom(
    val roomId: UUID,
    val command: Command,
    val role: Role,
    val lines: Map<String, Line>
)

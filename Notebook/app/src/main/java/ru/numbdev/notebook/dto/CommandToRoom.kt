package ru.numbdev.notebook.dto

import ru.numbdev.notebook.dto.command.Command

data class CommandToRoom(
    val command: Command,
    val role: Role,
    val lines: Map<String, Line>
)

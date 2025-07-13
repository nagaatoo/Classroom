package ru.numbdev.notebook.dto

import java.util.UUID

data class Room(
    var id: UUID,
    var name: String,
    var description: String
)

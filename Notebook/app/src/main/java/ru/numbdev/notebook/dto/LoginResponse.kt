package ru.numbdev.notebook.dto

data class LoginResponse(
    val userId: String,
    val role: Role
)
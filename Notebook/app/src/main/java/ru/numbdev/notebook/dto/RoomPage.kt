package ru.numbdev.notebook.dto

data class RoomPage(
    var rooms: List<Room>,
    var currentPage: Int,
    var totalPages: Int,
    var totalItems: Int,
    var itemsPerPage: Int
) 
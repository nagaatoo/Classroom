package ru.numbdev.notebook.dto.command

data class ToPageCommand(
    val pageNumber: Number
) :BaseCommand(command = Command.TO_PAGE) {
}
package ru.numbdev.notebook.dto.command

data class CleanCommand(val timestamp: Long) : BaseCommand(command = Command.CLEAN)

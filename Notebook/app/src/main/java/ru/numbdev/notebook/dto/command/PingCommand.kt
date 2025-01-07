package ru.numbdev.notebook.dto.command

data class PingCommand(val timestamp: Long) : BaseCommand(command = Command.PING)

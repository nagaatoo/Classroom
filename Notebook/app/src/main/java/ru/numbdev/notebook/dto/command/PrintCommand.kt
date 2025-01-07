package ru.numbdev.notebook.dto.command

import ru.numbdev.notebook.dto.LineBlock

data class PrintCommand(
    var block: LineBlock
) : BaseCommand(command = Command.PRINT)

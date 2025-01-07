package ru.numbdev.classroom.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommandFromRoom {
    private Command command;
    private LineBlock block;
}

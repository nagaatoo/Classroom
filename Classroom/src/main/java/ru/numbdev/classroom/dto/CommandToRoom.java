package ru.numbdev.classroom.dto;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommandToRoom {
    private Command command;
    private Map<String, Line> lines;
}

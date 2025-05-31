package ru.numbdev.classroom.dto;

import java.util.Map;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommandToRoom {
    private Command command;
    private UUID roomId;
    private Role role;
    private Map<String, Line> lines;
}

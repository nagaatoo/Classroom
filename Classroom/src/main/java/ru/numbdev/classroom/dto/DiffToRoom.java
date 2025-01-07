package ru.numbdev.classroom.dto;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiffToRoom {
    private Map<String, Line> diff;
}

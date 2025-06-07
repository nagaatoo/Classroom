package ru.numbdev.classroom.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LineBlock {
    private String id;
    private int pageNumber;
    private ToolType type;
    private Point point;
}

package ru.numbdev.classroom.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Point {
    private Float x;
    private Float y;
    private Long timestamp;
    private boolean wasReaded;
    private LineOrder order;
}

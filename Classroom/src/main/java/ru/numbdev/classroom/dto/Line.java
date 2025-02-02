package ru.numbdev.classroom.dto;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Line {
    private UUID id;
    private String sessionIdOwner;
    private String userIdOwner;
    private Role role;
    private ToolType type;
    private List<Point> points;
    private Long created;

    public Line cloneLine() {
        return Line.builder()
                .id(id)
                .sessionIdOwner(sessionIdOwner)
                .userIdOwner(userIdOwner)
                .role(role)
                .created(created)
                .type(type)
                .points(points)
                .build();
    }
}

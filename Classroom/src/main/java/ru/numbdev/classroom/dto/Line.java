package ru.numbdev.classroom.dto;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Builder.Default
    private transient AtomicBoolean isFinished = new AtomicBoolean();
    @Builder.Default
    private transient AtomicBoolean isDeleted = new AtomicBoolean();
    @Builder.Default
    private transient AtomicBoolean wasReaded = new AtomicBoolean();
    @Builder.Default
    private transient AtomicInteger lastReadedPointId = new AtomicInteger(0);

    public Line cloneLine(List<Point> newPoints) {
        return Line.builder()
                .id(id)
                .sessionIdOwner(sessionIdOwner)
                .userIdOwner(userIdOwner)
                .role(role)
                .created(created)
                .type(type)
                .points(newPoints)
                .build();
    }
}

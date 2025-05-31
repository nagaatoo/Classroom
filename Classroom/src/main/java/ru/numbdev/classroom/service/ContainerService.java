package ru.numbdev.classroom.service;

import java.util.Map;

import ru.numbdev.classroom.dto.DiffToRoom;
import ru.numbdev.classroom.dto.Line;
import ru.numbdev.classroom.dto.LineBlock;
import ru.numbdev.classroom.dto.RoomWebSocketSession;

public interface ContainerService {
    void addLine(RoomWebSocketSession session, LineBlock block);
    Map<String, Line> clean(String sessionId);
    DiffToRoom getCurrent();
    DiffToRoom diffAndCommit();
}

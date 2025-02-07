package ru.numbdev.classroom.service;

import ru.numbdev.classroom.dto.DiffToRoom;
import ru.numbdev.classroom.dto.Line;
import ru.numbdev.classroom.dto.LineBlock;
import ru.numbdev.classroom.dto.RoomWebSocketSessionInfo;

import java.util.Map;

public interface ContainerService {
    void addLine(RoomWebSocketSessionInfo info, LineBlock block);
    Map<String, Line> clean(RoomWebSocketSessionInfo info);

    DiffToRoom getCurrent(String roomId);
    DiffToRoom sortAndCommit(String roomId);
}

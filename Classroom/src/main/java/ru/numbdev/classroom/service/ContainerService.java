package ru.numbdev.classroom.service;

import ru.numbdev.classroom.dto.DiffToRoom;
import ru.numbdev.classroom.dto.LineBlock;
import ru.numbdev.classroom.dto.RoomWebSocketSessionInfo;

public interface ContainerService {
    void addLine(RoomWebSocketSessionInfo info, LineBlock block);
    void clean(RoomWebSocketSessionInfo info);
    DiffToRoom sortAndCommit(String roomId);
}

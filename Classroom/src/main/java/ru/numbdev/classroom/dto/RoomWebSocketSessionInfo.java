package ru.numbdev.classroom.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomWebSocketSessionInfo {
    private String sessionId;
    private String roomId;
    private String userId;
    private Role role;
}

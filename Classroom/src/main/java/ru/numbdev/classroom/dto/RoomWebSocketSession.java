package ru.numbdev.classroom.dto;

import java.util.UUID;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.Gson;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class RoomWebSocketSession {

    private static final String ROOM_ID_HEADER = "room_id";
    private static final String USER_ID_HEADER = "user_id";

    private Gson gson;
    private UUID roomId;
    private Role role;
    private WebSocketSession session;

    private RoomWebSocketSession(WebSocketSession session, Gson gson) {
        this.session = session;
        this.gson = gson;
    }

    public static RoomWebSocketSession getInstanse(WebSocketSession targetSession, Gson gson) {
        return new RoomWebSocketSession(targetSession, gson);
    }

    public UUID getRoomId() {
        if (roomId == null && session.getHandshakeHeaders().get(ROOM_ID_HEADER) != null) {
            roomId = UUID.fromString(session.getHandshakeHeaders().get(ROOM_ID_HEADER).getFirst());
        }

        return roomId;
    }

    public String getUserId() {
        return session.getHandshakeHeaders().get(USER_ID_HEADER).getFirst();
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void initRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public String getSessionId() {
        return session.getId();
    }

    public void sendMessage(Command command, DiffToRoom diffToRoom) {
        try {
            var message = CommandToRoom.builder()
                    .roomId(roomId)
                    .command(command)
                    .role(role)
                    .lines(diffToRoom.getDiff())
                    .build();
            session.sendMessage(new TextMessage(gson.toJson(message)));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}

package ru.numbdev.classroom.dto;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.Gson;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class RoomWebSocketSession {

    private static final String ROOM_ID_HEADER = "room_id";
    private static final String USER_ID_HEADER = "user_id";

    private Gson gson;
    private UUID roomId;
    private UserInfo userInfo;
    @Setter
    private int currentPage;
    private WebSocketSession session;

    private RoomWebSocketSession(WebSocketSession session, UserInfo userInfo, Gson gson) {
        this.session = session;
        this.userInfo = userInfo;
        this.gson = gson;
    }

    public static RoomWebSocketSession getInstanse(WebSocketSession targetSession, UserInfo userInfo, Gson gson) {
        return new RoomWebSocketSession(targetSession, userInfo, gson);
    }

    public UUID getRoomId() {
        if (roomId == null && session.getHandshakeHeaders().get(ROOM_ID_HEADER) != null) {
            var roomIdFromHeader = session.getHandshakeHeaders().get(ROOM_ID_HEADER).getFirst();

            if (StringUtils.isNotBlank(roomIdFromHeader) && !roomIdFromHeader.equals("null")) {
                roomId = UUID.fromString(roomIdFromHeader);
            }
        }

        return roomId;
    }

    public String getUserId() {
        return session.getHandshakeHeaders().get(USER_ID_HEADER).getFirst();
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
                    .role(userInfo.getRole())
                    .lines(diffToRoom.getDiff().get(currentPage))
                    .build();
            session.sendMessage(new TextMessage(gson.toJson(message)));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}

package ru.numbdev.classroom.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.numbdev.classroom.dto.CommandToRoom;
import ru.numbdev.classroom.dto.Role;
import ru.numbdev.classroom.dto.RoomWebSocketSessionInfo;

@Log4j2
@Service
@RequiredArgsConstructor
public class SessionService {

    private static final String ROOM_ID_HEADER = "room_id";
    private final Gson gson;

    private final Map<String, List<WebSocketSession>> sessionsInRooms = new ConcurrentHashMap<>();
    private WebSocketSession teacherSession; // Бутафория

    public String getRoomIdFromHeader(WebSocketSession session) {
        return session.getHandshakeHeaders().get(ROOM_ID_HEADER).getFirst();
    }

    public void addSession(WebSocketSession session) {
        var roomId = getRoomIdFromHeader(session);
        this.sessionsInRooms.computeIfPresent(roomId, (id, sessions) -> {
            sessions.add(session);
            return sessions;
        });
        this.sessionsInRooms.computeIfAbsent(roomId, id -> {
            List<WebSocketSession> sessions = new ArrayList<>();
            sessions.add(session);
            return sessions;
        });

        if (teacherSession == null) { // Бутафория
            teacherSession = session;
        }
    }

    public void removeSession(WebSocketSession session) {
        var roomId = getRoomIdFromHeader(session);
        this.sessionsInRooms.computeIfPresent(roomId, (id, sessions) -> {
            sessions.remove(session);
            return sessions;
        });

        if (CollectionUtils.isEmpty(sessionsInRooms.get(roomId))) {
            sessionsInRooms.remove(roomId);
        }

        if (teacherSession == session) { // Бутафория
            teacherSession = null;
        }
    }

    public boolean roomIsEmpty(String roomId) {
        return CollectionUtils.isEmpty(sessionsInRooms.get(roomId));
    }

    public void sendToRoom(String roomId, CommandToRoom message) {
        if (sessionsInRooms.isEmpty()) {
            return;
        }

        for (WebSocketSession session : sessionsInRooms.get(roomId)) {
            try {
                session.sendMessage(new TextMessage(gson.toJson(message)));
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    public RoomWebSocketSessionInfo getSessionInfo(WebSocketSession session) {
        return RoomWebSocketSessionInfo.builder()
                .sessionId(session.getId())
                .roomId(getRoomIdFromHeader(session))
                .role(getRole(session))
                .build();
    }

    private Role getRole(WebSocketSession session) {
        return teacherSession == session ? Role.TEACHER : Role.STUDENT; // Бутафория
    }

    @Scheduled(fixedRate = 10000)
    public void cleanRoomsJob() {
        List<String> keysForRemove = new ArrayList<>();
        for (Map.Entry<String, List<WebSocketSession>> es : sessionsInRooms.entrySet()) {
            if (CollectionUtils.isEmpty(es.getValue())) {
                keysForRemove.add(es.getKey());
            }
        }

        keysForRemove.forEach(sessionsInRooms::remove);
    }
}

package ru.numbdev.classroom.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.numbdev.classroom.context.RoomContext;
import ru.numbdev.classroom.dto.LineBlock;
import ru.numbdev.classroom.dto.RoomWebSocketSession;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final Map<UUID, RoomContext> roomTasks = new ConcurrentHashMap<>();
    private final Map<String, UUID> sessionsInRooms = new HashMap<>();
    private final UserService userService;
    private final ApplicationContext applicationContext;
    private final ScheduledTaskService scheduledTaskService;

    @PostConstruct
    private void initForTest() {
        for (var id : List.of(
                UUID.fromString("9ffeec9b-1035-4b6f-b6e7-6a51ce97d943"),
                UUID.fromString("015949bb-3694-4841-bdb6-101d565fa586"))) {
            var context = applicationContext.getBean(RoomContext.class).setRoomId(id);
            roomTasks.put(context.getRoomId(), context);
        }
    }

    public void registerRoomIfAbsent(RoomWebSocketSession session) {
        RoomContext context;
        if (session.getRoomId() == null) {
            context = applicationContext.getBean(RoomContext.class);
            roomTasks.put(context.getRoomId(), context);
            session.initRoomId(context.getRoomId());
        } else {
            context = roomTasks.get(session.getRoomId());
        }

        var userRole = userService.getRoleUser(session.getUserId());
        session.setRole(userRole);
        sessionsInRooms.put(session.getSessionId(), session.getRoomId());
        context.addSession(session);
        scheduledTaskService.addToSchedule(context);
    }

    public void removeFromRoom(String sessionId) {
        var roomId = sessionsInRooms.remove(sessionId);

        if (roomId == null) {
            return;
        }

        var room = roomTasks.get(roomId);
        if (room == null) {
            return;
        }

        room.removeFromRoom(sessionId);

        if (room.isEmpty()) {
            roomTasks.remove(roomId);
            scheduledTaskService.removeFromSchedule(room);
        }

        userService.remove(); // Удалить после введения авторизации
    }

    public void addDiff(String sessionId, LineBlock block) {
        var roomId = sessionsInRooms.get(sessionId);
        roomTasks.get(roomId).addDiff(sessionId, block);
    }

    public void sendClean(String sessionId) {
        var roomId = sessionsInRooms.get(sessionId);
        roomTasks.get(roomId).sendClean(sessionId);
    }

    public void goToPage(String sessionId, int pageNumber) {
        var roomId = sessionsInRooms.get(sessionId);
        roomTasks.get(roomId).goToPage(sessionId, pageNumber);
    }

}

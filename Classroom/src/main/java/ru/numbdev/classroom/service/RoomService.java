package ru.numbdev.classroom.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.numbdev.classroom.context.RoomContext;
import ru.numbdev.classroom.dto.LineBlock;
import ru.numbdev.classroom.dto.RoomDto;
import ru.numbdev.classroom.dto.RoomPage;
import ru.numbdev.classroom.dto.RoomWebSocketSession;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final Map<UUID, RoomContext> roomTasks = new ConcurrentHashMap<>();
    private final Map<String, UUID> sessionsInRooms = new HashMap<>();
    private final ApplicationContext applicationContext;
    private final ScheduledTaskService scheduledTaskService;

    public void registerRoomIfAbsent(RoomWebSocketSession session) {
        RoomContext context;
        if (session.getRoomId() == null) {
            context = applicationContext.getBean(RoomContext.class);
            roomTasks.put(context.getRoomId(), context);
            session.initRoomId(context.getRoomId());
        } else {
            context = roomTasks.get(session.getRoomId());
        }

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

    public RoomPage getAllRooms() {
        var user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            return RoomPage.builder().build();
        }

        return RoomPage.builder()
                .rooms(
                        roomTasks
                                .keySet()
                                .stream()
                                .map(id -> RoomDto.builder()
                                        .id(id.toString())
                                        .build())
                                .toList())
                .build();
    }

}

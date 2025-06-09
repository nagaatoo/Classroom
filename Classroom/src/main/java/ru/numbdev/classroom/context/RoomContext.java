package ru.numbdev.classroom.context;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.numbdev.classroom.dto.Command;
import ru.numbdev.classroom.dto.DiffToRoom;
import ru.numbdev.classroom.dto.LineBlock;
import ru.numbdev.classroom.dto.Role;
import ru.numbdev.classroom.dto.RoomWebSocketSession;
import ru.numbdev.classroom.service.ContainerService;

@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class RoomContext implements ScheduledContext {

    @Getter
    @Setter
    private UUID roomId;
    private final Map<String, RoomWebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ContainerService containerService;

    public void addSession(RoomWebSocketSession session) {
        sessions.put(session.getSessionId(), session);
        session.sendMessage(Command.INIT, containerService.getCurrent(session.getCurrentPage()));
    }

    public void removeFromRoom(String sessionId) {
        sessions.remove(sessionId);
    }

    public void addDiff(String sessionId, LineBlock block) {
        containerService.addLine(sessions.get(sessionId), block);
    }

    public void sendClean(String sessionId) {
        var sessioin = sessions.get(sessionId);
        var deletedLines = containerService.clean(sessionId, sessioin.getCurrentPage());
        var command = sessioin.getUserInfo().getRole() == Role.TEACHER ? Command.TEACHER_CLEAN : Command.CLEAN;

        sessions.values().forEach(session -> {
            session.sendMessage(command, deletedLines);
        });
    }

    public void goToPage(String sessionId, int pageNumber) {
        var session = sessions.get(sessionId);
        session.setCurrentPage(pageNumber);
        session.sendMessage(Command.TO_PAGE, containerService.getCurrent(session.getCurrentPage()));
    }

    public boolean isEmpty() {
        return sessions.isEmpty();
    }

    @Override
    public void sendDiff() {
        if (sessions.isEmpty()) {
            return;
        }

        var diffs = containerService.diffAndCommit();
        if (!CollectionUtils.isEmpty(diffs)) {
            sessions.values().forEach(session -> {
                session.sendMessage(
                        Command.PRINT,
                        DiffToRoom.builder()
                                .diff(
                                        Map.of(
                                                session.getCurrentPage(),
                                                diffs.getOrDefault(session.getCurrentPage(), new HashMap<>())))
                                .build());
            });
        }
    }

}

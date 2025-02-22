package ru.numbdev.classroom.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.WebSocketSession;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.numbdev.classroom.dto.Command;
import ru.numbdev.classroom.dto.CommandToRoom;
import ru.numbdev.classroom.dto.LineBlock;
import ru.numbdev.classroom.dto.Role;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
        Runtime.getRuntime().availableProcessors(),
        Thread.ofVirtual().factory()
    );
    private final Map<String, ScheduledFuture<?>> roomTasks = new ConcurrentHashMap<>();
    
    
    // private final Map<String, Thread> rooms = new ConcurrentHashMap<>();
    private final SessionService sessionService;
    private final ContainerService containerService;

    public void registerRoomIfAbsent(WebSocketSession session) {
        sessionService.addSession(session);
        var info = sessionService.getSessionInfo(session);
        roomTasks.computeIfAbsent(info.getRoomId(), roomId -> {
            return scheduler.scheduleAtFixedRate(
                () -> doTick(roomId),
                0, 50, TimeUnit.MILLISECONDS
            );
        });
        sessionService.sendInitState(
                info,
                CommandToRoom.builder()
                        .command(Command.INIT)
                        .role(info.getRole())
                        .lines(containerService.getCurrent(info.getRoomId()).getDiff())
                        .build()
        );
    }

    public void removeFromRoom(WebSocketSession session) {
        var info = sessionService.getSessionInfo(session);
        sessionService.removeSession(session);
        if (sessionService.roomIsEmpty(info.getRoomId())) {
            roomTasks.computeIfPresent(info.getRoomId(), (roomId, job) -> {
                job.cancel(true);
                return null;
            });

            // rooms.remove(info.getRoomId());
        }
    }

    public void addDiff(WebSocketSession session, LineBlock block) {
        var info = sessionService.getSessionInfo(session);
        containerService.addLine(info, block);
    }

    public void sendClean(WebSocketSession session) {
        var info = sessionService.getSessionInfo(session);
        var deletedLines = containerService.clean(info);
        if (CollectionUtils.isEmpty(deletedLines)) {
            return;
        }

        sessionService.sendToRoom(
                info.getRoomId(),
                CommandToRoom.builder()
                        .command(info.getRole() == Role.TEACHER ? Command.TEACHER_CLEAN : Command.CLEAN)
                        .role(info.getRole())
                        .lines(deletedLines)
                        .build()
        );
    }

    private void doTick(String roomId) {
        log.info("run for room {}", roomId);
        var diffs = containerService.sortAndCommit(roomId);
        if (diffs.getDiff() != null) {
            sessionService.sendToRoom(
                    roomId,
                    CommandToRoom.builder()
                            .command(Command.PRINT)
                            .lines(diffs.getDiff())
                            .build()
            );
        }
    }
}

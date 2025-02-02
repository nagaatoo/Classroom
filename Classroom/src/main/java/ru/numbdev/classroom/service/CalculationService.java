package ru.numbdev.classroom.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import ru.numbdev.classroom.dto.Command;
import ru.numbdev.classroom.dto.LineBlock;
import ru.numbdev.classroom.dto.CommandToRoom;
import ru.numbdev.classroom.dto.Role;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculationService {

    private final Map<String, Thread> rooms = new ConcurrentHashMap<>();
    private final SessionService sessionService;
    private final ContainerService containerService;

    public void registerRoomIfAbsent(WebSocketSession session) {
        sessionService.addSession(session);
        var info = sessionService.getSessionInfo(session);
        rooms.computeIfAbsent(info.getRoomId(), roomId -> {
            return Thread.ofVirtual().start(
                    () -> {
                        while (true) {
                            try {
                                Thread.sleep(50);
                                doTick(roomId);
                                log.info("foo" + Thread.currentThread().getName());
                            } catch (InterruptedException e) {
                                return;
                            } catch (Exception e) {
                                log.error("Job error", e);
                            }
                        }
                    }
            );
        });
    }

    public void removeFromRoom(WebSocketSession session) {
        var info = sessionService.getSessionInfo(session);
        sessionService.removeSession(session);
        if (sessionService.roomIsEmpty(info.getRoomId())) {
            rooms.computeIfPresent(info.getRoomId(), (roomId, job) -> {
                job.interrupt();
                return job;
            });

            rooms.remove(info.getRoomId());
        }
    }

    public void addDiff(WebSocketSession session, LineBlock block) {
        var info = sessionService.getSessionInfo(session);
        containerService.addLine(info, block);
    }

    public void sendClean(WebSocketSession session) {
        var info = sessionService.getSessionInfo(session);
        containerService.clean(info);
        sessionService.sendToRoom(
                info.getRoomId(),
                CommandToRoom.builder()
                        .command(info.getRole() == Role.TEACHER ? Command.TEACHER_CLEAN : Command.CLEAN)
                        .build()
        );
    }

    private void doTick(String roomId) {
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

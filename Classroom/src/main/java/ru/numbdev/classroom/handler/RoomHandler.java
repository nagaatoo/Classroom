package ru.numbdev.classroom.handler;

import java.util.Optional;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.numbdev.classroom.dto.Command;
import ru.numbdev.classroom.dto.CommandFromRoom;
import ru.numbdev.classroom.service.RoomService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomHandler implements WebSocketHandler {

    private final Gson gson;
    private final RoomService roomService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        roomService.registerRoomIfAbsent(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        mapToObject((String) message.getPayload())
                .ifPresent(command -> doCommand(
                        session,
                        command)
                );
    }

    private Optional<CommandFromRoom> mapToObject(String msg) {
        var obj = gson.fromJson(msg, CommandFromRoom.class);
        return obj.getCommand() == Command.PING ? Optional.empty() : Optional.of(obj);
    }

    private void doCommand(WebSocketSession session, CommandFromRoom command) {
        switch (command.getCommand()) {
            case PRINT -> {
                var line = command.getBlock();
                roomService.addDiff(session, line);
                log.info("Do print for :" + session.getId());
            }
            case CLEAN -> {
                roomService.sendClean(session);
                log.info("Do clean for: " + session.getId());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("Connection error", exception);
        roomService.removeFromRoom(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("Connection error");
        roomService.removeFromRoom(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}

package ru.numbdev.classroom.handler;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.numbdev.classroom.dto.Command;
import ru.numbdev.classroom.dto.CommandFromRoom;
import ru.numbdev.classroom.dto.RoomWebSocketSession;
import ru.numbdev.classroom.service.RoomService;
import ru.numbdev.classroom.service.UserService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomHandler implements WebSocketHandler {

    private final Gson gson;
    private final RoomService roomService;
    private final UserService userService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        roomService.registerRoomIfAbsent(
                RoomWebSocketSession.getInstanse(
                        session,
                        userService.getUserInfo(RoomWebSocketSession.getUserId(session)),
                        gson));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        mapToObject((String) message.getPayload())
                .ifPresent(command -> doCommand(
                        session,
                        command));
    }

    private Optional<CommandFromRoom> mapToObject(String msg) {
        var obj = gson.fromJson(msg, CommandFromRoom.class);
        return obj.getCommand() == Command.PING ? Optional.empty() : Optional.of(obj);
    }

    private void doCommand(WebSocketSession session, CommandFromRoom command) {
        switch (command.getCommand()) {
            case PRINT -> {
                var line = command.getBlock();
                roomService.addDiff(session.getId(), line);
                log.info("Do print for :" + session.getId());
            }
            case CLEAN -> {
                roomService.sendClean(session.getId());
                log.info("Do clean for: " + session.getId());
            }
            case TO_PAGE -> {
                roomService.goToPage(session.getId(), command.getPageNumber());
                log.info("{} go to page {}: ", session.getId(), command.getPageNumber());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("Connection error", exception);
        roomService.removeFromRoom(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("Connection error");
        roomService.removeFromRoom(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}

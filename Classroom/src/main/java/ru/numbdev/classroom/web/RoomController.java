package ru.numbdev.classroom.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ru.numbdev.classroom.dto.RoomPage;
import ru.numbdev.classroom.service.RoomService;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/all")
    public ResponseEntity<RoomPage> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

}

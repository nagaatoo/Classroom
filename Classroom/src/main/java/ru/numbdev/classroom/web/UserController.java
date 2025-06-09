package ru.numbdev.classroom.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ru.numbdev.classroom.dto.UserAuthorizeDto;
import ru.numbdev.classroom.dto.UserInfo;
import ru.numbdev.classroom.service.UserService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserInfo> authorize(@RequestBody UserAuthorizeDto userInfo) {
        return ResponseEntity.ok(userService.getUserInfo(userInfo.getUserName()));
    }
}

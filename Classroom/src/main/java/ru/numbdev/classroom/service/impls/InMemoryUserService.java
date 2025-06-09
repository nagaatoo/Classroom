package ru.numbdev.classroom.service.impls;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import ru.numbdev.classroom.dto.Role;
import ru.numbdev.classroom.dto.UserInfo;
import ru.numbdev.classroom.service.UserService;

@Service
@RequiredArgsConstructor
public class InMemoryUserService implements UserService {

    private static final Map<String, UserInfo> users = new HashMap<>();

    @PostConstruct
    private void initForTest() {
        users.put("user", UserInfo.builder().userId("4a747a34-dd76-478f-958e-eef957da362b").name("user").role(Role.STUDENT).build());
        users.put("teacher", UserInfo.builder().userId("7aee1fea-04c7-4b72-be16-295a31951d9c").name("teacher").role(Role.TEACHER).build());
    }

    public UserInfo getUserInfo(String userName) {
        return users.getOrDefault(
                userName,
                UserInfo.builder()
                        .userId(UUID.randomUUID().toString())
                        .name(userName)
                        .role(Role.STUDENT)
                        .build());
    }

}

package ru.numbdev.classroom.service;

import ru.numbdev.classroom.dto.UserInfo;

public interface UserService {

    UserInfo autorization(String userName);
    UserInfo getUserInfo(String userId);

}

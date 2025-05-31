package ru.numbdev.classroom.service;

import org.springframework.stereotype.Service;

import ru.numbdev.classroom.dto.Role;

@Service
public class UserService { // Удалить после введения авторизации

    private String teacherId;

    public Role getRoleUser(String userId) {
        if (teacherId == null) {
            teacherId = userId;
            return Role.TEACHER;
        }    

        return Role.STUDENT;
    } 

    public void remove() {
        teacherId = null;
    }

}

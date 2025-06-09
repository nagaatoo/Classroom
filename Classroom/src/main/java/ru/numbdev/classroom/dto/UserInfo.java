package ru.numbdev.classroom.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfo {

    private String userId;
    private String name;
    private Role role;

}

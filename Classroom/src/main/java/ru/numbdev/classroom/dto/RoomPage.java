package ru.numbdev.classroom.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomPage {

    private List<RoomDto> rooms;
    private int currentPage;
    private int totalPages;
    private int totalItems;
    private int itemsPerPage;

}

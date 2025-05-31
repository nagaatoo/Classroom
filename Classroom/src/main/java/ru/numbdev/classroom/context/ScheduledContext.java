package ru.numbdev.classroom.context;

import java.util.UUID;

public interface ScheduledContext {
    void sendDiff();
    UUID getRoomId();
}

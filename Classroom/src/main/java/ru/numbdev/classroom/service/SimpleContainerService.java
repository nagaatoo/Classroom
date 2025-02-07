package ru.numbdev.classroom.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.numbdev.classroom.dto.*;

@Service
@RequiredArgsConstructor
public class SimpleContainerService implements ContainerService {

    private final Map<String, Map<String, Line>> cache = new HashMap<>();

    @Override
    public void addLine(RoomWebSocketSessionInfo info, LineBlock block) {
        cache.computeIfAbsent(info.getRoomId(), k -> new HashMap<>());
        var room = cache.get(info.getRoomId());
        synchronized (room) {
            cache
                    .get(info.getRoomId())
                    .computeIfPresent(
                            block.getId(),
                            (k, v) -> {
                                var newPoint = block.getPoint();
                                v.getPoints().add(newPoint);
                                if (newPoint.getOrder() == LineOrder.LAST) {
                                    v.setFinished(true);
                                }
                                return v;
                            }
                    );
            cache
                    .get(info.getRoomId())
                    .computeIfAbsent(
                            block.getId(),
                            k -> {
                                List<Point> points = new ArrayList<>();
                                points.add(block.getPoint());
                                return Line.builder()
                                        .id(UUID.fromString(k))
                                        .sessionIdOwner(info.getSessionId())
                                        .userIdOwner(info.getUserId())
                                        .role(info.getRole())
                                        .created(block.getPoint().getTimestamp())
                                        .type(block.getType())
                                        .points(points)
                                        .build();
                            }
                    );
        }
    }

    @Override
    public Map<String, Line> clean(RoomWebSocketSessionInfo info) {
        Map<String, Line> lines = cache.getOrDefault(info.getRoomId(), Map.of());
        if (lines.isEmpty()) {
            return Map.of();
        }

        synchronized (lines) {
            Map<String, Line> deletedLines = new HashMap<>();
            for (Map.Entry<String, Line> es : lines.entrySet()) {
                var line = es.getValue();
                if (line.getUserIdOwner().equals(info.getUserId())) {
                    deletedLines.put(line.getId().toString(), line);
                    line.setDeleted(true);
                }
            }

            return deletedLines;
        }
    }

    @Override
    public DiffToRoom getCurrent(String roomId) {
        var room = cache.get(roomId);
        if (room == null) {
            return DiffToRoom.builder().build();
        }
        synchronized (room) {
            return DiffToRoom.builder().diff(room).build();
        }
    }

    @Override
    public DiffToRoom sortAndCommit(String roomId) {
        Map<String, Line> current = cache.getOrDefault(roomId, new HashMap<>());

        if (current.isEmpty()) {
            return DiffToRoom.builder().build();
        }

        Map<String, Line> result = new HashMap<>();
        for (Map.Entry<String, Line> keyWithLine : current.entrySet()) {
            var idLine = keyWithLine.getKey();
            var line = keyWithLine.getValue();
            if (line.isDeleted()) {
                continue;
            }

            List<Point> targetPoints = line.getPoints();
            if (line.isWasReaded()) {
                continue;
            }

            List<Point> sortedPoints = new ArrayList<>();
            for (int i = line.getLastReadedPointId(); i <= targetPoints.size() - 1; i++) {
                var point = targetPoints.get(i);
                sortedPoints.add(point);
                point.setWasReaded(true);
                line.setLastReadedPointId(i);
            }

            if (targetPoints.getLast() != null && targetPoints.getLast().isWasReaded()) {
                line.setWasReaded(true);
            }

            result.put(idLine, line.cloneLine(sortedPoints));
        }

        return DiffToRoom.builder()
                .diff(result)
                .build();
    }

    @Scheduled(fixedRate = 1000)
    private void clean() {
        for (Map.Entry<String, Map<String, Line>> rooms : cache.entrySet()) {
             synchronized (rooms.getValue()) {
                 var room = rooms.getValue();

                 List<String> keysForDelete = new ArrayList<>();
                 synchronized (room) {
                     for (Map.Entry<String, Line> keyWithLine : room.entrySet()) {
                         if (keyWithLine.getValue().isDeleted()) {
                             keysForDelete.add(keyWithLine.getKey());
                         }
                     }

                     keysForDelete.forEach(room::remove);
                 }
             }
        }
    }
}

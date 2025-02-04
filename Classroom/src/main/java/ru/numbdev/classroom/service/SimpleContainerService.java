package ru.numbdev.classroom.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.numbdev.classroom.dto.DiffToRoom;
import ru.numbdev.classroom.dto.Line;
import ru.numbdev.classroom.dto.LineBlock;
import ru.numbdev.classroom.dto.Point;
import ru.numbdev.classroom.dto.RoomWebSocketSessionInfo;

@Service
@RequiredArgsConstructor
public class SimpleContainerService implements ContainerService {

    private final Map<String, Map<String, Line>> cache = new ConcurrentHashMap<>();

    @Override
    public void addLine(RoomWebSocketSessionInfo info, LineBlock block) {
        cache.computeIfAbsent(info.getRoomId(), k -> new ConcurrentHashMap<>());
        var room = cache.get(info.getRoomId());
        synchronized (room) {
            cache
                    .get(info.getRoomId())
                    .computeIfPresent(
                            block.getId(),
                            (k, v) -> {
                                v.getPoints().add(block.getPoint());
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
    public void clean(RoomWebSocketSessionInfo info) {
        Map<String, Line> lines = cache.getOrDefault(info.getRoomId(), Map.of());
        if (lines.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Line> es : lines.entrySet()) {
            var line = es.getValue();
            if (line.getUserIdOwner().equals(info.getUserId())) {
                lines.remove(line.getId().toString());
            }
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
        for (Map.Entry<String, Line> es : current.entrySet()) {
            var sortedPoints = es
                    .getValue()
                    .getPoints()
                    .stream()
                    .filter(p -> !p.isForDelete())
                    .sorted(Comparator.comparing(Point::getTimestamp))
                    .peek(p -> p.setForDelete(true))
                    .toList();

            result.put(es.getKey(), es.getValue().cloneLine().setPoints(sortedPoints));
        }

        return DiffToRoom.builder()
                .diff(result)
                .build();
    }

//    @Scheduled(fixedRate = 1000)
    private void clean() {
        for (Map.Entry<String, Map<String, Line>> es : cache.entrySet()) {
            for (Map.Entry<String, Line> esLine : es.getValue().entrySet()) {
                var points = esLine.getValue().getPoints();
                if (CollectionUtils.isEmpty(points)) {
//                    es.getValue().remove(esLine.getKey());
                    continue;
                }

                for (int i = 0; i < points.size(); i++) {
                    var point = points.get(i);
                    if (point.isForDelete()) {
                        points.remove(point);
                    }
                }
            }
        }
    }
}

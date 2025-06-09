package ru.numbdev.classroom.service.impls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ru.numbdev.classroom.dto.DiffToRoom;
import ru.numbdev.classroom.dto.Line;
import ru.numbdev.classroom.dto.LineBlock;
import ru.numbdev.classroom.dto.LineOrder;
import ru.numbdev.classroom.dto.Point;
import ru.numbdev.classroom.dto.RoomWebSocketSession;
import ru.numbdev.classroom.service.ContainerService;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class SimpleContainerService implements ContainerService {

    private final Map<Integer, Map<String, Line>> cache = new ConcurrentHashMap<>();

    @Override
    public void addLine(RoomWebSocketSession session, LineBlock block) {
        var pageCache = cache.computeIfAbsent(block.getPageNumber(), k -> new ConcurrentHashMap<>());

        pageCache
                .computeIfPresent(
                        block.getId(),
                        (k, v) -> {
                            var newPoint = block.getPoint();
                            v.getPoints().add(newPoint);
                            if (newPoint.getOrder() == LineOrder.LAST) {
                                v.getIsFinished().set(true);
                            }
                            return v;
                        });
        pageCache
                .computeIfAbsent(
                        block.getId(),
                        k -> {
                            List<Point> points = new CopyOnWriteArrayList<>();
                            points.add(block.getPoint());
                            return Line.builder()
                                    .id(UUID.fromString(k))
                                    .sessionIdOwner(session.getSessionId())
                                    .userIdOwner(session.getUserId())
                                    .role(session.getUserInfo().getRole())
                                    .created(block.getPoint().getTimestamp())
                                    .type(block.getType())
                                    .points(points)
                                    .build();
                        });
    }

    @Override
    public DiffToRoom clean(String sessionId, int pageNumber) {
        var pageCache = cache.get(pageNumber);
        if (pageCache == null) {
            return DiffToRoom.builder().build();
        }

        Map<String, Line> deletedLines = new HashMap<>();
        for (Map.Entry<String, Line> es : pageCache.entrySet()) {
            var line = es.getValue();
            if (line.getSessionIdOwner().equals(sessionId)) {
                deletedLines.put(line.getId().toString(), line);
                line.getIsDeleted().set(true);
            }
        }

        return DiffToRoom
                .builder()
                .diff(Map.of(pageNumber, deletedLines))
                .build();
    }

    @Override
    public DiffToRoom getCurrent(int pageNumber) {
        return DiffToRoom
                .builder()
                .diff(Map.of(pageNumber, Map.copyOf(cache.getOrDefault(pageNumber, new HashMap<>()))))
                .build();
    }

    @Override
    public synchronized Map<Integer, Map<String, Line>> diffAndCommit() {
        if (cache.isEmpty()) {
            return Map.of();
        }

        Map<Integer, Map<String, Line>> result = new HashMap<>();
        for (var esPage : cache.entrySet()) {
            var pageNumber = esPage.getKey();
            var page = esPage.getValue();
            Map<String, Line> pageResult = new HashMap<>();
            for (Map.Entry<String, Line> keyWithLine : page.entrySet()) {
                var idLine = keyWithLine.getKey();
                var line = keyWithLine.getValue();
                if (line.getIsDeleted().get()) {
                    continue;
                }

                List<Point> targetPoints = line.getPoints();
                if (line.getWasReaded().get()) {
                    continue;
                }

                List<Point> sortedPoints = new ArrayList<>();
                for (int i = line.getLastReadedPointId().get(); i <= targetPoints.size() - 1; i++) {
                    var point = targetPoints.get(i);
                    sortedPoints.add(point);
                    point.setWasReaded(true);
                    line.getLastReadedPointId().incrementAndGet();
                }

                if (line.getIsFinished().get()) {
                    line.getWasReaded().set(true);
                }

                pageResult.put(idLine, line.cloneLine(sortedPoints));
            }

            result.put(pageNumber, pageResult);
        }

        return result;
    }

}

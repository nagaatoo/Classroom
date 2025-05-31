package ru.numbdev.classroom.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import ru.numbdev.classroom.context.ScheduledContext;

@Slf4j
@Service
public class ScheduledTaskService {

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService virtualThreadScheduler = Executors.newScheduledThreadPool(
        100,
        Thread.ofVirtual()
              .name("virtual-scheduled-", 0)
              .factory()
    );

    public void addToSchedule(ScheduledContext context) {
        String contextKey = generateContextKey(context);
        
        ScheduledFuture<?> future = virtualThreadScheduler.scheduleAtFixedRate(
            () -> {
                try {
                    context.sendDiff();
                } catch (Exception e) {
                    log.error("Error in scheduled task for {}: {}", contextKey, e.getMessage(), e);
                }
            },
            0, 
            50,
            TimeUnit.MILLISECONDS
        );
        
        scheduledTasks.put(contextKey, future);
        log.info("Added task with virtual threads for: {}", contextKey);
    }
    
    /**
     * Удаляет ScheduledContext из планировщика
     */
    public void removeFromSchedule(ScheduledContext context) {
        String contextKey = generateContextKey(context);
        ScheduledFuture<?> future = scheduledTasks.remove(contextKey);
        
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
            log.info("Task was deleted: {}", contextKey);
        }
    }
    
    /**
     * Возвращает количество активных задач
     */
    public int getActiveTasksCount() {
        return (int) scheduledTasks.values().stream()
                .filter(future -> !future.isCancelled() && !future.isDone())
                .count();
    }
    
    /**
     * Останавливает все scheduled задачи
     */
    public void stopAllTasks() {
        scheduledTasks.values().forEach(future -> {
            if (!future.isCancelled()) {
                future.cancel(true);
            }
        });
        scheduledTasks.clear();
        log.info("Stopped all tasks");
    }
    
    /**
     * Проверяет, запущена ли задача для контекста
     */
    public boolean isScheduled(ScheduledContext context) {
        String contextKey = generateContextKey(context);
        ScheduledFuture<?> future = scheduledTasks.get(contextKey);
        return future != null && !future.isCancelled() && !future.isDone();
    }
    
    private String generateContextKey(ScheduledContext context) {
        return context.getClass().getSimpleName() + "@" + context.getRoomId();
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("Stopping scheduler with virtual threads");
        stopAllTasks();
        
        virtualThreadScheduler.shutdown();
        try {
            if (!virtualThreadScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                virtualThreadScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            virtualThreadScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

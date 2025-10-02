package com.fullcars.restapi.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

@Service
public class TaskQueueService {

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final Map<String, TaskStatusInfo> tasks = new ConcurrentHashMap<>();

    public String enqueue(Runnable job) {
        String taskId = UUID.randomUUID().toString();
        tasks.put(taskId, new TaskStatusInfo(TaskStatus.PENDIENTE, null));

        executor.submit(() -> {
            tasks.put(taskId, new TaskStatusInfo(TaskStatus.CARGANDO, null));
            try {
                job.run();
                tasks.put(taskId, new TaskStatusInfo(TaskStatus.TERMINADO, null));
            } catch (Exception e) {
                tasks.put(taskId, new TaskStatusInfo(TaskStatus.FALLADO, e.getMessage()));
            }
        });

        return taskId;
    }

    public TaskStatusInfo getStatus(String taskId) {
        return tasks.getOrDefault(taskId, new TaskStatusInfo(TaskStatus.NO_ENCONTRADO, null));
    }

    // DTO para estado de tarea
    public static class TaskStatusInfo {
        private final TaskStatus status;
        private final String error;

        public TaskStatusInfo(TaskStatus status, String error) {
            this.status = status;
            this.error = error;
        }

        public TaskStatus getStatus() { return status; }
        public String getError() { return error; }
    }

    public enum TaskStatus {
        PENDIENTE, CARGANDO, TERMINADO, FALLADO, NO_ENCONTRADO
    }
}


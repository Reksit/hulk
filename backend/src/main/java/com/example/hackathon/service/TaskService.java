package com.example.hackathon.service;

import com.example.hackathon.dto.TaskRequest;
import com.example.hackathon.dto.TaskResponse;
import com.example.hackathon.model.Task;
import com.example.hackathon.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private TaskReminderService taskReminderService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TaskResponse createTask(String studentId, TaskRequest request) {
        Task task = new Task();
        task.setStudentId(studentId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStartDateTime(request.getStartDateTime());
        task.setEndDateTime(request.getEndDateTime());
        task.setPriority(Task.TaskPriority.valueOf(request.getPriority()));
        
        // Auto-determine status based on dates
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(request.getStartDateTime())) {
            task.setStatus(Task.TaskStatus.PENDING);
        } else if (now.isAfter(request.getStartDateTime()) && now.isBefore(request.getEndDateTime())) {
            task.setStatus(Task.TaskStatus.ONGOING);
        }
        
        Task savedTask = taskRepository.save(task);
        return new TaskResponse(savedTask);
    }

    public List<TaskResponse> getAllTasks(String studentId) {
        List<Task> tasks = taskRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
        return tasks.stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByStatus(String studentId, Task.TaskStatus status) {
        List<Task> tasks = taskRepository.findByStudentIdAndStatus(studentId, status);
        return tasks.stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    public TaskResponse updateTask(String taskId, String studentId, TaskRequest request) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            throw new RuntimeException("Task not found");
        }
        
        Task task = taskOpt.get();
        if (!task.getStudentId().equals(studentId)) {
            throw new RuntimeException("Unauthorized to update this task");
        }
        
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStartDateTime(request.getStartDateTime());
        task.setEndDateTime(request.getEndDateTime());
        task.setPriority(Task.TaskPriority.valueOf(request.getPriority()));
        task.setUpdatedAt(LocalDateTime.now());
        
        Task savedTask = taskRepository.save(task);
        return new TaskResponse(savedTask);
    }

    public void deleteTask(String taskId, String studentId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            throw new RuntimeException("Task not found");
        }
        
        Task task = taskOpt.get();
        if (!task.getStudentId().equals(studentId)) {
            throw new RuntimeException("Unauthorized to delete this task");
        }
        
        taskRepository.deleteById(taskId);
    }

    public TaskResponse markTaskCompleted(String taskId, String studentId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            throw new RuntimeException("Task not found");
        }
        
        Task task = taskOpt.get();
        if (!task.getStudentId().equals(studentId)) {
            throw new RuntimeException("Unauthorized to update this task");
        }
        
        task.setStatus(Task.TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        Task savedTask = taskRepository.save(task);
        return new TaskResponse(savedTask);
    }

    public TaskResponse updateTaskStatus(String taskId, String studentId, Task.TaskStatus status) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            throw new RuntimeException("Task not found");
        }
        
        Task task = taskOpt.get();
        if (!task.getStudentId().equals(studentId)) {
            throw new RuntimeException("Unauthorized to update this task");
        }
        
        task.setStatus(status);
        if (status == Task.TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        }
        task.setUpdatedAt(LocalDateTime.now());
        
        Task savedTask = taskRepository.save(task);
        return new TaskResponse(savedTask);
    }

    public TaskResponse getTaskById(String taskId, String studentId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            throw new RuntimeException("Task not found");
        }
        
        Task task = taskOpt.get();
        if (!task.getStudentId().equals(studentId)) {
            throw new RuntimeException("Unauthorized to view this task");
        }
        
        return new TaskResponse(task);
    }

    public long getTaskCountByStatus(String studentId, Task.TaskStatus status) {
        return taskRepository.countByStudentIdAndStatus(studentId, status);
    }

    public List<TaskResponse> getOverdueTasks(String studentId) {
        List<Task> tasks = taskRepository.findByStudentIdAndEndDateTimeBeforeAndStatusNot(
            studentId, LocalDateTime.now(), Task.TaskStatus.COMPLETED);
        return tasks.stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getUpcomingTasks(String studentId) {
        List<Task> tasks = taskReminderService.getUpcomingTasks(studentId);
        return tasks.stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getRoadmapTasks(String studentId) {
        List<Task> tasks = taskRepository.findByStudentIdAndTaskType(studentId, "ROADMAP");
        return tasks.stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    public TaskResponse createRoadmapTask(String studentId, String title, String domain, String timeframe, List<String> roadmapSteps) {
        try {
            Task task = new Task();
            task.setStudentId(studentId);
            task.setTitle(title);
            task.setDescription("AI-generated learning roadmap for " + domain + " (" + timeframe + ")");
            task.setTaskType("ROADMAP");
            task.setStatus(Task.TaskStatus.PENDING);
            task.setPriority(Task.TaskPriority.MEDIUM);
            
            // Set dates - roadmap tasks are long-term
            LocalDateTime now = LocalDateTime.now();
            task.setStartDateTime(now);
            
            // Parse timeframe and set end date accordingly
            LocalDateTime endDate = parseTimeframeToEndDate(timeframe, now);
            task.setEndDateTime(endDate);
            
            // Store roadmap data as JSON
            Map<String, Object> roadmapData = new HashMap<>();
            roadmapData.put("domain", domain);
            roadmapData.put("timeframe", timeframe);
            roadmapData.put("steps", roadmapSteps);
            task.setRoadmapData(objectMapper.writeValueAsString(roadmapData));
            
            Task savedTask = taskRepository.save(task);
            return new TaskResponse(savedTask);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create roadmap task: " + e.getMessage());
        }
    }

    private LocalDateTime parseTimeframeToEndDate(String timeframe, LocalDateTime startDate) {
        String lowerTimeframe = timeframe.toLowerCase();
        
        if (lowerTimeframe.contains("week")) {
            int weeks = extractNumber(lowerTimeframe);
            return startDate.plusWeeks(weeks > 0 ? weeks : 4);
        } else if (lowerTimeframe.contains("month")) {
            int months = extractNumber(lowerTimeframe);
            return startDate.plusMonths(months > 0 ? months : 3);
        } else if (lowerTimeframe.contains("year")) {
            int years = extractNumber(lowerTimeframe);
            return startDate.plusYears(years > 0 ? years : 1);
        } else if (lowerTimeframe.contains("day")) {
            int days = extractNumber(lowerTimeframe);
            return startDate.plusDays(days > 0 ? days : 30);
        } else {
            // Default to 3 months
            return startDate.plusMonths(3);
        }
    }

    private int extractNumber(String text) {
        try {
            String[] words = text.split("\\s+");
            for (String word : words) {
                if (word.matches("\\d+")) {
                    return Integer.parseInt(word);
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return 0;
    }
}
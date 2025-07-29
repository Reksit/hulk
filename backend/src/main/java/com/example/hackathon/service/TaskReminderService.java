package com.example.hackathon.service;

import com.example.hackathon.model.Task;
import com.example.hackathon.model.User;
import com.example.hackathon.repository.TaskRepository;
import com.example.hackathon.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskReminderService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void checkTaskReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);

        // Find all tasks that are due within 24 hours and not completed
        List<Task> upcomingTasks = taskRepository.findTasksDueWithin24Hours(now, tomorrow);

        for (Task task : upcomingTasks) {
            // Check if reminder was already sent (you might want to add a field to track this)
            if (!task.isReminderSent()) {
                sendTaskReminder(task);
                task.setReminderSent(true);
                taskRepository.save(task);
            }
        }
    }

    private void sendTaskReminder(Task task) {
        try {
            User student = userRepository.findById(task.getStudentId()).orElse(null);
            if (student != null) {
                emailService.sendTaskReminder(student.getEmail(), task);
            }
        } catch (Exception e) {
            System.err.println("Failed to send task reminder: " + e.getMessage());
        }
    }

    public List<Task> getUpcomingTasks(String studentId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        return taskRepository.findUpcomingTasksForStudent(studentId, now, tomorrow);
    }
}
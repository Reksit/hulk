package com.example.hackathon.repository;

import com.example.hackathon.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByStudentIdOrderByCreatedAtDesc(String studentId);
    List<Task> findByStudentIdAndStatus(String studentId, Task.TaskStatus status);
    List<Task> findByStudentIdAndStartDateTimeBetween(String studentId, LocalDateTime start, LocalDateTime end);
    List<Task> findByStudentIdAndEndDateTimeBeforeAndStatusNot(String studentId, LocalDateTime dateTime, Task.TaskStatus status);
    long countByStudentIdAndStatus(String studentId, Task.TaskStatus status);
    
    @Query("{'endDateTime': {'$gte': ?0, '$lte': ?1}, 'status': {'$ne': 'COMPLETED'}, 'reminderSent': false}")
    List<Task> findTasksDueWithin24Hours(LocalDateTime now, LocalDateTime tomorrow);
    
    @Query("{'studentId': ?0, 'endDateTime': {'$gte': ?1, '$lte': ?2}, 'status': {'$ne': 'COMPLETED'}}")
    List<Task> findUpcomingTasksForStudent(String studentId, LocalDateTime now, LocalDateTime tomorrow);
    
    List<Task> findByStudentIdAndTaskType(String studentId, String taskType);
}
package com.example.hackathon.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.example.hackathon.model.Task;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendOTP(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@smarteval.com");
        message.setTo(to);
        message.setSubject("SmartEval - Email Verification");
        message.setText("Your verification code is: " + otp + "\n\nThis code will expire in 10 minutes.");
        
        emailSender.send(message);
    }

    public void sendAlumniApprovalNotification(String to, String professorName, boolean approved) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@smarteval.com");
        message.setTo(to);
        message.setSubject("SmartEval - Alumni Request Status");
        
        String status = approved ? "approved" : "rejected";
        message.setText("Your alumni registration request has been " + status + " by Professor " + professorName + ".");
        
        emailSender.send(message);
    }

    public void sendTaskReminder(String to, Task task) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@smarteval.com");
        message.setTo(to);
        message.setSubject("SmartEval - Task Reminder: " + task.getTitle());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        String dueDate = task.getEndDateTime().format(formatter);
        
        String messageText = String.format(
            "Hi there!\n\n" +
            "This is a friendly reminder that your task '%s' is due soon.\n\n" +
            "Due Date: %s\n" +
            "Priority: %s\n\n" +
            "%s\n\n" +
            "Don't forget to complete it on time!\n\n" +
            "Best regards,\n" +
            "SmartEval Team",
            task.getTitle(),
            dueDate,
            task.getPriority(),
            task.getDescription() != null ? "Description: " + task.getDescription() : ""
        );
        
        message.setText(messageText);
        emailSender.send(message);
    }
}
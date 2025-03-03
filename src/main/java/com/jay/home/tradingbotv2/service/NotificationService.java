package com.jay.home.tradingbotv2.service;

import com.jay.home.tradingbotv2.model.Budget;
import com.jay.home.tradingbotv2.model.Notification;
import com.jay.home.tradingbotv2.model.User;
import com.jay.home.tradingbotv2.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;


    public void sendBudgetWarning(User user, Budget budget, BigDecimal currentSpending, BigDecimal percentageUsed) {
        String categoryName = budget.getCategory() != null ? budget.getCategory().getName() : "all categories";
        
        String message = String.format(
                "Warning: You've used %.2f%% (%.2f of %.2f) of your %s budget for %s.",
                percentageUsed.multiply(BigDecimal.valueOf(100)),
                currentSpending,
                budget.getAmount(),
                budget.getPeriod().toLowerCase(),
                categoryName
        );

        // Create notification record
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setBudget(budget);
        notification.setMessage(message);
        notification.setSentAt(LocalDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);

        // Send email
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(user.getEmail());
        email.setSubject("Budget Alert: " + budget.getName());
        email.setText(message);
        mailSender.send(email);
    }
}
package com.jay.home.finmanapp.repository;

import com.jay.home.finmanapp.model.Budget;
import com.jay.home.finmanapp.model.Notification;
import com.jay.home.finmanapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find notifications by user
    List<Notification> findByUser(User user);

    // Find notifications by user with pagination
    Page<Notification> findByUser(User user, Pageable pageable);

    // Find unread notifications by user
    List<Notification> findByUserAndIsReadFalse(User user);

    // Count unread notifications by user
    long countByUserAndIsReadFalse(User user);

    // Find notifications by user and budget
    List<Notification> findByUserAndBudget(User user, Budget budget);

    // Find notifications by user within a date range
    List<Notification> findByUserAndSentAtBetween(User user, LocalDateTime start, LocalDateTime end);

    // Find notifications by user and message content
    List<Notification> findByUserAndMessageContaining(User user, String keyword);

    // Mark a notification as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    int markAsRead(Long id);

    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsRead(Long userId);

    // Delete old notifications (e.g., for cleanup jobs)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.sentAt < :olderThan")
    int deleteOldNotifications(LocalDateTime olderThan);

    // Find recent notifications for a user
    @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.sentAt DESC")
    List<Notification> findRecentNotifications(User user, Pageable pageable);

    // Delete notifications for a specific budget
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.budget.id = :budgetId")
    int deleteByBudget(Long budgetId);
}
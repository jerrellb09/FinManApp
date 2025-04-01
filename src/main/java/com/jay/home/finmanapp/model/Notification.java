package com.jay.home.finmanapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity class representing a notification in the application.
 * 
 * This class stores information about notifications sent to users regarding their
 * financial activities. Notifications can be related to budget alerts, bill payment
 * reminders, unusual spending patterns, or other important financial events.
 * 
 * The application uses notifications to keep users informed about their financial
 * status and to encourage proactive financial management.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    /**
     * Unique identifier for the notification in the application's database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who should receive this notification.
     * Each notification is targeted to a specific user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The budget associated with this notification, if any.
     * Budget-related notifications include alerts about approaching or exceeding budget limits.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    /**
     * The content of the notification message.
     * This text is displayed to the user in the notifications area of the application.
     */
    @Column(nullable = false)
    private String message;

    /**
     * Timestamp when the notification was created/sent.
     * Used for sorting notifications chronologically.
     */
    @Column(nullable = false)
    private LocalDateTime sentAt;

    /**
     * Flag indicating whether the user has read this notification.
     * Used to highlight unread notifications and track user engagement.
     */
    @Column(nullable = false)
    private boolean isRead;
}
package com.jay.home.tradingbotv2.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // CHECKING, SAVINGS, CREDIT, etc.

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private String accountId; // External account ID (from Plaid/Stripe)

    @Column(nullable = false)
    private String accessToken; // Access token for the account integration

    @Column(nullable = false)
    private String institutionId; // Bank/Institution ID

    @Column(nullable = false)
    private String institutionName; // Bank/Institution name

    @Column(nullable = false)
    private LocalDateTime lastSynced;
}
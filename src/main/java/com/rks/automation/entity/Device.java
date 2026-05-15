package com.rks.automation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a smart home device owned by a user.
 */
@Entity
@Table(name = "devices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** e.g. LIGHT, FAN, AC, LOCK, CAMERA */
    @Column(nullable = false, length = 50)
    private String type;

    /** Room or location label, e.g. "Living Room" */
    @Column(length = 100)
    private String location;

    /** Current state: ON / OFF */
    @Builder.Default
    @Column(nullable = false, length = 10)
    private String status = "OFF";

    /** Extra metadata — brightness level, temperature set-point, etc. */
    @Column(length = 255)
    private String metadata;

    /** Owner of this device */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt  = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

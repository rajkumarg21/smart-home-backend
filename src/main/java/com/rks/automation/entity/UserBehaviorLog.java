package com.rks.automation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Records every device control action (ON/OFF) with timestamp.
 * Used by the AI predictive automation engine to learn user patterns.
 */
@Entity
@Table(name = "user_behavior_logs",
        indexes = {
                @Index(name = "idx_ubl_user_device", columnList = "user_id, device_id"),
                @Index(name = "idx_ubl_recorded_at", columnList = "recorded_at")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    /** ON or OFF */
    @Column(nullable = false, length = 10)
    private String action;

    /** Source: USER, AI, AI_AGENT, DEVICE */
    @Column(length = 20)
    private String source;

    /** Hour of day 0-23 */
    @Column(nullable = false)
    private Integer hourOfDay;

    /** Day of week 1=MON … 7=SUN */
    @Column(nullable = false)
    private Integer dayOfWeek;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
        if (hourOfDay == null) {
            hourOfDay = recordedAt.getHour();
        }
        if (dayOfWeek == null) {
            dayOfWeek = recordedAt.getDayOfWeek().getValue(); // 1=MON
        }
    }
}

package com.rks.automation.entity;



import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_analytics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(nullable = false)
    private Double power;       // watts

    @Column(nullable = false)
    private Double duration;    // minutes

    @Column(nullable = false)
    private LocalDateTime recordedAt;
}


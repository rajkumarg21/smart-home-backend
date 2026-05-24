package com.rks.automation.repository;

import com.rks.automation.entity.UserBehaviorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserBehaviorLogRepository extends JpaRepository<UserBehaviorLog, Long> {

    List<UserBehaviorLog> findByUserIdOrderByRecordedAtDesc(Long userId);

    List<UserBehaviorLog> findByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long userId, LocalDateTime start, LocalDateTime end);

    /** Count how many times a device was turned ON at a specific hour across all days */
    @Query("""
            SELECT COUNT(l) FROM UserBehaviorLog l
            WHERE l.user.id = :userId
              AND l.device.id = :deviceId
              AND l.action = 'ON'
              AND l.hourOfDay = :hour
            """)
    long countOnActionsAtHour(@Param("userId") Long userId,
                               @Param("deviceId") Long deviceId,
                               @Param("hour") int hour);

    /** Find the most frequent ON hours for a device */
    @Query("""
            SELECT l.hourOfDay, COUNT(l) as freq
            FROM UserBehaviorLog l
            WHERE l.user.id = :userId
              AND l.device.id = :deviceId
              AND l.action = 'ON'
              AND l.recordedAt >= :since
            GROUP BY l.hourOfDay
            ORDER BY freq DESC
            """)
    List<Object[]> findTopOnHours(@Param("userId") Long userId,
                                   @Param("deviceId") Long deviceId,
                                   @Param("since") LocalDateTime since);

    /** Find the most frequent OFF hours for a device */
    @Query("""
            SELECT l.hourOfDay, COUNT(l) as freq
            FROM UserBehaviorLog l
            WHERE l.user.id = :userId
              AND l.device.id = :deviceId
              AND l.action = 'OFF'
              AND l.recordedAt >= :since
            GROUP BY l.hourOfDay
            ORDER BY freq DESC
            """)
    List<Object[]> findTopOffHours(@Param("userId") Long userId,
                                    @Param("deviceId") Long deviceId,
                                    @Param("since") LocalDateTime since);

    /** Aggregate: device + action + hour frequency for pattern analysis */
    @Query("""
            SELECT l.device.id, l.action, l.hourOfDay, COUNT(l) as freq
            FROM UserBehaviorLog l
            WHERE l.user.id = :userId
              AND l.recordedAt >= :since
            GROUP BY l.device.id, l.action, l.hourOfDay
            ORDER BY freq DESC
            """)
    List<Object[]> findAllPatterns(@Param("userId") Long userId,
                                    @Param("since") LocalDateTime since);

    boolean existsByUserIdAndDeviceIdAndActionAndHourOfDay(
            Long userId, Long deviceId, String action, int hourOfDay);
}

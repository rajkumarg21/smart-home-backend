package com.rks.automation.repository;

import com.rks.automation.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    /** All devices belonging to a specific user. */
    List<Device> findByUserId(Long userId);

    /** Find a device by id and owner — prevents users accessing other users' devices. */
    Optional<Device> findByIdAndUserId(Long id, Long userId);
}

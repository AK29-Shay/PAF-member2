package com.campusops.booking.repository;

import com.campusops.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.resource.id = :resourceId " +
           "AND b.status IN ('PENDING', 'APPROVED') " +
           "AND (b.startTime < :endTime AND b.endTime > :startTime)")
    boolean existsOverlappingBooking(@Param("resourceId") Long resourceId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    List<Booking> findByUserId(Long userId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.resource JOIN FETCH b.user ORDER BY b.startTime DESC")
    List<Booking> findAllWithDetails();
    
    @Query("SELECT b FROM Booking b JOIN FETCH b.resource JOIN FETCH b.user WHERE b.user.id = :userId ORDER BY b.startTime DESC")
    List<Booking> findByUserIdWithDetails(@Param("userId") Long userId);
}

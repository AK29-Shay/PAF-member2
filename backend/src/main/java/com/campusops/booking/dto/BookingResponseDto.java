package com.campusops.booking.dto;

import com.campusops.booking.model.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponseDto {
    private Long id;
    private Long resourceId;
    private String resourceName;
    private Long userId;
    private String userName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
    private Integer expectedAttendees;
    private BookingStatus status;
    private String rejectionReason;
}

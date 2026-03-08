package com.campusops.booking.dto;

import com.campusops.booking.model.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingStatusUpdateDto {

    @NotNull(message = "Status is required")
    private BookingStatus status;

    private String reason;
}

package com.campusops.booking.controller;

import com.campusops.booking.dto.BookingRequestDto;
import com.campusops.booking.dto.BookingResponseDto;
import com.campusops.booking.dto.BookingStatusUpdateDto;
import com.campusops.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // For local React testing
public class BookingController {

    private final BookingService bookingService;

    // GET - View all bookings (Admin Flow)
    @GetMapping("/all")
    public ResponseEntity<List<BookingResponseDto>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // 1. GET - View user's own bookings
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponseDto>> getUserBookings(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId));
    }

    // 2. POST - Request a new booking
    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@Valid @RequestBody BookingRequestDto request) {
        BookingResponseDto created = bookingService.createBookingRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created); // 201 Created
    }

    // 3. PATCH - Approve/Reject a booking (Admin Flow)
    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingResponseDto> updateBookingStatus(
            @PathVariable Long id,
            @Valid @RequestBody BookingStatusUpdateDto statusUpdate) {
        BookingResponseDto updated = bookingService.updateStatus(id, statusUpdate);
        return ResponseEntity.ok(updated); // 200 OK
    }

    // 4. DELETE - Cancel a booking
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}

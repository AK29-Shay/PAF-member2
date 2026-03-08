package com.campusops.booking.service;

import com.campusops.booking.dto.BookingRequestDto;
import com.campusops.booking.dto.BookingResponseDto;
import com.campusops.booking.dto.BookingStatusUpdateDto;
import com.campusops.booking.exception.ResourceConflictException;
import com.campusops.booking.exception.ResourceNotFoundException;
import com.campusops.booking.model.Booking;
import com.campusops.booking.model.BookingStatus;
import com.campusops.booking.model.Resource;
import com.campusops.booking.model.User;
import com.campusops.booking.repository.BookingRepository;
import com.campusops.booking.repository.ResourceRepository;
import com.campusops.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllBookings() {
        return bookingRepository.findAllWithDetails().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdWithDetails(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponseDto createBookingRequest(BookingRequestDto dto) {
        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        Resource resource = resourceRepository.findById(dto.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        if (!"ACTIVE".equals(resource.getStatus())) {
            throw new IllegalArgumentException("Resource is not currently active.");
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Conflict Checking
        boolean conflict = bookingRepository.existsOverlappingBooking(
                dto.getResourceId(), dto.getStartTime(), dto.getEndTime()
        );

        if (conflict) {
            throw new ResourceConflictException("The selected resource is not available during this timeframe.");
        }

        Booking booking = Booking.builder()
                .resource(resource)
                .user(user)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .purpose(dto.getPurpose())
                .expectedAttendees(dto.getExpectedAttendees())
                .status(BookingStatus.PENDING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return mapToDto(savedBooking);
    }

    @Transactional
    public BookingResponseDto updateStatus(Long bookingId, BookingStatusUpdateDto dto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));

        if (dto.getStatus() == BookingStatus.REJECTED &&
                (dto.getReason() == null || dto.getReason().trim().isEmpty())) {
            throw new IllegalArgumentException("A rejection reason is mandatory.");
        }

        booking.setStatus(dto.getStatus());
        booking.setRejectionReason(dto.getReason());

        Booking savedBooking = bookingRepository.save(booking);
        return mapToDto(savedBooking);
    }

    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking is already cancelled.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private BookingResponseDto mapToDto(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .resourceId(booking.getResource().getId())
                .resourceName(booking.getResource().getName())
                .userId(booking.getUser().getId())
                .userName(booking.getUser().getName())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .purpose(booking.getPurpose())
                .expectedAttendees(booking.getExpectedAttendees())
                .status(booking.getStatus())
                .rejectionReason(booking.getRejectionReason())
                .build();
    }
}

package com.campusops.booking.service;

import com.campusops.booking.dto.BookingRequestDto;
import com.campusops.booking.dto.BookingResponseDto;
import com.campusops.booking.dto.BookingStatusUpdateDto;
import com.campusops.booking.exception.ResourceConflictException;
import com.campusops.booking.exception.ResourceNotFoundException;
import com.campusops.booking.model.*;
import com.campusops.booking.repository.BookingRepository;
import com.campusops.booking.repository.ResourceRepository;
import com.campusops.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    private Resource testResource;
    private User testUser;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testResource = Resource.builder()
                .id(1L).name("Lecture Hall A").type("Lecture Hall")
                .capacity(100).location("Building 1").status("ACTIVE")
                .build();

        testUser = User.builder()
                .id(1L).name("Standard User").email("user@example.com").role("USER")
                .build();

        testBooking = Booking.builder()
                .id(1L).resource(testResource).user(testUser)
                .startTime(LocalDateTime.of(2026, 10, 10, 10, 0))
                .endTime(LocalDateTime.of(2026, 10, 10, 12, 0))
                .purpose("IT3030 Lecture").expectedAttendees(50)
                .status(BookingStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Should return all bookings with details")
    void getAllBookings_ShouldReturnAllBookings() {
        when(bookingRepository.findAllWithDetails()).thenReturn(Arrays.asList(testBooking));

        List<BookingResponseDto> result = bookingService.getAllBookings();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Lecture Hall A", result.get(0).getResourceName());
        verify(bookingRepository, times(1)).findAllWithDetails();
    }

    @Test
    @DisplayName("Should return bookings for a specific user")
    void getUserBookings_ShouldReturnUserBookings() {
        when(bookingRepository.findByUserIdWithDetails(1L)).thenReturn(Arrays.asList(testBooking));

        List<BookingResponseDto> result = bookingService.getUserBookings(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Standard User", result.get(0).getUserName());
    }

    @Test
    @DisplayName("Should create a booking request successfully")
    void createBookingRequest_Success() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setResourceId(1L);
        dto.setUserId(1L);
        dto.setStartTime(LocalDateTime.of(2026, 12, 1, 10, 0));
        dto.setEndTime(LocalDateTime.of(2026, 12, 1, 12, 0));
        dto.setPurpose("Team Meeting");
        dto.setExpectedAttendees(10);

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testResource));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookingRepository.existsOverlappingBooking(anyLong(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        BookingResponseDto result = bookingService.createBookingRequest(dto);

        assertNotNull(result);
        assertEquals(BookingStatus.PENDING, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception when start time is after end time")
    void createBookingRequest_InvalidTimeRange() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setResourceId(1L);
        dto.setUserId(1L);
        dto.setStartTime(LocalDateTime.of(2026, 12, 1, 14, 0));
        dto.setEndTime(LocalDateTime.of(2026, 12, 1, 10, 0));
        dto.setPurpose("Test");
        dto.setExpectedAttendees(5);

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.createBookingRequest(dto));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when resource does not exist")
    void createBookingRequest_ResourceNotFound() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setResourceId(999L);
        dto.setUserId(1L);
        dto.setStartTime(LocalDateTime.of(2026, 12, 1, 10, 0));
        dto.setEndTime(LocalDateTime.of(2026, 12, 1, 12, 0));
        dto.setPurpose("Test");
        dto.setExpectedAttendees(5);

        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                bookingService.createBookingRequest(dto));
    }

    @Test
    @DisplayName("Should throw ResourceConflictException when time slot overlaps")
    void createBookingRequest_ConflictDetected() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setResourceId(1L);
        dto.setUserId(1L);
        dto.setStartTime(LocalDateTime.of(2026, 12, 1, 10, 0));
        dto.setEndTime(LocalDateTime.of(2026, 12, 1, 12, 0));
        dto.setPurpose("Conflicting Meeting");
        dto.setExpectedAttendees(5);

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testResource));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookingRepository.existsOverlappingBooking(eq(1L), any(), any())).thenReturn(true);

        assertThrows(ResourceConflictException.class, () ->
                bookingService.createBookingRequest(dto));
    }

    @Test
    @DisplayName("Should throw exception when booking inactive resource")
    void createBookingRequest_InactiveResource() {
        Resource inactiveResource = Resource.builder()
                .id(4L).name("Broken Projector").type("Equipment")
                .capacity(1).location("Store").status("OUT_OF_SERVICE")
                .build();

        BookingRequestDto dto = new BookingRequestDto();
        dto.setResourceId(4L);
        dto.setUserId(1L);
        dto.setStartTime(LocalDateTime.of(2026, 12, 1, 10, 0));
        dto.setEndTime(LocalDateTime.of(2026, 12, 1, 12, 0));
        dto.setPurpose("Use projector");
        dto.setExpectedAttendees(1);

        when(resourceRepository.findById(4L)).thenReturn(Optional.of(inactiveResource));

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.createBookingRequest(dto));
    }

    @Test
    @DisplayName("Should approve a pending booking")
    void updateStatus_ApproveBooking() {
        BookingStatusUpdateDto statusDto = new BookingStatusUpdateDto();
        statusDto.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        BookingResponseDto result = bookingService.updateStatus(1L, statusDto);

        assertNotNull(result);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should reject a booking with mandatory reason")
    void updateStatus_RejectWithReason() {
        BookingStatusUpdateDto statusDto = new BookingStatusUpdateDto();
        statusDto.setStatus(BookingStatus.REJECTED);
        statusDto.setReason("Room under maintenance");

        Booking rejectedBooking = Booking.builder()
                .id(1L).resource(testResource).user(testUser)
                .startTime(testBooking.getStartTime()).endTime(testBooking.getEndTime())
                .purpose("Test").expectedAttendees(5)
                .status(BookingStatus.REJECTED).rejectionReason("Room under maintenance")
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(rejectedBooking);

        BookingResponseDto result = bookingService.updateStatus(1L, statusDto);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should throw exception when rejecting without reason")
    void updateStatus_RejectWithoutReason_ThrowsException() {
        BookingStatusUpdateDto statusDto = new BookingStatusUpdateDto();
        statusDto.setStatus(BookingStatus.REJECTED);
        statusDto.setReason("");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.updateStatus(1L, statusDto));
    }

    @Test
    @DisplayName("Should cancel an existing booking")
    void cancelBooking_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        bookingService.cancelBooking(1L);

        assertEquals(BookingStatus.CANCELLED, testBooking.getStatus());
        verify(bookingRepository).save(testBooking);
    }

    @Test
    @DisplayName("Should throw exception when cancelling already cancelled booking")
    void cancelBooking_AlreadyCancelled() {
        testBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.cancelBooking(1L));
    }

    @Test
    @DisplayName("Should throw exception when booking ID not found for cancellation")
    void cancelBooking_NotFound() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                bookingService.cancelBooking(999L));
    }
}

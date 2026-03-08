package com.campusops.booking.controller;

import com.campusops.booking.dto.BookingRequestDto;
import com.campusops.booking.dto.BookingResponseDto;
import com.campusops.booking.dto.BookingStatusUpdateDto;
import com.campusops.booking.model.BookingStatus;
import com.campusops.booking.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    private ObjectMapper objectMapper;
    private BookingResponseDto sampleResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sampleResponse = BookingResponseDto.builder()
                .id(1L).resourceId(1L).resourceName("Lecture Hall A")
                .userId(1L).userName("Standard User")
                .startTime(LocalDateTime.of(2026, 10, 10, 10, 0))
                .endTime(LocalDateTime.of(2026, 10, 10, 12, 0))
                .purpose("IT3030 Lab").expectedAttendees(30)
                .status(BookingStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("GET /api/bookings/all - should return 200 with all bookings")
    void getAllBookings_Returns200() throws Exception {
        List<BookingResponseDto> bookings = Arrays.asList(sampleResponse);
        when(bookingService.getAllBookings()).thenReturn(bookings);

        mockMvc.perform(get("/api/bookings/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resourceName").value("Lecture Hall A"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/bookings/user/{id} - should return user bookings")
    void getUserBookings_Returns200() throws Exception {
        when(bookingService.getUserBookings(1L)).thenReturn(Arrays.asList(sampleResponse));

        mockMvc.perform(get("/api/bookings/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userName").value("Standard User"));
    }

    @Test
    @DisplayName("POST /api/bookings - should return 201 Created")
    void createBooking_Returns201() throws Exception {
        BookingRequestDto request = new BookingRequestDto();
        request.setResourceId(1L);
        request.setUserId(1L);
        request.setStartTime(LocalDateTime.of(2026, 12, 1, 10, 0));
        request.setEndTime(LocalDateTime.of(2026, 12, 1, 12, 0));
        request.setPurpose("Group Meeting");
        request.setExpectedAttendees(10);

        when(bookingService.createBookingRequest(any(BookingRequestDto.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resourceName").value("Lecture Hall A"));
    }

    @Test
    @DisplayName("POST /api/bookings - should return 400 for invalid request")
    void createBooking_Returns400_InvalidRequest() throws Exception {
        BookingRequestDto request = new BookingRequestDto();
        // Missing required fields

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/bookings/{id}/status - should approve booking")
    void updateBookingStatus_Returns200() throws Exception {
        BookingStatusUpdateDto statusUpdate = new BookingStatusUpdateDto();
        statusUpdate.setStatus(BookingStatus.APPROVED);

        BookingResponseDto approvedResponse = BookingResponseDto.builder()
                .id(1L).resourceId(1L).resourceName("Lecture Hall A")
                .userId(1L).userName("Standard User")
                .startTime(LocalDateTime.of(2026, 10, 10, 10, 0))
                .endTime(LocalDateTime.of(2026, 10, 10, 12, 0))
                .purpose("IT3030 Lab").expectedAttendees(30)
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.updateStatus(eq(1L), any(BookingStatusUpdateDto.class))).thenReturn(approvedResponse);

        mockMvc.perform(patch("/api/bookings/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("DELETE /api/bookings/{id} - should return 204 No Content")
    void cancelBooking_Returns204() throws Exception {
        doNothing().when(bookingService).cancelBooking(1L);

        mockMvc.perform(delete("/api/bookings/1"))
                .andExpect(status().isNoContent());
    }
}

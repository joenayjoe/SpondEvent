package com.junaid.spond.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.junaid.spond.dtos.EventResponse;
import com.junaid.spond.dtos.NewEventRequest;
import com.junaid.spond.services.EventService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventController.class)
class EventControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private EventService eventService;

  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreateEvent() throws Exception {
    var newEventRequest =
        NewEventRequest.builder()
            .name("Sample Event")
            .description("Sample Event Description")
            .latitude(59.91)
            .longitude(10.75)
            .startDateTime(Instant.parse("2025-05-10T10:00:00Z"))
            .endDateTime(Instant.parse("2025-05-10T12:00:00Z"))
            .build();

    var eventResponse =
        EventResponse.builder()
            .id(1L)
            .name("Sample Event")
            .description("Sample Event Description")
            .latitude(59.91)
            .longitude(10.75)
            .startDateTime(Instant.parse("2025-05-10T10:00:00Z"))
            .endDateTime(Instant.parse("2025-05-10T12:00:00Z"))
            .airTemperature(32.0)
            .windSpeed(3.0)
            .build();

    when(eventService.saveEvent(any(NewEventRequest.class))).thenReturn(eventResponse);

    mockMvc
        .perform(
            post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEventRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("Sample Event"))
        .andExpect(jsonPath("$.description").value("Sample Event Description"))
        .andExpect(jsonPath("$.latitude").value(59.91))
        .andExpect(jsonPath("$.longitude").value(10.75))
        .andExpect(jsonPath("$.airTemperature").value(32.0))
        .andExpect(jsonPath("$.windSpeed").value(3.0));

    verify(eventService, times(1)).saveEvent(any(NewEventRequest.class));
  }

  @Test
  void testCreateEvent_MissingNameField() throws Exception {

    var newEventRequest =
        NewEventRequest.builder()
            .description("Sample Event Description")
            .latitude(59.91)
            .longitude(10.75)
            .startDateTime(Instant.parse("2025-05-10T10:00:00Z"))
            .endDateTime(Instant.parse("2025-05-10T12:00:00Z"))
            .build();

    mockMvc
        .perform(
            post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEventRequest)))
        .andExpect(status().isBadRequest()) // Expect HTTP 400 Bad Request
        .andExpect(jsonPath("$.errors").exists())
        .andExpect(jsonPath("$.errors.name[0].code").value("NotBlank"))
        .andExpect(jsonPath("$.errors.name[0].message").value("Name is required"));
  }

  @Test
  void testGetEventById() throws Exception {
    Long eventId = 1L;
    var eventResponse =
        EventResponse.builder()
            .id(1L)
            .name("Sample Event")
            .description("Sample Event Description")
            .latitude(59.91)
            .longitude(10.75)
            .startDateTime(Instant.parse("2025-05-10T10:00:00Z"))
            .endDateTime(Instant.parse("2025-05-10T12:00:00Z"))
            .airTemperature(32.0)
            .windSpeed(3.0)
            .build();

    when(eventService.getEventById(eventId)).thenReturn(eventResponse);

    mockMvc
        .perform(get("/api/events/{id}", eventId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("Sample Event"))
        .andExpect(jsonPath("$.description").value("Sample Event Description"))
        .andExpect(jsonPath("$.latitude").value(59.91))
        .andExpect(jsonPath("$.longitude").value(10.75))
        .andExpect(jsonPath("$.airTemperature").value(32.0))
        .andExpect(jsonPath("$.windSpeed").value(3.0));

    verify(eventService, times(1)).getEventById(eventId);
  }
}

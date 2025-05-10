package com.junaid.spond.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Cache;
import com.junaid.spond.dtos.EventResponse;
import com.junaid.spond.dtos.NewEventRequest;
import com.junaid.spond.exceptions.ResourceNotFoundException;
import com.junaid.spond.mappers.EventMapper;
import com.junaid.spond.models.Event;
import com.junaid.spond.models.ForecastData;
import com.junaid.spond.repositories.EventRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

class EventServiceTest {

  @Mock private EventRepository eventRepository;
  @Mock private Cache<String, Event> forecastCache;
  @Mock private ForecastService forecastService;

  @InjectMocks private EventService eventService;

  private Event unExpiredEvent;
  private Event expiredEvent;
  private EventResponse eventResponse;

  @BeforeEach
  void setUp() {
    var startDateTime = Instant.now().plusSeconds(3600); // 1 hour in the future
    var endDateTime = Instant.now().plusSeconds(7200); // 2 hours in the future
    unExpiredEvent =
        Event.builder()
            .id(1L)
            .name("Sample Event")
            .description("Sample Event Description")
            .latitude(59.91)
            .longitude(10.75)
            .startDateTime(startDateTime)
            .endDateTime(endDateTime)
            .forecastExpiresAt(Instant.now().plusSeconds(100))
            .build();
    expiredEvent =
        Event.builder()
            .id(1L)
            .name("Sample Event")
            .description("Sample Event Description")
            .latitude(59.91)
            .longitude(10.75)
            .startDateTime(startDateTime)
            .endDateTime(endDateTime)
            .forecastExpiresAt(Instant.now().minusSeconds(100))
            .build();
    eventResponse =
        EventResponse.builder()
            .id(1L)
            .name("Sample Event")
            .description("Sample Event Description")
            .latitude(59.91)
            .longitude(10.75)
            .startDateTime(startDateTime)
            .endDateTime(endDateTime)
            .airTemperature(32.0)
            .windSpeed(3.0)
            .build();
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testSaveEvent() {
    var startDateTime = Instant.now().plusSeconds(3600); // 1 hour in the future
    var endDateTime = Instant.now().plusSeconds(7200); // 2 hours in the future
    var newEventRequest =
        NewEventRequest.builder()
            .name("Sample Event")
            .description("Sample Event Description")
            .latitude(59.91)
            .longitude(10.75)
            .startDateTime(startDateTime)
            .endDateTime(endDateTime)
            .build();

    var savedEvent =
        Event.builder()
            .id(1L)
            .name("Sample Event")
            .description("Sample Event Description")
            .latitude(59.91)
            .longitude(10.75)
            .startDateTime(startDateTime)
            .endDateTime(endDateTime)
            .build();
    try (MockedStatic<EventMapper> mockedMapper = mockStatic(EventMapper.class)) {
      mockedMapper.when(() -> EventMapper.toEntity(newEventRequest)).thenReturn(unExpiredEvent);
      when(eventRepository.save(unExpiredEvent)).thenReturn(savedEvent);
      mockedMapper.when(() -> EventMapper.toEventResponse(savedEvent)).thenReturn(eventResponse);

      // Act
      EventResponse response = eventService.saveEvent(newEventRequest);

      // Assert
      assertNotNull(response);
      verify(eventRepository, times(1)).save(unExpiredEvent);
    }
  }

  @Test
  void testGetEventById_EventExistsInCache() {
    // Arrange
    Long eventId = 1L;
    Event cachedEvent =
        Event.builder().id(eventId).forecastExpiresAt(Instant.now().plusSeconds(100)).build();
    when(forecastCache.getIfPresent(eventId.toString())).thenReturn(cachedEvent);
    try (MockedStatic<EventMapper> mockedMapper = mockStatic(EventMapper.class)) {
      mockedMapper.when(() -> EventMapper.toEventResponse(cachedEvent)).thenReturn(eventResponse);

      // Act
      EventResponse response = eventService.getEventById(eventId);

      // Assert
      assertNotNull(response);
      verify(forecastCache, times(1)).getIfPresent(eventId.toString());
      verify(eventRepository, never()).findById(eventId);
    }
  }

  @Test
  void testGetEventById_EventNotInCacheAndExistInDatabase() {
    // Arrange
    Long eventId = 1L;

    when(forecastCache.getIfPresent(eventId.toString())).thenReturn(null);
    when(eventRepository.findById(eventId)).thenReturn(Optional.of(unExpiredEvent));
    try (MockedStatic<EventMapper> mockedMapper = mockStatic(EventMapper.class)) {
      mockedMapper
          .when(() -> EventMapper.toEventResponse(unExpiredEvent))
          .thenReturn(eventResponse);

      // Act
      EventResponse response = eventService.getEventById(eventId);

      // Assert
      assertNotNull(response);
      verify(forecastCache, times(1)).getIfPresent(eventId.toString());
      verify(eventRepository, times(1)).findById(eventId);
    }
  }

  @Test
  void testGetEventById_EventNotFound() {
    // Arrange
    Long eventId = 1L;
    when(forecastCache.getIfPresent(eventId.toString())).thenReturn(null);
    when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(ResourceNotFoundException.class, () -> eventService.getEventById(eventId));
    assertNotNull(exception);
    assertEquals("Event not found", exception.getMessage());
    verify(forecastCache, times(1)).getIfPresent(eventId.toString());
    verify(eventRepository, times(1)).findById(eventId);
  }

  @Test
  void testGetEventById_FetchForecastData() {
    // Arrange
    Long eventId = 1L;
    ForecastData forecastData =
        new ForecastData(32.0, 3.0, Instant.now().plus(Duration.ofHours(6)), Instant.now());

    when(forecastCache.getIfPresent(eventId.toString())).thenReturn(null);
    when(eventRepository.findById(eventId)).thenReturn(Optional.of(expiredEvent));
    when(eventRepository.save(any())).thenReturn(unExpiredEvent);
    when(forecastService.getForecastData(expiredEvent)).thenReturn(forecastData);

    // Act
    EventResponse response = eventService.getEventById(eventId);

    // Assert
    assertNotNull(response);
    verify(forecastService, times(1)).getForecastData(expiredEvent);
    verify(eventRepository, times(1)).save(expiredEvent);
    verify(forecastCache, times(1)).put(eventId.toString(), unExpiredEvent);
  }
}

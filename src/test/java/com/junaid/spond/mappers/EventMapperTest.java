package com.junaid.spond.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.junaid.spond.dtos.NewEventRequest;
import com.junaid.spond.models.Event;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

public class EventMapperTest {

  @Test
  public void testMapToEvent() {
    var newEventRequest =
        NewEventRequest.builder()
            .name("Sample Event")
            .description("Sample Event Description")
            .latitude(59.91)
            .longitude(10.75)
            .startDateTime(Instant.parse("2025-05-10T10:00:00Z"))
            .endDateTime(Instant.parse("2025-05-10T12:00:00Z"))
            .build();
    var event = EventMapper.toEntity(newEventRequest);
    assertEquals(newEventRequest.getName(), event.getName());
  }

  @Test
  public void testMapToEventResponse() {
    var event =
        Event.builder()
            .id(1L)
            .name("Sample Event")
            .description("Sample Event Description")
            .latitude(59.91)
            .longitude(10.75)
            .startDateTime(Instant.parse("2025-05-10T10:00:00Z"))
            .endDateTime(Instant.parse("2025-05-10T12:00:00Z"))
            .build();
    var eventResponse = EventMapper.toEventResponse(event);
    assertEquals(event.getId(), eventResponse.getId());
  }

  @Test
  public void testMapToEventResponseList() {
    var event1 =
        Event.builder()
            .id(1L)
            .name("Sample Event 1")
            .description("Sample Event Description 1")
            .latitude(59.91)
            .longitude(10.75)
            .startDateTime(Instant.parse("2025-05-10T10:00:00Z"))
            .endDateTime(Instant.parse("2025-05-10T12:00:00Z"))
            .build();
    var event2 =
        Event.builder()
            .id(2L)
            .name("Sample Event 2")
            .description("Sample Event Description 2")
            .latitude(59.92)
            .longitude(10.76)
            .startDateTime(Instant.parse("2025-05-11T10:00:00Z"))
            .endDateTime(Instant.parse("2025-05-11T12:00:00Z"))
            .build();
    var events = List.of(event1, event2);
    var eventResponses = EventMapper.toEventResponseList(events);
    assertEquals(events.size(), eventResponses.size());
    assertEquals(event1.getName(), eventResponses.get(0).getName());
    assertEquals(event2.getName(), eventResponses.get(1).getName());
  }
}

package com.junaid.spond.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.junaid.spond.dtos.NewEventRequest;
import com.junaid.spond.models.Event;
import java.time.Instant;
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
}

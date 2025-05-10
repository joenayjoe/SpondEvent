package com.junaid.spond.mappers;

import com.junaid.spond.dtos.EventResponse;
import com.junaid.spond.dtos.NewEventRequest;
import com.junaid.spond.models.Event;

public class EventMapper {
  public static Event toEntity(NewEventRequest newEventRequest) {
    return Event.builder()
        .name(newEventRequest.getName())
        .description(newEventRequest.getDescription())
        .latitude(Double.parseDouble(String.format("%.4f", newEventRequest.getLatitude())))
        .longitude(Double.parseDouble(String.format("%.4f", newEventRequest.getLongitude())))
        .startDateTime(newEventRequest.getStartDateTime())
        .endDateTime(newEventRequest.getEndDateTime())
        .build();
  }

  public static EventResponse toEventResponse(Event event) {
    return EventResponse.builder()
        .id(event.getId())
        .name(event.getName())
        .description(event.getDescription())
        .latitude(event.getLatitude())
        .longitude(event.getLongitude())
        .startDateTime(event.getStartDateTime())
        .endDateTime(event.getEndDateTime())
        .airTemperature(event.getAirTemperature())
        .windSpeed(event.getWindSpeed())
        .build();
  }
}

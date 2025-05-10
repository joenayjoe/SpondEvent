package com.junaid.spond.dtos;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EventResponse {
  private Long id;
  private String name;
  private String description;
  private Double latitude;
  private Double longitude;
  private Instant startDateTime;
  private Instant endDateTime;
  private Double airTemperature;
  private Double windSpeed;
}

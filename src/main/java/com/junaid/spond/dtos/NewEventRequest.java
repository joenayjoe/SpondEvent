package com.junaid.spond.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NewEventRequest {
  @NotBlank(message = "Name is required")
  private String name;

  private String description;
  private Double longitude;
  private Double latitude;

  @NotNull(message = "Start date and time is required")
  private Instant startDateTime;

  @NotNull(message = "End date and time is required")
  private Instant endDateTime;
}

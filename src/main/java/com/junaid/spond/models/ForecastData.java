package com.junaid.spond.models;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForecastData {
  private Double airTemperature;
  private Double windSpeed;
  private Instant forecastExpiresAt;
  private Instant forecastLastModifiedAt;
}

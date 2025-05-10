package com.junaid.spond.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String description;
  private Double latitude;
  private Double longitude;

  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant startDateTime;

  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant endDateTime;

  private Double airTemperature;
  private Double windSpeed;

  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant forecastExpiresAt;

  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant forecastLastModifiedAt;
}

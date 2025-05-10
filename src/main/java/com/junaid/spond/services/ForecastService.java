package com.junaid.spond.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.junaid.spond.models.Event;
import com.junaid.spond.models.ForecastData;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ForecastService {
  @Autowired private RestTemplate restTemplate;

  @Autowired private ObjectMapper objectMapper;

  private static final String MET_API_URL =
      "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat={lat}&lon={lon}";
  private static final String USER_AGENT = "SpondEventApp/1.0 (contact: joenayjoe@gmail.com)";

  public ForecastData getForecastData(Event event) {
    // set headers
    HttpHeaders headers = new HttpHeaders();
    headers.set("User-Agent", USER_AGENT);
    headers.set("Accept", "application/json");

    // add if mofified since header if forecast last modified at is not null
    if (event.getForecastLastModifiedAt() != null) {
      DateTimeFormatter formatter =
          DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.US).withZone(ZoneId.of("GMT"));

      headers.set("If-Modified-Since", formatter.format(event.getForecastLastModifiedAt()));
    }

    var entity = new HttpEntity<>(headers);

    try {
      // make the api call
      var response =
          restTemplate.exchange(
              MET_API_URL,
              HttpMethod.GET,
              entity,
              String.class,
              event.getLatitude(),
              event.getLongitude());

      if (response == null || response.getStatusCode() == HttpStatus.NOT_MODIFIED) {
        log.info("Weather data not modified since last fetch");
        return ForecastData.builder()
            .airTemperature(event.getAirTemperature())
            .windSpeed(event.getWindSpeed())
            .forecastExpiresAt(event.getForecastExpiresAt())
            .forecastLastModifiedAt(event.getForecastLastModifiedAt())
            .build();
      }
      if (response.getStatusCode().is2xxSuccessful()) {
        log.info("Weather data fetched successfully");
        return parseWeatherData(event, response);
      }

      return ForecastData.builder()
          .airTemperature(event.getAirTemperature())
          .windSpeed(event.getWindSpeed())
          .forecastExpiresAt(event.getForecastExpiresAt())
          .forecastLastModifiedAt(event.getForecastLastModifiedAt())
          .build();

    } catch (Exception e) {
      log.error("Error while fetching Weather Forecast", e);
      throw new RuntimeException("Error while fetching Weather Forecast", e);
    }
  }

  private ForecastData parseWeatherData(Event event, ResponseEntity<String> response) {

    try {
      var formatter =
          DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.US).withZone(ZoneId.of("GMT"));

      var lastModified = response.getHeaders().getFirst("Last-Modified");
      var expires = response.getHeaders().getFirst("Expires");

      var rootNode = objectMapper.readTree(response.getBody());
      var timeSeries = rootNode.path("properties").path("timeseries");
      var eventStartTime = event.getStartDateTime();
      var closestForecast = findClosestForecast(timeSeries, eventStartTime);
      var details = closestForecast.path("data").path("instant").path("details");
      var airTemperature = details.path("air_temperature").asDouble();
      var windSpeed = details.path("wind_speed").asDouble();

      return ForecastData.builder()
          .airTemperature(airTemperature)
          .windSpeed(windSpeed)
          .forecastExpiresAt(Instant.from(formatter.parse(expires)))
          .forecastLastModifiedAt(Instant.from(formatter.parse(lastModified)))
          .build();
    } catch (Exception e) {
      log.error("Error while parsing Weather Forecast", e);
      return ForecastData.builder()
          .airTemperature(event.getAirTemperature())
          .windSpeed(event.getWindSpeed())
          .forecastExpiresAt(event.getForecastExpiresAt())
          .forecastLastModifiedAt(event.getForecastLastModifiedAt())
          .build();
    }
  }

  private JsonNode findClosestForecast(JsonNode timeSeries, Instant eventStartTime) {
    JsonNode closestNode = null;
    var timeDiference = Long.MAX_VALUE;
    for (var node : timeSeries) {
      var time = node.path("time").asText();
      var forecastTime = Instant.parse(time);
      var difference = Math.abs(Duration.between(eventStartTime, forecastTime).toSeconds());
      if (difference < timeDiference) {
        timeDiference = difference;
        closestNode = node;
      }
    }
    return closestNode;
  }
}

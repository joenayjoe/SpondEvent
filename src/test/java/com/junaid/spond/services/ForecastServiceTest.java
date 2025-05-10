package com.junaid.spond.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.junaid.spond.models.Event;
import com.junaid.spond.models.ForecastData;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class ForecastServiceTest {

  @Mock private RestTemplate restTemplate;
  @Mock private ObjectMapper objectMapper;

  @InjectMocks private ForecastService forecastService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetForecastData_SuccessfulResponse() throws Exception {
    // Arrange
    Event event = new Event();
    event.setLatitude(59.91);
    event.setLongitude(10.75);
    event.setStartDateTime(Instant.parse("2025-05-10T10:00:00Z"));

    String responseBody = "{ \"properties\": { \"timeseries\": [] } }";
    ResponseEntity<String> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class),
            anyDouble(),
            anyDouble()))
        .thenReturn(response);

    when(objectMapper.readTree(responseBody)).thenReturn(mock(JsonNode.class));

    // Act
    ForecastData forecastData = forecastService.getForecastData(event);

    // Assert
    assertNotNull(forecastData);
    verify(restTemplate, times(1))
        .exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class),
            anyDouble(),
            anyDouble());
  }

  @Test
  void testGetForecastData_NotModifiedResponse() {
    // Arrange
    Event event = new Event();
    event.setAirTemperature(25.0);
    event.setWindSpeed(5.0);
    event.setForecastExpiresAt(Instant.parse("2025-05-10T12:00:00Z"));
    event.setForecastLastModifiedAt(Instant.parse("2025-05-10T08:00:00Z"));
    event.setLatitude(59.91);
    event.setLongitude(10.75);

    ResponseEntity<String> response = new ResponseEntity<>(HttpStatus.NOT_MODIFIED);

    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class),
            anyDouble(),
            anyDouble()))
        .thenReturn(response);

    // Act
    ForecastData forecastData = forecastService.getForecastData(event);

    // Assert
    assertNotNull(forecastData);
    assertEquals(25.0, forecastData.getAirTemperature());
    assertEquals(5.0, forecastData.getWindSpeed());
    verify(restTemplate, times(1))
        .exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class),
            anyDouble(),
            anyDouble());
  }

  @Test
  void testGetForecastData_ErrorResponse() {
    // Arrange
    Event event = new Event();
    event.setLatitude(59.91);
    event.setLongitude(10.75);

    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class),
            anyDouble(),
            anyDouble()))
        .thenThrow(new RuntimeException("API error"));

    // Act & Assert
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> forecastService.getForecastData(event));
    assertEquals("Error while fetching Weather Forecast", exception.getMessage());
  }

  @Test
  void testParseWeatherData_Successful() throws Exception {
    // Arrange
    Event event = new Event();
    event.setLatitude(59.91);
    event.setLongitude(10.75);
    event.setStartDateTime(Instant.parse("2025-05-10T10:00:00Z"));

    String responseBody = "{ \"properties\": { \"timeseries\": [] } }";
    ResponseEntity<String> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

    JsonNode mockRootNode = mock(JsonNode.class);
    when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class),
            anyDouble(),
            anyDouble()))
        .thenReturn(response);
    when(objectMapper.readTree(anyString())).thenReturn(mockRootNode);

    // Act
    ForecastData forecastData = forecastService.getForecastData(event);

    // Assert
    assertNotNull(forecastData);
    verify(objectMapper, times(1)).readTree(responseBody);
  }
}

package com.junaid.spond.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.junaid.spond.dtos.EventResponse;
import com.junaid.spond.dtos.NewEventRequest;
import com.junaid.spond.dtos.PageableResponse;
import com.junaid.spond.exceptions.ResourceNotFoundException;
import com.junaid.spond.mappers.EventMapper;
import com.junaid.spond.models.Event;
import com.junaid.spond.repositories.EventRepository;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventService {
  @Autowired private EventRepository eventRepository;

  @Autowired private Cache<String, Event> forecastCache;

  @Autowired private ForecastService forecastService;

  public EventResponse saveEvent(NewEventRequest newEventRequest) {
    var event = EventMapper.toEntity(newEventRequest);
    event = eventRepository.save(event);
    return EventMapper.toEventResponse(event);
  }

  @Transactional
  public EventResponse getEventById(Long id) {
    // check if the event is in cache
    var cachedEvent = forecastCache.getIfPresent(id.toString());
    if (cachedEvent != null && !isForecastExpired(cachedEvent)) {
      log.info("Event found in cache with id: {}", id);
      return EventMapper.toEventResponse(cachedEvent);
    }

    // if not in cache, fetch from database
    var event =
        eventRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

    // add to cache if the forecast is not yet expired
    if (!isForecastExpired(event)) {
      log.info("Event adding to cache with id: {} as forecast is not yet expired", id);
      forecastCache.put(id.toString(), event);
      return EventMapper.toEventResponse(event);
    }

    // if the event is in next 7 days and forecast is expired, fetch the forecast data
    if (isEventInNext7Days(event) && isForecastExpired(event)) {
      log.info("Fetching forecast and caching the event with id : {}", id);
      var forecastData = forecastService.getForecastData(event);

      // update the event with forecast data
      event.setAirTemperature(forecastData.getAirTemperature());
      event.setWindSpeed(forecastData.getWindSpeed());
      event.setForecastExpiresAt(forecastData.getForecastExpiresAt());
      event.setForecastLastModifiedAt(forecastData.getForecastLastModifiedAt());
      event = eventRepository.save(event);

      // add to the cache
      forecastCache.put(event.getId().toString(), event);
      return EventMapper.toEventResponse(event);
    }

    log.info("Event is not within 7 days. Doesn't need foreacst data for event with id : {} ", id);
    return EventMapper.toEventResponse(event);
  }

  public PageableResponse<EventResponse> getEvents(int page, int size) {
    var pageable = PageRequest.of(page, size);
    var eventPage = eventRepository.findAll(pageable);
    var pageableResponse =
        new PageableResponse<>(
            EventMapper.toEventResponseList(eventPage.getContent()),
            eventPage.getNumber(),
            eventPage.getTotalPages(),
            eventPage.getTotalElements());
    return pageableResponse;
  }

  private boolean isEventInNext7Days(Event event) {
    var now = Instant.now();
    var sevenDaysFromNow = now.plus(Duration.ofDays(7));
    return event.getStartDateTime().isBefore(sevenDaysFromNow)
        && event.getStartDateTime().isAfter(now);
  }

  private boolean isForecastExpired(Event event) {
    var now = Instant.now();
    if (event.getForecastExpiresAt() == null) {
      // forecast not fetched yet. need to fetch it
      return true;
    }
    if (now.isAfter(event.getForecastExpiresAt())) {
      log.info("Forecast expired for event id: {}. Invalidating the cache.", event.getId());
      forecastCache.invalidate(event.getId().toString());
      return true;
    }
    return false;
  }
}

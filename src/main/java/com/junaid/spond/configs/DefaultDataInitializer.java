package com.junaid.spond.configs;

import com.junaid.spond.models.Event;
import com.junaid.spond.repositories.EventRepository;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DefaultDataInitializer implements CommandLineRunner {
  @Autowired private EventRepository eventRepository;

  @Override
  public void run(String... args) throws Exception {
    if (eventRepository.count() == 0) {
      var now = Instant.now();
      // event 1
      var event1 =
          Event.builder()
              .name("Sampple Event 1")
              .description("Sample Event 1 Description")
              .latitude(59.91)
              .longitude(10.75)
              .startDateTime(now.minus(Duration.ofDays(1)))
              .endDateTime(now.minus(Duration.ofDays(1).plusHours(2)))
              .build();
      eventRepository.save(event1);

      // event 2
      var event2 =
          Event.builder()
              .name("Sampple Event 2")
              .description("Sample Event 2 Description")
              .latitude(59.73)
              .longitude(10.90)
              .startDateTime(now.plus(Duration.ofDays(2)))
              .endDateTime(now.plus(Duration.ofDays(2).plusHours(2)))
              .build();
      eventRepository.save(event2);
      // event 3
      var event3 =
          Event.builder()
              .name("Sampple Event 3")
              .description("Sample Event 3 Description")
              .latitude(58.97)
              .longitude(5.73)
              .startDateTime(now.plus(Duration.ofDays(3)))
              .endDateTime(now.plus(Duration.ofDays(3).plusHours(2)))
              .build();
      eventRepository.save(event3);
      // event 4
      var event4 =
          Event.builder()
              .name("Sampple Event 4")
              .description("Sample Event 4 Description")
              .latitude(58.97)
              .longitude(5.73)
              .startDateTime(now.plus(Duration.ofDays(8)))
              .endDateTime(now.plus(Duration.ofDays(8).plusHours(2)))
              .build();
      eventRepository.save(event4);
    }
  }
}

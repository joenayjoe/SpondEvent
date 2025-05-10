package com.junaid.spond.repositories;

import com.junaid.spond.models.Event;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

  @Query("SELECT e.id FROM Event e WHERE e.forecastExpiresAt < :now")
  List<Long> findIdsWithExpiredForecasts(Instant now);
}

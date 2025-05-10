package com.junaid.spond.controllers;

import com.junaid.spond.dtos.EventResponse;
import com.junaid.spond.dtos.NewEventRequest;
import com.junaid.spond.dtos.PageableResponse;
import com.junaid.spond.services.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {
  @Autowired private EventService eventService;

  @PostMapping()
  public ResponseEntity<EventResponse> createEvent(
      @RequestBody @Valid NewEventRequest eventRequest) {
    var eventResponse = eventService.saveEvent(eventRequest);
    return new ResponseEntity<>(eventResponse, HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
    var eventResponse = eventService.getEventById(id);
    return ResponseEntity.ok(eventResponse);
  }

  @GetMapping()
  public ResponseEntity<PageableResponse<EventResponse>> getEvents(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(eventService.getEvents(page, size));
  }
}

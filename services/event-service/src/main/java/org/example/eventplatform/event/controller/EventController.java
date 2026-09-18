package org.example.eventplatform.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventplatform.event.dto.EventRequest;
import org.example.eventplatform.event.dto.EventResponse;
import org.example.eventplatform.event.service.EventService;
import org.example.eventplatform.shared.security.JwtPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public, sàn-facing entry point: a show request can come in from the
 * marketplace (an unauthenticated customer flow) or a tenant admin creating
 * one for themselves — see EventService#createEvent for how the two differ.
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest request,
                                                       @AuthenticationPrincipal JwtPrincipal principal) {
        return new ResponseEntity<>(eventService.createEvent(request, principal), HttpStatus.CREATED);
    }
}

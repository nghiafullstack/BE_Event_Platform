package org.example.eventplatform.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventplatform.event.dto.CustomerBookingRequest;
import org.example.eventplatform.event.dto.EventResponse;
import org.example.eventplatform.event.service.EventService;
import org.example.eventplatform.shared.security.JwtPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerBookingController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> create(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody CustomerBookingRequest request) {
        return ResponseEntity.ok(eventService.createCustomerBooking(request, principal));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> myBookings(@AuthenticationPrincipal JwtPrincipal principal) {
        return ResponseEntity.ok(eventService.listCustomerBookings(principal.userId()));
    }
}

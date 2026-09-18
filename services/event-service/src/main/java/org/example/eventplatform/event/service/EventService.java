package org.example.eventplatform.event.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.eventplatform.event.dto.*;
import org.example.eventplatform.event.entity.AssignStatus;
import org.example.eventplatform.event.entity.Event;
import org.example.eventplatform.event.entity.EventStatus;
import org.example.eventplatform.event.entity.UserEvent;
import org.example.eventplatform.event.repository.EventRepository;
import org.example.eventplatform.event.repository.UserEventRepository;
import org.example.eventplatform.shared.security.JwtPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventService {

    private static final DateTimeFormatter HOUR_MINUTE = DateTimeFormatter.ofPattern("HH:mm");

    private final EventRepository eventRepository;
    private final UserEventRepository userEventRepository;
    private final NotificationPublisher notificationPublisher;

    @Transactional
    public EventResponse createEvent(EventRequest request, JwtPrincipal principal) {
        Event event = Event.builder()
                .name(request.getName())
                .type(request.getType())
                .eventDate(request.getEventDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .customerId(request.getCustomerId())
                .totalAmount(request.getTotalAmount())
                .description(request.getDescription())
                .concentrateTime(request.getConcentrateTime())
                .concentrateLocation(request.getConcentrateLocation())
                .status(EventStatus.SCHEDULED)
                .build();

        boolean isTenantAdmin = principal != null && principal.authorities().contains("ROLE_ADMIN");
        if (isTenantAdmin) {
            event.setTenantId(principal.tenantId());
            event.setPlatformFee(BigDecimal.ZERO);
            event.setCreatedBy(principal.username());
        } else {
            event.setTenantId(request.getTenantId());
            event.setPlatformFee(calculateDefaultFee(request.getTotalAmount()));
            event.setCreatedBy(principal != null ? principal.username() : "GUEST");
        }

        return toResponse(eventRepository.save(event));
    }

    private BigDecimal calculateDefaultFee(BigDecimal totalAmount) {
        return totalAmount != null ? totalAmount.multiply(new BigDecimal("0.1")) : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public EventResponse getEventDetail(Long id) {
        return toResponse(getEventOrThrow(id));
    }

    @Transactional(readOnly = true)
    public EventWithMembersResponse getEventDetailWithMembers(Long id) {
        Event event = getEventOrThrow(id);
        List<AssignmentResponse> members = userEventRepository.findByEventId(id).stream()
                .map(this::toAssignmentResponse)
                .toList();
        return EventWithMembersResponse.builder().eventInfo(toResponse(event)).members(members).build();
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> getTenantEvents(Long tenantId, Pageable pageable) {
        return eventRepository.findByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> getTenantSchedule(Long tenantId, int month, int year, Pageable pageable) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return eventRepository.findByTenantIdAndEventDateBetween(tenantId, start, end, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MonthlySummaryResponse getTenantMonthlySummary(Long tenantId, int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<Event> events = eventRepository.findByTenantIdAndEventDateBetween(tenantId, start, end);

        BigDecimal revenue = events.stream()
                .filter(e -> e.getStatus() != EventStatus.CANCELLED)
                .map(Event::getTotalAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completed = events.stream().filter(e -> e.getStatus() == EventStatus.COMPLETED).count();
        double rate = events.isEmpty() ? 0 : (double) completed / events.size() * 100;

        return MonthlySummaryResponse.builder()
                .totalEvents(events.size())
                .estimatedRevenue(revenue)
                .completionRate(rate)
                .build();
    }

    @Transactional
    public void acceptEvent(Long eventId, Long tenantId) {
        Event event = getEventOrThrow(eventId);
        assertOwnership(event, tenantId);
        event.setStatus(EventStatus.CONFIRMED);
        eventRepository.save(event);
    }

    @Transactional
    public void rejectEvent(Long eventId, Long tenantId) {
        Event event = getEventOrThrow(eventId);
        assertOwnership(event, tenantId);
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    @Transactional
    public void assignMembers(Long eventId, Long tenantId, List<AssignMemberRequest> requests) {
        Event event = getEventOrThrow(eventId);
        assertOwnership(event, tenantId);

        for (AssignMemberRequest req : requests) {
            UserEvent userEvent = userEventRepository.findByEventIdAndUserId(eventId, req.getUserId())
                    .orElse(new UserEvent());
            userEvent.setEvent(event);
            userEvent.setUserId(req.getUserId());
            userEvent.setPosition(req.getPosition());
            userEvent.setStatus(AssignStatus.PENDING);
            userEventRepository.save(userEvent);

            notificationPublisher.publish(
                    "MEMBER_ASSIGNED",
                    req.getUserId(),
                    tenantId,
                    "Bạn được gán vào show mới",
                    "Show \"" + event.getName() + "\" — vị trí " + req.getPosition(),
                    Map.of("eventId", String.valueOf(eventId), "position", String.valueOf(req.getPosition()))
            );
        }

        if (event.getStatus() == EventStatus.SCHEDULED) {
            event.setStatus(EventStatus.CONFIRMED);
            eventRepository.save(event);
        }
    }

    @Transactional
    public void respondToAssignment(Long userEventId, JwtPrincipal principal, AssignStatus status, String note) {
        UserEvent ue = getAssignmentOrThrow(userEventId);
        assertSelfOrAdmin(ue, principal);

        ue.setStatus(status);
        ue.setNote(note);
        ue.setRespondedAt(LocalDateTime.now());
        userEventRepository.save(ue);

        String type = status == AssignStatus.REJECTED ? "MEMBER_REJECTED" : "MEMBER_ACCEPTED";
        notificationPublisher.publish(
                type,
                null,
                ue.getEvent().getTenantId(),
                status == AssignStatus.REJECTED ? "Thành viên đã từ chối show" : "Thành viên đã nhận show",
                "User #" + ue.getUserId() + " đã phản hồi show \"" + ue.getEvent().getName() + "\"",
                Map.of("userEventId", String.valueOf(userEventId), "note", note == null ? "" : note)
        );
    }

    @Transactional
    public String concentrateCheckIn(Long userEventId, JwtPrincipal principal) {
        UserEvent ue = getAssignmentOrThrow(userEventId);
        assertSelfOrAdmin(ue, principal);

        LocalTime now = LocalTime.now();
        ue.setActualConcentrateAt(now);
        ue.setStatus(AssignStatus.CHECKIN_CONCENTRATE);
        userEventRepository.save(ue);

        LocalTime scheduledTime = ue.getEvent().getConcentrateTime();
        if (scheduledTime == null) {
            return "Xác nhận có mặt thành công lúc " + now.format(HOUR_MINUTE);
        }
        if (!now.isAfter(scheduledTime)) {
            return "Xác nhận: có mặt ĐÚNG GIỜ (" + now.format(HOUR_MINUTE) + ").";
        }
        long minutesLate = java.time.Duration.between(scheduledTime, now).toMinutes();
        return "Xác nhận: có mặt MUỘN " + minutesLate + " phút (giờ quy định " + scheduledTime.format(HOUR_MINUTE) + ").";
    }

    @Transactional
    public void checkIn(Long userEventId, JwtPrincipal principal, String location) {
        UserEvent ue = getAssignmentOrThrow(userEventId);
        assertSelfOrAdmin(ue, principal);

        if (ue.getStatus() != AssignStatus.ACCEPTED && ue.getStatus() != AssignStatus.CHECKIN_CONCENTRATE) {
            throw new IllegalStateException("Bạn phải xác nhận tham gia hoặc check-in tập trung trước khi check-in điểm diễn");
        }

        ue.setCheckinAt(LocalTime.now());
        ue.setCheckinLocation(location);
        ue.setStatus(AssignStatus.CHECKED_IN);
        userEventRepository.save(ue);

        Event event = ue.getEvent();
        if (event.getStatus() == EventStatus.CONFIRMED) {
            event.setStatus(EventStatus.IN_PROGRESS);
            eventRepository.save(event);
        }
    }

    @Transactional
    public String checkOut(Long userEventId, JwtPrincipal principal) {
        UserEvent ue = getAssignmentOrThrow(userEventId);
        assertSelfOrAdmin(ue, principal);

        if (ue.getStatus() != AssignStatus.CHECKED_IN) {
            throw new IllegalStateException("Bạn chưa check-in điểm diễn");
        }

        ue.setCheckoutAt(LocalTime.now());
        ue.setStatus(AssignStatus.COMPLETED);
        userEventRepository.save(ue);

        autoCompleteEventIfFinished(ue.getEvent().getId());
        return "Đã hoàn thành show diễn!";
    }

    private void autoCompleteEventIfFinished(Long eventId) {
        List<UserEvent> members = userEventRepository.findByEventId(eventId);
        boolean finished = members.stream()
                .filter(m -> m.getStatus() != AssignStatus.REJECTED)
                .allMatch(m -> m.getStatus() == AssignStatus.COMPLETED);

        if (finished) {
            Event event = getEventOrThrow(eventId);
            event.setStatus(EventStatus.COMPLETED);
            eventRepository.save(event);

            notificationPublisher.publish(
                    "EVENT_AUTO_COMPLETED",
                    null,
                    event.getTenantId(),
                    "Show đã hoàn thành",
                    "Show \"" + event.getName() + "\" đã hoàn thành!",
                    Map.of("eventId", String.valueOf(eventId))
            );
        }
    }

    @Transactional
    public EventResponse updateConcentrateInfo(Long eventId, Long tenantId, UpdateConcentrateRequest request) {
        Event event = getEventOrThrow(eventId);
        assertOwnership(event, tenantId);

        event.setConcentrateTime(request.getConcentrateTime());
        event.setConcentrateLocation(request.getConcentrateLocation());
        Event saved = eventRepository.save(event);

        userEventRepository.findByEventId(eventId).stream()
                .filter(m -> m.getStatus() == AssignStatus.ACCEPTED || m.getStatus() == AssignStatus.PENDING)
                .forEach(m -> notificationPublisher.publish(
                        "CONCENTRATE_INFO_UPDATED",
                        m.getUserId(),
                        tenantId,
                        "Cập nhật giờ tập trung",
                        "Show \"" + saved.getName() + "\" — tập trung lúc " + request.getConcentrateTime()
                                + " tại " + request.getConcentrateLocation(),
                        Map.of("eventId", String.valueOf(eventId))
                ));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getMyAssignedEvents(Long userId) {
        return userEventRepository.findByUserId(userId).stream()
                .map(this::toAssignmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DashboardStatResponse getMemberDashboardStats(Long tenantId, Long userId) {
        long finished = userEventRepository.countFinishedShows(tenantId, userId);
        long pending = userEventRepository.countPendingShows(tenantId, userId);
        BigDecimal earnings = userEventRepository.sumTotalEarnings(tenantId, userId);
        return new DashboardStatResponse(finished, pending, earnings != null ? earnings : BigDecimal.ZERO, rank(finished));
    }

    private String rank(long finished) {
        if (finished > 50) return "Kim Cương";
        if (finished > 20) return "Bạch Kim";
        return "NEWBIE";
    }

    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sự kiện với ID: " + id));
    }

    private UserEvent getAssignmentOrThrow(Long id) {
        return userEventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bản ghi phân công"));
    }

    private void assertOwnership(Event event, Long tenantId) {
        if (event.getTenantId() == null || !event.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("Show này không thuộc đơn vị của bạn");
        }
    }

    private void assertSelfOrAdmin(UserEvent ue, JwtPrincipal principal) {
        boolean isAdmin = principal.authorities().contains("ROLE_ADMIN") || principal.authorities().contains("ROLE_SUPER_ADMIN");
        if (!isAdmin && !ue.getUserId().equals(principal.userId())) {
            throw new AccessDeniedException("Bạn không có quyền thao tác trên bản ghi phân công này");
        }
    }

    private EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .type(event.getType())
                .typeDisplayName(event.getType() != null ? event.getType().getDisplayName() : null)
                .status(event.getStatus())
                .statusDisplayName(formatEventStatus(event.getStatus()))
                .eventDate(event.getEventDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .customerId(event.getCustomerId())
                .tenantId(event.getTenantId())
                .concentrateTime(event.getConcentrateTime())
                .concentrateLocation(event.getConcentrateLocation())
                .totalAmount(event.getTotalAmount())
                .platformFee(event.getPlatformFee())
                .createdAt(event.getCreatedAt())
                .build();
    }

    private AssignmentResponse toAssignmentResponse(UserEvent ue) {
        Event event = ue.getEvent();
        List<AssignmentResponse.Teammate> teammates = event.getAssignedMembers().stream()
                .filter(m -> !m.getUserId().equals(ue.getUserId()))
                .map(m -> AssignmentResponse.Teammate.builder()
                        .userId(m.getUserId())
                        .position(m.getPosition())
                        .status(formatAssignStatus(m.getStatus()))
                        .build())
                .toList();

        return AssignmentResponse.builder()
                .id(ue.getId())
                .eventId(event.getId())
                .eventName(event.getName())
                .eventDate(event.getEventDate())
                .location(event.getLocation())
                .userId(ue.getUserId())
                .position(ue.getPosition())
                .status(formatAssignStatus(ue.getStatus()))
                .note(ue.getNote())
                .actualConcentrateAt(ue.getActualConcentrateAt())
                .checkinAt(ue.getCheckinAt())
                .checkoutAt(ue.getCheckoutAt())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .concentrateTime(event.getConcentrateTime())
                .concentrateLocation(event.getConcentrateLocation())
                .teammates(teammates)
                .build();
    }

    private String formatEventStatus(EventStatus status) {
        if (status == null) return "";
        return switch (status) {
            case SCHEDULED -> "Chờ duyệt";
            case CONFIRMED -> "Đã chốt show";
            case IN_PROGRESS -> "Đang diễn";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
        };
    }

    private String formatAssignStatus(AssignStatus status) {
        if (status == null) return "Chờ xác nhận";
        return switch (status) {
            case PENDING -> "Đang mời";
            case ACCEPTED -> "Sẵn sàng";
            case REJECTED -> "Từ chối";
            case COMPLETED -> "Đã diễn xong";
            case CHECKED_IN -> "Đã checkin diễn";
            case CHECKED_OUT -> "Đã checkout diễn";
            case CHECKIN_CONCENTRATE -> "Đã checkin tập trung";
        };
    }
}

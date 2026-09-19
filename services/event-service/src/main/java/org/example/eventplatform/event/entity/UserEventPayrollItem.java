package org.example.eventplatform.event.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * One line of a member's payout for a show (base wage, bonus, allowance,
 * lucky money...) — kept as free-form line items instead of fixed columns
 * since the set of bonuses/allowances varies show to show.
 */
@Entity
@Table(name = "user_event_payroll_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEventPayrollItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_event_id")
    private UserEvent userEvent;

    private String label;
    private BigDecimal amount;
}

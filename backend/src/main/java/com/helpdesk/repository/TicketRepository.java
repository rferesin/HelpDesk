package com.helpdesk.repository;

import com.helpdesk.domain.enums.TicketStatus;
import com.helpdesk.domain.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    List<Ticket> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<Ticket> findByAssigneeIdOrderByCreatedAtDesc(Long assigneeId);

    long countByStatus(TicketStatus status);

    long countByClientId(Long clientId);

    long countByAssigneeId(Long assigneeId);
}

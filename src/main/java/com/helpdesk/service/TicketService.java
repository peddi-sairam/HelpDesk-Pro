package com.helpdesk.service;

import com.helpdesk.entity.Ticket;
import com.helpdesk.entity.TicketComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TicketService {
    Ticket createTicket(Ticket ticket, Long customerId);
    Ticket updateTicket(Long id, Ticket ticketDetails);
    Ticket assignTicket(Long ticketId, Long agentId);
    Ticket transferTicket(Long ticketId, Long departmentId);
    Ticket resolveTicket(Long ticketId, String resolutionComment, Long userId);
    Ticket closeTicket(Long ticketId, Long userId);
    Ticket reopenTicket(Long ticketId, Long userId);
    TicketComment addComment(Long ticketId, Long userId, String commentText, boolean internalNote);
    List<TicketComment> getComments(Long ticketId, Long userId);
    Ticket getTicketById(Long id);
    Page<Ticket> getTickets(Long userId, String search, String status, String priority, Pageable pageable);
}

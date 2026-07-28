package com.helpdesk.controller;

import com.helpdesk.dto.ApiResponse;
import com.helpdesk.entity.Ticket;
import com.helpdesk.entity.TicketComment;
import com.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping
    public ResponseEntity<ApiResponse<Ticket>> createTicket(
            @RequestParam Long customerId,
            @Valid @RequestBody Ticket ticket) {
        Ticket created = ticketService.createTicket(ticket, customerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ticket created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Ticket>> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody Ticket ticketDetails) {
        Ticket updated = ticketService.updateTicket(id, ticketDetails);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ticket updated successfully", updated));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<Ticket>> assignTicket(@PathVariable Long id, @RequestParam Long agentId) {
        Ticket updated = ticketService.assignTicket(id, agentId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ticket assigned successfully", updated));
    }

    @PatchMapping("/{id}/transfer")
    public ResponseEntity<ApiResponse<Ticket>> transferTicket(@PathVariable Long id, @RequestParam Long departmentId) {
        Ticket updated = ticketService.transferTicket(id, departmentId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ticket transferred successfully", updated));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<Ticket>> resolveTicket(
            @PathVariable Long id,
            @RequestParam String resolutionComment,
            @RequestParam Long userId) {
        Ticket updated = ticketService.resolveTicket(id, resolutionComment, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ticket resolved successfully", updated));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<ApiResponse<Ticket>> closeTicket(@PathVariable Long id, @RequestParam Long userId) {
        Ticket updated = ticketService.closeTicket(id, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ticket closed successfully", updated));
    }

    @PatchMapping("/{id}/reopen")
    public ResponseEntity<ApiResponse<Ticket>> reopenTicket(@PathVariable Long id, @RequestParam Long userId) {
        Ticket updated = ticketService.reopenTicket(id, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ticket reopened successfully", updated));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<TicketComment>> addComment(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam String commentText,
            @RequestParam(defaultValue = "false") boolean internalNote) {
        TicketComment created = ticketService.addComment(id, userId, commentText, internalNote);
        return ResponseEntity.ok(new ApiResponse<>(true, "Comment added successfully", created));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<List<TicketComment>>> getComments(@PathVariable Long id, @RequestParam Long userId) {
        List<TicketComment> comments = ticketService.getComments(id, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Comments fetched successfully", comments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Ticket>> getTicketById(@PathVariable Long id) {
        Ticket ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ticket fetched successfully", ticket));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Ticket>>> getTickets(
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(required = false, defaultValue = "") String priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Ticket> tickets = ticketService.getTickets(userId, search, status, priority, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tickets fetched successfully", tickets));
    }
}

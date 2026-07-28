package com.helpdesk.service.impl;

import com.helpdesk.entity.*;
import com.helpdesk.exception.BadRequestException;
import com.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.repository.DepartmentRepository;
import com.helpdesk.repository.TicketCommentRepository;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    @Autowired
    private com.helpdesk.service.NotificationService notificationService;

    @Override
    public Ticket createTicket(Ticket ticket, Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + customerId));

        ticket.setCustomer(customer);
        ticket.setCompany(customer.getCompany());
        ticket.setStatus(TicketStatus.OPEN);
        Ticket saved = ticketRepository.save(ticket);
        try {
            notificationService.sendNotification(customer.getId(), "Your ticket #" + saved.getId() + " - '" + saved.getTitle() + "' has been submitted successfully.");
        } catch (Exception e) {
            System.err.println("Notification failed: " + e.getMessage());
        }
        return saved;
    }

    @Override
    public Ticket updateTicket(Long id, Ticket ticketDetails) {
        Ticket ticket = getTicketById(id);
        ticket.setTitle(ticketDetails.getTitle());
        ticket.setDescription(ticketDetails.getDescription());
        ticket.setPriority(ticketDetails.getPriority());
        ticket.setCategory(ticketDetails.getCategory());
        return ticketRepository.save(ticket);
    }

    @Override
    public Ticket assignTicket(Long ticketId, Long agentId) {
        Ticket ticket = getTicketById(ticketId);
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id " + agentId));

        // Allow assigning to Agents or Admins within the same company
        if (!agent.getCompany().getId().equals(ticket.getCompany().getId())) {
            throw new BadRequestException("Agent must belong to the same company as the ticket");
        }

        ticket.setAgent(agent);
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.ASSIGNED);
        }
        Ticket saved = ticketRepository.save(ticket);
        try {
            if (saved.getCustomer() != null) {
                notificationService.sendNotification(saved.getCustomer().getId(), "Your ticket #" + saved.getId() + " - '" + saved.getTitle() + "' has been assigned to support staff member " + agent.getFullName() + ".");
            }
            notificationService.sendNotification(agent.getId(), "Ticket #" + saved.getId() + " - '" + saved.getTitle() + "' has been assigned to you.");
        } catch (Exception e) {
            System.err.println("Notification failed: " + e.getMessage());
        }
        return saved;
    }

    @Override
    public Ticket transferTicket(Long ticketId, Long departmentId) {
        Ticket ticket = getTicketById(ticketId);
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id " + departmentId));

        if (!department.getCompany().getId().equals(ticket.getCompany().getId())) {
            throw new BadRequestException("Department must belong to the same company as the ticket");
        }

        ticket.setDepartment(department);
        // Clear assignee on department transfers so agents in that department can claim it
        ticket.setAgent(null);
        ticket.setStatus(TicketStatus.OPEN);
        Ticket saved = ticketRepository.save(ticket);
        try {
            if (saved.getCustomer() != null) {
                notificationService.sendNotification(saved.getCustomer().getId(), "Your ticket #" + saved.getId() + " - '" + saved.getTitle() + "' has been transferred to the " + department.getName() + " department.");
            }
        } catch (Exception e) {
            System.err.println("Notification failed: " + e.getMessage());
        }
        return saved;
    }

    @Override
    public Ticket resolveTicket(Long ticketId, String resolutionComment, Long userId) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());
        
        String commentText = "Ticket resolved. Resolution Details: " + 
                             (resolutionComment != null ? resolutionComment : "No additional details provided.");
        addComment(ticketId, userId, commentText, false);

        Ticket saved = ticketRepository.save(ticket);
        try {
            if (saved.getCustomer() != null) {
                notificationService.sendNotification(saved.getCustomer().getId(), "Your ticket #" + saved.getId() + " - '" + saved.getTitle() + "' has been resolved. Resolution: " + resolutionComment);
            }
        } catch (Exception e) {
            System.err.println("Notification failed: " + e.getMessage());
        }
        return saved;
    }

    @Override
    public Ticket closeTicket(Long ticketId, Long userId) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());

        addComment(ticketId, userId, "Ticket closed.", false);
        Ticket saved = ticketRepository.save(ticket);
        try {
            if (saved.getCustomer() != null) {
                notificationService.sendNotification(saved.getCustomer().getId(), "Ticket #" + saved.getId() + " - '" + saved.getTitle() + "' has been closed.");
            }
            if (saved.getAgent() != null) {
                notificationService.sendNotification(saved.getAgent().getId(), "Ticket #" + saved.getId() + " - '" + saved.getTitle() + "' has been closed by user.");
            }
        } catch (Exception e) {
            System.err.println("Notification failed: " + e.getMessage());
        }
        return saved;
    }

    @Override
    public Ticket reopenTicket(Long ticketId, Long userId) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setStatus(ticket.getAgent() != null ? TicketStatus.ASSIGNED : TicketStatus.OPEN);
        ticket.setResolvedAt(null);
        ticket.setClosedAt(null);

        addComment(ticketId, userId, "Ticket reopened.", false);
        Ticket saved = ticketRepository.save(ticket);
        try {
            if (saved.getCustomer() != null) {
                notificationService.sendNotification(saved.getCustomer().getId(), "Ticket #" + saved.getId() + " - '" + saved.getTitle() + "' has been reopened.");
            }
            if (saved.getAgent() != null) {
                notificationService.sendNotification(saved.getAgent().getId(), "Ticket #" + saved.getId() + " - '" + saved.getTitle() + "' has been reopened.");
            }
        } catch (Exception e) {
            System.err.println("Notification failed: " + e.getMessage());
        }
        return saved;
    }

    @Override
    public TicketComment addComment(Long ticketId, Long userId, String commentText, boolean internalNote) {
        Ticket ticket = getTicketById(ticketId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .user(user)
                .commentText(commentText)
                .internalNote(internalNote)
                .build();

        TicketComment saved = ticketCommentRepository.save(comment);
        try {
            if (!internalNote) {
                // If comment author is agent/admin, notify customer
                if (user.getRole().getName() == RoleName.ROLE_SUPPORT_AGENT || user.getRole().getName() == RoleName.ROLE_COMPANY_ADMIN) {
                    if (ticket.getCustomer() != null) {
                        notificationService.sendNotification(ticket.getCustomer().getId(), "Support staff member " + user.getFullName() + " posted a comment on ticket #" + ticket.getId() + " - '" + ticket.getTitle() + "'.");
                    }
                } else if (user.getRole().getName() == RoleName.ROLE_CUSTOMER) {
                    // If comment author is customer, notify assigned agent
                    if (ticket.getAgent() != null) {
                        notificationService.sendNotification(ticket.getAgent().getId(), "Customer " + user.getFullName() + " posted a comment on ticket #" + ticket.getId() + " - '" + ticket.getTitle() + "'.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Notification failed: " + e.getMessage());
        }
        return saved;
    }

    @Override
    public List<TicketComment> getComments(Long ticketId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        // Customers can only see public comments; admins/agents can see all including internal notes
        if (user.getRole().getName() == RoleName.ROLE_CUSTOMER) {
            return ticketCommentRepository.findByTicketIdAndInternalNoteFalseOrderByCreatedAtAsc(ticketId);
        }
        return ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    @Override
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id " + id));
    }

    @Override
    public Page<Ticket> getTickets(Long userId, String search, String status, String priority, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        Specification<Ticket> spec = Specification.where(null);

        // 1. Role-based scoping
        if (user.getRole().getName() == RoleName.ROLE_CUSTOMER) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("customer").get("id"), userId));
        } else if (user.getRole().getName() == RoleName.ROLE_COMPANY_ADMIN || 
                   user.getRole().getName() == RoleName.ROLE_SUPPORT_AGENT) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("company").get("id"), user.getCompany().getId()));
        }

        // 2. Search query filtering
        if (search != null && !search.trim().isEmpty()) {
            final String searchPattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), searchPattern),
                cb.like(cb.lower(root.get("description")), searchPattern),
                cb.like(cb.lower(root.get("category")), searchPattern)
            ));
        }

        // 3. Status filter
        if (status != null && !status.trim().isEmpty()) {
            try {
                TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), ticketStatus));
            } catch (IllegalArgumentException e) {
                // ignore invalid enum
            }
        }

        // 4. Priority filter
        if (priority != null && !priority.trim().isEmpty()) {
            try {
                TicketPriority ticketPriority = TicketPriority.valueOf(priority.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), ticketPriority));
            } catch (IllegalArgumentException e) {
                // ignore invalid enum
            }
        }

        return ticketRepository.findAll(spec, pageable);
    }
}

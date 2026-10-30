package com.helpdesk.web.mapper;

import com.helpdesk.domain.model.Attachment;
import com.helpdesk.domain.model.Category;
import com.helpdesk.domain.model.Comment;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.TicketHistory;
import com.helpdesk.domain.model.User;
import com.helpdesk.web.dto.AuthDtos.UserResponse;
import com.helpdesk.web.dto.TicketDtos.AttachmentResponse;
import com.helpdesk.web.dto.TicketDtos.CategoryResponse;
import com.helpdesk.web.dto.TicketDtos.CommentResponse;
import com.helpdesk.web.dto.TicketDtos.HistoryResponse;
import com.helpdesk.web.dto.TicketDtos.TicketDetailResponse;
import com.helpdesk.web.dto.TicketDtos.TicketSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DtoMapper {

    public UserResponse toUser(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isActive());
    }

    public CategoryResponse toCategory(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription(), category.isActive());
    }

    public TicketSummaryResponse toSummary(Ticket ticket) {
        return new TicketSummaryResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCategory() != null ? ticket.getCategory().getName() : null,
                ticket.getClient() != null ? ticket.getClient().getName() : null,
                ticket.getAssignee() != null ? ticket.getAssignee().getName() : null,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    public TicketDetailResponse toDetail(Ticket ticket, List<Comment> comments, List<Attachment> attachments, List<TicketHistory> history) {
        return new TicketDetailResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCategory() != null ? ticket.getCategory().getId() : null,
                ticket.getCategory() != null ? ticket.getCategory().getName() : null,
                ticket.getClient() != null ? ticket.getClient().getId() : null,
                ticket.getClient() != null ? ticket.getClient().getName() : null,
                ticket.getAssignee() != null ? ticket.getAssignee().getId() : null,
                ticket.getAssignee() != null ? ticket.getAssignee().getName() : null,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getClosedAt(),
                comments.stream().map(this::toComment).toList(),
                attachments.stream().map(this::toAttachment).toList(),
                history.stream().map(this::toHistory).toList()
        );
    }

    public CommentResponse toComment(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getName(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

    public AttachmentResponse toAttachment(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getOriginalName(),
                attachment.getContentType(),
                attachment.getSize(),
                attachment.getCreatedAt()
        );
    }

    public HistoryResponse toHistory(TicketHistory history) {
        return new HistoryResponse(
                history.getId(),
                history.getChangedBy().getName(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getNote(),
                history.getCreatedAt()
        );
    }
}

package com.helpdesk.web.dto;

import com.helpdesk.domain.enums.TicketPriority;
import com.helpdesk.domain.enums.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class TicketDtos {

    private TicketDtos() {
    }

    public record CreateTicketRequest(
            @NotBlank @Size(min = 5, max = 160) String title,
            @NotBlank @Size(min = 10, max = 4000) String description,
            @NotNull Long categoryId,
            TicketPriority priority
    ) {
    }

    public record UpdateTicketRequest(
            String title,
            String description,
            Long categoryId,
            TicketPriority priority
    ) {
    }

    public record ChangeStatusRequest(
            @NotNull TicketStatus status,
            String note
    ) {
    }

    public record AssignRequest(
            Long assigneeId
    ) {
    }

    public record CommentRequest(
            @NotBlank @Size(min = 2, max = 2000) String content
    ) {
    }

    public record CategoryRequest(
            @NotBlank @Size(min = 2, max = 80) String name,
            @Size(max = 255) String description
    ) {
    }

    public record CategoryResponse(
            Long id,
            String name,
            String description,
            boolean active
    ) {
    }

    public record CommentResponse(
            Long id,
            Long authorId,
            String authorName,
            String content,
            LocalDateTime createdAt
    ) {
    }

    public record AttachmentResponse(
            Long id,
            String originalName,
            String contentType,
            long size,
            LocalDateTime createdAt
    ) {
    }

    public record HistoryResponse(
            Long id,
            String changedByName,
            TicketStatus fromStatus,
            TicketStatus toStatus,
            String note,
            LocalDateTime createdAt
    ) {
    }

    public record TicketSummaryResponse(
            Long id,
            String title,
            TicketStatus status,
            TicketPriority priority,
            String categoryName,
            String clientName,
            String assigneeName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record TicketDetailResponse(
            Long id,
            String title,
            String description,
            TicketStatus status,
            TicketPriority priority,
            Long categoryId,
            String categoryName,
            Long clientId,
            String clientName,
            Long assigneeId,
            String assigneeName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime closedAt,
            List<CommentResponse> comments,
            List<AttachmentResponse> attachments,
            List<HistoryResponse> history
    ) {
    }

    public record DashboardResponse(
            long total,
            long aberto,
            long emAtendimento,
            long resolvido,
            long fechado
    ) {
    }
}

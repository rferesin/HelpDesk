package com.helpdesk.web.controller;

import com.helpdesk.service.TicketService;
import com.helpdesk.web.dto.TicketDtos.AssignRequest;
import com.helpdesk.web.dto.TicketDtos.ChangeStatusRequest;
import com.helpdesk.web.dto.TicketDtos.CommentRequest;
import com.helpdesk.web.dto.TicketDtos.CreateTicketRequest;
import com.helpdesk.web.dto.TicketDtos.DashboardResponse;
import com.helpdesk.web.dto.TicketDtos.TicketDetailResponse;
import com.helpdesk.web.dto.TicketDtos.TicketSummaryResponse;
import com.helpdesk.web.dto.TicketDtos.UpdateTicketRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> dashboard(Authentication authentication) {
        return ResponseEntity.ok(ticketService.dashboard(authentication.getName()));
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<TicketSummaryResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(ticketService.list(authentication.getName()));
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<TicketDetailResponse> findById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ticketService.findById(id, authentication.getName()));
    }

    @PostMapping("/tickets")
    public ResponseEntity<TicketDetailResponse> create(@Valid @RequestBody CreateTicketRequest request,
                                                       Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(request, authentication.getName()));
    }

    @PutMapping("/tickets/{id}")
    public ResponseEntity<TicketDetailResponse> update(@PathVariable Long id,
                                                       @RequestBody UpdateTicketRequest request,
                                                       Authentication authentication) {
        return ResponseEntity.ok(ticketService.update(id, request, authentication.getName()));
    }

    @PostMapping("/tickets/{id}/assign")
    public ResponseEntity<TicketDetailResponse> assign(@PathVariable Long id,
                                                       @RequestBody(required = false) AssignRequest request,
                                                       Authentication authentication) {
        AssignRequest payload = request != null ? request : new AssignRequest(null);
        return ResponseEntity.ok(ticketService.assign(id, payload, authentication.getName()));
    }

    @PatchMapping("/tickets/{id}/status")
    public ResponseEntity<TicketDetailResponse> changeStatus(@PathVariable Long id,
                                                             @Valid @RequestBody ChangeStatusRequest request,
                                                             Authentication authentication) {
        return ResponseEntity.ok(ticketService.changeStatus(id, request, authentication.getName()));
    }

    @PostMapping("/tickets/{id}/comments")
    public ResponseEntity<TicketDetailResponse> comment(@PathVariable Long id,
                                                        @Valid @RequestBody CommentRequest request,
                                                        Authentication authentication) {
        return ResponseEntity.ok(ticketService.addComment(id, request, authentication.getName()));
    }

    @PostMapping(value = "/tickets/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketDetailResponse> attach(@PathVariable Long id,
                                                       @RequestPart("file") MultipartFile file,
                                                       Authentication authentication) {
        return ResponseEntity.ok(ticketService.addAttachment(id, file, authentication.getName()));
    }

    @GetMapping("/tickets/{id}/attachments/{attachmentId}")
    public ResponseEntity<Resource> download(@PathVariable Long id,
                                             @PathVariable Long attachmentId,
                                             Authentication authentication) {
        Resource resource = ticketService.loadAttachment(id, attachmentId, authentication.getName());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}

package com.helpdesk.service;

import com.helpdesk.domain.enums.Role;
import com.helpdesk.domain.enums.TicketPriority;
import com.helpdesk.domain.enums.TicketStatus;
import com.helpdesk.domain.exception.BusinessException;
import com.helpdesk.domain.exception.ResourceNotFoundException;
import com.helpdesk.domain.exception.UnauthorizedActionException;
import com.helpdesk.domain.model.Attachment;
import com.helpdesk.domain.model.Category;
import com.helpdesk.domain.model.Comment;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.TicketHistory;
import com.helpdesk.domain.model.User;
import com.helpdesk.repository.AttachmentRepository;
import com.helpdesk.repository.CommentRepository;
import com.helpdesk.repository.TicketHistoryRepository;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.web.dto.TicketDtos.AssignRequest;
import com.helpdesk.web.dto.TicketDtos.ChangeStatusRequest;
import com.helpdesk.web.dto.TicketDtos.CommentRequest;
import com.helpdesk.web.dto.TicketDtos.CreateTicketRequest;
import com.helpdesk.web.dto.TicketDtos.DashboardResponse;
import com.helpdesk.web.dto.TicketDtos.TicketDetailResponse;
import com.helpdesk.web.dto.TicketDtos.TicketSummaryResponse;
import com.helpdesk.web.dto.TicketDtos.UpdateTicketRequest;
import com.helpdesk.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class TicketService {

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS = Map.of(
            TicketStatus.ABERTO, EnumSet.of(TicketStatus.EM_ATENDIMENTO),
            TicketStatus.EM_ATENDIMENTO, EnumSet.of(TicketStatus.RESOLVIDO, TicketStatus.ABERTO),
            TicketStatus.RESOLVIDO, EnumSet.of(TicketStatus.FECHADO, TicketStatus.EM_ATENDIMENTO),
            TicketStatus.FECHADO, EnumSet.noneOf(TicketStatus.class)
    );

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final TicketHistoryRepository historyRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final DtoMapper mapper;
    private final Path uploadDir;

    public TicketService(TicketRepository ticketRepository,
                         CommentRepository commentRepository,
                         AttachmentRepository attachmentRepository,
                         TicketHistoryRepository historyRepository,
                         CategoryService categoryService,
                         UserService userService,
                         DtoMapper mapper,
                         @Value("${app.upload-dir:./uploads}") String uploadDir) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.attachmentRepository = attachmentRepository;
        this.historyRepository = historyRepository;
        this.categoryService = categoryService;
        this.userService = userService;
        this.mapper = mapper;
        this.uploadDir = Paths.get(uploadDir);
    }

    @Transactional(readOnly = true)
    public List<TicketSummaryResponse> list(String email) {
        User actor = userService.requireByEmail(email);
        List<Ticket> tickets = switch (actor.getRole()) {
            case CLIENTE -> ticketRepository.findByClientIdOrderByCreatedAtDesc(actor.getId());
            case ATENDENTE -> ticketRepository.findAll().stream()
                    .filter(ticket -> ticket.getAssignee() == null
                            || actor.getId().equals(ticket.getAssignee().getId()))
                    .toList();
            case ADMIN -> ticketRepository.findAll();
        };
        return tickets.stream().map(mapper::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public TicketDetailResponse findById(Long id, String email) {
        User actor = userService.requireByEmail(email);
        Ticket ticket = requireVisible(id, actor);
        return mapper.toDetail(
                ticket,
                commentRepository.findByTicketIdOrderByCreatedAtAsc(id),
                attachmentRepository.findByTicketIdOrderByCreatedAtAsc(id),
                historyRepository.findByTicketIdOrderByCreatedAtAsc(id)
        );
    }

    @Transactional
    public TicketDetailResponse create(CreateTicketRequest request, String email) {
        User actor = userService.requireByEmail(email);
        if (actor.getRole() != Role.CLIENTE && actor.getRole() != Role.ADMIN) {
            throw new UnauthorizedActionException("Apenas clientes podem abrir chamados");
        }
        Category category = categoryService.require(request.categoryId());
        if (!category.isActive()) {
            throw new BusinessException("Categoria inativa");
        }
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setCategory(category);
        ticket.setClient(actor);
        ticket.setPriority(request.priority() != null ? request.priority() : TicketPriority.MEDIA);
        ticket.setStatus(TicketStatus.ABERTO);
        Ticket saved = ticketRepository.save(ticket);
        registerHistory(saved, actor, null, TicketStatus.ABERTO, "Chamado aberto");
        return findById(saved.getId(), email);
    }

    @Transactional
    public TicketDetailResponse update(Long id, UpdateTicketRequest request, String email) {
        User actor = userService.requireByEmail(email);
        Ticket ticket = requireVisible(id, actor);
        if (ticket.getStatus() == TicketStatus.FECHADO) {
            throw new BusinessException("Chamado fechado nao pode ser editado");
        }
        if (actor.getRole() == Role.CLIENTE && !ticket.getClient().getId().equals(actor.getId())) {
            throw new UnauthorizedActionException("Sem permissao para editar este chamado");
        }
        if (request.title() != null && !request.title().isBlank()) {
            ticket.setTitle(request.title());
        }
        if (request.description() != null && !request.description().isBlank()) {
            ticket.setDescription(request.description());
        }
        if (request.categoryId() != null) {
            ticket.setCategory(categoryService.require(request.categoryId()));
        }
        if (request.priority() != null) {
            ticket.setPriority(request.priority());
        }
        ticketRepository.save(ticket);
        return findById(id, email);
    }

    @Transactional
    public TicketDetailResponse assign(Long id, AssignRequest request, String email) {
        User actor = userService.requireByEmail(email);
        if (actor.getRole() == Role.CLIENTE) {
            throw new UnauthorizedActionException("Cliente nao pode assumir chamados");
        }
        Ticket ticket = requireVisible(id, actor);
        User assignee = request.assigneeId() == null ? actor : userService.requireUser(request.assigneeId());
        if (assignee.getRole() == Role.CLIENTE) {
            throw new BusinessException("Cliente nao pode ser atendente do chamado");
        }
        ticket.setAssignee(assignee);
        if (ticket.getStatus() == TicketStatus.ABERTO) {
            TicketStatus from = ticket.getStatus();
            ticket.setStatus(TicketStatus.EM_ATENDIMENTO);
            registerHistory(ticket, actor, from, TicketStatus.EM_ATENDIMENTO, "Atendente assumiu o chamado");
        }
        ticketRepository.save(ticket);
        return findById(id, email);
    }

    @Transactional
    public TicketDetailResponse changeStatus(Long id, ChangeStatusRequest request, String email) {
        User actor = userService.requireByEmail(email);
        Ticket ticket = requireVisible(id, actor);
        TicketStatus current = ticket.getStatus();
        TicketStatus next = request.status();
        if (current == next) {
            return findById(id, email);
        }
        validateTransition(actor, ticket, current, next);
        ticket.setStatus(next);
        if (next == TicketStatus.FECHADO) {
            ticket.setClosedAt(LocalDateTime.now());
        }
        if (next == TicketStatus.EM_ATENDIMENTO && ticket.getAssignee() == null && actor.getRole() != Role.CLIENTE) {
            ticket.setAssignee(actor);
        }
        ticketRepository.save(ticket);
        registerHistory(ticket, actor, current, next, request.note());
        return findById(id, email);
    }

    @Transactional
    public TicketDetailResponse addComment(Long id, CommentRequest request, String email) {
        User actor = userService.requireByEmail(email);
        Ticket ticket = requireVisible(id, actor);
        if (ticket.getStatus() == TicketStatus.FECHADO) {
            throw new BusinessException("Nao e possivel comentar em chamado fechado");
        }
        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setAuthor(actor);
        comment.setContent(request.content());
        commentRepository.save(comment);
        return findById(id, email);
    }

    @Transactional
    public TicketDetailResponse addAttachment(Long id, MultipartFile file, String email) {
        User actor = userService.requireByEmail(email);
        Ticket ticket = requireVisible(id, actor);
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo obrigatorio");
        }
        try {
            Files.createDirectories(uploadDir);
            String storedName = UUID.randomUUID() + "_" + sanitize(file.getOriginalFilename());
            Path target = uploadDir.resolve(storedName);
            file.transferTo(target.toFile());
            Attachment attachment = new Attachment();
            attachment.setTicket(ticket);
            attachment.setUploadedBy(actor);
            attachment.setOriginalName(file.getOriginalFilename());
            attachment.setStoredName(storedName);
            attachment.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            attachment.setSize(file.getSize());
            attachmentRepository.save(attachment);
            return findById(id, email);
        } catch (IOException ex) {
            throw new BusinessException("Falha ao salvar anexo");
        }
    }

    public Resource loadAttachment(Long ticketId, Long attachmentId, String email) {
        User actor = userService.requireByEmail(email);
        requireVisible(ticketId, actor);
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Anexo nao encontrado"));
        if (!attachment.getTicket().getId().equals(ticketId)) {
            throw new ResourceNotFoundException("Anexo nao encontrado");
        }
        Path path = uploadDir.resolve(attachment.getStoredName());
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Arquivo nao encontrado");
        }
        return new FileSystemResource(path);
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard(String email) {
        User actor = userService.requireByEmail(email);
        List<Ticket> tickets = switch (actor.getRole()) {
            case CLIENTE -> ticketRepository.findByClientIdOrderByCreatedAtDesc(actor.getId());
            case ATENDENTE -> ticketRepository.findByAssigneeIdOrderByCreatedAtDesc(actor.getId());
            case ADMIN -> ticketRepository.findAll();
        };
        long aberto = tickets.stream().filter(t -> t.getStatus() == TicketStatus.ABERTO).count();
        long emAtendimento = tickets.stream().filter(t -> t.getStatus() == TicketStatus.EM_ATENDIMENTO).count();
        long resolvido = tickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVIDO).count();
        long fechado = tickets.stream().filter(t -> t.getStatus() == TicketStatus.FECHADO).count();
        return new DashboardResponse(tickets.size(), aberto, emAtendimento, resolvido, fechado);
    }

    private void validateTransition(User actor, Ticket ticket, TicketStatus current, TicketStatus next) {
        Set<TicketStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw new BusinessException("Transicao de " + current + " para " + next + " nao permitida");
        }
        if (actor.getRole() == Role.CLIENTE) {
            if (next != TicketStatus.FECHADO || current != TicketStatus.RESOLVIDO) {
                throw new UnauthorizedActionException("Cliente so pode fechar chamado resolvido");
            }
            if (!ticket.getClient().getId().equals(actor.getId())) {
                throw new UnauthorizedActionException("Sem permissao neste chamado");
            }
        }
        if (actor.getRole() == Role.ATENDENTE && ticket.getAssignee() != null
                && !ticket.getAssignee().getId().equals(actor.getId())
                && next != TicketStatus.EM_ATENDIMENTO) {
            throw new UnauthorizedActionException("Somente o atendente responsavel pode alterar este chamado");
        }
    }

    private Ticket requireVisible(Long id, User actor) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado nao encontrado"));
        if (actor.getRole() == Role.CLIENTE && !ticket.getClient().getId().equals(actor.getId())) {
            throw new UnauthorizedActionException("Sem permissao para visualizar este chamado");
        }
        return ticket;
    }

    private void registerHistory(Ticket ticket, User user, TicketStatus from, TicketStatus to, String note) {
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setChangedBy(user);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setNote(note);
        historyRepository.save(history);
    }

    private String sanitize(String original) {
        if (original == null || original.isBlank()) {
            return "file";
        }
        return original.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}

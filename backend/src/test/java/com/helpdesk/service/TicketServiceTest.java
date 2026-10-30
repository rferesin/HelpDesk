package com.helpdesk.service;

import com.helpdesk.domain.enums.Role;
import com.helpdesk.domain.enums.TicketPriority;
import com.helpdesk.domain.enums.TicketStatus;
import com.helpdesk.domain.exception.BusinessException;
import com.helpdesk.domain.exception.UnauthorizedActionException;
import com.helpdesk.domain.model.Category;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.User;
import com.helpdesk.repository.AttachmentRepository;
import com.helpdesk.repository.CommentRepository;
import com.helpdesk.repository.TicketHistoryRepository;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.web.dto.TicketDtos.AssignRequest;
import com.helpdesk.web.dto.TicketDtos.ChangeStatusRequest;
import com.helpdesk.web.dto.TicketDtos.CreateTicketRequest;
import com.helpdesk.web.mapper.DtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private AttachmentRepository attachmentRepository;
    @Mock private TicketHistoryRepository historyRepository;
    @Mock private CategoryService categoryService;
    @Mock private UserService userService;

    private TicketService ticketService;
    private User cliente;
    private User atendente;
    private User admin;
    private Category category;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService(
                ticketRepository,
                commentRepository,
                attachmentRepository,
                historyRepository,
                categoryService,
                userService,
                new DtoMapper(),
                "./uploads-test"
        );
        cliente = user(1L, "Carlos", "cliente@helpdesk.local", Role.CLIENTE);
        atendente = user(2L, "Ana", "atendente@helpdesk.local", Role.ATENDENTE);
        admin = user(3L, "Admin", "admin@helpdesk.local", Role.ADMIN);
        category = new Category();
        category.setId(10L);
        category.setName("Hardware");
        category.setActive(true);
    }

    @Test
    void createTicketStartsAsAberto() {
        when(userService.requireByEmail(cliente.getEmail())).thenReturn(cliente);
        when(categoryService.require(10L)).thenReturn(category);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(99L);
            return ticket;
        });
        stubDetailLoad(99L, ticket(99L, TicketStatus.ABERTO, cliente, null));

        ticketService.create(new CreateTicketRequest("Notebook nao liga", "Equipamento da recepcao sem boot", 10L, TicketPriority.ALTA), cliente.getEmail());

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        assertEquals(TicketStatus.ABERTO, captor.getValue().getStatus());
        assertEquals(cliente, captor.getValue().getClient());
        verify(historyRepository).save(any());
    }

    @Test
    void atendenteCannotOpenTicket() {
        when(userService.requireByEmail(atendente.getEmail())).thenReturn(atendente);

        assertThrows(UnauthorizedActionException.class, () ->
                ticketService.create(new CreateTicketRequest("Titulo valido", "Descricao valida do chamado", 10L, TicketPriority.MEDIA), atendente.getEmail()));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void assignMovesFromAbertoToEmAtendimento() {
        Ticket ticket = ticket(1L, TicketStatus.ABERTO, cliente, null);
        when(userService.requireByEmail(atendente.getEmail())).thenReturn(atendente);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        stubDetailLoad(1L, ticket);

        ticketService.assign(1L, new AssignRequest(null), atendente.getEmail());

        assertEquals(TicketStatus.EM_ATENDIMENTO, ticket.getStatus());
        assertEquals(atendente, ticket.getAssignee());
    }

    @Test
    void invalidTransitionIsRejected() {
        Ticket ticket = ticket(1L, TicketStatus.ABERTO, cliente, null);
        when(userService.requireByEmail(atendente.getEmail())).thenReturn(atendente);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(BusinessException.class, () ->
                ticketService.changeStatus(1L, new ChangeStatusRequest(TicketStatus.FECHADO, "skip"), atendente.getEmail()));
    }

    @Test
    void clienteCanCloseOnlyResolvedTicket() {
        Ticket ticket = ticket(1L, TicketStatus.RESOLVIDO, cliente, atendente);
        when(userService.requireByEmail(cliente.getEmail())).thenReturn(cliente);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        stubDetailLoad(1L, ticket);

        ticketService.changeStatus(1L, new ChangeStatusRequest(TicketStatus.FECHADO, "ok"), cliente.getEmail());

        assertEquals(TicketStatus.FECHADO, ticket.getStatus());
        assertNotNull(ticket.getClosedAt());
    }

    @Test
    void clienteCannotSeeOtherClientTicket() {
        User other = user(8L, "Outro", "outro@helpdesk.local", Role.CLIENTE);
        Ticket ticket = ticket(1L, TicketStatus.ABERTO, other, null);
        when(userService.requireByEmail(cliente.getEmail())).thenReturn(cliente);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(UnauthorizedActionException.class, () -> ticketService.findById(1L, cliente.getEmail()));
    }

    @Test
    void dashboardCountsByStatusForAdmin() {
        when(userService.requireByEmail(admin.getEmail())).thenReturn(admin);
        when(ticketRepository.findAll()).thenReturn(List.of(
                ticket(1L, TicketStatus.ABERTO, cliente, null),
                ticket(2L, TicketStatus.EM_ATENDIMENTO, cliente, atendente),
                ticket(3L, TicketStatus.RESOLVIDO, cliente, atendente),
                ticket(4L, TicketStatus.FECHADO, cliente, atendente)
        ));

        var dashboard = ticketService.dashboard(admin.getEmail());

        assertEquals(4, dashboard.total());
        assertEquals(1, dashboard.aberto());
        assertEquals(1, dashboard.emAtendimento());
        assertEquals(1, dashboard.resolvido());
        assertEquals(1, dashboard.fechado());
    }

    private void stubDetailLoad(Long id, Ticket ticket) {
        when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
        when(commentRepository.findByTicketIdOrderByCreatedAtAsc(id)).thenReturn(List.of());
        when(attachmentRepository.findByTicketIdOrderByCreatedAtAsc(id)).thenReturn(List.of());
        when(historyRepository.findByTicketIdOrderByCreatedAtAsc(id)).thenReturn(List.of());
    }

    private Ticket ticket(Long id, TicketStatus status, User owner, User assignee) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setTitle("Chamado " + id);
        ticket.setDescription("Descricao do chamado " + id);
        ticket.setStatus(status);
        ticket.setPriority(TicketPriority.MEDIA);
        ticket.setCategory(category);
        ticket.setClient(owner);
        ticket.setAssignee(assignee);
        return ticket;
    }

    private User user(Long id, String name, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setActive(true);
        return user;
    }
}

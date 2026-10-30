package com.helpdesk.config;

import com.helpdesk.domain.enums.Role;
import com.helpdesk.domain.enums.TicketPriority;
import com.helpdesk.domain.enums.TicketStatus;
import com.helpdesk.domain.model.Category;
import com.helpdesk.domain.model.Ticket;
import com.helpdesk.domain.model.TicketHistory;
import com.helpdesk.domain.model.User;
import com.helpdesk.repository.CategoryRepository;
import com.helpdesk.repository.TicketHistoryRepository;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      TicketRepository ticketRepository,
                      TicketHistoryRepository ticketHistoryRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.ticketRepository = ticketRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User admin = createUser("Administrador", "admin@helpdesk.local", "admin123", Role.ADMIN);
        User atendente = createUser("Ana Atendente", "atendente@helpdesk.local", "atendente123", Role.ATENDENTE);
        User cliente = createUser("Carlos Cliente", "cliente@helpdesk.local", "cliente123", Role.CLIENTE);
        createUser("Beatriz Cliente", "beatriz@helpdesk.local", "cliente123", Role.CLIENTE);

        Category hardware = createCategory("Hardware", "Problemas com equipamentos");
        Category software = createCategory("Software", "Falhas em aplicativos e sistemas");
        Category rede = createCategory("Rede", "Conectividade e VPN");
        createCategory("Acesso", "Permissoes e credenciais");

        Ticket aberto = createTicket("Notebook nao liga", "O equipamento da recepcao nao inicia apos o ultimo update.", hardware, cliente, null, TicketStatus.ABERTO, TicketPriority.ALTA);
        registerHistory(aberto, admin, null, TicketStatus.ABERTO, "Chamado aberto pelo cliente");

        Ticket emAtendimento = createTicket("VPN instavel", "Quedas frequentes na VPN corporativa no periodo da tarde.", rede, cliente, atendente, TicketStatus.EM_ATENDIMENTO, TicketPriority.URGENTE);
        registerHistory(emAtendimento, cliente, null, TicketStatus.ABERTO, "Chamado aberto pelo cliente");
        registerHistory(emAtendimento, atendente, TicketStatus.ABERTO, TicketStatus.EM_ATENDIMENTO, "Atendente assumiu o chamado");

        Ticket resolvido = createTicket("Erro ao abrir ERP", "Mensagem de licenca invalida ao iniciar o ERP.", software, cliente, atendente, TicketStatus.RESOLVIDO, TicketPriority.MEDIA);
        registerHistory(resolvido, cliente, null, TicketStatus.ABERTO, "Chamado aberto pelo cliente");
        registerHistory(resolvido, atendente, TicketStatus.ABERTO, TicketStatus.EM_ATENDIMENTO, "Atendente assumiu o chamado");
        registerHistory(resolvido, atendente, TicketStatus.EM_ATENDIMENTO, TicketStatus.RESOLVIDO, "Licenca renovada");
    }

    private User createUser(String name, String email, String password, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user);
    }

    private Category createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setActive(true);
        return categoryRepository.save(category);
    }

    private Ticket createTicket(String title, String description, Category category, User client, User assignee, TicketStatus status, TicketPriority priority) {
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setCategory(category);
        ticket.setClient(client);
        ticket.setAssignee(assignee);
        ticket.setStatus(status);
        ticket.setPriority(priority);
        return ticketRepository.save(ticket);
    }

    private void registerHistory(Ticket ticket, User user, TicketStatus from, TicketStatus to, String note) {
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setChangedBy(user);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setNote(note);
        ticketHistoryRepository.save(history);
    }
}

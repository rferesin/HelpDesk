import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { TicketService } from '../../core/ticket.service';
import { PRIORITY_LABEL, STATUS_LABEL, TicketStatus, TicketSummary } from '../../core/models';

@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './ticket-list.component.html'
})
export class TicketListComponent implements OnInit {
  tickets: TicketSummary[] = [];
  filtered: TicketSummary[] = [];
  statusFilter = '';
  search = '';
  readonly statusLabel = STATUS_LABEL;
  readonly priorityLabel = PRIORITY_LABEL;

  constructor(private ticketService: TicketService, public auth: AuthService) {}

  ngOnInit(): void {
    this.ticketService.listTickets().subscribe(list => {
      this.tickets = list;
      this.apply();
    });
  }

  apply(): void {
    const term = this.search.toLowerCase();
    this.filtered = this.tickets.filter(ticket => {
      const statusOk = !this.statusFilter || ticket.status === this.statusFilter;
      const searchOk = !term
        || ticket.title.toLowerCase().includes(term)
        || ticket.clientName.toLowerCase().includes(term)
        || String(ticket.id).includes(term);
      return statusOk && searchOk;
    });
  }

  statusClass(status: TicketStatus): string {
    switch (status) {
      case 'ABERTO': return 'bg-warning text-dark';
      case 'EM_ATENDIMENTO': return 'bg-primary text-white';
      case 'RESOLVIDO': return 'bg-success text-white';
      default: return 'bg-secondary text-white';
    }
  }
}

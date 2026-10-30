import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { TicketService } from '../../core/ticket.service';
import { Dashboard, TicketSummary, STATUS_LABEL, PRIORITY_LABEL } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  dashboard: Dashboard | null = null;
  recent: TicketSummary[] = [];
  readonly statusLabel = STATUS_LABEL;
  readonly priorityLabel = PRIORITY_LABEL;

  constructor(private tickets: TicketService, public auth: AuthService) {}

  ngOnInit(): void {
    this.tickets.dashboard().subscribe(data => this.dashboard = data);
    this.tickets.listTickets().subscribe(list => this.recent = list.slice(0, 6));
  }

  statusClass(status: string): string {
    switch (status) {
      case 'ABERTO': return 'bg-warning text-dark';
      case 'EM_ATENDIMENTO': return 'bg-primary';
      case 'RESOLVIDO': return 'bg-success';
      default: return 'bg-secondary';
    }
  }
}

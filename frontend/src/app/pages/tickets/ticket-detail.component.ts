import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { AuthService } from '../../core/auth.service';
import { TicketService } from '../../core/ticket.service';
import { PRIORITY_LABEL, STATUS_LABEL, TicketDetail, TicketStatus } from '../../core/models';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [FormsModule, RouterLink, DatePipe],
  templateUrl: './ticket-detail.component.html'
})
export class TicketDetailComponent implements OnInit {
  ticket: TicketDetail | null = null;
  comment = '';
  note = '';
  error = '';
  readonly statusLabel = STATUS_LABEL;
  readonly priorityLabel = PRIORITY_LABEL;

  constructor(
    private route: ActivatedRoute,
    private ticketService: TicketService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.ticketService.getTicket(id).subscribe({
      next: ticket => this.ticket = ticket,
      error: () => this.error = 'Chamado nao encontrado ou sem permissao.'
    });
  }

  assign(): void {
    if (!this.ticket) {
      return;
    }
    this.ticketService.assign(this.ticket.id).subscribe(ticket => this.ticket = ticket);
  }

  changeStatus(status: TicketStatus): void {
    if (!this.ticket) {
      return;
    }
    this.ticketService.changeStatus(this.ticket.id, status, this.note || undefined).subscribe({
      next: ticket => {
        this.ticket = ticket;
        this.note = '';
      },
      error: err => this.error = err.error?.message || 'Transicao nao permitida.'
    });
  }

  addComment(): void {
    if (!this.ticket || !this.comment.trim()) {
      return;
    }
    this.ticketService.addComment(this.ticket.id, this.comment).subscribe({
      next: ticket => {
        this.ticket = ticket;
        this.comment = '';
      },
      error: err => this.error = err.error?.message || 'Nao foi possivel comentar.'
    });
  }

  upload(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file || !this.ticket) {
      return;
    }
    this.ticketService.uploadAttachment(this.ticket.id, file).subscribe({
      next: ticket => this.ticket = ticket,
      error: err => this.error = err.error?.message || 'Falha no anexo.'
    });
    input.value = '';
  }

  canAssign(): boolean {
    return !!this.ticket && this.auth.hasRole('ADMIN', 'ATENDENTE') && this.ticket.status === 'ABERTO';
  }

  canResolve(): boolean {
    return !!this.ticket && this.auth.hasRole('ADMIN', 'ATENDENTE') && this.ticket.status === 'EM_ATENDIMENTO';
  }

  canReopen(): boolean {
    return !!this.ticket && this.auth.hasRole('ADMIN', 'ATENDENTE') && this.ticket.status === 'RESOLVIDO';
  }

  canClose(): boolean {
    return !!this.ticket && this.ticket.status === 'RESOLVIDO'
      && (this.auth.hasRole('ADMIN') || this.auth.hasRole('CLIENTE'));
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

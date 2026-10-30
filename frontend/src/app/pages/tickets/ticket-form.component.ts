import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TicketService } from '../../core/ticket.service';
import { Category, TicketPriority } from '../../core/models';

@Component({
  selector: 'app-ticket-form',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './ticket-form.component.html'
})
export class TicketFormComponent implements OnInit {
  title = '';
  description = '';
  categoryId: number | null = null;
  priority: TicketPriority = 'MEDIA';
  categories: Category[] = [];
  error = '';
  loading = false;

  constructor(private ticketService: TicketService, private router: Router) {}

  ngOnInit(): void {
    this.ticketService.activeCategories().subscribe(list => {
      this.categories = list;
      if (list.length > 0) {
        this.categoryId = list[0].id;
      }
    });
  }

  submit(): void {
    if (!this.categoryId) {
      this.error = 'Selecione uma categoria.';
      return;
    }
    this.loading = true;
    this.error = '';
    this.ticketService.createTicket({
      title: this.title,
      description: this.description,
      categoryId: this.categoryId,
      priority: this.priority
    }).subscribe({
      next: ticket => this.router.navigate(['/tickets', ticket.id]),
      error: err => {
        this.error = err.error?.message || 'Nao foi possivel abrir o chamado.';
        this.loading = false;
      }
    });
  }
}

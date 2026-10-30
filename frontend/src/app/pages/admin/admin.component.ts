import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TicketService } from '../../core/ticket.service';
import { Category } from '../../core/models';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './admin.component.html'
})
export class AdminComponent implements OnInit {
  categories: Category[] = [];
  name = '';
  description = '';
  error = '';

  constructor(private ticketService: TicketService) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.ticketService.categories().subscribe(list => this.categories = list);
  }

  create(): void {
    this.error = '';
    this.ticketService.createCategory({ name: this.name, description: this.description }).subscribe({
      next: () => {
        this.name = '';
        this.description = '';
        this.reload();
      },
      error: err => this.error = err.error?.message || 'Nao foi possivel criar a categoria.'
    });
  }

  toggle(category: Category): void {
    this.ticketService.toggleCategory(category.id).subscribe(() => this.reload());
  }
}

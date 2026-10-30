import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TicketService } from '../../core/ticket.service';
import { ROLE_LABEL, Role, User } from '../../core/models';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './users.component.html'
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  name = '';
  email = '';
  password = '';
  role: Role = 'CLIENTE';
  error = '';
  readonly roleLabel = ROLE_LABEL;

  constructor(private ticketService: TicketService) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.ticketService.users().subscribe(list => this.users = list);
  }

  create(): void {
    this.error = '';
    this.ticketService.createUser({
      name: this.name,
      email: this.email,
      password: this.password,
      role: this.role
    }).subscribe({
      next: () => {
        this.name = '';
        this.email = '';
        this.password = '';
        this.role = 'CLIENTE';
        this.reload();
      },
      error: err => this.error = err.error?.message || 'Nao foi possivel criar o usuario.'
    });
  }

  toggle(user: User): void {
    this.ticketService.toggleUser(user.id).subscribe(() => this.reload());
  }
}

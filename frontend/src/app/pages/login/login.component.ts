import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  email = 'admin@helpdesk.local';
  password = 'admin123';
  error = '';
  loading = false;

  constructor(private auth: AuthService, private router: Router) {}

  submit(): void {
    this.error = '';
    this.loading = true;
    this.auth.login(this.email, this.password).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => {
        this.error = 'Credenciais invalidas. Verifique e-mail e senha.';
        this.loading = false;
      }
    });
  }

  fill(role: 'admin' | 'atendente' | 'cliente'): void {
    if (role === 'admin') {
      this.email = 'admin@helpdesk.local';
      this.password = 'admin123';
    } else if (role === 'atendente') {
      this.email = 'atendente@helpdesk.local';
      this.password = 'atendente123';
    } else {
      this.email = 'cliente@helpdesk.local';
      this.password = 'cliente123';
    }
  }
}

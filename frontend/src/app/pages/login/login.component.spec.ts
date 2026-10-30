import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../core/auth.service';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let auth: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    auth = jasmine.createSpyObj('AuthService', ['login']);
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: auth }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(LoginComponent);
  });

  it('shows error on invalid credentials', () => {
    auth.login.and.returnValue(throwError(() => ({ status: 401 })));
    const component = fixture.componentInstance;
    component.submit();
    expect(component.error).toContain('Credenciais invalidas');
  });

  it('fills demo credentials', () => {
    const component = fixture.componentInstance;
    component.fill('cliente');
    expect(component.email).toBe('cliente@helpdesk.local');
  });

  it('calls login with form values', () => {
    auth.login.and.returnValue(of({
      token: 't',
      type: 'Bearer',
      user: { id: 1, name: 'Admin', email: 'admin@helpdesk.local', role: 'ADMIN', active: true }
    }));
    const component = fixture.componentInstance;
    component.submit();
    expect(auth.login).toHaveBeenCalled();
  });
});

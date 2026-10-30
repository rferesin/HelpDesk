import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService, provideRouter([])]
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('stores token and user after login', () => {
    service.login('admin@helpdesk.local', 'admin123').subscribe();
    const req = http.expectOne(`${environment.apiUrl}/auth/login`);
    req.flush({
      token: 'jwt-token',
      type: 'Bearer',
      user: { id: 1, name: 'Admin', email: 'admin@helpdesk.local', role: 'ADMIN', active: true }
    });
    expect(service.token()).toBe('jwt-token');
    expect(service.hasRole('ADMIN')).toBeTrue();
    expect(service.isLoggedIn()).toBeTrue();
  });

  it('clears session on logout', () => {
    localStorage.setItem('helpdesk_token', 'x');
    service.logout();
    expect(service.token()).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
  });
});

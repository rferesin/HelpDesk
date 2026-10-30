# HelpDesk - Sistema de Chamados

Aplicacao fullstack corporativa com Angular, Spring Boot e PostgreSQL.
Arquitetura em camadas no backend (Controller -> Service -> Repository) e frontend
standalone com Bootstrap, autenticacao JWT e controle de acesso por perfil.

## Arquitetura

```
helpdesk/
├── frontend/          Angular 18 + Bootstrap 5
├── backend/           Spring Boot 3 / Java 17
├── docker-compose.yml
└── README.md
```

### Backend (Spring Boot)

| Camada | Pacote | Responsabilidade |
| --- | --- | --- |
| Controller | `web.controller` | HTTP, validacao de entrada, status codes |
| DTO / Mapper | `web.dto`, `web.mapper` | Contrato da API, sem vazar entidade JPA |
| Service | `service` | Regras de negocio, transicoes de status, autorizacao |
| Repository | `repository` | Persistencia JPA |
| Domain | `domain.model`, `domain.enums` | Entidades e enumeracoes |
| Security | `security` | JWT, filtro e UserDetails |

Fluxo de um chamado:

```
Cliente abre chamado
        |
     ABERTO
        |
Atendente assume
        |
  EM_ATENDIMENTO
        |
    RESOLVIDO
        |
     FECHADO
```

Transicoes permitidas:

- ABERTO -> EM_ATENDIMENTO
- EM_ATENDIMENTO -> RESOLVIDO ou ABERTO
- RESOLVIDO -> FECHADO ou EM_ATENDIMENTO
- FECHADO nao transiciona

### Frontend (Angular)

- Login com JWT persistido em `localStorage`
- Interceptor HTTP anexa o Bearer token
- Guards por autenticacao e por perfil (`ADMIN`, `ATENDENTE`, `CLIENTE`)
- Telas: login, dashboard, lista de chamados, novo chamado, detalhes, usuarios e administracao
- Estilizacao com Bootstrap 5
- Proxy `/api` -> backend na porta 8080 (dev e Docker/nginx)

### Perfis

| Perfil | Permissoes |
| --- | --- |
| CLIENTE | Abre chamados, comenta, anexa arquivos, fecha chamado resolvido |
| ATENDENTE | Assume chamados, atende, resolve, reabre |
| ADMIN | Tudo acima + usuarios e categorias |

## Credenciais de demonstracao

| Perfil | E-mail | Senha |
| --- | --- | --- |
| ADMIN | admin@helpdesk.local | admin123 |
| ATENDENTE | atendente@helpdesk.local | atendente123 |
| CLIENTE | cliente@helpdesk.local | cliente123 |

## Subir com Docker

Na raiz do projeto:

```bash
docker compose up --build
```

Servicos:

- Frontend: http://localhost:4200
- Backend: http://localhost:8080
- PostgreSQL: localhost:5432 (`helpdesk` / `helpdesk`)
- Swagger: http://localhost:8080/swagger-ui.html

## Desenvolvimento local (sem Docker)

Backend (H2 em memoria, perfil `local`):

```bash
cd backend
mvn test
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm start
```

O Angular CLI usa `proxy.conf.json` para encaminhar `/api` para `http://localhost:8080`.

## Testes

Backend (JUnit 5 + Mockito):

```bash
cd backend
mvn test
```

Cobertura principal:

- Maquina de estados do chamado
- Permissoes de cliente/atendente
- Cadastro de usuario e categoria
- Geracao e parse de JWT

Frontend (Jasmine/Karma):

```bash
cd frontend
npm test -- --watch=false --browsers=ChromeHeadless
```

## API principal

| Metodo | Recurso | Descricao |
| --- | --- | --- |
| POST | `/api/auth/login` | Autenticacao JWT |
| GET | `/api/auth/me` | Usuario autenticado |
| GET | `/api/dashboard` | Contadores por status |
| GET/POST | `/api/tickets` | Listar / abrir chamado |
| GET | `/api/tickets/{id}` | Detalhe com comentarios, anexos e historico |
| POST | `/api/tickets/{id}/assign` | Atendente assume |
| PATCH | `/api/tickets/{id}/status` | Transicao de status |
| POST | `/api/tickets/{id}/comments` | Comentario |
| POST | `/api/tickets/{id}/attachments` | Upload de anexo |
| GET/POST | `/api/users` | Gestao de usuarios (ADMIN) |
| GET/POST | `/api/categories` | Gestao de categorias |

## Modelo de dados

- `User` - nome, e-mail, senha (BCrypt), role, ativo
- `Ticket` - titulo, descricao, status, prioridade, categoria, cliente, atendente
- `Category` - nome, descricao, ativo
- `Comment` - autor, conteudo, data
- `Attachment` - arquivo persistido em disco, metadados no banco
- `TicketHistory` - de/para status, usuario e nota da transicao

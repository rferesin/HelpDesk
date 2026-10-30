export type Role = 'ADMIN' | 'ATENDENTE' | 'CLIENTE';
export type TicketStatus = 'ABERTO' | 'EM_ATENDIMENTO' | 'RESOLVIDO' | 'FECHADO';
export type TicketPriority = 'BAIXA' | 'MEDIA' | 'ALTA' | 'URGENTE';

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
  active: boolean;
}

export interface LoginResponse {
  token: string;
  type: string;
  user: User;
}

export interface Category {
  id: number;
  name: string;
  description: string;
  active: boolean;
}

export interface TicketSummary {
  id: number;
  title: string;
  status: TicketStatus;
  priority: TicketPriority;
  categoryName: string;
  clientName: string;
  assigneeName: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Comment {
  id: number;
  authorId: number;
  authorName: string;
  content: string;
  createdAt: string;
}

export interface Attachment {
  id: number;
  originalName: string;
  contentType: string;
  size: number;
  createdAt: string;
}

export interface History {
  id: number;
  changedByName: string;
  fromStatus: TicketStatus | null;
  toStatus: TicketStatus;
  note: string;
  createdAt: string;
}

export interface TicketDetail {
  id: number;
  title: string;
  description: string;
  status: TicketStatus;
  priority: TicketPriority;
  categoryId: number;
  categoryName: string;
  clientId: number;
  clientName: string;
  assigneeId: number | null;
  assigneeName: string | null;
  createdAt: string;
  updatedAt: string;
  closedAt: string | null;
  comments: Comment[];
  attachments: Attachment[];
  history: History[];
}

export interface Dashboard {
  total: number;
  aberto: number;
  emAtendimento: number;
  resolvido: number;
  fechado: number;
}

export const STATUS_LABEL: Record<TicketStatus, string> = {
  ABERTO: 'Aberto',
  EM_ATENDIMENTO: 'Em atendimento',
  RESOLVIDO: 'Resolvido',
  FECHADO: 'Fechado'
};

export const PRIORITY_LABEL: Record<TicketPriority, string> = {
  BAIXA: 'Baixa',
  MEDIA: 'Media',
  ALTA: 'Alta',
  URGENTE: 'Urgente'
};

export const ROLE_LABEL: Record<Role, string> = {
  ADMIN: 'Administrador',
  ATENDENTE: 'Atendente',
  CLIENTE: 'Cliente'
};

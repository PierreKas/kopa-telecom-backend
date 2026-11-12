import { apiRequest } from './apiClient';
import type { Customer, CustomerStatus, DashboardFailures, DashboardSummary, SmsMessage, Transaction } from '../types';

const BASE_URL = '/api/proxy/admin';

function req<T>(path: string): Promise<T> {
  return apiRequest<T>(BASE_URL, path);
}

export function searchCustomers(q: string): Promise<Customer[]> {
  const query = q ? `?q=${encodeURIComponent(q)}` : '';
  return req<Customer[]>(`/api/v1/admin/customers${query}`);
}

export function getCustomer(id: string): Promise<Customer> {
  return req<Customer>(`/api/v1/admin/customers/${id}`);
}

// Creation and SIM activation are two separate calls (2026-11-03 split) - a customer can exist
// as PENDING_ACTIVATION with no SIM yet if activation is skipped or retried later. No PIN is
// set here or at activation by the admin (2026-11-03) - the customer's first PIN is a random
// code the backend generates and sends by SMS once the SIM is actually active.
export interface CreateCustomerInput {
  msisdn: string;
  custType: string;
  fullName: string;
  nationalId: string;
  birthDay: string;
  addressDetail: string;
  email?: string;
  longitude?: string;
  latitude?: string;
}

export interface CreateCustomerResult {
  customerId: string;
  msisdn: string;
  ktrnCustId: string | null;
  status: CustomerStatus;
}

export function createCustomer(input: CreateCustomerInput): Promise<CreateCustomerResult> {
  return apiRequest<CreateCustomerResult>(BASE_URL, '/api/v1/admin/customers', {
    method: 'POST',
    body: JSON.stringify(input),
  });
}

export interface ActivateSimResult {
  customerId: string;
  msisdn: string;
  status: CustomerStatus;
}

export function activateSim(customerId: string, iccid: string): Promise<ActivateSimResult> {
  return apiRequest<ActivateSimResult>(BASE_URL, `/api/v1/admin/customers/${customerId}/activate-sim`, {
    method: 'POST',
    body: JSON.stringify({ iccid }),
  });
}

export function searchTransactions(filters: { msisdn?: string; status?: string }): Promise<Transaction[]> {
  const params = new URLSearchParams();
  if (filters.msisdn) params.set('msisdn', filters.msisdn);
  if (filters.status) params.set('status', filters.status);
  const query = params.toString() ? `?${params.toString()}` : '';
  return req<Transaction[]>(`/api/v1/admin/transactions${query}`);
}

export function getTransaction(id: string): Promise<Transaction> {
  return req<Transaction>(`/api/v1/admin/transactions/${id}`);
}

export function searchSmsMessages(msisdn: string): Promise<SmsMessage[]> {
  const query = msisdn ? `?msisdn=${encodeURIComponent(msisdn)}` : '';
  return req<SmsMessage[]>(`/api/v1/admin/sms-messages${query}`);
}

export function getDashboardSummary(): Promise<DashboardSummary> {
  return req<DashboardSummary>('/api/v1/admin/dashboard/summary');
}

export function getDashboardFailures(): Promise<DashboardFailures> {
  return req<DashboardFailures>('/api/v1/admin/dashboard/failures');
}

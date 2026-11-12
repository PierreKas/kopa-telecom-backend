export function money(amount: string | number | null | undefined, currency = 'RWF'): string {
  if (amount == null || amount === '') return '—';
  const n = typeof amount === 'string' ? Number(amount) : amount;
  if (Number.isNaN(n)) return String(amount);
  return `${n.toLocaleString('en-US', { maximumFractionDigits: 2 })} ${currency}`;
}

export function formatDate(iso: string | null | undefined): string {
  if (!iso) return '—';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return iso;
  return d.toLocaleDateString('en-US', { day: '2-digit', month: 'short', year: 'numeric' });
}

export function formatDateTime(iso: string | null | undefined): string {
  if (!iso) return '—';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return iso;
  return d.toLocaleString('en-US', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

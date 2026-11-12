'use client';
import { Suspense, useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import { searchTransactions } from '@/lib/http/adminClient';
import type { Transaction, TransactionStatus } from '@/lib/types';
import { money, formatDateTime } from '@/lib/format';
import { useToast } from '@/app/components/providers/ToastProvider';
import {
  Badge,
  DetailField,
  DetailHero,
  DetailSection,
  Drawer,
  EmptyState,
  Field,
  IconAlertTriangle,
  IconCalendar,
  IconClock,
  IconHash,
  IconMessage,
  IconPhone,
  IconServer,
  IconWallet,
  Input,
  PageHeader,
  Select,
  Spinner,
} from '@/app/components/ui';

const STATUS_OPTIONS: TransactionStatus[] = ['INITIATED', 'OTP_PENDING', 'SUBMITTED', 'SUCCESS', 'FAILED', 'EXPIRED'];

const STATUS_STYLE: Record<TransactionStatus, { bg: string; color: string }> = {
  INITIATED: { bg: 'var(--kopa-sky-pale)', color: 'var(--kopa-sky)' },
  OTP_PENDING: { bg: 'var(--kopa-amber-pale)', color: 'var(--kopa-amber)' },
  SUBMITTED: { bg: 'var(--kopa-sky-pale)', color: 'var(--kopa-sky)' },
  SUCCESS: { bg: 'var(--kopa-green-pale)', color: 'var(--kopa-green)' },
  FAILED: { bg: 'var(--kopa-red-pale)', color: 'var(--kopa-red)' },
  // Distinct from FAILED - the OTP window lapsed with the customer never completing the
  // challenge, not a rejection at any layer (see kopa.customer-self-care's TransactionStatus).
  EXPIRED: { bg: 'var(--kopa-gray-100)', color: 'var(--kopa-gray-600)' },
};

function StatusBadge({ status }: { status: TransactionStatus }) {
  const s = STATUS_STYLE[status];
  return <Badge bg={s.bg} color={s.color}>{status.replace('_', ' ')}</Badge>;
}

export default function TransactionsPage() {
  return (
    <Suspense fallback={<div style={{ padding: '28px 32px' }}><Spinner /></div>}>
      <TransactionsPageInner />
    </Suspense>
  );
}

function TransactionsPageInner() {
  const toast = useToast();
  const searchParams = useSearchParams();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [msisdn, setMsisdn] = useState(() => searchParams.get('msisdn') ?? '');
  const [status, setStatus] = useState(() => searchParams.get('status') ?? '');
  const [selected, setSelected] = useState<Transaction | null>(null);

  const load = useCallback((filters: { msisdn?: string; status?: string }) => {
    setLoading(true);
    searchTransactions(filters)
      .then((rows) => {
        setTransactions(rows);
        setLoading(false);
      })
      .catch((err) => {
        toast.error(err instanceof Error ? err.message : 'Error loading transactions.');
        setLoading(false);
      });
  }, [toast]);

  useEffect(() => load({ msisdn: msisdn || undefined, status: status || undefined }), []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    const t = setTimeout(() => load({ msisdn: msisdn || undefined, status: status || undefined }), 350);
    return () => clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [msisdn, status]);

  return (
    <div style={{ padding: '28px 32px' }}>
      <PageHeader title="Transactions" subtitle="Recharges, voucher recharges, cancellations and bundle purchases" />

      <div style={{ display: 'flex', gap: 14, marginBottom: 18, flexWrap: 'wrap' }}>
        <Field label="MSISDN">
          <Input value={msisdn} onChange={(e) => setMsisdn(e.target.value)} placeholder="Filter by MSISDN…" style={{ minWidth: 200 }} />
        </Field>
        <Field label="Status">
          <Select value={status} onChange={(e) => setStatus(e.target.value)} style={{ minWidth: 180 }}>
            <option value="">All statuses</option>
            {STATUS_OPTIONS.map((s) => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
          </Select>
        </Field>
      </div>

      {loading ? (
        <Spinner />
      ) : transactions.length === 0 ? (
        <EmptyState icon="💳" text="No transactions found." />
      ) : (
        <div className="card-grid">
          {transactions.map((t) => {
            const s = STATUS_STYLE[t.status];
            return (
              <div
                key={t.id}
                onClick={() => setSelected(t)}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => { if (e.key === 'Enter') setSelected(t); }}
                style={{
                  background: 'white', borderRadius: 14, border: '1px solid var(--kopa-gray-200)',
                  boxShadow: '0 1px 3px rgba(72,26,56,0.06)', cursor: 'pointer', overflow: 'hidden',
                  display: 'flex', flexDirection: 'column', transition: 'box-shadow 0.15s, transform 0.15s',
                }}
                onMouseEnter={(e) => { e.currentTarget.style.boxShadow = '0 8px 24px rgba(72,26,56,0.12)'; e.currentTarget.style.transform = 'translateY(-2px)'; }}
                onMouseLeave={(e) => { e.currentTarget.style.boxShadow = '0 1px 3px rgba(72,26,56,0.06)'; e.currentTarget.style.transform = 'none'; }}
              >
                <div style={{ padding: 18, flex: 1 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 14 }}>
                    <StatusBadge status={t.status} />
                  </div>
                  <div style={{ fontSize: 16, fontWeight: 700, color: 'var(--kopa-navy)', marginBottom: 2 }}>{money(t.amount, t.currency ?? 'RWF')}</div>
                  <div style={{ fontSize: 13, color: 'var(--kopa-gray-600)', marginBottom: 10 }}>{t.msisdn}</div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}>
                    <span style={{ fontSize: 12.5, fontWeight: 600, padding: '3px 10px', borderRadius: 20, background: 'var(--kopa-gray-100)', color: 'var(--kopa-gray-600)' }}>
                      {t.transactionType.replace(/_/g, ' ')}
                    </span>
                    {t.productCode && <span style={{ fontSize: 12, color: 'var(--kopa-gray-400)' }}>{t.productCode}</span>}
                  </div>
                  <div style={{ display: 'flex', gap: 16 }}>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 5, fontSize: 11, color: 'var(--kopa-gray-400)', marginBottom: 3 }}>
                        <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--kopa-sky)', display: 'inline-block' }} />
                        Requested
                      </div>
                      <div style={{ fontSize: 12.5, color: 'var(--kopa-gray-800)' }}>{formatDateTime(t.requestedAt)}</div>
                    </div>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 5, fontSize: 11, color: 'var(--kopa-gray-400)', marginBottom: 3 }}>
                        <span style={{ width: 6, height: 6, borderRadius: '50%', background: s.color, display: 'inline-block' }} />
                        Completed
                      </div>
                      <div style={{ fontSize: 12.5, color: 'var(--kopa-gray-800)' }}>{t.completedAt ? formatDateTime(t.completedAt) : '—'}</div>
                    </div>
                  </div>
                </div>
                <div style={{ height: 4, background: s.color }} />
              </div>
            );
          })}
        </div>
      )}

      <Drawer open={!!selected} onClose={() => setSelected(null)} title="Transaction detail">
        {selected && (
          <>
            <DetailHero
              icon={<IconWallet />}
              title={`${selected.transactionType.replace(/_/g, ' ')} · ${money(selected.amount, selected.currency ?? 'RWF')}`}
              status={<StatusBadge status={selected.status} />}
              tintBg={STATUS_STYLE[selected.status].bg}
              tintColor={STATUS_STYLE[selected.status].color}
            />
            <DetailSection title="Transaction">
              <DetailField icon={<IconHash />} label="Transaction ID" value={selected.id} mono wide />
              <DetailField icon={<IconPhone />} label="MSISDN" value={selected.msisdn} />
              <DetailField icon={<IconHash />} label="Product code" value={selected.productCode ?? '—'} />
              <DetailField icon={<IconClock />} label="Duration" value={selected.durationMs != null ? `${selected.durationMs} ms` : '—'} />
            </DetailSection>
            <DetailSection title="KTRN Response">
              <DetailField icon={<IconHash />} label="Message code" value={selected.ktrnMsgcode ?? '—'} />
              <DetailField icon={<IconMessage />} label="Message" value={selected.ktrnMessage ?? '—'} wide />
            </DetailSection>
            {selected.status === 'FAILED' && (
              <DetailSection title="Failure" tone="alert">
                <DetailField icon={<IconServer />} label="Failure layer" value={selected.failureLayer ?? '—'} />
                <DetailField icon={<IconAlertTriangle />} label="Failure detail" value={selected.failureDetail ?? '—'} wide />
              </DetailSection>
            )}
            <DetailSection title="Timeline">
              <DetailField icon={<IconCalendar />} label="Requested" value={formatDateTime(selected.requestedAt)} />
              <DetailField icon={<IconCalendar />} label="Completed" value={formatDateTime(selected.completedAt)} />
            </DetailSection>
          </>
        )}
      </Drawer>
    </div>
  );
}

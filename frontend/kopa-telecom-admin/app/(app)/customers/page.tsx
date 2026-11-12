'use client';
import { Suspense, useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import { searchCustomers } from '@/lib/http/adminClient';
import type { Customer, CustomerStatus } from '@/lib/types';
import { formatDate, formatDateTime } from '@/lib/format';
import { useToast } from '@/app/components/providers/ToastProvider';
import {
  Badge,
  Button,
  DetailField,
  DetailHero,
  DetailSection,
  Drawer,
  EmptyState,
  IconCalendar,
  IconHash,
  IconIdCard,
  IconMail,
  IconPhone,
  PageHeader,
  SearchInput,
  Spinner,
} from '@/app/components/ui';
import { ActivateSimModal } from './ActivateSimModal';
import { CreateCustomerModal } from './CreateCustomerModal';

const STATUS_STYLE: Record<CustomerStatus, { bg: string; color: string }> = {
  PENDING_ACTIVATION: { bg: 'var(--kopa-amber-pale)', color: 'var(--kopa-amber)' },
  ACTIVE: { bg: 'var(--kopa-green-pale)', color: 'var(--kopa-green)' },
  SUSPENDED: { bg: 'var(--kopa-amber-pale)', color: 'var(--kopa-amber)' },
  DEACTIVATED: { bg: 'var(--kopa-red-pale)', color: 'var(--kopa-red)' },
};

function StatusBadge({ status }: { status: CustomerStatus }) {
  const s = STATUS_STYLE[status];
  return <Badge bg={s.bg} color={s.color}>{status.replace('_', ' ')}</Badge>;
}

function initials(name: string): string {
  const parts = name.trim().split(/\s+/);
  return ((parts[0]?.[0] ?? '') + (parts[1]?.[0] ?? '')).toUpperCase() || '?';
}

export default function CustomersPage() {
  return (
    <Suspense fallback={<div style={{ padding: '28px 32px' }}><Spinner /></div>}>
      <CustomersPageInner />
    </Suspense>
  );
}

function CustomersPageInner() {
  const toast = useToast();
  const searchParams = useSearchParams();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState(() => searchParams.get('q') ?? '');
  const [selected, setSelected] = useState<Customer | null>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [pendingActivation, setPendingActivation] = useState<{ customerId: string; fullName: string; msisdn: string } | null>(null);

  const load = useCallback((q: string) => {
    setLoading(true);
    searchCustomers(q)
      .then((rows) => {
        setCustomers(rows);
        setLoading(false);
      })
      .catch((err) => {
        toast.error(err instanceof Error ? err.message : 'Error loading customers.');
        setLoading(false);
      });
  }, [toast]);

  useEffect(() => load(query), []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    const t = setTimeout(() => load(query), 350);
    return () => clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query]);

  return (
    <div style={{ padding: '28px 32px' }}>
      <PageHeader
        title="Customers"
        subtitle="Search and view subscriber accounts"
        actions={<Button onClick={() => setCreateOpen(true)}>+ New customer</Button>}
      />
      <CreateCustomerModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={(created) => { load(query); setPendingActivation(created); }}
      />
      {pendingActivation && (
        <ActivateSimModal
          open
          customerId={pendingActivation.customerId}
          fullName={pendingActivation.fullName}
          msisdn={pendingActivation.msisdn}
          onClose={() => setPendingActivation(null)}
          onActivated={() => load(query)}
        />
      )}

      <div style={{ marginBottom: 18 }}>
        <SearchInput value={query} onChange={setQuery} placeholder="Search by MSISDN or name…" />
      </div>

      {loading ? (
        <Spinner />
      ) : customers.length === 0 ? (
        <EmptyState icon="👤" text="No customers found." />
      ) : (
        <div className="card-grid">
          {customers.map((c) => {
            const s = STATUS_STYLE[c.status];
            return (
              <div
                key={c.id}
                onClick={() => setSelected(c)}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => { if (e.key === 'Enter') setSelected(c); }}
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
                    <StatusBadge status={c.status} />
                  </div>
                  <div style={{ fontSize: 16, fontWeight: 700, color: 'var(--kopa-navy)', marginBottom: 10 }}>{c.msisdn}</div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}>
                    <span style={{ fontSize: 12.5, fontWeight: 600, padding: '3px 10px', borderRadius: 20, background: 'var(--kopa-gray-100)', color: 'var(--kopa-gray-600)' }}>
                      {c.fullName}
                    </span>
                    {c.soId && <span style={{ fontSize: 12, color: 'var(--kopa-gray-400)' }}>SO {c.soId}</span>}
                  </div>
                  <div style={{ display: 'flex', gap: 16 }}>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 5, fontSize: 11, color: 'var(--kopa-gray-400)', marginBottom: 3 }}>
                        <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--kopa-sky)', display: 'inline-block' }} />
                        Registered
                      </div>
                      <div style={{ fontSize: 12.5, color: 'var(--kopa-gray-800)' }}>{formatDate(c.createdAt)}</div>
                    </div>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 5, fontSize: 11, color: 'var(--kopa-gray-400)', marginBottom: 3 }}>
                        <span style={{ width: 6, height: 6, borderRadius: '50%', background: s.color, display: 'inline-block' }} />
                        Updated
                      </div>
                      <div style={{ fontSize: 12.5, color: 'var(--kopa-gray-800)' }}>{formatDate(c.updatedAt)}</div>
                    </div>
                  </div>
                </div>
                <div style={{ height: 4, background: s.color }} />
              </div>
            );
          })}
        </div>
      )}

      <Drawer open={!!selected} onClose={() => setSelected(null)} title="Customer detail">
        {selected && (
          <>
            <DetailHero
              icon={initials(selected.fullName)}
              title={selected.fullName}
              status={<StatusBadge status={selected.status} />}
              tintBg={STATUS_STYLE[selected.status].bg}
              tintColor={STATUS_STYLE[selected.status].color}
            />
            {selected.status === 'PENDING_ACTIVATION' && (
              <div style={{ padding: '0 20px 16px' }}>
                <Button
                  onClick={() => setPendingActivation({ customerId: selected.id, fullName: selected.fullName, msisdn: selected.msisdn })}
                >
                  Activate SIM
                </Button>
              </div>
            )}
            <DetailSection title="Account">
              <DetailField icon={<IconPhone />} label="MSISDN" value={selected.msisdn} />
              <DetailField icon={<IconIdCard />} label="National ID" value={selected.nationalId ?? '—'} />
              <DetailField icon={<IconHash />} label="KTRN customer ID" value={selected.custId ?? '—'} />
              <DetailField icon={<IconHash />} label="ICCID" value={selected.iccid ?? '—'} mono />
              <DetailField icon={<IconHash />} label="SO ID" value={selected.soId ?? '—'} />
            </DetailSection>
            <DetailSection title="Personal">
              <DetailField icon={<IconMail />} label="Email" value={selected.email ?? '—'} />
              <DetailField icon={<IconCalendar />} label="Date of birth" value={formatDate(selected.birthDay)} />
            </DetailSection>
            <DetailSection title="Timeline">
              <DetailField icon={<IconCalendar />} label="Registered" value={formatDateTime(selected.createdAt)} />
              <DetailField icon={<IconCalendar />} label="Last updated" value={formatDateTime(selected.updatedAt)} />
            </DetailSection>
          </>
        )}
      </Drawer>
    </div>
  );
}

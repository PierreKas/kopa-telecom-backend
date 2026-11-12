'use client';
import { useCallback, useEffect, useState } from 'react';
import { searchSmsMessages } from '@/lib/http/adminClient';
import type { SmsMessage, SmsMessageStatus } from '@/lib/types';
import { money, formatDateTime } from '@/lib/format';
import { useToast } from '@/app/components/providers/ToastProvider';
import {
  Badge,
  DetailField,
  DetailHero,
  DetailSection,
  Drawer,
  EmptyState,
  IconAlertTriangle,
  IconCalendar,
  IconHash,
  IconMessage,
  IconPhone,
  IconServer,
  IconWallet,
  PageHeader,
  SearchInput,
  Spinner,
} from '@/app/components/ui';

const STATUS_STYLE: Record<SmsMessageStatus, { bg: string; color: string }> = {
  QUEUED: { bg: 'var(--kopa-sky-pale)', color: 'var(--kopa-sky)' },
  SENT: { bg: 'var(--kopa-green-pale)', color: 'var(--kopa-green)' },
  DELIVERED: { bg: 'var(--kopa-green-pale)', color: 'var(--kopa-green)' },
  UNDELIVERED: { bg: 'var(--kopa-red-pale)', color: 'var(--kopa-red)' },
  ERRORED: { bg: 'var(--kopa-red-pale)', color: 'var(--kopa-red)' },
  FAILED_TO_SEND: { bg: 'var(--kopa-red-pale)', color: 'var(--kopa-red)' },
};

function StatusBadge({ status }: { status: SmsMessageStatus }) {
  const s = STATUS_STYLE[status];
  return <Badge bg={s.bg} color={s.color}>{status.replace(/_/g, ' ')}</Badge>;
}

export default function SmsMessagesPage() {
  const toast = useToast();
  const [messages, setMessages] = useState<SmsMessage[]>([]);
  const [loading, setLoading] = useState(true);
  const [msisdn, setMsisdn] = useState('');
  const [selected, setSelected] = useState<SmsMessage | null>(null);

  const load = useCallback((q: string) => {
    setLoading(true);
    searchSmsMessages(q)
      .then((rows) => {
        setMessages(rows);
        setLoading(false);
      })
      .catch((err) => {
        toast.error(err instanceof Error ? err.message : 'Error loading SMS messages.');
        setLoading(false);
      });
  }, [toast]);

  useEffect(() => load(''), [load]);

  useEffect(() => {
    const t = setTimeout(() => load(msisdn), 350);
    return () => clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [msisdn]);

  return (
    <div style={{ padding: '28px 32px' }}>
      <PageHeader title="SMS Messages" subtitle="Send and delivery history" />

      <div style={{ marginBottom: 18 }}>
        <SearchInput value={msisdn} onChange={setMsisdn} placeholder="Filter by MSISDN…" />
      </div>

      {loading ? (
        <Spinner />
      ) : messages.length === 0 ? (
        <EmptyState icon="✉️" text="No SMS messages found." />
      ) : (
        <div className="card-grid">
          {messages.map((m) => {
            const s = STATUS_STYLE[m.status];
            return (
              <div
                key={m.id}
                onClick={() => setSelected(m)}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => { if (e.key === 'Enter') setSelected(m); }}
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
                    <StatusBadge status={m.status} />
                  </div>
                  <div style={{ fontSize: 16, fontWeight: 700, color: 'var(--kopa-navy)', marginBottom: 2 }}>{m.purpose.replace(/_/g, ' ')}</div>
                  <div style={{ fontSize: 13, color: 'var(--kopa-gray-600)', marginBottom: 10 }}>{m.msisdn}</div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}>
                    <span style={{ fontSize: 12.5, fontWeight: 600, padding: '3px 10px', borderRadius: 20, background: 'var(--kopa-gray-100)', color: 'var(--kopa-gray-600)', fontFamily: 'monospace' }}>
                      {m.templateCode}
                    </span>
                    {m.cost != null && <span style={{ fontSize: 12, color: 'var(--kopa-gray-400)' }}>{money(m.cost, 'RWF')}</span>}
                  </div>
                  <div style={{ display: 'flex', gap: 16 }}>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 5, fontSize: 11, color: 'var(--kopa-gray-400)', marginBottom: 3 }}>
                        <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'var(--kopa-sky)', display: 'inline-block' }} />
                        Sent
                      </div>
                      <div style={{ fontSize: 12.5, color: 'var(--kopa-gray-800)' }}>{formatDateTime(m.createdAt)}</div>
                    </div>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 5, fontSize: 11, color: 'var(--kopa-gray-400)', marginBottom: 3 }}>
                        <span style={{ width: 6, height: 6, borderRadius: '50%', background: s.color, display: 'inline-block' }} />
                        By
                      </div>
                      <div style={{ fontSize: 12.5, color: 'var(--kopa-gray-800)' }}>{m.requestedByService}</div>
                    </div>
                  </div>
                </div>
                <div style={{ height: 4, background: s.color }} />
              </div>
            );
          })}
        </div>
      )}

      <Drawer open={!!selected} onClose={() => setSelected(null)} title="SMS detail">
        {selected && (
          <>
            <DetailHero
              icon={<IconMessage />}
              title={selected.purpose.replace(/_/g, ' ')}
              status={<StatusBadge status={selected.status} />}
              tintBg={STATUS_STYLE[selected.status].bg}
              tintColor={STATUS_STYLE[selected.status].color}
            />
            <DetailSection title="Message">
              <DetailField icon={<IconPhone />} label="MSISDN" value={selected.msisdn} />
              <DetailField icon={<IconHash />} label="Template" value={selected.templateCode} mono />
              <DetailField icon={<IconHash />} label="Provider message ID" value={selected.providerMessageId ?? '—'} mono />
              <DetailField icon={<IconWallet />} label="Cost" value={money(selected.cost, 'RWF')} />
              <DetailField icon={<IconServer />} label="Requested by" value={selected.requestedByService} />
            </DetailSection>
            {selected.failureLayer && (
              <DetailSection title="Failure" tone="alert">
                <DetailField icon={<IconServer />} label="Failure layer" value={selected.failureLayer} />
                <DetailField icon={<IconAlertTriangle />} label="Failure detail" value={selected.failureDetail ?? '—'} wide />
              </DetailSection>
            )}
            <DetailSection title="Timeline">
              <DetailField icon={<IconCalendar />} label="Created" value={formatDateTime(selected.createdAt)} />
              <DetailField icon={<IconCalendar />} label="Last updated" value={formatDateTime(selected.updatedAt)} />
            </DetailSection>
          </>
        )}
      </Drawer>
    </div>
  );
}

'use client';
import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { getDashboardFailures, getDashboardSummary, searchCustomers, searchTransactions } from '@/lib/http/adminClient';
import type { Customer, DashboardFailures, DashboardSummary, Transaction, TransactionStatus } from '@/lib/types';
import { money } from '@/lib/format';
import { useToast } from '@/app/components/providers/ToastProvider';
import {
  Badge,
  Card,
  EmptyState,
  IconCheckCircle,
  IconUsers,
  IconXCircle,
  PageHeader,
  Spinner,
} from '@/app/components/ui';

const TX_STATUS_COLOR: Record<TransactionStatus, string> = {
  INITIATED: 'var(--kopa-sky)',
  OTP_PENDING: 'var(--kopa-amber)',
  SUBMITTED: 'var(--kopa-sky)',
  SUCCESS: 'var(--kopa-green)',
  FAILED: 'var(--kopa-red)',
  EXPIRED: 'var(--kopa-gray-600)',
};

const ArrowUpRight = () => (
  <svg width="15" height="15" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24"><line x1="7" y1="17" x2="17" y2="7" /><polyline points="7 7 17 7 17 17" /></svg>
);

function HeroStatCard({ label, value, href }: { label: string; value: React.ReactNode; href: string }) {
  return (
    <Link href={href} style={{ textDecoration: 'none' }}>
      <div style={{
        background: 'linear-gradient(135deg, var(--kopa-blue), var(--kopa-ink))', borderRadius: 14, padding: 20,
        color: 'white', cursor: 'pointer', boxShadow: '0 8px 24px rgba(197,73,154,0.3)',
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 20 }}>
          <span style={{ fontSize: 13.5, fontWeight: 600, opacity: 0.9 }}>{label}</span>
          <div style={{ width: 28, height: 28, borderRadius: '50%', background: 'rgba(255,255,255,0.18)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <ArrowUpRight />
          </div>
        </div>
        <div style={{ fontSize: 30, fontWeight: 700, letterSpacing: '-0.5px' }}>{value}</div>
        <div style={{ fontSize: 11.5, opacity: 0.75, marginTop: 6 }}>View all transactions</div>
      </div>
    </Link>
  );
}

function PlainStatCard({ label, value, color, icon, href }: { label: string; value: React.ReactNode; color: string; icon: React.ReactNode; href: string }) {
  return (
    <Link href={href} style={{ textDecoration: 'none' }}>
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 14 }}>
          <div style={{ width: 36, height: 36, borderRadius: 10, background: color + '18', display: 'flex', alignItems: 'center', justifyContent: 'center', color }}>
            {icon}
          </div>
          <div style={{ width: 26, height: 26, borderRadius: '50%', border: '1px solid var(--kopa-gray-200)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--kopa-gray-400)' }}>
            <ArrowUpRight />
          </div>
        </div>
        <div style={{ fontSize: 22, fontWeight: 700, color: 'var(--kopa-navy)', letterSpacing: '-0.5px' }}>{value}</div>
        <div style={{ fontSize: 12, color: 'var(--kopa-gray-600)', marginTop: 2 }}>{label}</div>
      </Card>
    </Link>
  );
}

function FailureBarChart({ failures }: { failures: DashboardFailures }) {
  const max = Math.max(1, ...failures.byLayer.map((r) => r.count));
  return (
    <Card style={{ display: 'flex', flexDirection: 'column' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 18 }}>
        <span style={{ fontSize: 14, fontWeight: 600, color: 'var(--kopa-navy)' }}>Failure Breakdown</span>
        {failures.totalFailed > 0 && <Badge bg="var(--kopa-red-pale)" color="var(--kopa-red)">{failures.totalFailed} total</Badge>}
      </div>
      {failures.byLayer.length === 0 ? (
        <EmptyState icon="✅" text="No failures recorded." />
      ) : (
        <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-around', gap: 10, flex: 1, minHeight: 160 }}>
          {failures.byLayer.map((row) => {
            const heightPct = Math.max(10, (row.count / max) * 100);
            return (
              <div key={row.failureLayer} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', flex: 1, height: '100%', justifyContent: 'flex-end' }}>
                <span style={{ fontSize: 12, fontWeight: 700, color: 'var(--kopa-navy)', marginBottom: 6 }}>{row.count}</span>
                <div style={{
                  width: '100%', maxWidth: 46, height: `${heightPct}%`, minHeight: 10, borderRadius: 10,
                  background: 'linear-gradient(180deg, var(--kopa-blue), var(--kopa-ink))',
                }} />
                <span style={{ fontSize: 10.5, color: 'var(--kopa-gray-600)', marginTop: 8, textAlign: 'center', fontWeight: 500 }}>
                  {row.failureLayer.replace(/_/g, ' ')}
                </span>
              </div>
            );
          })}
        </div>
      )}
    </Card>
  );
}

function SmsTodayCard({ summary }: { summary: DashboardSummary }) {
  return (
    <Card style={{ display: 'flex', flexDirection: 'column' }}>
      <span style={{ fontSize: 14, fontWeight: 600, color: 'var(--kopa-navy)', marginBottom: 16 }}>SMS Today</span>
      <div style={{ display: 'flex', gap: 16, marginBottom: 18 }}>
        <div>
          <div style={{ fontSize: 22, fontWeight: 700, color: 'var(--kopa-green)' }}>{summary.smsSentToday}</div>
          <div style={{ fontSize: 11.5, color: 'var(--kopa-gray-600)' }}>Sent</div>
        </div>
        <div>
          <div style={{ fontSize: 22, fontWeight: 700, color: 'var(--kopa-red)' }}>{summary.smsFailedToday}</div>
          <div style={{ fontSize: 11.5, color: 'var(--kopa-gray-600)' }}>Failed</div>
        </div>
      </div>
      <Link
        href="/sms-messages"
        style={{
          marginTop: 'auto', background: 'var(--kopa-ink)', color: 'white', borderRadius: 8, padding: '10px 14px',
          fontSize: 13, fontWeight: 600, textAlign: 'center', textDecoration: 'none',
        }}
      >
        View SMS Messages
      </Link>
    </Card>
  );
}

function RecentTransactionsCard({ transactions }: { transactions: Transaction[] }) {
  return (
    <Card style={{ display: 'flex', flexDirection: 'column' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
        <span style={{ fontSize: 14, fontWeight: 600, color: 'var(--kopa-navy)' }}>Recent Transactions</span>
        <Link href="/transactions" style={{ fontSize: 12, fontWeight: 600, color: 'var(--kopa-blue)', textDecoration: 'none' }}>View all</Link>
      </div>
      {transactions.length === 0 ? (
        <EmptyState icon="💳" text="No transactions yet." />
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {transactions.slice(0, 6).map((t) => (
            <div key={t.id} style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <span style={{ width: 8, height: 8, borderRadius: '50%', background: TX_STATUS_COLOR[t.status], flexShrink: 0 }} />
              <div style={{ minWidth: 0, flex: 1 }}>
                <div style={{ fontSize: 13, fontWeight: 600, color: 'var(--kopa-gray-800)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {t.transactionType.replace(/_/g, ' ')} · {money(t.amount, t.currency ?? 'RWF')}
                </div>
                <div style={{ fontSize: 11.5, color: 'var(--kopa-gray-400)' }}>{t.msisdn}</div>
              </div>
            </div>
          ))}
        </div>
      )}
    </Card>
  );
}

function initials(name: string): string {
  const parts = name.trim().split(/\s+/);
  return ((parts[0]?.[0] ?? '') + (parts[1]?.[0] ?? '')).toUpperCase() || '?';
}

function RecentCustomersCard({ customers }: { customers: Customer[] }) {
  return (
    <Card style={{ display: 'flex', flexDirection: 'column' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
        <span style={{ fontSize: 14, fontWeight: 600, color: 'var(--kopa-navy)' }}>Recent Customers</span>
        <Link href="/customers" style={{ fontSize: 12, fontWeight: 600, color: 'var(--kopa-blue)', textDecoration: 'none' }}>View all</Link>
      </div>
      {customers.length === 0 ? (
        <EmptyState icon="👤" text="No customers yet." />
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          {customers.slice(0, 5).map((c) => (
            <div key={c.id} style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <div style={{ width: 34, height: 34, borderRadius: '50%', background: 'var(--kopa-blue)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 12.5, fontWeight: 700, color: 'white', flexShrink: 0 }}>
                {initials(c.fullName)}
              </div>
              <div style={{ minWidth: 0, flex: 1 }}>
                <div style={{ fontSize: 13, fontWeight: 600, color: 'var(--kopa-gray-800)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{c.fullName}</div>
                <div style={{ fontSize: 11.5, color: 'var(--kopa-gray-400)' }}>{c.msisdn}</div>
              </div>
              <Badge
                bg={c.status === 'ACTIVE' ? 'var(--kopa-green-pale)' : c.status === 'DEACTIVATED' || c.status === 'SUSPENDED' ? 'var(--kopa-red-pale)' : 'var(--kopa-amber-pale)'}
                color={c.status === 'ACTIVE' ? 'var(--kopa-green)' : c.status === 'DEACTIVATED' || c.status === 'SUSPENDED' ? 'var(--kopa-red)' : 'var(--kopa-amber)'}
              >
                {c.status.replace('_', ' ')}
              </Badge>
            </div>
          ))}
        </div>
      )}
    </Card>
  );
}

function DeliveryDonut({ rate }: { rate: number }) {
  const size = 150;
  const stroke = 14;
  const r = (size - stroke) / 2;
  const circumference = 2 * Math.PI * r;
  const clamped = Math.max(0, Math.min(100, rate));
  const offset = circumference * (1 - clamped / 100);
  const color = clamped >= 95 ? 'var(--kopa-green)' : clamped >= 80 ? 'var(--kopa-amber)' : 'var(--kopa-red)';

  return (
    <Card style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
      <span style={{ fontSize: 14, fontWeight: 600, color: 'var(--kopa-navy)', alignSelf: 'flex-start', marginBottom: 10 }}>SMS Delivery Rate</span>
      <div style={{ position: 'relative', width: size, height: size, margin: '6px 0 16px' }}>
        <svg width={size} height={size} style={{ transform: 'rotate(-90deg)' }}>
          <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="var(--kopa-gray-100)" strokeWidth={stroke} />
          <circle
            cx={size / 2} cy={size / 2} r={r} fill="none" stroke={color} strokeWidth={stroke}
            strokeDasharray={circumference} strokeDashoffset={offset} strokeLinecap="round"
            style={{ transition: 'stroke-dashoffset 0.4s ease' }}
          />
        </svg>
        <div style={{ position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
          <span style={{ fontSize: 24, fontWeight: 700, color: 'var(--kopa-navy)' }}>{clamped.toFixed(0)}%</span>
          <span style={{ fontSize: 10.5, color: 'var(--kopa-gray-400)' }}>today</span>
        </div>
      </div>
      <div style={{ display: 'flex', gap: 16 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <span style={{ width: 8, height: 8, borderRadius: '50%', background: color, display: 'inline-block' }} />
          <span style={{ fontSize: 11.5, color: 'var(--kopa-gray-600)' }}>Delivered</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <span style={{ width: 8, height: 8, borderRadius: '50%', background: 'var(--kopa-gray-100)', display: 'inline-block' }} />
          <span style={{ fontSize: 11.5, color: 'var(--kopa-gray-600)' }}>Failed</span>
        </div>
      </div>
    </Card>
  );
}

function ClockCard({ onRefresh, lastRefreshed, refreshing }: { onRefresh: () => void; lastRefreshed: Date | null; refreshing: boolean }) {
  const [now, setNow] = useState<Date | null>(null);
  useEffect(() => {
    setNow(new Date());
    const t = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(t);
  }, []);

  return (
    <div style={{
      background: 'var(--kopa-ink)', borderRadius: 14, padding: 20, color: 'white',
      display: 'flex', flexDirection: 'column', justifyContent: 'space-between',
    }}>
      <span style={{ fontSize: 13.5, fontWeight: 600, opacity: 0.85 }}>Current Time</span>
      <div style={{ fontSize: 30, fontWeight: 700, letterSpacing: '1px', margin: '10px 0' }}>
        {now ? now.toLocaleTimeString('en-US', { hour12: false }) : '--:--:--'}
      </div>
      <div style={{ fontSize: 11, opacity: 0.6, marginBottom: 14 }}>
        {lastRefreshed ? `Data refreshed at ${lastRefreshed.toLocaleTimeString('en-US', { hour12: false })}` : 'Not yet refreshed'}
      </div>
      <button
        onClick={onRefresh}
        disabled={refreshing}
        style={{
          background: 'rgba(255,255,255,0.14)', border: '1px solid rgba(255,255,255,0.2)', color: 'white',
          borderRadius: 8, padding: '9px 14px', fontSize: 13, fontWeight: 600, cursor: refreshing ? 'default' : 'pointer',
          opacity: refreshing ? 0.6 : 1, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
        }}
      >
        <svg width="14" height="14" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24" style={{ animation: refreshing ? 'kopa-spin 0.8s linear infinite' : undefined }}>
          <polyline points="23 4 23 10 17 10" /><polyline points="1 20 1 14 7 14" /><path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15" />
        </svg>
        {refreshing ? 'Refreshing…' : 'Refresh dashboard'}
      </button>
    </div>
  );
}

export default function DashboardPage() {
  const toast = useToast();
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [failures, setFailures] = useState<DashboardFailures | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [lastRefreshed, setLastRefreshed] = useState<Date | null>(null);

  const load = useCallback((isRefresh = false) => {
    if (isRefresh) setRefreshing(true); else setLoading(true);
    Promise.all([getDashboardSummary(), getDashboardFailures(), searchTransactions({}), searchCustomers('')])
      .then(([s, f, tx, cust]) => {
        setSummary(s);
        setFailures(f);
        setTransactions([...tx].sort((a, b) => b.requestedAt.localeCompare(a.requestedAt)));
        setCustomers([...cust].sort((a, b) => b.createdAt.localeCompare(a.createdAt)));
        setLastRefreshed(new Date());
        setLoading(false);
        setRefreshing(false);
      })
      .catch((err) => {
        toast.error(err instanceof Error ? err.message : 'Error loading the dashboard.');
        setLoading(false);
        setRefreshing(false);
      });
  }, [toast]);

  useEffect(() => load(false), [load]);

  return (
    <div style={{ padding: '28px 32px' }}>
      <PageHeader title="Dashboard" subtitle="Today's registrations, transactions and SMS delivery" />

      {loading || !summary || !failures ? (
        <Spinner />
      ) : (
        <>
          <div className="stat-grid" style={{ marginBottom: 20 }}>
            <HeroStatCard label="Transactions Today" value={summary.transactionsToday} href="/transactions" />
            <PlainStatCard label="Successful Today" value={summary.successfulTransactionsToday} color="var(--kopa-green)" icon={<IconCheckCircle />} href="/transactions?status=SUCCESS" />
            <PlainStatCard label="Failed Today" value={summary.failedTransactionsToday} color="var(--kopa-red)" icon={<IconXCircle />} href="/transactions?status=FAILED" />
            <PlainStatCard label="New Registrations" value={summary.newRegistrationsToday} color="var(--kopa-blue)" icon={<IconUsers />} href="/customers" />
          </div>

          <div className="dashboard-row" style={{ marginBottom: 20 }}>
            <FailureBarChart failures={failures} />
            <SmsTodayCard summary={summary} />
            <RecentTransactionsCard transactions={transactions} />
          </div>

          <div className="dashboard-row">
            <RecentCustomersCard customers={customers} />
            <DeliveryDonut rate={summary.smsDeliveryRateToday} />
            <ClockCard onRefresh={() => load(true)} lastRefreshed={lastRefreshed} refreshing={refreshing} />
          </div>
        </>
      )}
    </div>
  );
}

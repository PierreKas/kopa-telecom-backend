'use client';
import { useState } from 'react';
import { createCustomer } from '@/lib/http/adminClient';
import { useToast } from '@/app/components/providers/ToastProvider';
import { Button, Field, Input, Modal, Select } from '@/app/components/ui';

const CUST_TYPES = [
  { value: 'A', label: 'Individual' },
  { value: 'B', label: 'Self-employed' },
  { value: 'C', label: 'Foreigner' },
  { value: 'D', label: 'Corporate' },
  { value: 'E', label: 'Public sector' },
];

const EMPTY = {
  fullName: '', custType: 'A', nationalId: '', birthDay: '', addressDetail: '', email: '',
  msisdn: '',
};

/** Creation only (2026-11-03 split) - no ICCID here. On success the customer already exists as
 *  PENDING_ACTIVATION even if the SIM is never activated; onCreated hands the parent page the
 *  new customer's id/name/msisdn so it can immediately offer ActivateSimModal. */
export function CreateCustomerModal({
  open, onClose, onCreated,
}: {
  open: boolean;
  onClose: () => void;
  onCreated: (created: { customerId: string; fullName: string; msisdn: string }) => void;
}) {
  const toast = useToast();
  const [form, setForm] = useState(EMPTY);
  const [submitting, setSubmitting] = useState(false);

  function set<K extends keyof typeof EMPTY>(key: K, value: string) {
    setForm((f) => ({ ...f, [key]: value }));
  }

  function close() {
    setForm(EMPTY);
    onClose();
  }

  function captureGeolocation(): Promise<{ longitude?: string; latitude?: string }> {
    return new Promise((resolve) => {
      if (!navigator.geolocation) {
        resolve({});
        return;
      }
      navigator.geolocation.getCurrentPosition(
        (pos) => resolve({ longitude: String(pos.coords.longitude), latitude: String(pos.coords.latitude) }),
        () => resolve({}),
      );
    });
  }

  async function submit() {
    if (!form.fullName || !form.nationalId || !form.birthDay || !form.addressDetail || !form.msisdn) {
      toast.error('Fill in every required field first.');
      return;
    }
    setSubmitting(true);
    try {
      const geo = await captureGeolocation();
      const result = await createCustomer({ ...form, ...geo });
      toast.success(`${form.fullName} created — now activate their SIM.`);
      onCreated({ customerId: result.customerId, fullName: form.fullName, msisdn: result.msisdn });
      close();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Could not create this customer.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Modal
      open={open}
      onClose={close}
      title="Create customer"
      width={640}
      footer={
        <>
          <Button variant="secondary" onClick={close}>Cancel</Button>
          <Button onClick={submit} disabled={submitting}>{submitting ? 'Creating…' : 'Create customer'}</Button>
        </>
      }
    >
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
        <Field label="Full name" required><Input value={form.fullName} onChange={(e) => set('fullName', e.target.value)} /></Field>
        <Field label="Account type" required>
          <Select value={form.custType} onChange={(e) => set('custType', e.target.value)}>
            {CUST_TYPES.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
          </Select>
        </Field>
        <Field label="National ID" required><Input value={form.nationalId} onChange={(e) => set('nationalId', e.target.value)} /></Field>
        <Field label="Date of birth" required>
          <Input
            type="date"
            max={new Date().toISOString().slice(0, 10)}
            value={form.birthDay ? `${form.birthDay.slice(0, 4)}-${form.birthDay.slice(4, 6)}-${form.birthDay.slice(6, 8)}` : ''}
            onChange={(e) => set('birthDay', e.target.value.replaceAll('-', ''))}
          />
        </Field>
        <Field label="Address" required span><Input value={form.addressDetail} onChange={(e) => set('addressDetail', e.target.value)} /></Field>
        <Field label="Email"><Input type="email" value={form.email} onChange={(e) => set('email', e.target.value)} /></Field>
        <Field label="MSISDN" required><Input value={form.msisdn} onChange={(e) => set('msisdn', e.target.value)} placeholder="0771234567" /></Field>
      </div>
    </Modal>
  );
}

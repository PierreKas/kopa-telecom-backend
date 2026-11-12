'use client';
import { useState } from 'react';
import { activateSim } from '@/lib/http/adminClient';
import { useToast } from '@/app/components/providers/ToastProvider';
import { Button, Field, Input, Modal } from '@/app/components/ui';

/** Standalone SIM-activation step (2026-11-03 split) - reused both right after a customer is
 *  created and later from the customer list for anyone still PENDING_ACTIVATION, since the
 *  activation call can fail/time out independently of creation and is safe to retry any time. */
export function ActivateSimModal({
  open, customerId, fullName, msisdn, onClose, onActivated,
}: {
  open: boolean;
  customerId: string;
  fullName: string;
  msisdn: string;
  onClose: () => void;
  onActivated: () => void;
}) {
  const toast = useToast();
  const [iccid, setIccid] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function submit() {
    if (!/^\d{18}$/.test(iccid)) {
      toast.error('ICCID must be 18 digits.');
      return;
    }
    setSubmitting(true);
    try {
      await activateSim(customerId, iccid);
      toast.success(`${fullName}'s SIM (${msisdn}) is now active. A temporary PIN was sent by SMS, valid for 15 minutes.`);
      setIccid('');
      onClose();
      onActivated();
    } catch (err) {
      toast.error(err instanceof Error ? err.message : 'Could not activate this SIM.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Activate SIM"
      width={480}
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>Not now</Button>
          <Button onClick={submit} disabled={submitting}>{submitting ? 'Activating…' : 'Activate SIM'}</Button>
        </>
      }
    >
      <div style={{ fontSize: 13, color: 'var(--kopa-gray-600)', marginBottom: 14 }}>
        {fullName} — {msisdn}
      </div>
      <Field label="ICCID" required hint="18 digits">
        <Input value={iccid} onChange={(e) => setIccid(e.target.value)} placeholder="892501720000111620" />
      </Field>
    </Modal>
  );
}

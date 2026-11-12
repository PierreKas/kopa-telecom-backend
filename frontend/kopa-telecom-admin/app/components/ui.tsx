'use client';
import Image from 'next/image';
import React, { useEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';

// Shared UI kit - inline styles + the CSS variables defined in globals.css. Structure ported
// from the Inuwa-Admin reference project (same author's house style), rebranded with Kopa
// Telecom's own color tokens.

// ---- Layout primitives ------------------------------------------------------

export function PageHeader({
  title,
  subtitle,
  eyebrow,
  actions,
}: {
  title: string;
  subtitle?: string;
  eyebrow?: string;
  actions?: React.ReactNode;
}) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 16, marginBottom: 24, flexWrap: 'wrap' }}>
      <div>
        {eyebrow && <div style={{ fontSize: 13, color: 'var(--kopa-blue)', fontWeight: 600, marginBottom: 4 }}>{eyebrow}</div>}
        <h1 style={{ fontSize: 24, fontWeight: 700, color: 'var(--kopa-navy)', margin: 0, letterSpacing: '-0.4px' }}>{title}</h1>
        {subtitle && <p style={{ color: 'var(--kopa-gray-600)', marginTop: 6, fontSize: 14 }}>{subtitle}</p>}
      </div>
      {actions && <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>{actions}</div>}
    </div>
  );
}

export function Card({ children, style, padding = 20 }: { children: React.ReactNode; style?: React.CSSProperties; padding?: number }) {
  return (
    <div style={{ background: 'white', borderRadius: 12, border: '1px solid var(--kopa-gray-200)', boxShadow: '0 1px 3px rgba(72,26,56,0.06)', padding, ...style }}>
      {children}
    </div>
  );
}

export function SectionTitle({ children, right }: { children: React.ReactNode; right?: React.ReactNode }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
      <span style={{ fontSize: 14, fontWeight: 600, color: 'var(--kopa-navy)' }}>{children}</span>
      {right}
    </div>
  );
}

export function StatCard({ label, value, sub, color = 'var(--kopa-navy)', icon }: { label: string; value: React.ReactNode; sub?: React.ReactNode; color?: string; icon?: React.ReactNode }) {
  return (
    <Card>
      {icon && (
        <div style={{ width: 38, height: 38, borderRadius: 10, background: color + '18', display: 'flex', alignItems: 'center', justifyContent: 'center', color, marginBottom: 12 }}>
          {icon}
        </div>
      )}
      <div style={{ fontSize: 22, fontWeight: 700, color, letterSpacing: '-0.5px' }}>{value}</div>
      <div style={{ fontSize: 12, color: 'var(--kopa-gray-600)', marginTop: 2 }}>{label}</div>
      {sub != null && <div style={{ fontSize: 11, color: 'var(--kopa-gray-400)', marginTop: 4, fontWeight: 500 }}>{sub}</div>}
    </Card>
  );
}

// ---- Buttons ----------------------------------------------------------------

type Variant = 'primary' | 'secondary' | 'ghost' | 'danger';

const VARIANT_STYLE: Record<Variant, React.CSSProperties> = {
  primary: { background: 'var(--kopa-blue)', color: 'white', border: '1px solid var(--kopa-blue)' },
  secondary: { background: 'var(--kopa-navy)', color: 'white', border: '1px solid var(--kopa-navy)' },
  ghost: { background: 'white', color: 'var(--kopa-gray-600)', border: '1px solid var(--kopa-gray-200)' },
  danger: { background: 'var(--kopa-red)', color: 'white', border: '1px solid var(--kopa-red)' },
};

export function Button({
  children,
  variant = 'primary',
  onClick,
  type = 'button',
  disabled,
  full,
  size = 'md',
  style,
}: {
  children: React.ReactNode;
  variant?: Variant;
  onClick?: () => void;
  type?: 'button' | 'submit';
  disabled?: boolean;
  full?: boolean;
  size?: 'sm' | 'md' | 'lg';
  style?: React.CSSProperties;
}) {
  const pad = size === 'sm' ? '7px 14px' : size === 'lg' ? '14px 20px' : '10px 18px';
  const fontSize = size === 'sm' ? 13 : size === 'lg' ? 15 : 14;
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      style={{
        ...VARIANT_STYLE[variant],
        padding: pad,
        fontSize,
        fontWeight: 600,
        borderRadius: 8,
        cursor: disabled ? 'not-allowed' : 'pointer',
        opacity: disabled ? 0.5 : 1,
        width: full ? '100%' : undefined,
        transition: 'all 0.15s',
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 6,
        ...style,
      }}
    >
      {children}
    </button>
  );
}

// ---- Form fields ------------------------------------------------------------

export function fieldBaseStyle(error?: boolean): React.CSSProperties {
  return {
    width: '100%',
    padding: '10px 12px',
    border: `1.5px solid ${error ? 'var(--kopa-red)' : 'var(--kopa-gray-200)'}`,
    borderRadius: 8,
    fontSize: 14,
    outline: 'none',
    color: 'var(--kopa-gray-800)',
    background: 'white',
    fontFamily: 'inherit',
  };
}

export function Field({ label, required, error, hint, children, span }: { label?: string; required?: boolean; error?: string; hint?: string; children: React.ReactNode; span?: boolean }) {
  return (
    <div style={span ? { gridColumn: '1 / -1' } : undefined}>
      {label && (
        <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--kopa-gray-600)', display: 'block', marginBottom: 5 }}>
          {label} {required && <span style={{ color: 'var(--kopa-red)' }}>*</span>}
        </label>
      )}
      {children}
      {hint && !error && <div style={{ fontSize: 11, color: 'var(--kopa-gray-400)', marginTop: 4 }}>{hint}</div>}
      {error && <div style={{ fontSize: 11, color: 'var(--kopa-red)', marginTop: 4 }}>{error}</div>}
    </div>
  );
}

type InputProps = React.InputHTMLAttributes<HTMLInputElement> & { error?: boolean };
export const Input = React.forwardRef<HTMLInputElement, InputProps>(function Input({ error, style, ...rest }, ref) {
  return <input ref={ref} style={{ ...fieldBaseStyle(error), ...style }} {...rest} />;
});

/** Password field with a show/hide toggle. */
export const PasswordInput = React.forwardRef<HTMLInputElement, InputProps>(function PasswordInput({ error, style, ...rest }, ref) {
  const [visible, setVisible] = useState(false);
  return (
    <div style={{ position: 'relative' }}>
      <input
        ref={ref}
        type={visible ? 'text' : 'password'}
        style={{ ...fieldBaseStyle(error), paddingRight: 40, ...style }}
        {...rest}
      />
      <button
        type="button"
        onClick={() => setVisible((v) => !v)}
        aria-label={visible ? 'Hide password' : 'Show password'}
        tabIndex={-1}
        style={{
          position: 'absolute', right: 4, top: '50%', transform: 'translateY(-50%)',
          width: 32, height: 32, display: 'flex', alignItems: 'center', justifyContent: 'center',
          background: 'none', border: 'none', cursor: 'pointer', color: 'var(--kopa-gray-400)', padding: 0,
        }}
      >
        {visible ? (
          <svg width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" /><line x1="1" y1="1" x2="23" y2="23" /></svg>
        ) : (
          <svg width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" /><circle cx="12" cy="12" r="3" /></svg>
        )}
      </button>
    </div>
  );
});

type SelectProps = React.SelectHTMLAttributes<HTMLSelectElement> & { error?: boolean };
export function Select({ error, style, children, ...rest }: SelectProps) {
  return (
    <select style={{ ...fieldBaseStyle(error), ...style }} {...rest}>
      {children}
    </select>
  );
}

type TextareaProps = React.TextareaHTMLAttributes<HTMLTextAreaElement> & { error?: boolean };
export function Textarea({ error, style, ...rest }: TextareaProps) {
  return <textarea style={{ ...fieldBaseStyle(error), resize: 'vertical', ...style }} {...rest} />;
}

export function SearchInput({ value, onChange, placeholder, style }: { value: string; onChange: (v: string) => void; placeholder?: string; style?: React.CSSProperties }) {
  return (
    <div style={{ position: 'relative', flex: 1, minWidth: 200, ...style }}>
      <svg style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--kopa-gray-400)' }} width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
        <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
      </svg>
      <input
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        style={{ ...fieldBaseStyle(), padding: '9px 12px 9px 36px', background: 'var(--kopa-gray-50)' }}
      />
    </div>
  );
}

// ---- Badges -----------------------------------------------------------------

export function Badge({ children, bg, color }: { children: React.ReactNode; bg: string; color: string }) {
  return <span style={{ fontSize: 12, fontWeight: 600, padding: '4px 10px', borderRadius: 20, background: bg, color, whiteSpace: 'nowrap' }}>{children}</span>;
}

export function CodeChip({ children }: { children: React.ReactNode }) {
  return <span style={{ fontSize: 12, fontFamily: 'monospace', background: 'var(--kopa-gray-100)', color: 'var(--kopa-gray-600)', padding: '2px 8px', borderRadius: 4, whiteSpace: 'nowrap' }}>{children}</span>;
}

// ---- Tabs -------------------------------------------------------------------

export function Tabs<T extends string>({ tabs, active, onChange }: { tabs: { key: T; label: string }[]; active: T; onChange: (k: T) => void }) {
  return (
    <div style={{ display: 'flex', background: 'white', border: '1px solid var(--kopa-gray-200)', borderRadius: 10, padding: 4, width: 'fit-content', gap: 4, flexWrap: 'wrap' }}>
      {tabs.map((t) => (
        <button
          key={t.key}
          onClick={() => onChange(t.key)}
          style={{
            padding: '8px 18px',
            borderRadius: 7,
            border: 'none',
            cursor: 'pointer',
            fontSize: 14,
            fontWeight: active === t.key ? 600 : 400,
            background: active === t.key ? 'var(--kopa-navy)' : 'transparent',
            color: active === t.key ? 'white' : 'var(--kopa-gray-600)',
            transition: 'all 0.15s',
          }}
        >
          {t.label}
        </button>
      ))}
    </div>
  );
}

// ---- Table & empty states ---------------------------------------------------

export function Th({ children, align = 'left' }: { children: React.ReactNode; align?: 'left' | 'right' | 'center' }) {
  return (
    <th style={{ padding: '11px 18px', textAlign: align, fontSize: 11, fontWeight: 600, color: 'var(--kopa-gray-400)', textTransform: 'uppercase', letterSpacing: '0.05em', whiteSpace: 'nowrap', background: 'var(--kopa-gray-50)' }}>
      {children}
    </th>
  );
}

export function Td({ children, align = 'left', style }: { children: React.ReactNode; align?: 'left' | 'right' | 'center'; style?: React.CSSProperties }) {
  return <td style={{ padding: '13px 18px', textAlign: align, fontSize: 13, color: 'var(--kopa-gray-800)', ...style }}>{children}</td>;
}

export function EmptyState({ icon = '📭', text }: { icon?: string; text: string }) {
  return (
    <div style={{ padding: '48px', textAlign: 'center', color: 'var(--kopa-gray-400)', fontSize: 14 }}>
      <div style={{ fontSize: 34, marginBottom: 12 }}>{icon}</div>
      {text}
    </div>
  );
}

const LOADER_RING = 132; // spinner ring size, at scale 1
const LOADER_CORE = 92; // white circle behind the logo, at scale 1

/** The branded "KopaLoader" ported from the mobile app's own login loader
 *  (src/components/ui/kopa-loader.tsx) - three staggered pulse rings, a spinning arc, a
 *  breathing gradient circle around the logo mark, and animated dots after the label - so every
 *  loading state in the admin reads as the same piece of branding as the mobile app's.
 *  `fullscreen` (used by FullPageLoader below) pins it over the whole viewport with the
 *  "KOPA TELECOM" footer; without it (used by Spinner below), it's a smaller, contained block
 *  that drops into whatever card/section is already on screen. */
function KopaLoader({ label = 'Loading…', fullscreen = false }: { label?: string; fullscreen?: boolean }) {
  const [dots, setDots] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => setDots((d) => (d + 1) % 4), 400);
    return () => clearInterval(timer);
  }, []);

  const scale = fullscreen ? 1 : 0.72;
  const ring = LOADER_RING * scale;
  const core = LOADER_CORE * scale;

  return (
    <div
      style={
        fullscreen
          ? { position: 'fixed', inset: 0, background: 'rgba(255,255,255,0.96)', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }
          : { padding: '48px', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }
      }
    >
      <div style={{ width: ring, height: ring, position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        {/* Pulse rings */}
        {[0, 800, 1600].map((delay) => (
          <span
            key={delay}
            style={{
              position: 'absolute', width: core, height: core, borderRadius: '50%',
              border: '2px solid var(--kopa-blue)',
              animation: 'kopa-loader-ring 2.4s ease-out infinite', animationDelay: `${delay}ms`,
            }}
          />
        ))}

        {/* Spinning arc */}
        <span
          style={{
            position: 'absolute', width: ring, height: ring, borderRadius: '50%',
            border: '4px solid rgba(197,73,154,0.13)', borderTopColor: 'var(--kopa-blue)',
            animation: 'kopa-spin 1.1s linear infinite',
          }}
        />

        {/* Breathing gradient core + logo */}
        <div
          style={{
            width: core + 8, height: core + 8, borderRadius: '50%',
            background: 'linear-gradient(135deg, var(--kopa-blue), var(--kopa-navy))',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            boxShadow: '0 6px 16px rgba(197,73,154,0.35)',
            animation: 'kopa-loader-breathe 1.8s ease-in-out infinite',
          }}
        >
          <div style={{ width: core, height: core, borderRadius: '50%', background: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Image src="/images/logo-icon.png" alt="" width={core * 0.5} height={core * 0.5} style={{ objectFit: 'contain' }} />
          </div>
        </div>
      </div>

      <div style={{ marginTop: fullscreen ? 32 : 20, textAlign: 'center' }}>
        <span style={{ color: 'var(--kopa-gray-800)', fontSize: fullscreen ? 17 : 14, fontWeight: 600 }}>
          {label}
          <span style={{ color: 'var(--kopa-blue)' }}>{'.'.repeat(dots)}</span>
        </span>
      </div>

      {fullscreen && (
        <span style={{ position: 'absolute', bottom: 40, letterSpacing: 6, color: 'var(--kopa-gray-200)', fontSize: 11, fontWeight: 600 }}>
          KOPA TELECOM
        </span>
      )}
    </div>
  );
}

export function Spinner({ label = 'Loading…' }: { label?: string }) {
  return <KopaLoader label={label} />;
}

/** Full-viewport loading state - the initial auth check. */
export function FullPageLoader({ label = 'Loading…' }: { label?: string }) {
  return <KopaLoader label={label} fullscreen />;
}

// ---- Modal (centered dialog) ------------------------------------------------

/**
 * The dialog box itself (header/body/footer, capped to viewport height, only the body
 * scrolls) - with no overlay of its own. Factored out of Modal so a caller that needs several
 * dialogs sharing one overlay can lay out multiple DialogPanels directly instead of nesting one
 * Modal's overlay inside another's.
 */
export function DialogPanel({ onClose, title, children, footer, width = 560, style }: { onClose: () => void; title: string; children: React.ReactNode; footer?: React.ReactNode; width?: number; style?: React.CSSProperties }) {
  return (
    <div
      onMouseDown={(e) => e.stopPropagation()}
      style={{
        background: 'white', borderRadius: 14, width: '100%', maxWidth: width, maxHeight: '100%',
        display: 'flex', flexDirection: 'column', boxShadow: '0 20px 60px rgba(72,26,56,0.3)', overflow: 'hidden',
        ...style,
      }}
    >
      <div style={{ padding: '18px 22px', background: 'var(--kopa-navy)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexShrink: 0 }}>
        <h2 style={{ fontSize: 16, fontWeight: 700, color: 'white', margin: 0 }}>{title}</h2>
        <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'rgba(255,255,255,0.75)', padding: 4, display: 'flex' }} aria-label="Close">
          <svg width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></svg>
        </button>
      </div>
      <div style={{ padding: 22, overflowY: 'auto', flex: 1 }}>{children}</div>
      {footer && <div style={{ padding: '16px 22px', borderTop: '1px solid var(--kopa-gray-100)', display: 'flex', justifyContent: 'flex-end', gap: 10, flexShrink: 0 }}>{footer}</div>}
    </div>
  );
}

/**
 * Centered dialog - a DialogPanel plus its own full-screen overlay. Most callers want this;
 * use DialogPanel directly only when several panels must share one overlay.
 */
export function Modal({ open, onClose, title, children, footer, width = 560 }: { open: boolean; onClose: () => void; title: string; children: React.ReactNode; footer?: React.ReactNode; width?: number }) {
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', onKey);
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.style.overflow = '';
    };
  }, [open, onClose]);

  if (!open) return null;
  return (
    <div
      onMouseDown={onClose}
      style={{ position: 'fixed', inset: 0, background: 'rgba(72,26,56,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '32px 20px', zIndex: 1000 }}
    >
      <DialogPanel onClose={onClose} title={title} footer={footer} width={width}>{children}</DialogPanel>
    </div>
  );
}

/** Side panel sliding in from the left or right - for read-only detail views. */
export function Drawer({ open, onClose, title, children, footer, width = 420, side = 'right' }: { open: boolean; onClose: () => void; title: string; children: React.ReactNode; footer?: React.ReactNode; width?: number; side?: 'left' | 'right' }) {
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', onKey);
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.style.overflow = '';
    };
  }, [open, onClose]);

  if (!open) return null;
  return (
    <div onMouseDown={onClose} style={{ position: 'fixed', inset: 0, background: 'rgba(72,26,56,0.5)', zIndex: 1000 }}>
      <div
        onMouseDown={(e) => e.stopPropagation()}
        style={{
          position: 'absolute', top: 0, bottom: 0, [side]: 0, width: '100%', maxWidth: width, background: 'white',
          borderTop: '3px solid var(--kopa-blue)',
          boxShadow: side === 'left' ? '8px 0 40px rgba(72,26,56,0.25)' : '-8px 0 40px rgba(72,26,56,0.25)',
          display: 'flex', flexDirection: 'column', animation: `drawer-in-${side} 0.22s ease-out`,
        }}
      >
        <div style={{ padding: '18px 22px', borderBottom: '1px solid var(--kopa-gray-100)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexShrink: 0 }}>
          <h2 style={{ fontSize: 16, fontWeight: 700, color: 'var(--kopa-navy)', margin: 0 }}>{title}</h2>
          <button onClick={onClose} className="drawer-close-btn" style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--kopa-gray-400)', padding: 6, borderRadius: 8, display: 'flex' }} aria-label="Close">
            <svg width="20" height="20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></svg>
          </button>
        </div>
        <div style={{ padding: 22, overflowY: 'auto', flex: 1, display: 'flex', flexDirection: 'column', gap: 18 }}>{children}</div>
        {footer && <div style={{ padding: '16px 22px', borderTop: '1px solid var(--kopa-gray-100)', display: 'flex', justifyContent: 'flex-end', gap: 10, flexShrink: 0 }}>{footer}</div>}
      </div>
    </div>
  );
}

// ---- Drawer detail-view building blocks --------------------------------------
// A richer alternative to a flat label/value stack: a tinted identity header, grouped
// field sections with a subtle card treatment, and icon-labeled fields in a 2-col grid.

/** Identity block at the top of a Drawer's content - an icon/initials chip, a title, and an
 *  optional status badge, on a very light wash of the entity's status color. Pass the same
 *  {bg, color} pair already used for that entity's StatusBadge as tintBg/tintColor. */
export function DetailHero({
  icon,
  title,
  status,
  tintBg = 'var(--kopa-gray-50)',
  tintColor = 'var(--kopa-gray-400)',
}: {
  icon: React.ReactNode;
  title: React.ReactNode;
  status?: React.ReactNode;
  tintBg?: string;
  tintColor?: string;
}) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 14, padding: '16px 18px', borderRadius: 12, background: tintBg }}>
      <div style={{
        width: 48, height: 48, borderRadius: '50%', background: tintColor, flexShrink: 0,
        display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontSize: 16, fontWeight: 700,
      }}>
        {icon}
      </div>
      <div style={{ minWidth: 0, flex: 1 }}>
        <div style={{ fontSize: 16, fontWeight: 700, color: 'var(--kopa-navy)', wordBreak: 'break-word', marginBottom: status ? 6 : 0 }}>
          {title}
        </div>
        {status}
      </div>
    </div>
  );
}

/** A titled group of DetailFields on a subtle card background. tone="alert" switches the
 *  background/label to the red-pale treatment, for a failure/error section. */
export function DetailSection({ title, tone = 'default', children }: { title: string; tone?: 'default' | 'alert'; children: React.ReactNode }) {
  const bg = tone === 'alert' ? 'var(--kopa-red-pale)' : 'var(--kopa-gray-50)';
  const labelColor = tone === 'alert' ? 'var(--kopa-red)' : 'var(--kopa-gray-400)';
  return (
    <div>
      <div style={{ fontSize: 11, fontWeight: 700, color: labelColor, textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 10 }}>
        {title}
      </div>
      <div style={{ background: bg, borderRadius: 10, padding: 14, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
        {children}
      </div>
    </div>
  );
}

/** One icon-labeled field inside a DetailSection's grid. wide spans both columns - use for
 *  long values (a full KTRN message, a failure detail sentence, a long id). */
export function DetailField({ icon, label, value, mono, wide }: { icon: React.ReactNode; label: string; value: React.ReactNode; mono?: boolean; wide?: boolean }) {
  return (
    <div style={wide ? { gridColumn: '1 / -1' } : undefined}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 10.5, fontWeight: 600, color: 'var(--kopa-gray-400)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 5 }}>
        <span style={{ display: 'flex' }}>{icon}</span>
        {label}
      </div>
      <div style={{ fontSize: 13.5, color: 'var(--kopa-gray-800)', fontWeight: 500, fontFamily: mono ? 'monospace' : 'inherit', wordBreak: 'break-word' }}>
        {value}
      </div>
    </div>
  );
}

// ---- Shared icons for ActionMenu (and elsewhere as needed) ------------------

const iconProps = { width: 15, height: 15, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', strokeWidth: 2, strokeLinecap: 'round' as const, strokeLinejoin: 'round' as const };

export const IconEye = () => (
  <svg {...iconProps}><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" /><circle cx="12" cy="12" r="3" /></svg>
);
export const IconEdit = () => (
  <svg {...iconProps}><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" /><path d="M18.5 2.5a2.12 2.12 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" /></svg>
);
export const IconTrash = () => (
  <svg {...iconProps}><polyline points="3 6 5 6 21 6" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /></svg>
);
export const IconArrowDown = () => (
  <svg {...iconProps}><line x1="12" y1="5" x2="12" y2="19" /><polyline points="19 12 12 19 5 12" /></svg>
);
export const IconArrowUp = () => (
  <svg {...iconProps}><line x1="12" y1="19" x2="12" y2="5" /><polyline points="5 12 12 5 19 12" /></svg>
);
export const IconWallet = () => (
  <svg {...iconProps}><path d="M21 12V7H5a2 2 0 0 1 0-4h14v4" /><path d="M3 5v14a2 2 0 0 0 2 2h16v-5" /><path d="M18 12a2 2 0 0 0 0 4h4v-4z" /></svg>
);
export const IconCheckCircle = () => (
  <svg {...iconProps}><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" /><polyline points="22 4 12 14.01 9 11.01" /></svg>
);
export const IconXCircle = () => (
  <svg {...iconProps}><circle cx="12" cy="12" r="10" /><line x1="15" y1="9" x2="9" y2="15" /><line x1="9" y1="9" x2="15" y2="15" /></svg>
);
export const IconUsers = () => (
  <svg {...iconProps}><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M23 21v-2a4 4 0 0 0-3-3.87" /><path d="M16 3.13a4 4 0 0 1 0 7.75" /></svg>
);
export const IconMessage = () => (
  <svg {...iconProps}><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" /></svg>
);
export const IconPhone = () => (
  <svg {...iconProps}><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.9.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.91.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z" /></svg>
);
export const IconIdCard = () => (
  <svg {...iconProps}><rect x="2" y="5" width="20" height="14" rx="2" /><circle cx="8" cy="12" r="2" /><line x1="13" y1="10" x2="19" y2="10" /><line x1="13" y1="14" x2="19" y2="14" /></svg>
);
export const IconHash = () => (
  <svg {...iconProps}><line x1="4" y1="9" x2="20" y2="9" /><line x1="4" y1="15" x2="20" y2="15" /><line x1="10" y1="3" x2="8" y2="21" /><line x1="16" y1="3" x2="14" y2="21" /></svg>
);
export const IconMail = () => (
  <svg {...iconProps}><path d="M4 4h16a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z" /><polyline points="22,6 12,13 2,6" /></svg>
);
export const IconCalendar = () => (
  <svg {...iconProps}><rect x="3" y="4" width="18" height="18" rx="2" /><line x1="16" y1="2" x2="16" y2="6" /><line x1="8" y1="2" x2="8" y2="6" /><line x1="3" y1="10" x2="21" y2="10" /></svg>
);
export const IconAlertTriangle = () => (
  <svg {...iconProps}><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" /><line x1="12" y1="9" x2="12" y2="13" /><line x1="12" y1="17" x2="12.01" y2="17" /></svg>
);
export const IconServer = () => (
  <svg {...iconProps}><rect x="2" y="3" width="20" height="7" rx="1.5" /><rect x="2" y="14" width="20" height="7" rx="1.5" /><line x1="6" y1="6.5" x2="6.01" y2="6.5" /><line x1="6" y1="17.5" x2="6.01" y2="17.5" /></svg>
);
export const IconClock = () => (
  <svg {...iconProps}><circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" /></svg>
);

// ---- Action menu (kebab) -----------------------------------------------

export interface ActionMenuItem {
  label: string;
  icon: React.ReactNode;
  onClick: () => void;
  danger?: boolean;
  disabled?: boolean;
}

const ACTION_MENU_WIDTH = 200;

/**
 * Row-level "..." menu replacing a row of separate buttons in a table's last column. The
 * dropdown is rendered through a portal into <body> as `position: fixed`, positioned from the
 * trigger's own bounding box, and flips to open upward when there isn't enough room below.
 * Kept for parity with the reference project's UI kit even though no page here uses it yet -
 * every data page is read-only reporting.
 */
export function ActionMenu({ items }: { items: ActionMenuItem[] }) {
  const [open, setOpen] = useState(false);
  const [coords, setCoords] = useState<{ top: number; left: number; openUp: boolean } | null>(null);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const menuRef = useRef<HTMLDivElement>(null);

  const place = () => {
    const btn = triggerRef.current;
    if (!btn) return;
    const rect = btn.getBoundingClientRect();
    const estimatedHeight = items.length * 38 + 16;
    const openUp = rect.bottom + estimatedHeight > window.innerHeight && rect.top > estimatedHeight;
    setCoords({
      top: openUp ? rect.top - 4 : rect.bottom + 4,
      left: Math.min(rect.right - ACTION_MENU_WIDTH, window.innerWidth - ACTION_MENU_WIDTH - 8),
      openUp,
    });
  };

  const toggle = () => {
    if (!open) place();
    setOpen((v) => !v);
  };

  useEffect(() => {
    if (!open) return;
    const onDown = (e: MouseEvent) => {
      const target = e.target as Node;
      if (triggerRef.current?.contains(target) || menuRef.current?.contains(target)) return;
      setOpen(false);
    };
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false);
    };
    const onScroll = () => setOpen(false);
    document.addEventListener('mousedown', onDown);
    document.addEventListener('keydown', onKey);
    window.addEventListener('scroll', onScroll, true);
    window.addEventListener('resize', onScroll);
    return () => {
      document.removeEventListener('mousedown', onDown);
      document.removeEventListener('keydown', onKey);
      window.removeEventListener('scroll', onScroll, true);
      window.removeEventListener('resize', onScroll);
    };
  }, [open]);

  return (
    <>
      <button
        ref={triggerRef}
        type="button"
        onClick={toggle}
        aria-label="Actions"
        aria-haspopup="menu"
        aria-expanded={open}
        style={{
          width: 32, height: 32, display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
          background: open ? 'var(--kopa-gray-100)' : 'none', border: 'none', borderRadius: 8,
          cursor: 'pointer', color: 'var(--kopa-gray-400)',
        }}
      >
        <svg width="17" height="17" viewBox="0 0 24 24" fill="currentColor">
          <circle cx="12" cy="5" r="2.1" /><circle cx="12" cy="12" r="2.1" /><circle cx="12" cy="19" r="2.1" />
        </svg>
      </button>
      {open && coords && typeof document !== 'undefined' && createPortal(
        <div
          ref={menuRef}
          role="menu"
          style={{
            position: 'fixed', top: coords.top, left: coords.left,
            transform: coords.openUp ? 'translateY(-100%)' : undefined,
            width: ACTION_MENU_WIDTH, background: 'white',
            borderRadius: 10, border: '1px solid var(--kopa-gray-200)', boxShadow: '0 12px 32px rgba(72,26,56,0.18)',
            zIndex: 1000, padding: 6,
          }}
        >
          {items.map((it, i) => {
            const showDivider = i > 0 && !!it.danger && !items[i - 1].danger;
            return (
              <React.Fragment key={it.label}>
                {showDivider && <div style={{ height: 1, background: 'var(--kopa-gray-100)', margin: '5px 6px' }} />}
                <button
                  type="button"
                  role="menuitem"
                  disabled={it.disabled}
                  onClick={() => {
                    setOpen(false);
                    it.onClick();
                  }}
                  className={`action-menu-item${it.danger ? ' danger' : ''}`}
                  style={{
                    width: '100%', display: 'flex', alignItems: 'center', gap: 10, padding: '9px 10px', borderRadius: 7,
                    border: 'none', background: 'none', cursor: it.disabled ? 'not-allowed' : 'pointer',
                    fontSize: 13.5, fontWeight: 500, textAlign: 'left',
                    color: it.disabled ? 'var(--kopa-gray-400)' : it.danger ? 'var(--kopa-red)' : 'var(--kopa-gray-800)',
                    opacity: it.disabled ? 0.5 : 1,
                  }}
                >
                  <span style={{ display: 'flex', width: 16, flexShrink: 0 }}>{it.icon}</span>
                  {it.label}
                </button>
              </React.Fragment>
            );
          })}
        </div>,
        document.body,
      )}
    </>
  );
}

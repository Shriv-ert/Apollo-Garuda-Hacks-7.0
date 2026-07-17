const reEmail = /^[^\s@]+@[^\s@]+\.[a-z]{2,}$/i;
const rePhone = /^(62|0)[0-9]{7,13}$/;
const reBank = /^[1-9][0-9]{7,19}$/;
const reURL = /^([a-z0-9-]+\.)+[a-z]{2,}(\/.*)?$/i;
const reNumeric = /^[+()\-\.\s0-9]+$/;
const reNonDigit = /[^0-9]/g;

export function normalizeEntity(raw: string): string {
  if (!raw) return '';
  let v = raw.trim().toLowerCase();
  if (!v) return '';

  if (v.includes('@')) {
    return v;
  }

  // Phone / Bank account: strip all non-digit separators if it looks numeric
  if (reNumeric.test(v)) {
    let digits = v.replace(reNonDigit, '');
    if (digits.startsWith('62')) {
      digits = '0' + digits.slice(2);
    }
    return digits;
  }

  // URL: strip schema, www prefix, and trailing slash
  v = v.replace(/^https?:\/\//, '');
  v = v.replace(/^www\./, '');
  if (v.endsWith('/')) {
    v = v.slice(0, -1);
  }
  return v;
}

export function detectEntityType(
  value: string,
): 'phone' | 'bank_account' | 'url' | 'email' | null {
  if (reEmail.test(value)) {
    return 'email';
  }
  if (rePhone.test(value)) {
    return 'phone';
  }
  if (reBank.test(value)) {
    return 'bank_account';
  }
  if (reURL.test(value)) {
    return 'url';
  }
  return null;
}

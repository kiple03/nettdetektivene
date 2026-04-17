export type AuthField = 'email' | 'password' | 'confirm';

export type AuthFieldErrors = Partial<Record<AuthField, string>>;

export function validateUsername(value: string): string {
	if (!value.trim()) return 'Brukernavn er påkrevd.';
	if (value.trim().length < 3) return 'Brukernavn må ha minst 3 tegn.';
	if (!/^[a-zA-Z0-9_]+$/.test(value)) return 'Brukernavn kan bare inneholde bokstaver, tall og _.';
	return '';
}

export function validateEmail(value: string): string {
	if (!value.trim()) return 'E-post er påkrevd.';
	if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) return 'Skriv inn en gyldig e-postadresse.';
	return '';
}

export function validatePassword(value: string): string {
	if (!value) return 'Passord er påkrevd.';
	if (value.length < 8) return 'Passord må ha minst 8 tegn.';
	if (!/[A-Za-z]/.test(value) || !/\d/.test(value)) return 'Passord må inneholde både bokstaver og tall.';
	return '';
}

export function validatePasswordConfirm(password: string, confirm: string): string {
	if (!confirm) return 'Bekreft passordet.';
	if (password !== confirm) return 'Passordene stemmer ikke overens.';
	return '';
}

export function hasAuthErrors(errors: AuthFieldErrors): boolean {
	return Object.values(errors).some(Boolean);
}

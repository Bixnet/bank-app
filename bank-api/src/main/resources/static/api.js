// Small helpers shared by all pages: CSRF token, who is logged in, login, register, logout.

async function csrfHeader() {
    const response = await fetch('/api/csrf');
    if (!response.ok) {
        throw new Error('could not get a security token (status ' + response.status + ')');
    }
    const csrf = await response.json();
    return { [csrf.headerName]: csrf.token };
}

/** Returns { username, role } of the logged-in user, or null when nobody is logged in. */
export async function getCurrentUser() {
    const response = await fetch('/api/me');
    if (response.status === 401) {
        return null;
    }
    if (!response.ok) {
        throw new Error('server answered with status ' + response.status);
    }
    return response.json();
}

/** Returns true when the login worked, false for a wrong username or password. */
export async function login(username, password) {
    const response = await fetch('/api/login', {
        method: 'POST',
        headers: await csrfHeader(),
        body: new URLSearchParams({ username, password })
    });
    if (response.status === 401) {
        return false;
    }
    if (!response.ok) {
        throw new Error('server answered with status ' + response.status);
    }
    return true;
}

/** Returns { ok: true } or { ok: false, message } with the server's explanation. */
export async function register(username, password) {
    const response = await fetch('/api/register', {
        method: 'POST',
        headers: { ...(await csrfHeader()), 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    });
    if (response.status === 201) {
        return { ok: true };
    }
    const body = await response.json().catch(() => ({}));
    return { ok: false, message: body.error ?? 'Registration failed (status ' + response.status + ').' };
}

export async function logout() {
    const response = await fetch('/api/logout', { method: 'POST', headers: await csrfHeader() });
    if (!response.ok) {
        throw new Error('server answered with status ' + response.status);
    }
}

/**
 * Deposit or withdraw. action is 'deposit' or 'withdraw'.
 * Returns { ok: true, account } with the new balance, or { ok: false, message, signedOut }.
 */
export async function moveMoney(accountNumber, action, amount) {
    const response = await fetch('/accounts/' + encodeURIComponent(accountNumber) + '/' + action, {
        method: 'POST',
        headers: { ...(await csrfHeader()), 'Content-Type': 'application/json' },
        body: JSON.stringify({ amount })
    });
    if (response.ok) {
        return { ok: true, account: await response.json() };
    }
    if (response.status === 401) {
        return { ok: false, signedOut: true, message: 'Your session has ended. Please sign in again.' };
    }
    const body = await response.json().catch(() => ({}));
    return { ok: false, message: body.error ?? 'The transaction failed (status ' + response.status + ').' };
}
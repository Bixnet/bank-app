import { getCurrentUser, logout } from './api.js';

const statusEl = document.getElementById('status');
const totalEl = document.getElementById('total');
const accountsEl = document.getElementById('accounts');
const stateEl = document.getElementById('state');
const userAreaEl = document.getElementById('user-area');
const userNameEl = document.getElementById('user-name');
const logoutButton = document.getElementById('logout');

const money = new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' });

const TYPE_LABELS = {
    SavingsAccount: 'Savings',
    CheckingAccount: 'Checking'
};

const SKELETON_COUNT = 2;
const SLOW_HINT_DELAY_MS = 4000;

function textElement(tag, className, text) {
    const element = document.createElement(tag);
    element.className = className;
    element.textContent = text;
    return element;
}

function createCard(account) {
    const card = document.createElement('article');
    card.className = 'account-card';
    card.append(
        textElement('span', 'account-type', TYPE_LABELS[account.type] ?? account.type),
        textElement('h3', 'account-name', account.name),
        textElement('p', 'account-meta', 'No. ' + account.accountNumber + ' · ' + account.branch),
        textElement('p', 'account-balance', money.format(account.balance))
    );
    return card;
}

function createSkeletonCard() {
    const card = document.createElement('article');
    card.className = 'account-card skeleton-card';
    card.setAttribute('aria-hidden', 'true');
    for (const size of ['short', 'medium', 'medium', 'large']) {
        const line = document.createElement('div');
        line.className = 'skeleton-line ' + size;
        card.append(line);
    }
    return card;
}

function showMessage(kind, title, text, onRetry) {
    const children = [
        textElement('p', 'state-title', title),
        textElement('p', 'state-text', text)
    ];
    if (onRetry) {
        const button = textElement('button', 'button', 'Retry');
        button.type = 'button';
        button.addEventListener('click', onRetry);
        children.push(button);
    }
    stateEl.className = 'state state-' + kind;
    stateEl.replaceChildren(...children);
    stateEl.hidden = false;
}

function showLoading() {
    stateEl.hidden = true;
    accountsEl.setAttribute('aria-busy', 'true');
    accountsEl.replaceChildren(...Array.from({ length: SKELETON_COUNT }, createSkeletonCard));
    totalEl.textContent = '–';
    statusEl.textContent = 'Loading accounts…';
}

function showUser(user) {
    userNameEl.textContent = user.username;
    userAreaEl.hidden = false;
}

function showAccounts(accounts) {
    accountsEl.setAttribute('aria-busy', 'false');

    if (accounts.length === 0) {
        accountsEl.replaceChildren();
        totalEl.textContent = money.format(0);
        statusEl.textContent = '0 accounts';
        showMessage('empty', 'No accounts yet', 'When an account is opened, it will appear here.');
        return;
    }

    const total = accounts.reduce((sum, account) => sum + account.balance, 0);
    stateEl.hidden = true;
    accountsEl.replaceChildren(...accounts.map(createCard));
    totalEl.textContent = money.format(total);
    statusEl.textContent = 'Across ' + accounts.length + (accounts.length === 1 ? ' account' : ' accounts');
}

function showError(error) {
    accountsEl.setAttribute('aria-busy', 'false');
    accountsEl.replaceChildren();
    totalEl.textContent = '–';
    statusEl.textContent = 'Accounts could not be loaded';
    showMessage(
        'error',
        'Something went wrong',
        'We could not load your accounts (' + error.message + '). Please try again.',
        () => { void start(); }
    );
}

async function fetchAccounts() {
    const response = await fetch('/accounts');
    if (!response.ok) {
        throw new Error('server answered with status ' + response.status);
    }
    return response.json();
}

async function start() {
    showLoading();
    const slowHint = setTimeout(() => {
        statusEl.textContent = 'Waking up the server… this can take up to a minute on the free plan.';
    }, SLOW_HINT_DELAY_MS);

    try {
        const user = await getCurrentUser();
        if (!user) {
            window.location.replace('/login.html');
            return;
        }
        showUser(user);
        showAccounts(await fetchAccounts());
    } catch (error) {
        showError(error);
    } finally {
        clearTimeout(slowHint);
    }
}

async function signOut() {
    logoutButton.disabled = true;
    try {
        await logout();
        window.location.replace('/login.html');
    } catch (error) {
        statusEl.textContent = 'Could not sign out: ' + error.message;
        logoutButton.disabled = false;
    }
}

logoutButton.addEventListener('click', () => { void signOut(); });

await start();
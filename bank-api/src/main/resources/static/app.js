import { getCurrentUser, logout, moveMoney } from './api.js';

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

// The accounts currently shown, so the total can be recalculated after a transaction
let currentAccounts = [];

function textElement(tag, className, text) {
    const element = document.createElement(tag);
    element.className = className;
    element.textContent = text;
    return element;
}

function updateTotal() {
    const total = currentAccounts.reduce((sum, account) => sum + account.balance, 0);
    totalEl.textContent = money.format(total);
}

function applyUpdate(updated) {
    currentAccounts = currentAccounts.map((account) =>
        account.accountNumber === updated.accountNumber ? updated : account);
    updateTotal();
}

function createMoneyForm(account, balanceEl, messageEl, actionsEl) {
    const form = document.createElement('form');
    form.className = 'money-form';
    form.hidden = true;
    form.noValidate = true;

    const inputId = 'amount-' + account.accountNumber;
    const label = textElement('label', 'field-label', 'Amount in €');
    label.htmlFor = inputId;
    const input = document.createElement('input');
    input.className = 'field-input';
    input.id = inputId;
    input.type = 'number';
    input.min = '0.01';
    input.step = '0.01';
    input.inputMode = 'decimal';
    input.required = true;

    const submit = textElement('button', 'button button-small', '');
    submit.type = 'submit';
    const cancel = textElement('button', 'button button-small button-secondary', 'Cancel');
    cancel.type = 'button';
    const buttons = document.createElement('div');
    buttons.className = 'form-buttons';
    buttons.append(submit, cancel);
    form.append(label, input, buttons);

    let action = 'deposit';

    function showCardMessage(kind, text) {
        messageEl.className = 'card-message card-message-' + kind;
        messageEl.textContent = text;
        messageEl.hidden = false;
    }

    function open(chosenAction) {
        action = chosenAction;
        submit.textContent = chosenAction === 'deposit' ? 'Confirm deposit' : 'Confirm withdrawal';
        actionsEl.hidden = true;
        messageEl.hidden = true;
        form.hidden = false;
        input.value = '';
        input.focus();
    }

    function close() {
        form.hidden = true;
        actionsEl.hidden = false;
    }

    async function submitAmount() {
        const amount = input.valueAsNumber;
        if (Number.isNaN(amount)) {
            showCardMessage('error', 'Please enter an amount.');
            input.focus();
            return;
        }
        submit.disabled = true;
        cancel.disabled = true;
        try {
            const result = await moveMoney(account.accountNumber, action, amount);
            if (result.signedOut) {
                window.location.replace('/login.html');
                return;
            }
            if (!result.ok) {
                showCardMessage('error', result.message);
                input.focus();
                return;
            }
            applyUpdate(result.account);
            balanceEl.textContent = money.format(result.account.balance);
            close();
            showCardMessage('success', (action === 'deposit' ? 'Deposited ' : 'Withdrew ') + money.format(amount) + '.');
        } catch (error) {
            showCardMessage('error', 'The transaction failed: ' + error.message);
        } finally {
            submit.disabled = false;
            cancel.disabled = false;
        }
    }

    cancel.addEventListener('click', close);
    form.addEventListener('submit', (event) => {
        event.preventDefault();
        void submitAmount();
    });

    return { element: form, open };
}

function createCard(account) {
    const card = document.createElement('article');
    card.className = 'account-card';

    const balanceEl = textElement('p', 'account-balance', money.format(account.balance));
    const messageEl = textElement('p', 'card-message', '');
    messageEl.hidden = true;
    messageEl.setAttribute('role', 'status');

    const actionsEl = document.createElement('div');
    actionsEl.className = 'card-actions';
    const moneyForm = createMoneyForm(account, balanceEl, messageEl, actionsEl);

    const depositButton = textElement('button', 'button button-small', 'Deposit');
    depositButton.type = 'button';
    depositButton.addEventListener('click', () => moneyForm.open('deposit'));
    const withdrawButton = textElement('button', 'button button-small button-secondary', 'Withdraw');
    withdrawButton.type = 'button';
    withdrawButton.addEventListener('click', () => moneyForm.open('withdraw'));
    actionsEl.append(depositButton, withdrawButton);

    card.append(
        textElement('span', 'account-type', TYPE_LABELS[account.type] ?? account.type),
        textElement('h3', 'account-name', account.name),
        textElement('p', 'account-meta', 'No. ' + account.accountNumber + ' · ' + account.branch),
        balanceEl,
        actionsEl,
        moneyForm.element,
        messageEl
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

    currentAccounts = accounts;
    stateEl.hidden = true;
    accountsEl.replaceChildren(...accounts.map(createCard));
    updateTotal();
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
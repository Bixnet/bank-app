const statusEl = document.getElementById('status');
const totalEl = document.getElementById('total');
const accountsEl = document.getElementById('accounts');

const money = new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' });

const TYPE_LABELS = {
    SavingsAccount: 'Savings',
    CheckingAccount: 'Checking'
};

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

async function loadAccounts() {
    try {
        const response = await fetch('/accounts');
        if (!response.ok) {
            throw new Error('Server answered with status ' + response.status);
        }
        const accounts = await response.json();
        const total = accounts.reduce((sum, account) => sum + account.balance, 0);

        accountsEl.replaceChildren(...accounts.map(createCard));
        totalEl.textContent = money.format(total);
        statusEl.textContent = 'Across ' + accounts.length + (accounts.length === 1 ? ' account' : ' accounts');
    } catch (error) {
        statusEl.textContent = 'Could not load accounts: ' + error.message;
    }
}

await loadAccounts();
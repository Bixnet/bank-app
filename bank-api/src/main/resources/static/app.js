const statusEl = document.getElementById('status');
const accountsEl = document.getElementById('accounts');

const money = new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' });

function createCard(account) {
    const card = document.createElement('article');
    card.className = 'account-card';

    const title = document.createElement('h3');
    title.textContent = account.name;

    const details = document.createElement('p');
    details.textContent = account.type + ' · ' + account.accountNumber + ' · ' + account.branch;

    const balance = document.createElement('p');
    balance.className = 'balance';
    balance.textContent = money.format(account.balance);

    card.append(title, details, balance);
    return card;
}

async function loadAccounts() {
    try {
        const response = await fetch('/accounts');
        if (!response.ok) {
            throw new Error('Server answered with status ' + response.status);
        }
        const accounts = await response.json();
        accountsEl.replaceChildren(...accounts.map(createCard));
        statusEl.textContent = accounts.length + ' account(s)';
    } catch (error) {
        statusEl.textContent = 'Could not load accounts: ' + error.message;
    }
}

loadAccounts();
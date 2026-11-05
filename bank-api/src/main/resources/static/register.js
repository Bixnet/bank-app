import { login, register } from './api.js';

const form = document.getElementById('register-form');
const errorEl = document.getElementById('form-error');
const submitButton = document.getElementById('submit');

function showFormError(message) {
    errorEl.textContent = message;
    errorEl.hidden = false;
}

async function handleSubmit(event) {
    event.preventDefault();
    errorEl.hidden = true;

    const username = form.username.value.trim();
    const password = form.password.value;
    if (password !== form.confirm.value) {
        showFormError('The two passwords are not the same.');
        form.confirm.focus();
        return;
    }

    submitButton.disabled = true;
    submitButton.textContent = 'Creating account…';
    try {
        const result = await register(username, password);
        if (!result.ok) {
            showFormError(result.message);
            return;
        }
        // Account created: sign in right away, so the user lands on the dashboard
        const loggedIn = await login(username, password);
        window.location.replace(loggedIn ? '/' : '/login.html');
    } catch (error) {
        showFormError('Could not create the account: ' + error.message);
    } finally {
        submitButton.disabled = false;
        submitButton.textContent = 'Create account';
    }
}

form.addEventListener('submit', (event) => { void handleSubmit(event); });
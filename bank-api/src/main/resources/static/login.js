import { getCurrentUser, login } from './api.js';

const form = document.getElementById('login-form');
const errorEl = document.getElementById('form-error');
const submitButton = document.getElementById('submit');

function showFormError(message) {
    errorEl.textContent = message;
    errorEl.hidden = false;
}

async function handleSubmit(event) {
    event.preventDefault();
    errorEl.hidden = true;
    submitButton.disabled = true;
    submitButton.textContent = 'Signing in…';

    try {
        const loggedIn = await login(form.username.value.trim(), form.password.value);
        if (loggedIn) {
            window.location.replace('/');
            return;
        }
        showFormError('Wrong username or password.');
        form.password.value = '';
        form.password.focus();
    } catch (error) {
        showFormError('Could not sign in: ' + error.message);
    } finally {
        submitButton.disabled = false;
        submitButton.textContent = 'Sign in';
    }
}

form.addEventListener('submit', (event) => { void handleSubmit(event); });

// Already signed in? Then this page is not needed.
const user = await getCurrentUser().catch(() => null);
if (user) {
    window.location.replace('/');
}
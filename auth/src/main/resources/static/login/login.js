// =========================================
// LOGIN - Controlador
// =========================================
const alertBox = document.getElementById('alertBox');

// Toggle visibilidad de contraseña
function togglePass(inputId, btn) {
    const input = document.getElementById(inputId);
    const img = btn.querySelector('img');
    if (input.type === 'password') {
        input.type = 'text';
        img.src = '/icons/2.png';
        img.alt = 'ver';
    } else {
        input.type = 'password';
        img.src = '/icons/1.png';
        img.alt = 'ocultar';
    }
}

function showAlert(msg, type = 'danger') {
    alertBox.textContent = msg;
    alertBox.className = `alert alert-${type} show`;
}
function hideAlert() {
    alertBox.className = 'alert';
    alertBox.textContent = '';
}
function showPanel(panel) {
    hideAlert();
    document.getElementById('loginForm').style.display = panel === 'login' ? 'block' : 'none';
    document.getElementById('registerForm').style.display = panel === 'register' ? 'block' : 'none';
    document.getElementById('forgotForm').style.display = panel === 'forgot' ? 'block' : 'none';
}

// LOGIN
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault(); hideAlert();
    const btn = document.getElementById('loginBtn');
    btn.disabled = true; btn.textContent = 'Ingresando...';
    try {
        const data = await authAPI.login(
            document.getElementById('loginEmail').value,
            document.getElementById('loginPassword').value
        );
        saveTokens(data.accessToken, data.refreshToken);
        saveUser(data.user);
        window.location.href = '/dashboard/Dashboard.html';
    } catch (err) { showAlert(err.message); }
    finally { btn.disabled = false; btn.textContent = 'Iniciar Sesión'; }
});

// REGISTER
document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault(); hideAlert();
    const btn = document.getElementById('registerBtn');

    const pass = document.getElementById('regPassword').value;
    const confirm = document.getElementById('regConfirmPassword').value;

    if (pass !== confirm) {
        showAlert('Las contraseñas no coinciden');
        return;
    }

    btn.disabled = true; btn.textContent = 'Registrando...';
    try {
        await authAPI.register({
            firstName: document.getElementById('regFirstName').value,
            lastName: document.getElementById('regLastName').value,
            email: document.getElementById('regEmail').value,
            password: pass
        });
        showAlert('Registro exitoso. Revisa tu correo para verificar tu cuenta.', 'success');
        setTimeout(() => showPanel('login'), 2500);
    } catch (err) { showAlert(err.message); }
    finally { btn.disabled = false; btn.textContent = 'Registrarse'; }
});

// FORGOT
document.getElementById('forgotForm').addEventListener('submit', async (e) => {
    e.preventDefault(); hideAlert();
    const btn = document.getElementById('forgotBtn');
    btn.disabled = true; btn.textContent = 'Enviando...';
    try {
        const email = document.getElementById('forgotEmail').value;
        await authAPI.forgotPassword(email);
        document.getElementById('resetEmail').value = email;
        showAlert('Si el correo existe, recibirás un código.', 'success');
        setTimeout(() => showPanel('reset'), 1500);
    } catch (err) { showAlert(err.message); }
    finally { btn.disabled = false; btn.textContent = 'Enviar código'; }
});

if (isAuthenticated()) window.location.href = '/dashboard/Dashboard.html';

const params = new URLSearchParams(window.location.search);
const token = params.get('token');
const email = params.get('email');

const alertBox = document.getElementById('alertBox');
function showAlert(msg, type = 'danger') {
    alertBox.textContent = msg;
    alertBox.className = `alert alert-${type} show`;
}
function hideAlert() { alertBox.className = 'alert'; alertBox.textContent = ''; }

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

// Mostrar mensaje si faltan parámetros
if (!token || !email) {
    document.getElementById('resetForm').style.display = 'none';
    document.getElementById('statusMsg').innerHTML = `
        <h1>❌ Enlace inválido</h1>
        <p>El enlace de recuperación no es válido o está incompleto.</p>
        <a href="/login/Login.html" style="color:#2563eb;font-weight:600;">Volver al login</a>
    `;
} else {
    document.getElementById('userEmail').textContent = email;
}

document.getElementById('resetForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    hideAlert();

    const newPass = document.getElementById('newPassword').value;
    const confirmPass = document.getElementById('confirmPassword').value;

    if (newPass !== confirmPass) {
        showAlert('Las contraseñas no coinciden');
        return;
    }

    const btn = document.getElementById('resetBtn');
    btn.disabled = true; btn.textContent = 'Restableciendo...';

    try {
        await authAPI.resetPassword({
            email: email,
            token: token,
            newPassword: newPass
        });
        document.getElementById('resetForm').style.display = 'none';
        document.getElementById('statusMsg').innerHTML = `
            <h1>✅ ¡Contraseña restablecida!</h1>
            <p>Tu contraseña ha sido actualizada exitosamente.</p>
            <a href="/login/Login.html" style="display:inline-block;margin-top:16px;padding:12px 24px;
                background:#2563eb;color:#fff;border-radius:8px;font-weight:600;text-decoration:none;">
                Ir al Login
            </a>
        `;
    } catch (err) {
        showAlert(err.message);
    } finally {
        btn.disabled = false; btn.textContent = 'Restablecer Contraseña';
    }
});

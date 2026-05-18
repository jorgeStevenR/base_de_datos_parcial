// Obtener token de la URL
const params = new URLSearchParams(window.location.search);
const token = params.get('token');

const statusDiv = document.getElementById('status');

async function verificar() {
    if (!token) {
        statusDiv.innerHTML = `
            <div class="icon">❌</div>
            <h1>Token no válido</h1>
            <p>No se encontró un token de verificación en la URL. Revisa el enlace del correo.</p>
            <a href="/login/Login.html" class="btn btn-primary">Ir al Login</a>
        `;
        return;
    }

    try {
        const data = await authAPI.verifyEmail(token);
        statusDiv.innerHTML = `
            <div class="icon">✅</div>
            <h1>¡Correo verificado!</h1>
            <p>${data.message || 'Tu cuenta ha sido verificada exitosamente. Ahora puedes iniciar sesión.'}</p>
            <a href="/login/Login.html" class="btn btn-primary">Ir al Login</a>
        `;
    } catch (err) {
        statusDiv.innerHTML = `
            <div class="icon">❌</div>
            <h1>Error de verificación</h1>
            <p>${err.message}</p>
            <a href="/login/Login.html" class="btn btn-primary">Ir al Login</a>
        `;
    }
}

verificar();

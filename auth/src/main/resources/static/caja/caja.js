// =========================================
// CAJA - Controlador
// =========================================
if (!isAuthenticated()) window.location.href = '/login/Login.html';

const user = getUser();
document.getElementById('userInfo').textContent =
    user ? `${user.firstName || ''} ${user.lastName || ''} (${user.email})` : 'Usuario';

const alertBox = document.getElementById('alertBox');
function showAlert(msg, type = 'success') {
    alertBox.textContent = msg;
    alertBox.className = `alert alert-${type} show`;
    setTimeout(() => { alertBox.className = 'alert'; }, 4000);
}

async function cargarCaja() {
    try {
        const data = await cajaAPI.obtenerSaldo();
        document.getElementById('saldoTotal').textContent = `$${data.saldoTotal.toFixed(2)}`;
        document.getElementById('totalVentasCount').textContent = `${data.totalVentas} ventas realizadas`;

        const tbody = document.getElementById('cajaHistorialBody');
        if (data.historico.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:#64748b;">No hay ventas registradas</td></tr>';
        } else {
            tbody.innerHTML = data.historico.map(v => `
                <tr>
                    <td>${v.id}</td>
                    <td>${v.clienteNombre}</td>
                    <td>${v.clienteDocumento}</td>
                    <td>$${v.total.toFixed(2)}</td>
                    <td>${new Date(v.fechaVenta).toLocaleString('es-CO')}</td>
                </tr>
            `).join('');
        }
    } catch (err) {
        document.getElementById('saldoTotal').textContent = '$0.00';
        showAlert('Error: ' + err.message, 'danger');
    }
}

document.getElementById('btnDescargarHistorico').addEventListener('click', async () => {
    try {
        const blob = await cajaAPI.descargarHistorico();
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `historico_ventas_${Date.now()}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
    } catch (err) {
        showAlert('Error al descargar: ' + err.message, 'danger');
    }
});

function cerrarSesion() { clearTokens(); window.location.href = '/login/Login.html'; }
cargarCaja();

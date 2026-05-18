// =========================================
// DASHBOARD - Controlador
// =========================================
if (!isAuthenticated()) window.location.href = '/login/Login.html';

const user = getUser();
document.getElementById('userInfo').textContent =
    user ? `${user.firstName || ''} ${user.lastName || ''} (${user.email})` : 'Usuario';
document.getElementById('fechaActual').textContent =
    new Date().toLocaleDateString('es-CO', {
        weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
    });

async function cargarDashboard() {
    try {
        const [clientes, productos, ventas, caja] = await Promise.all([
            clientesAPI.listar(),
            productosAPI.listar(),
            ventasAPI.listar(),
            cajaAPI.obtenerSaldo()
        ]);
        document.getElementById('statClientes').textContent = clientes.length;
        document.getElementById('statProductos').textContent = productos.length;
        document.getElementById('statVentas').textContent = ventas.length;
        document.getElementById('statCaja').textContent = `$${caja.saldoTotal.toFixed(2)}`;

        const tbody = document.getElementById('ultimasVentasBody');
        const ultimas = ventas.slice(0, 5);
        if (ultimas.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:#64748b;">No hay ventas registradas</td></tr>';
        } else {
            tbody.innerHTML = ultimas.map(v => `
                <tr>
                    <td>${v.id}</td>
                    <td>${v.clienteNombre}</td>
                    <td>${v.clienteDocumento}</td>
                    <td>$${v.total.toFixed(2)}</td>
                    <td>${new Date(v.fechaVenta).toLocaleDateString('es-CO')}</td>
                </tr>
            `).join('');
        }
    } catch (err) { console.error(err); }
}

function cerrarSesion() { clearTokens(); window.location.href = '/login/Login.html'; }
cargarDashboard();

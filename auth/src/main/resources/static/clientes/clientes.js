// =========================================
// CLIENTES - Controlador
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

// Cargar tabla
async function cargarClientes() {
    const tbody = document.getElementById('clientesBody');
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:#64748b;">Cargando...</td></tr>';
    try {
        const data = await clientesAPI.listar();
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:#64748b;">No hay clientes registrados</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(c => `
            <tr>
                <td>${c.id}</td>
                <td>${c.documentNumber}</td>
                <td>${c.firstName} ${c.lastName}</td>
                <td>${c.email}</td>
                <td>${c.phone}</td>
                <td>${c.rutPdfNombre ? '<button class="btn btn-sm btn-warning" onclick="descargarPdf('+c.id+')">📄 Ver</button>' : '-'}</td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="verClientePdf(${c.id})">Ver PDF</button>
                    <button class="btn btn-sm btn-danger" onclick="eliminarClienteConfirm(${c.id})">Eliminar</button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;color:#dc2626;">Error: ${err.message}</td></tr>`;
    }
}

// Modal crear cliente
const modalCrear = document.getElementById('modalCrear');
function abrirModalCrear() { modalCrear.classList.add('active'); }
function cerrarModalCrear() { modalCrear.classList.remove('active'); }

document.getElementById('formCrearCliente').addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = document.getElementById('btnGuardarCliente');
    btn.disabled = true; btn.textContent = 'Guardando...';

    try {
        const formData = new FormData();
        formData.append('cliente', new Blob([JSON.stringify({
            documentNumber: document.getElementById('docNumber').value,
            firstName: document.getElementById('cliFirstName').value,
            lastName: document.getElementById('cliLastName').value,
            email: document.getElementById('cliEmail').value,
            phone: document.getElementById('cliPhone').value,
            address: document.getElementById('cliAddress').value
        })], { type: 'application/json' }));

        const archivo = document.getElementById('cliRut').files[0];
        if (archivo) formData.append('archivo', archivo);

        await clientesAPI.crear(formData);
        showAlert('Cliente creado exitosamente');
        cerrarModalCrear();
        document.getElementById('formCrearCliente').reset();
        cargarClientes();
    } catch (err) {
        showAlert(err.message, 'danger');
    } finally {
        btn.disabled = false; btn.textContent = 'Guardar Cliente';
    }
});

// Búsqueda por cédula
document.getElementById('btnBuscarCliente').addEventListener('click', async () => {
    const cedula = document.getElementById('searchCedula').value.trim();
    if (!cedula) { cargarClientes(); return; }
    const tbody = document.getElementById('clientesBody');
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:#64748b;">Buscando...</td></tr>';
    try {
        const c = await clientesAPI.buscarPorCedula(cedula);
        tbody.innerHTML = `
            <tr>
                <td>${c.id}</td>
                <td>${c.documentNumber}</td>
                <td>${c.firstName} ${c.lastName}</td>
                <td>${c.email}</td>
                <td>${c.phone}</td>
                <td>${c.rutPdfNombre ? '<button class="btn btn-sm btn-warning" onclick="descargarPdf('+c.id+')">📄 Ver</button>' : '-'}</td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="verClientePdf(${c.id})">👁️ Ver PDF</button>
                    <button class="btn btn-sm btn-danger" onclick="eliminarClienteConfirm(${c.id})">🗑️ Eliminar</button>
                </td>
            </tr>
        `;
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;color:#dc2626;">${err.message}</td></tr>`;
    }
});

document.getElementById('searchCedula').addEventListener('keyup', (e) => {
    if (e.key === 'Enter') document.getElementById('btnBuscarCliente').click();
});

async function descargarPdf(id) {
    try {
        const response = await fetch(`http://localhost:8080/api/clientes/${id}/pdf`, {
            headers: { 'Authorization': `Bearer ${getToken()}` }
        });
        if (!response.ok) { const e = await response.json(); throw new Error(e.message || 'Error'); }
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
    } catch (err) {
        showAlert('Error al descargar PDF: ' + err.message, 'danger');
    }
}

// Ver PDF del cliente
async function verClientePdf(id) {
    try {
        const blob = await clientesAPI.generarClientePdf(id);
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
    } catch (err) {
        showAlert('Error al generar PDF: ' + err.message, 'danger');
    }
}

// Confirmar eliminación de cliente
function eliminarClienteConfirm(id) {
    if (confirm('¿Está seguro de que desea eliminar este cliente? Esta acción no se puede deshacer.')) {
        eliminarCliente(id);
    }
}

// Eliminar cliente
async function eliminarCliente(id) {
    try {
        await clientesAPI.eliminar(id);
        showAlert('Cliente eliminado exitosamente', 'success');
        cargarClientes();
    } catch (err) {
        showAlert('Error al eliminar cliente: ' + err.message, 'danger');
    }
}

function verCliente(id) { /* placeholder */ }

function cerrarSesion() { clearTokens(); window.location.href = '/login/Login.html'; }
cargarClientes();

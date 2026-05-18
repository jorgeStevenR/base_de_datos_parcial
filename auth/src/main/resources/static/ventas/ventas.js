// =========================================
// VENTAS - Controlador
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

// Estado de la venta actual
let productosDisponibles = [];
let detallesVenta = [];
let clienteSeleccionado = null;

// =====================
// INICIALIZAR
// =====================
async function init() {
    await Promise.all([cargarProductos(), cargarHistorialVentas()]);
}

// =====================
// PRODUCTOS SELECT
// =====================
async function cargarProductos() {
    try {
        productosDisponibles = await productosAPI.listar();
        const selects = document.querySelectorAll('.select-producto');
        selects.forEach(sel => {
            sel.innerHTML = '<option value="">-- Seleccionar --</option>' +
                productosDisponibles.map(p =>
                    `<option value="${p.id}" data-precio="${p.precio}" data-stock="${p.stock}">${p.nombre} (Stock: ${p.stock})</option>`
                ).join('');
        });
    } catch (err) { showAlert('Error cargando productos: ' + err.message, 'danger'); }
}

// =====================
// BUSCAR CLIENTE
// =====================
document.getElementById('btnBuscarClienteVenta').addEventListener('click', async () => {
    const cedula = document.getElementById('ventaDocCliente').value.trim();
    if (!cedula) {
        clienteSeleccionado = null;
        document.getElementById('clienteInfo').innerHTML = '';
        return;
    }
    try {
        const c = await clientesAPI.buscarPorCedula(cedula);
        clienteSeleccionado = c;
        document.getElementById('clienteInfo').innerHTML = `
            <div><strong>Cliente:</strong> ${c.firstName} ${c.lastName}</div>
            <div><strong>Cédula:</strong> ${c.documentNumber}</div>
            <div><strong>Email:</strong> ${c.email}</div>
            <div><strong>Teléfono:</strong> ${c.phone}</div>
        `;
    } catch (err) {
        clienteSeleccionado = null;
        document.getElementById('clienteInfo').innerHTML =
            `<div style="color:#dc2626;">${err.message}</div>`;
    }
});

// =====================
// AGREGAR / QUITAR DETALLE
// =====================
function agregarDetalle() {
    const select = document.querySelector('.select-producto');
    const cantidadInput = document.getElementById('detalleCantidad');

    const productoId = parseInt(select.value);
    const cantidad = parseInt(cantidadInput.value);

    if (!productoId) { showAlert('Selecciona un producto', 'danger'); return; }
    if (!cantidad || cantidad < 1) { showAlert('Cantidad inválida', 'danger'); return; }

    const producto = productosDisponibles.find(p => p.id === productoId);
    if (!producto) return;
    if (cantidad > producto.stock) {
        showAlert(`Stock insuficiente para "${producto.nombre}". Disponible: ${producto.stock}`, 'danger');
        return;
    }

    // Verificar si ya está agregado
    const existente = detallesVenta.find(d => d.productoId === productoId);
    if (existente) {
        const nuevaCant = existente.cantidad + cantidad;
        if (nuevaCant > producto.stock) {
            showAlert(`Stock insuficiente. Ya tienes ${existente.cantidad} de "${producto.nombre}"`, 'danger');
            return;
        }
        existente.cantidad = nuevaCant;
        existente.subtotal = existente.precioUnitario * nuevaCant;
    } else {
        detallesVenta.push({
            productoId: producto.id,
            productoNombre: producto.nombre,
            cantidad: cantidad,
            precioUnitario: producto.precio,
            subtotal: producto.precio * cantidad
        });
    }

    select.value = '';
    cantidadInput.value = '1';
    renderDetalles();
}

function quitarDetalle(index) {
    detallesVenta.splice(index, 1);
    renderDetalles();
}

function renderDetalles() {
    const container = document.getElementById('detallesContainer');
    if (detallesVenta.length === 0) {
        container.innerHTML = '<p style="color:#64748b;font-size:0.9rem;">Agrega productos a la venta</p>';
        document.getElementById('totalVenta').textContent = '$0.00';
        return;
    }
    container.innerHTML = detallesVenta.map((d, i) => `
        <div class="detalle-item">
            <span style="flex:2;font-weight:600;">${d.productoNombre}</span>
            <span style="flex:1;">Cant: ${d.cantidad}</span>
            <span style="flex:1;">$${d.precioUnitario.toFixed(2)}</span>
            <span style="flex:1;font-weight:600;">$${d.subtotal.toFixed(2)}</span>
            <button class="btn btn-sm btn-danger" onclick="quitarDetalle(${i})">✕</button>
        </div>
    `).join('');
    const total = detallesVenta.reduce((sum, d) => sum + d.subtotal, 0);
    document.getElementById('totalVenta').textContent = `$${total.toFixed(2)}`;
}

// =====================
// CREAR VENTA
// =====================
document.getElementById('btnRealizarVenta').addEventListener('click', async () => {
    if (detallesVenta.length === 0) {
        showAlert('Debes agregar al menos un producto', 'danger');
        return;
    }

    const btn = document.getElementById('btnRealizarVenta');
    btn.disabled = true; btn.textContent = 'Procesando...';

    try {
        const request = {
            clienteId: clienteSeleccionado ? clienteSeleccionado.id : null,
            documentNumber: clienteSeleccionado ? clienteSeleccionado.documentNumber : null,
            detalles: detallesVenta.map(d => ({
                productoId: d.productoId,
                cantidad: d.cantidad
            }))
        };

        const venta = await ventasAPI.crear(request);

        showAlert(`✅ Venta #${venta.id} realizada exitosamente por $${venta.total.toFixed(2)}`);

        // Reiniciar
        clienteSeleccionado = null;
        detallesVenta = [];
        document.getElementById('clienteInfo').innerHTML = '';
        document.getElementById('ventaDocCliente').value = '';
        renderDetalles();
        await Promise.all([cargarProductos(), cargarHistorialVentas()]);
    } catch (err) {
        showAlert('Error: ' + err.message, 'danger');
    } finally {
        btn.disabled = false; btn.textContent = '💰 Realizar Venta';
    }
});

// =====================
// HISTORIAL DE VENTAS
// =====================
async function cargarHistorialVentas() {
    const tbody = document.getElementById('ventasHistorialBody');
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#64748b;">Cargando...</td></tr>';
    try {
        const data = await ventasAPI.listar();
        if (data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#64748b;">No hay ventas registradas</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(v => `
            <tr>
                <td>${v.id}</td>
                <td>${v.clienteNombre}</td>
                <td>${v.clienteDocumento}</td>
                <td>$${v.total.toFixed(2)}</td>
                <td>${new Date(v.fechaVenta).toLocaleString('es-CO')}</td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="descargarFactura(${v.id})">📄</button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:#dc2626;">Error: ${err.message}</td></tr>`;
    }
}

async function descargarFactura(id) {
    try {
        const blob = await ventasAPI.descargarFactura(id);
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
    } catch (err) {
        showAlert('Error al descargar factura: ' + err.message, 'danger');
    }
}

function cerrarSesion() { clearTokens(); window.location.href = '/login/Login.html'; }
init();

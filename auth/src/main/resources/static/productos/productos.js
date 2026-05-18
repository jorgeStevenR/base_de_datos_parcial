// =========================================
// PRODUCTOS - Controlador
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

let productosData = [];

async function cargarProductos() {
    const tbody = document.getElementById('productosBody');
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#64748b;">Cargando...</td></tr>';
    try {
        productosData = await productosAPI.listar();
        if (productosData.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#64748b;">No hay productos registrados</td></tr>';
            return;
        }
        renderTabla(productosData);
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;color:#dc2626;">Error: ${err.message}</td></tr>`;
    }
}

function renderTabla(data) {
    const tbody = document.getElementById('productosBody');
    tbody.innerHTML = data.map(p => `
        <tr>
            <td>${p.id}</td>
            <td>${p.nombre}</td>
            <td>${p.descripcion || '-'}</td>
            <td>$${p.precio.toFixed(2)}</td>
            <td>
                <span style="padding:4px 8px;border-radius:4px;font-weight:600;
                    ${p.stock > 10 ? 'background:#dcfce7;color:#166534;' :
                      p.stock > 0 ? 'background:#fef3c7;color:#92400e;' :
                      'background:#fee2e2;color:#991b1b;'}">
                    ${p.stock}
                </span>
            </td>
            <td>
                <button class="btn btn-sm btn-warning" onclick="editarProducto(${p.id})">✏️</button>
                <button class="btn btn-sm btn-danger" onclick="eliminarProducto(${p.id})">🗑️</button>
            </td>
        </tr>
    `).join('');
}

// Filtrar por nombre
document.getElementById('searchProducto').addEventListener('keyup', (e) => {
    const q = e.target.value.toLowerCase();
    const filtrados = productosData.filter(p =>
        p.nombre.toLowerCase().includes(q) || (p.descripcion && p.descripcion.toLowerCase().includes(q))
    );
    renderTabla(filtrados);
});

// MODAL CREAR
const modalProducto = document.getElementById('modalProducto');
const modalTitle = document.getElementById('modalProductoTitle');
let editingId = null;

function abrirModalCrear() {
    editingId = null;
    modalTitle.textContent = 'Nuevo Producto';
    document.getElementById('prodNombre').value = '';
    document.getElementById('prodDesc').value = '';
    document.getElementById('prodPrecio').value = '';
    document.getElementById('prodStock').value = '';
    modalProducto.classList.add('active');
}

function abrirModalEditar(p) {
    editingId = p.id;
    modalTitle.textContent = 'Editar Producto';
    document.getElementById('prodNombre').value = p.nombre;
    document.getElementById('prodDesc').value = p.descripcion || '';
    document.getElementById('prodPrecio').value = p.precio;
    document.getElementById('prodStock').value = p.stock;
    modalProducto.classList.add('active');
}

function cerrarModalProducto() {
    modalProducto.classList.remove('active');
    editingId = null;
}

document.getElementById('formProducto').addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = {
        nombre: document.getElementById('prodNombre').value,
        descripcion: document.getElementById('prodDesc').value,
        precio: parseFloat(document.getElementById('prodPrecio').value),
        stock: parseInt(document.getElementById('prodStock').value)
    };
    const btn = document.getElementById('btnGuardarProducto');
    btn.disabled = true; btn.textContent = 'Guardando...';
    try {
        if (editingId) {
            await productosAPI.actualizar(editingId, data);
            showAlert('Producto actualizado');
        } else {
            await productosAPI.crear(data);
            showAlert('Producto creado');
        }
        cerrarModalProducto();
        cargarProductos();
    } catch (err) { showAlert(err.message, 'danger'); }
    finally { btn.disabled = false; btn.textContent = 'Guardar'; }
});

function editarProducto(id) {
    const p = productosData.find(x => x.id === id);
    if (p) abrirModalEditar(p);
}

async function eliminarProducto(id) {
    if (!confirm('¿Eliminar este producto?')) return;
    try {
        await productosAPI.eliminar(id);
        showAlert('Producto eliminado');
        cargarProductos();
    } catch (err) { showAlert(err.message, 'danger'); }
}

function cerrarSesion() { clearTokens(); window.location.href = '/login/Login.html'; }
cargarProductos();

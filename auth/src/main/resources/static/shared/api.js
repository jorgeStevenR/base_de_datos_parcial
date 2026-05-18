// ============================================================
// API SERVICE - Capa de comunicación con el backend
// ============================================================
const API_BASE = 'http://localhost:8080/api';

function getToken() {
    return localStorage.getItem('accessToken');
}
function getRefreshToken() {
    return localStorage.getItem('refreshToken');
}
function saveTokens(at, rt) {
    localStorage.setItem('accessToken', at);
    localStorage.setItem('refreshToken', rt);
}
function clearTokens() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
}
function isAuthenticated() {
    return !!getToken();
}
function getUser() {
    const u = localStorage.getItem('user');
    return u ? JSON.parse(u) : null;
}
function saveUser(u) {
    localStorage.setItem('user', JSON.stringify(u));
}

async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;
    const token = getToken();
    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
            ...options.headers
        },
        ...options
    };
    if (options.body instanceof FormData) {
        delete config.headers['Content-Type'];
    }
    try {
        const response = await fetch(url, config);
        if (response.status === 401 && getRefreshToken()) {
            const refreshed = await refreshAccessToken();
            if (refreshed) {
                config.headers['Authorization'] = `Bearer ${getToken()}`;
                const retry = await fetch(url, config);
                return handleResponse(retry);
            } else {
                clearTokens();
                window.location.href = '/login/Login.html';
                throw new Error('Sesión expirada');
            }
        }
        return handleResponse(response);
    } catch (error) {
        if (error.message === 'Sesión expirada') throw error;
        console.error('API Error:', error);
        throw new Error('Error de conexión con el servidor');
    }
}

async function handleResponse(response) {
    const ct = response.headers.get('content-type') || '';
    if (!response.ok) {
        let msg = `Error ${response.status}`;
        try {
            if (ct.includes('application/json')) {
                const d = await response.json();
                msg = d.message || msg;
            } else {
                msg = await response.text();
            }
        } catch (e) {}
        throw new Error(msg);
    }
    if (ct.includes('application/pdf')) return response.blob();
    if (ct.includes('application/json')) {
        const text = await response.text();
        return text ? JSON.parse(text) : null;
    }
    return response.text();
}

async function refreshAccessToken() {
    try {
        const r = await fetch(`${API_BASE}/auth/refresh`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken: getRefreshToken() })
        });
        if (!r.ok) return false;
        const d = await r.json();
        saveTokens(d.accessToken, d.refreshToken);
        return true;
    } catch (e) { return false; }
}

// ---------- AUTH ----------
const authAPI = {
    login: (e, p) => apiRequest('/auth/login', { method: 'POST', body: JSON.stringify({ email: e, password: p }) }),
    register: (d) => apiRequest('/auth/register', { method: 'POST', body: JSON.stringify(d) }),
    forgotPassword: (e) => apiRequest('/auth/forgot-password', { method: 'POST', body: JSON.stringify({ email: e }) }),
    resetPassword: (d) => apiRequest('/auth/reset-password', { method: 'POST', body: JSON.stringify(d) }),
    verifyEmail: (t) => apiRequest(`/auth/verify-email?token=${t}`)
};

// ---------- CLIENTES ----------
const clientesAPI = {
    listar: () => apiRequest('/clientes'),
    crear: (fd) => apiRequest('/clientes', { method: 'POST', body: fd }),
    buscarPorCedula: (c) => apiRequest(`/clientes/buscar/${encodeURIComponent(c)}`)
};

// ---------- PRODUCTOS ----------
const productosAPI = {
    listar: () => apiRequest('/productos'),
    obtener: (id) => apiRequest(`/productos/${id}`),
    crear: (d) => apiRequest('/productos', { method: 'POST', body: JSON.stringify(d) }),
    actualizar: (id, d) => apiRequest(`/productos/${id}`, { method: 'PUT', body: JSON.stringify(d) }),
    eliminar: (id) => apiRequest(`/productos/${id}`, { method: 'DELETE' })
};

// ---------- VENTAS ----------
const ventasAPI = {
    listar: () => apiRequest('/ventas'),
    obtener: (id) => apiRequest(`/ventas/${id}`),
    crear: (d) => apiRequest('/ventas', { method: 'POST', body: JSON.stringify(d) }),
    descargarFactura: (id) => apiRequest(`/ventas/${id}/factura`)
};

// ---------- CAJA ----------
const cajaAPI = {
    obtenerSaldo: () => apiRequest('/caja/saldo'),
    descargarHistorico: () => apiRequest('/caja/historico/pdf')
};

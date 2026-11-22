/* SINDACO - Frontend conectado a la API REST (arquitectura N capas: esta es la capa de presentación). */
const API = "/api";
const TOKEN_KEY = "sindaco_token";
const USER_KEY = "sindaco_user";

const TITULOS = {
  dashboard: "Dashboard",
  alumnos: "Alumnos",
  matriculas: "Matrículas",
  pagos: "Pagos",
  paquetes: "Paquetes",
  promociones: "Promociones",
  clases: "Clases",
  vehiculos: "Vehículos",
  instructores: "Instructores",
  asistencia: "Asistencia",
  mantenimiento: "Mantenimiento",
  ausencias: "Ausencias y reprogramaciones",
  reportes: "Reportes",
  historial: "Historial",
  notificaciones: "Notificaciones"
};

const PERMISOS = {
  dashboard: ["PROPIETARIO", "ADMINISTRADOR", "RECEPCIONISTA", "ENCARGADO_FLOTAS", "INSTRUCTOR", "ALUMNO"],
  matriculas: ["ADMINISTRADOR", "RECEPCIONISTA", "ALUMNO"],
  pagos: ["ADMINISTRADOR"],
  clases: ["ADMINISTRADOR", "INSTRUCTOR"],
  vehiculos: ["ADMINISTRADOR", "INSTRUCTOR", "ENCARGADO_FLOTAS"],
  instructores: ["ADMINISTRADOR", "INSTRUCTOR", "ENCARGADO_FLOTAS"],
  asistencia: ["ADMINISTRADOR", "INSTRUCTOR"],
  reportes: ["ADMINISTRADOR", "PROPIETARIO"],
  mantenimiento: ["ADMINISTRADOR", "INSTRUCTOR", "ENCARGADO_FLOTAS"],
  paquetes: ["ADMINISTRADOR", "RECEPCIONISTA", "PROPIETARIO"],
  promociones: ["ADMINISTRADOR", "RECEPCIONISTA", "PROPIETARIO"],
  ausencias: ["ADMINISTRADOR", "ALUMNO"],
  historial: ["ADMINISTRADOR", "ALUMNO"],
  alumnos: ["ADMINISTRADOR", "RECEPCIONISTA", "INSTRUCTOR", "ALUMNO"],
  notificaciones: ["PROPIETARIO", "ADMINISTRADOR", "RECEPCIONISTA", "ENCARGADO_FLOTAS", "INSTRUCTOR", "ALUMNO"]
};

const ROLES_LOGIN = {
  propietario: "PROPIETARIO",
  administrador: "ADMINISTRADOR",
  recepcionista: "RECEPCIONISTA",
  encargado_flotas: "ENCARGADO_FLOTAS",
  instructor: "INSTRUCTOR",
  alumno: "ALUMNO"
};

const ENTIDADES = {
  alumnos: { endpoint: "/alumnos", form: "alumnoForm", render: renderAlumnos },
  matriculas: { endpoint: "/matriculas", form: "matriculaForm", render: renderMatriculas },
  pagos: { endpoint: "/pagos", form: "pagoForm", render: renderPagos },
  paquetes: { endpoint: "/paquetes", form: "paqueteForm", render: renderPaquetes },
  promociones: { endpoint: "/promociones", form: "promocionForm", render: renderPromociones },
  clases: { endpoint: "/clases", form: "claseForm", render: renderClases },
  vehiculos: { endpoint: "/vehiculos", form: "vehiculoForm", render: renderVehiculos },
  instructores: { endpoint: "/instructores", form: "instructorForm", render: renderInstructores },
  asistencias: { endpoint: "/asistencias", form: "asistenciaForm", render: renderAsistencia },
  mantenimientos: { endpoint: "/mantenimientos", form: "mantenimientoForm", render: renderMantenimiento },
  reprogramaciones: { endpoint: "/reprogramaciones", form: "ausenciaForm", render: renderAusencias }
};

const state = {
  currentSection: "dashboard",
  currentUser: null,
  token: localStorage.getItem(TOKEN_KEY),
  filtros: { desde: "", hasta: "" }
};

/* ---------- Utilidades ---------- */
function esc(valor) {
  return String(valor ?? "").replace(/[&<>"']/g, c => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  })[c]);
}

function hoy() {
  return new Date().toISOString().split("T")[0];
}

function dinero(valor) {
  return `S/ ${Number(valor || 0).toFixed(2)}`;
}

function etiqueta(valor) {
  return String(valor ?? "-").replaceAll("_", " ").toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
}

function estadoHtml(valor) {
  return `<span class="status ${String(valor ?? "").toLowerCase()}">${esc(etiqueta(valor))}</span>`;
}

function datosFormulario(form) {
  return Object.fromEntries(new FormData(form).entries());
}

function notificar(mensaje, tipo = "success") {
  const root = document.getElementById("toastRoot");
  if (!root) return;
  const toast = document.createElement("div");
  toast.className = `toast ${tipo}`;
  toast.textContent = mensaje;
  root.appendChild(toast);
  setTimeout(() => toast.remove(), 4500);
}

function mostrarModal(titulo, contenido) {
  const root = document.getElementById("modalRoot");
  root.innerHTML = `
    <div class="modal-backdrop" data-action="cerrar-modal">
      <div class="modal-card" role="dialog" aria-modal="true">
        <div class="table-header"><h3>${esc(titulo)}</h3>
          <button class="mini-btn" data-action="cerrar-modal">Cerrar</button></div>
        ${contenido}
      </div>
    </div>`;
}

/* ---------- Cliente HTTP (capa de acceso a la API) ---------- */
async function api(path, opciones = {}) {
  const headers = { Accept: opciones.accept || "application/json" };
  if (state.token) headers.Authorization = `Bearer ${state.token}`;
  if (opciones.body !== undefined) headers["Content-Type"] = "application/json";

  const respuesta = await fetch(API + path, {
    method: opciones.method || "GET",
    headers,
    body: opciones.body !== undefined ? JSON.stringify(opciones.body) : undefined
  });

  let datos = null;
  const texto = await respuesta.text();
  if (texto) {
    try { datos = JSON.parse(texto); } catch { datos = texto; }
  }

  if (!respuesta.ok) {
    if (respuesta.status === 401 && state.token) {
      cerrarSesion(true);
      notificar("Su sesión expiró. Inicie sesión nuevamente.", "error");
    }
    const mensaje = datos && datos.mensaje ? datos.mensaje : `Error ${respuesta.status}`;
    const detalles = datos && datos.detalles && datos.detalles.length ? ` (${datos.detalles.join("; ")})` : "";
    throw new Error(mensaje + detalles);
  }
  return datos;
}

/* ---------- Autenticación ---------- */
function cerrarSesion(silent = false) {
  state.token = null;
  state.currentUser = null;
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  document.getElementById("loginView").classList.remove("hidden");
  document.getElementById("appView").classList.add("hidden");
  document.getElementById("loginForm").reset();
  const msg = document.getElementById("loginMessage");
  msg.textContent = "";
  msg.className = "login-message";
  if (!silent) notificar("Sesión cerrada correctamente");
}

function mostrarApp() {
  document.getElementById("loginView").classList.add("hidden");
  document.getElementById("appView").classList.remove("hidden");
  aplicarPermisos();
  renderHeader();
  renderSection();
}

function aplicarPermisos() {
  const rol = state.currentUser?.rol;
  document.querySelectorAll(".nav-item").forEach(btn => {
    const permitido = (PERMISOS[btn.dataset.section] || []).includes(rol);
    btn.classList.toggle("hidden", !permitido);
  });
}

function renderHeader() {
  if (!state.currentUser) return;
  document.getElementById("userBadge").textContent =
    `${state.currentUser.nombre} (${etiqueta(state.currentUser.rol)})`;
  document.getElementById("pageTitle").textContent = TITULOS[state.currentSection] || "Dashboard";
}

function setSection(seccion) {
  if (!(PERMISOS[seccion] || []).includes(state.currentUser?.rol)) {
    notificar("No tiene permiso para acceder a esa sección", "error");
    return;
  }
  state.currentSection = seccion;
  document.querySelectorAll(".nav-item").forEach(btn =>
    btn.classList.toggle("active", btn.dataset.section === seccion));
  renderHeader();
  renderSection();
}

/* ---------- Render genérico ---------- */
function contenedor() {
  return document.getElementById("viewContent");
}

function cargando() {
  contenedor().innerHTML = `<div class="loading-state"><span class="spinner"></span> Cargando información...</div>`;
}

async function renderSection() {
  if (!state.currentUser) return;
  cargando();
  try {
    const vistas = {
      dashboard: renderDashboard, alumnos: renderAlumnos, matriculas: renderMatriculas,
      pagos: renderPagos, paquetes: renderPaquetes, promociones: renderPromociones,
      clases: renderClases, vehiculos: renderVehiculos, instructores: renderInstructores,
      asistencia: renderAsistencia, mantenimiento: renderMantenimiento,
      ausencias: renderAusencias, reportes: renderReportes, historial: renderHistorial,
      notificaciones: renderNotificaciones
    };
    await (vistas[state.currentSection] || renderDashboard)();
  } catch (error) {
    contenedor().innerHTML = `<div class="empty-state">No se pudo cargar la información: ${esc(error.message)}</div>`;
    notificar(error.message, "error");
  }
}

function opciones(items, texto, valor = "id", seleccionado = "") {
  return `<option value="">Seleccione</option>` + items.map(item =>
    `<option value="${esc(item[valor])}" ${String(item[valor]) === String(seleccionado) ? "selected" : ""}>${esc(texto(item))}</option>`
  ).join("");
}

function tablaVacia(columnas, mensaje) {
  return `<tr><td colspan="${columnas}"><div class="empty-state">${esc(mensaje)}</div></td></tr>`;
}

function botonesEditarEliminar(entidad, id) {
  return `
    <button class="edit-btn" data-action="edit" data-entidad="${entidad}" data-id="${id}">Editar</button>
    <button class="delete-btn" data-action="delete" data-entidad="${entidad}" data-id="${id}">Eliminar</button>`;
}

/* ---------- Dashboard ---------- */
async function renderDashboard() {
  const reporteVacio = { totalIngresos: 0, totalPagos: 0, promedioPago: 0, porMetodo: [], porMes: [] };
  const [reporte, alumnos, clases, matriculas, paquetes, mantenimientos] = await Promise.all([
    api("/reportes/ingresos").catch(() => reporteVacio),
    api("/alumnos").catch(() => []),
    api("/clases").catch(() => []),
    api("/matriculas").catch(() => []),
    api("/paquetes").catch(() => []),
    api("/mantenimientos").catch(() => [])
  ]);
  const nombreAlumno = id => {
    const a = alumnos.find(x => x.id === id);
    return a ? `${a.nombre} ${a.apellidos}` : "Sin alumno";
  };

  contenedor().innerHTML = `
    <section class="stats-grid">
      <article class="stat-card blue"><small>Alumnos</small><h3>${alumnos.length}</h3><span>Registrados</span></article>
      <article class="stat-card"><small>Clases</small><h3>${clases.length}</h3><span>Programadas</span></article>
      <article class="stat-card"><small>Pagos</small><h3>${reporte.totalPagos}</h3><span>Registrados</span></article>
      <article class="stat-card"><small>Ingresos</small><h3>${dinero(reporte.totalIngresos)}</h3><span>Totales</span></article>
      <article class="stat-card"><small>Paquetes</small><h3>${paquetes.filter(p => p.vigente).length}</h3><span>Vigentes</span></article>
      <article class="stat-card"><small>Mantenimiento</small><h3>${mantenimientos.length}</h3><span>Registros</span></article>
    </section>

    <section class="content-grid">
      <div class="panel large">
        <div class="panel-header"><h3>Resumen de matrículas</h3></div>
        <table><thead><tr><th>Alumno</th><th>Paquete</th><th>Estado</th><th>Fecha</th></tr></thead>
          <tbody>${matriculas.length ? matriculas.map(m => `
            <tr><td>${esc(nombreAlumno(m.alumnoId))}</td><td>${esc(m.paquete)}</td>
            <td>${estadoHtml(m.estado)}</td><td>${esc(m.fecha)}</td></tr>`).join("")
            : tablaVacia(4, "No hay matrículas aún.")}</tbody></table>
      </div>
      <div class="panel">
        <div class="panel-header"><h3>Ingresos por método</h3></div>
        <ul class="list-compact">${reporte.porMetodo.length ? reporte.porMetodo.map(x =>
          `<li><strong>${esc(etiqueta(x.metodo))}</strong><span>${dinero(x.monto)}</span></li>`).join("")
          : `<li><span>No hay pagos registrados.</span></li>`}</ul>
      </div>
    </section>

    <section class="content-grid">
      <div class="panel">
        <div class="panel-header"><h3>Próximas clases</h3></div>
        <ul class="list-compact">${clases.length ? clases.slice(0, 6).map(c =>
          `<li><strong>${esc(c.nombre)}</strong><span>${esc(c.fecha)} ${esc(String(c.horaInicio || "").slice(0, 5))}</span></li>`).join("")
          : `<li><span>No hay clases.</span></li>`}</ul>
      </div>
      <div class="panel">
        <div class="panel-header"><h3>Requerimientos funcionales</h3></div>
        <div class="requirements-grid">
          <div class="requirement-item"><span>RF-01</span><p>Control de acceso por roles.</p></div>
          <div class="requirement-item"><span>RF-02</span><p>Registro de alumnos y paquetes.</p></div>
          <div class="requirement-item"><span>RF-03</span><p>Gestión de matrículas y pagos.</p></div>
          <div class="requirement-item"><span>RF-04</span><p>Programación de clases y recursos.</p></div>
          <div class="requirement-item"><span>RF-05</span><p>Asistencia y evaluación.</p></div>
          <div class="requirement-item"><span>RF-06</span><p>Mantenimiento y ausencias.</p></div>
          <div class="requirement-item"><span>RF-07</span><p>Reportes e historial.</p></div>
          <div class="requirement-item"><span>RF-08</span><p>Promociones y paquetes.</p></div>
        </div>
      </div>
    </section>`;
}

/* ---------- Alumnos ---------- */
async function renderAlumnos() {
  const alumnos = await api("/alumnos");
  contenedor().innerHTML = `
    <div class="section-shell">
      <div class="form-card"><h3>Registrar alumno</h3>
        <form id="alumnoForm">
          <div class="form-grid">
            <div class="form-group"><label>Nombre</label><input name="nombre" required /></div>
            <div class="form-group"><label>Apellidos</label><input name="apellidos" required /></div>
            <div class="form-group"><label>DNI</label><input name="dni" required /></div>
            <div class="form-group"><label>Email</label><input name="email" type="email" /></div>
            <div class="form-group"><label>Teléfono</label><input name="telefono" /></div>
            <div class="form-group"><label>Fecha registro</label><input name="fechaRegistro" type="date" value="${hoy()}" /></div>
            <div class="form-group" style="grid-column: span 3;"><label>Dirección</label><textarea name="direccion" rows="2"></textarea></div>
          </div>
          <div class="form-actions"><button type="submit" class="action-btn">Guardar alumno</button>
            <button type="button" class="mini-btn" data-action="limpiar" data-form="alumnoForm">Limpiar</button></div>
        </form>
      </div>
      <div class="data-table-card"><div class="table-header"><h3>Listado de alumnos</h3></div>
        <table><thead><tr><th>Nombre</th><th>DNI</th><th>Email</th><th>Teléfono</th><th>Acciones</th></tr></thead>
          <tbody>${alumnos.length ? alumnos.map(a => `
            <tr><td>${esc(a.nombre)} ${esc(a.apellidos)}</td><td>${esc(a.dni)}</td><td>${esc(a.email)}</td>
            <td>${esc(a.telefono)}</td><td>${botonesEditarEliminar("alumnos", a.id)}</td></tr>`).join("")
            : tablaVacia(5, "No hay alumnos registrados.")}</tbody></table>
      </div>
    </div>`;
}

/* ---------- Matrículas ---------- */
async function renderMatriculas() {
  const [matriculas, alumnos, paquetes] = await Promise.all([api("/matriculas"), api("/alumnos"), api("/paquetes?soloVigentes=true")]);
  contenedor().innerHTML = `
    <div class="section-shell">
      <div class="form-card"><h3>Registrar matrícula</h3>
        <form id="matriculaForm">
          <div class="form-grid two">
            <div class="form-group"><label>Alumno</label><select name="alumnoId" required>${opciones(alumnos, a => `${a.nombre} ${a.apellidos}`)}</select></div>
            <div class="form-group"><label>Paquete</label><select name="paqueteId" required>${opciones(paquetes, p => `${p.nombre} - ${dinero(p.precio)}`)}</select></div>
            <div class="form-group"><label>Fecha</label><input name="fecha" type="date" value="${hoy()}" required /></div>
            <div class="form-group"><label>Estado</label><select name="estado">
              <option value="POR_PAGAR">Por pagar</option><option value="CONFIRMADA">Confirmada</option>
              <option value="CANCELADA">Cancelada</option><option value="VENCIDA">Vencida</option></select></div>
            <div class="form-group"><label>Código de promoción (opcional)</label><input name="codigoPromocion" placeholder="Ej. PROMO2026" /></div>
          </div>
          <div class="form-actions"><button type="submit" class="action-btn">Guardar matrícula</button>
            <button type="button" class="mini-btn" data-action="limpiar" data-form="matriculaForm">Limpiar</button></div>
        </form>
      </div>
      <div class="data-table-card"><div class="table-header"><h3>Listado de matrículas</h3></div>
        <table><thead><tr><th>Alumno</th><th>Paquete</th><th>Estado</th><th>Fecha</th><th>Precio final</th><th>Descuento</th><th>Promoción</th><th>Acciones</th></tr></thead>
          <tbody>${matriculas.length ? matriculas.map(m => `
            <tr><td>${esc(m.alumnoNombre)}</td><td>${esc(m.paquete)}</td><td>${estadoHtml(m.estado)}</td>
            <td>${esc(m.fecha)}</td><td>${m.precioFinal != null ? dinero(m.precioFinal) : "-"}</td>
            <td>${m.descuento ? dinero(m.descuento) : "-"}</td><td>${m.codigoPromocion ? esc(m.codigoPromocion) : "-"}</td>
            <td>${botonesEditarEliminar("matriculas", m.id)}</td></tr>`).join("")
            : tablaVacia(8, "No hay matrículas.")}</tbody></table>
      </div>
    </div>`;
}

/* ---------- Pagos ---------- */
async function renderPagos() {
  const [pagos, matriculas] = await Promise.all([api("/pagos"), api("/matriculas")]);
  contenedor().innerHTML = `
    <div class="section-shell">
      <div class="form-card"><h3>Registrar pago</h3>
        <form id="pagoForm">
          <div class="form-grid two">
            <div class="form-group"><label>Matrícula</label><select name="matriculaId" required>
              ${opciones(matriculas, m => `${m.alumnoNombre} - ${m.paquete}`)}</select></div>
            <div class="form-group"><label>Monto</label><input name="monto" type="number" min="0.01" step="0.01" required /></div>
            <div class="form-group"><label>Método de pago</label><select name="metodo">
              <option value="EFECTIVO">Efectivo</option><option value="YAPE">Yape</option>
              <option value="PLIN">Plin</option><option value="TRANSFERENCIA">Transferencia</option></select></div>
            <div class="form-group"><label>Comprobante (opcional)</label><input name="comprobante" placeholder="Se genera automáticamente" /></div>
            <div class="form-group"><label>Fecha</label><input name="fecha" type="date" value="${hoy()}" required /></div>
          </div>
          <p style="font-size: 0.8rem; color: var(--gris-700); margin-top: 10px;">
            Se permiten pagos parciales: el saldo pendiente se calcula automáticamente.
          </p>
          <div class="form-actions"><button type="submit" class="action-btn">Guardar pago</button></div>
        </form>
      </div>
      <div class="data-table-card"><div class="table-header"><h3>Listado de pagos</h3></div>
        <table><thead><tr><th>Alumno</th><th>Monto</th><th>Tipo</th><th>Saldo pendiente</th><th>Método</th><th>Comprobante</th><th>Fecha</th><th>Acciones</th></tr></thead>
          <tbody>${pagos.length ? pagos.map(p => `
            <tr><td>${esc(p.alumnoNombre)}</td><td>${dinero(p.monto)}</td><td>${estadoHtml(p.tipo)}</td>
            <td>${dinero(p.saldoPendiente)}</td><td>${esc(etiqueta(p.metodo))}</td>
            <td><button class="edit-btn" data-action="comprobante" data-id="${p.id}">${esc(p.comprobante)}</button></td>
            <td>${esc(p.fecha)}</td><td><button class="delete-btn" data-action="delete" data-entidad="pagos" data-id="${p.id}">Eliminar</button></td></tr>`).join("")
            : tablaVacia(8, "No hay pagos.")}</tbody></table>
      </div>
    </div>`;
}

async function verComprobante(id) {
  try {
    const c = await api(`/pagos/${id}/comprobante`);
    mostrarModal("Comprobante de pago", `
      <div class="comprobante">
        <p><strong>Número:</strong> ${esc(c.numero)}</p>
        <p><strong>Fecha:</strong> ${esc(c.fecha)}</p>
        <p><strong>Alumno:</strong> ${esc(c.alumno)}</p>
        <p><strong>Paquete:</strong> ${esc(c.paquete)}</p>
        <p><strong>Método:</strong> ${esc(etiqueta(c.metodo))}</p>
        <p><strong>Monto:</strong> ${dinero(c.monto)}</p>
      </div>`);
  } catch (error) { notificar(error.message, "error"); }
}

/* ---------- Paquetes ---------- */
async function renderPaquetes() {
  const paquetes = await api("/paquetes");
  contenedor().innerHTML = `
    <div class="section-shell">
      <div class="form-card"><h3>Gestionar paquete</h3>
        <form id="paqueteForm">
          <div class="form-grid two">
            <div class="form-group"><label>Nombre</label><input name="nombre" required /></div>
            <div class="form-group"><label>Categoría</label><input name="categoria" required /></div>
            <div class="form-group"><label>Número de clases</label><input name="numeroClases" type="number" min="1" required /></div>
            <div class="form-group"><label>Precio</label><input name="precio" type="number" min="0" step="0.01" required /></div>
          </div>
          <div class="form-actions"><button type="submit" class="action-btn">Guardar paquete</button>
            <button type="button" class="mini-btn" data-action="limpiar" data-form="paqueteForm">Limpiar</button></div>
        </form>
      </div>
      <div class="data-table-card"><div class="table-header"><h3>Listado de paquetes</h3></div>
        <table><thead><tr><th>Nombre</th><th>Categoría</th><th>Clases</th><th>Precio</th><th>Vigencia</th><th>Acciones</th></tr></thead>
          <tbody>${paquetes.length ? paquetes.map(p => `
            <tr><td>${esc(p.nombre)}</td><td>${esc(p.categoria)}</td><td>${esc(p.numeroClases)}</td>
            <td>${dinero(p.precio)}</td><td>${p.vigente ? estadoHtml("VIGENTE") : estadoHtml("NO_VIGENTE")}</td>
            <td>${botonesEditarEliminar("paquetes", p.id)}</td></tr>`).join("")
            : tablaVacia(6, "No hay paquetes.")}</tbody></table>
      </div>
    </div>`;
}

/* ---------- Promociones ---------- */
async function renderPromociones() {
  const promociones = await api("/promociones");
  contenedor().innerHTML = `
    <div class="section-shell">
      <div class="form-card"><h3>Gestionar promoción</h3>
        <form id="promocionForm">
          <div class="form-grid two">
            <div class="form-group"><label>Código</label><input name="codigo" required /></div>
            <div class="form-group"><label>Descripción</label><input name="descripcion" required /></div>
            <div class="form-group"><label>Porcentaje de descuento</label><input name="porcentajeDescuento" type="number" min="0" max="100" step="0.01" required /></div>
            <div class="form-group"><label>Fecha inicio</label><input name="fechaInicio" type="date" value="${hoy()}" required /></div>
            <div class="form-group"><label>Fecha fin</label><input name="fechaFin" type="date" required /></div>
          </div>
          <div class="form-actions"><button type="submit" class="action-btn">Guardar promoción</button>
            <button type="button" class="mini-btn" data-action="limpiar" data-form="promocionForm">Limpiar</button></div>
        </form>
      </div>
      <div class="data-table-card"><div class="table-header"><h3>Listado de promociones</h3></div>
        <table><thead><tr><th>Código</th><th>Descripción</th><th>Descuento</th><th>Vigencia</th><th>Fecha fin</th><th>Activa hoy</th><th>Acciones</th></tr></thead>
          <tbody>${promociones.length ? promociones.map(p => `
            <tr><td>${esc(p.codigo)}</td><td>${esc(p.descripcion)}</td><td>${esc(p.porcentajeDescuento)}%</td>
            <td>${p.vigente ? estadoHtml("VIGENTE") : estadoHtml("NO_VIGENTE")}</td>
            <td>${esc(p.fechaFin)}</td>
            <td>${estadoHtml(p.activaHoy ? "ACTIVA_HOY" : "NO_ACTIVA_HOY")}</td>
            <td>${botonesEditarEliminar("promociones", p.id)}</td></tr>`).join("")
            : tablaVacia(7, "No hay promociones.")}</tbody></table>
      </div>
    </div>`;
}

/* ---------- Clases ---------- */
async function renderClases() {
  const [clases, alumnos, instructores, vehiculos] = await Promise.all([
    api("/clases"), api("/alumnos"), api("/instructores"), api("/vehiculos")
  ]);
  contenedor().innerHTML = `
    <div class="section-shell">
      <div class="form-card"><h3>Programar clase</h3>
        <form id="claseForm">
          <div class="form-grid">
            <div class="form-group"><label>Nombre</label><input name="nombre" required /></div>
            <div class="form-group"><label>Alumno</label><select name="alumnoId" required>${opciones(alumnos, a => `${a.nombre} ${a.apellidos}`)}</select></div>
            <div class="form-group"><label>Fecha</label><input name="fecha" type="date" value="${hoy()}" required /></div>
            <div class="form-group"><label>Hora inicio</label><input name="horaInicio" type="time" value="08:00" required /></div>
            <div class="form-group"><label>Hora fin</label><input name="horaFin" type="time" value="09:30" required /></div>
            <div class="form-group"><label>Instructor</label><select name="instructorId" required>${opciones(instructores.filter(i => i.disponible), i => i.nombre)}</select></div>
            <div class="form-group"><label>Vehículo</label><select name="vehiculoId" required>${opciones(vehiculos.filter(v => v.estado === "OPERATIVO"), v => `${v.marca} ${v.modelo} - ${v.placa}`)}</select></div>
            <div class="form-group"><label>Estado</label><select name="estado">
              <option value="PROGRAMADA">Programada</option><option value="CONFIRMADA">Confirmada</option>
              <option value="REALIZADA">Realizada</option><option value="CANCELADA">Cancelada</option></select></div>
          </div>
          <div class="form-actions"><button type="submit" class="action-btn">Guardar clase</button>
            <button type="button" class="mini-btn" data-action="limpiar" data-form="claseForm">Limpiar</button></div>
        </form>
      </div>
      <div class="data-table-card"><div class="table-header"><h3>Listado de clases</h3></div>
        <table><thead><tr><th>Clase</th><th>Alumno</th><th>Fecha</th><th>Horario</th><th>Instructor</th><th>Vehículo</th><th>Estado</th><th>Acciones</th></tr></thead>
          <tbody>${clases.length ? clases.map(c => `
            <tr><td>${esc(c.nombre)}</td><td>${esc(c.alumnoNombre)}</td><td>${esc(c.fecha)}</td>
            <td>${esc(String(c.horaInicio || "").slice(0, 5))} - ${esc(String(c.horaFin || "").slice(0, 5))}</td><td>${esc(c.instructorNombre)}</td>
            <td>${esc(c.vehiculoPlaca)}</td><td>${estadoHtml(c.estado)}</td>
            <td>${botonesEditarEliminar("clases", c.id)}</td></tr>`).join("")
            : tablaVacia(8, "No hay clases programadas.")}</tbody></table>
      </div>
    </div>`;
}

/* ---------- Vehículos ---------- */
async function renderVehiculos() {
  const vehiculos = await api("/vehiculos");
  contenedor().innerHTML = `
    <div class="section-shell">
      <div class="form-card"><h3>Registrar vehículo</h3>
        <form id="vehiculoForm">
          <div class="form-grid two">
            <div class="form-group"><label>Placa</label><input name="placa" required /></div>
            <div class="form-group"><label>Marca</label><input name="marca" required /></div>
            <div class="form-group"><label>Modelo</label><input name="modelo" required /></div>
            <div class="form-group"><label>Estado</label><select name="estado">
              <option value="OPERATIVO">Operativo</option><option value="EN_TALLER">En taller</option>
              <option value="INACTIVO">Inactivo</option></select></div>
          </div>
          <div class="form-actions"><button type="submit" class="action-btn">Guardar vehículo</button></div>
        </form>
      </div>
      <div class="data-table-card"><div class="table-header"><h3>Inventario de vehículos</h3></div>
        <table><thead><tr><th>Placa</th><th>Marca</th><th>Modelo</th><th>Estado</th><th>Acciones</th></tr></thead>
          <tbody>${vehiculos.length ? vehiculos.map(v => `
            <tr><td>${esc(v.placa)}</td><td>${esc(v.marca)}</td><td>${esc(v.modelo)}</td>
            <td>${estadoHtml(v.estado)}</td><td>${botonesEditarEliminar("vehiculos", v.id)}</td></tr>`).join("")
            : tablaVacia(5, "No hay vehículos.")}</tbody></table>
      </div>
    </div>`;
}

/* ---------- Instructores ---------- */
async function renderInstructores() {
  const instructores = await api("/instructores");
  contenedor().innerHTML = `
    <div class="section-shell">
      <div class="form-card"><h3>Registrar instructor</h3>
        <form id="instructorForm">
          <div class="form-grid two">
            <div class="form-group"><label>Nombre</label><input name="nombre" required /></div>
            <div class="form-group"><label>Especialidad</label><input name="especialidad" required /></div>
            <div class="form-group"><label>Teléfono</label><input name="telefono" /></div>
            <div class="form-group"><label>Disponible</label><select name="disponible">
              <option value="true">Sí</option><option value="false">No</option></select></div>
          </div>
          <div class="form-actions"><button type="submit" class="action-btn">Guardar instructor</button></div>
        </form>
      </div>
      <div class="data-table-card"><div class="table-header"><h3>Listado de instructores</h3></div>
        <table><thead><tr><th>Nombre</th><th>Especialidad</th><th>Teléfono</th><th>Disponibilidad</th><th>Acciones</th></tr></thead>
          <tbody>${instructores.length ? instructores.map(i => `
            <tr><td>${esc(i.nombre)}</td><td>${esc(i.especialidad)}</td><td>${esc(i.telefono)}</td>
            <td>${i.disponible ? "Disponible" : "No disponible"}</td>
            <td>${botonesEditarEliminar("instructores", i.id)}</td></tr>`).join("")
            : tablaVacia(5, "No hay instructores.")}</tbody></table>
      </div>
    </div>`;
}

/* ---------- Asistencia ---------- */
async function renderAsistencia() {
  const [asistencias, clases] = await Promise.all([api("/asistencias"), api("/clases")]);
  contenedor().innerHTML = `
    <div class="section-shell">
      <div class="form-card"><h3>Registrar asistencia y evaluación</h3>
        <form id="asistenciaForm">
          <div class="form-grid two">
            <div class="form-group"><label>Clase</label><select name="claseId" required>${opciones(clases, c => `${c.nombre} - ${c.fecha}`)}</select></div>
            <div class="form-group"><label>Asistió</label><select name="asistio">
              <option value="true">Sí</option><option value="false">No</option></select></div>
            <div class="form-group"><label>Calificación (0-20)</label><input name="calificacion" type="number" min="0" max="20" required /></div>
            <div class="form-group" style="grid-column: span 2;"><label>Observaciones</label><textarea name="observaciones" rows="2"></textarea></div>
          </div>
          <div class="form-actions"><button type="submit" class="action-btn">Guardar asistencia</button></div>
        </form>
      </div>
      <div class="data-table-card"><div class="table-header"><h3>Registro de asistencia</h3></div>
        <table><thead><tr><th>Clase</th><th>Alumno</th><th>Asistió</th><th>Calificación</th><th>Observaciones</th><th>Acciones</th></tr></thead>
          <tbody>${asistencias.length ? asistencias.map(a => `
            <tr><td>${esc(a.claseNombre)}</td><td>${esc(a.alumnoNombre)}</td>
            <td>${a.asistio ? "Sí" : "No"}</td><td>${esc(a.calificacion)}</td>
            <td>${esc(a.observaciones || "-")}</td>
            <td>${botonesEditarEliminar("asistencias", a.id)}</td></tr>`).join("")
            : tablaVacia(6, "No hay asistencias registradas.")}</tbody></table>
      </div>
    </div>`;
}

/* ---------- Mantenimiento ---------- */
async function renderMantenimiento() {
  const [mantenimientos, vehiculos] = await Promise.all([api("/mantenimientos"), api("/vehiculos")]);
  contenedor().innerHTML = `
    <div class="section-shell">
      <div class="form-card"><h3>Registrar mantenimiento</h3>
        <form id="mantenimientoForm">
          <div class="form-grid two">
            <div class="form-group"><label>Vehículo</label><select name="vehiculoId" required>${opciones(vehiculos, v => `${v.marca} ${v.modelo} - ${v.placa}`)}</select></div>
            <div class="form-group"><label>Tipo</label><select name="tipo">
              <option value="PREVENTIVO">Preventivo</option><option value="CORRECTIVO">Correctivo</option>
              <option value="INSPECCION">Inspección</option></select></div>
            <div class="form-group" style="grid-column: span 2;"><label>Descripción</label><textarea name="descripcion" rows="2" required></textarea></div>
            <div class="form-group"><label>Fecha</label><input name="fecha" type="date" value="${hoy()}" required /></div>
            <div class="form-group"><label>Estado</label><select name="estado">
              <option value="PENDIENTE">Pendiente</option><option value="EN_PROCESO">En proceso</option>
              <option value="COMPLETADO">Completado</option></select></div>
            <div class="form-group"><label>Proveedor</label><input name="proveedor" required /></div>
            <div class="form-group"><label>Costo</label><input name="costo" type="number" min="0" step="0.01" /></div>
          </div>
          <div class="form-actions"><button type="submit" class="action-btn">Guardar mantenimiento</button></div>
        </form>
      </div>
      <div class="data-table-card"><div class="table-header"><h3>Historial de mantenimiento</h3></div>
        <table><thead><tr><th>Vehículo</th><th>Tipo</th><th>Fecha</th><th>Estado</th><th>Proveedor</th><th>Costo</th><th>Acciones</th></tr></thead>
          <tbody>${mantenimientos.length ? mantenimientos.map(m => `
            <tr><td>${esc(m.vehiculoPlaca)}</td><td>${esc(etiqueta(m.tipo))}</td><td>${esc(m.fecha)}</td>
            <td>${estadoHtml(m.estado)}</td><td>${esc(m.proveedor)}</td><td>${dinero(m.costo)}</td>
            <td>${botonesEditarEliminar("mantenimientos", m.id)}</td></tr>`).join("")
            : tablaVacia(7, "No hay mantenimientos.")}</tbody></table>
      </div>
    </div>`;
}

/* ---------- Ausencias / reprogramaciones ---------- */
async function renderAusencias() {
  const [reprogramaciones, alumnos, clases] = await Promise.all([
    api("/reprogramaciones"), api("/alumnos"), api("/clases")
  ]);
  contenedor().innerHTML = `
    <div class="section-shell">
      <div class="form-card"><h3>Registrar ausencia / reprogramación</h3>
        <form id="ausenciaForm">
          <div class="form-grid two">
            <div class="form-group"><label>Alumno</label><select name="alumnoId" required>${opciones(alumnos, a => `${a.nombre} ${a.apellidos}`)}</select></div>
            <div class="form-group"><label>Clase</label><select name="claseId" required>${opciones(clases, c => `${c.nombre} - ${c.fecha}`)}</select></div>
            <div class="form-group" style="grid-column: span 2;"><label>Motivo</label><textarea name="motivo" rows="2" required></textarea></div>
            <div class="form-group"><label>Fecha</label><input name="fecha" type="date" value="${hoy()}" required /></div>
            <div class="form-group"><label>Nueva fecha</label><input name="nuevaFecha" type="date" /></div>
            <div class="form-group"><label>Nueva hora inicio</label><input name="nuevaHoraInicio" type="time" /></div>
            <div class="form-group"><label>Nueva hora fin</label><input name="nuevaHoraFin" type="time" /></div>
            <div class="form-group"><label>Estado</label><select name="estado">
              <option value="PENDIENTE_REPROGRAMACION">Pendiente reprogramación</option>
              <option value="REPROGRAMADA">Reprogramada</option>
              <option value="JUSTIFICADA">Justificada</option>
              <option value="PENDIENTE">Pendiente</option></select></div>
          </div>
          <div class="form-actions"><button type="submit" class="action-btn">Guardar ausencia</button></div>
        </form>
      </div>
      <div class="data-table-card"><div class="table-header"><h3>Control de ausencias</h3></div>
        <table><thead><tr><th>Alumno</th><th>Clase</th><th>Motivo</th><th>Fecha</th><th>Nueva fecha</th><th>Estado</th><th>Acciones</th></tr></thead>
          <tbody>${reprogramaciones.length ? reprogramaciones.map(a => `
            <tr><td>${esc(a.alumnoNombre)}</td><td>${esc(a.claseNombre)}</td><td>${esc(a.motivo)}</td>
            <td>${esc(a.fecha)}</td><td>${esc(a.nuevaFecha || "-")}</td><td>${estadoHtml(a.estado)}</td>
            <td>${botonesEditarEliminar("reprogramaciones", a.id)}</td></tr>`).join("")
            : tablaVacia(7, "No hay ausencias registradas.")}</tbody></table>
      </div>
    </div>`;
}

/* ---------- Reportes ---------- */
async function renderReportes() {
  const params = new URLSearchParams();
  if (state.filtros.desde) params.set("desde", state.filtros.desde);
  if (state.filtros.hasta) params.set("hasta", state.filtros.hasta);
  const r = await api("/reportes/ingresos" + (params.size ? "?" + params : ""));

  contenedor().innerHTML = `
    <div class="section-shell">
      <div class="form-card">
        <div class="form-grid two">
          <div class="form-group"><label>Desde</label><input id="reporteDesde" type="date" value="${esc(state.filtros.desde)}" /></div>
          <div class="form-group"><label>Hasta</label><input id="reporteHasta" type="date" value="${esc(state.filtros.hasta)}" /></div>
        </div>
        <div class="form-actions"><button class="action-btn" data-action="filtrar-reporte">Generar reporte</button>
          <button class="secondary-btn" data-action="descargar-csv">Descargar CSV</button></div>
      </div>
      <section class="stats-grid">
        <article class="stat-card blue"><small>Ingresos totales</small><h3>${dinero(r.totalIngresos)}</h3><span>Periodo seleccionado</span></article>
        <article class="stat-card"><small>Pagos</small><h3>${r.totalPagos}</h3><span>Registrados</span></article>
        <article class="stat-card"><small>Promedio</small><h3>${dinero(r.promedioPago)}</h3><span>Por pago</span></article>
      </section>
      <div class="content-grid">
        <div class="data-table-card"><div class="table-header"><h3>Ingresos por método</h3></div>
          <table><thead><tr><th>Método</th><th>Cantidad</th><th>Monto</th></tr></thead>
            <tbody>${r.porMetodo.length ? r.porMetodo.map(x => `
              <tr><td>${esc(etiqueta(x.metodo))}</td><td>${x.cantidad}</td><td>${dinero(x.monto)}</td></tr>`).join("")
              : tablaVacia(3, "Sin ingresos en el periodo.")}</tbody></table></div>
        <div class="data-table-card"><div class="table-header"><h3>Ingresos por mes</h3></div>
          <table><thead><tr><th>Mes</th><th>Monto</th></tr></thead>
            <tbody>${r.porMes.length ? r.porMes.map(x => `
              <tr><td>${esc(x.mes)}</td><td>${dinero(x.monto)}</td></tr>`).join("")
              : tablaVacia(2, "Sin ingresos en el periodo.")}</tbody></table></div>
      </div>
    </div>`;
}

/* ---------- Historial ---------- */
async function renderHistorial() {
  const esAlumno = state.currentUser?.rol === "ALUMNO";
  const alumnos = esAlumno ? [] : await api("/alumnos");
  contenedor().innerHTML = `
    <div class="section-shell">
      ${esAlumno ? "" : `
      <div class="form-card"><h3>Consultar historial</h3>
        <div class="form-grid two">
          <div class="form-group" style="grid-column: span 2;"><label>Alumno</label>
            <select id="historialAlumnoSelect">${opciones(alumnos, a => `${a.nombre} ${a.apellidos}`)}</select></div>
        </div>
      </div>`}
      <div class="data-table-card"><div class="table-header"><h3>Historial académico</h3></div>
        <table><thead><tr><th>Fecha</th><th>Paquete</th><th>Estado</th><th>Clase</th><th>Asistencia</th><th>Calificación</th></tr></thead>
          <tbody id="historialAcademico"><tr><td colspan="6">Seleccione un alumno.</td></tr></tbody></table></div>
      <div class="data-table-card"><div class="table-header"><h3>Historial financiero</h3></div>
        <table><thead><tr><th>Fecha</th><th>Estado</th><th>Monto</th><th>Comprobante</th></tr></thead>
          <tbody id="historialFinanciero"><tr><td colspan="4">Seleccione un alumno.</td></tr></tbody></table></div>
    </div>`;

  if (esAlumno) {
    await cargarHistorial();
    return;
  }
  const select = document.getElementById("historialAlumnoSelect");
  if (alumnos.length) {
    select.value = alumnos[0].id;
    await cargarHistorial(alumnos[0].id);
  }
}

async function cargarHistorial(alumnoId) {
  const esAlumno = state.currentUser?.rol === "ALUMNO";
  if (!esAlumno && !alumnoId) return;
  const academicas = document.getElementById("historialAcademico");
  const financieras = document.getElementById("historialFinanciero");
  if (!academicas || !financieras) return;
  academicas.innerHTML = `<tr><td colspan="6">Cargando...</td></tr>`;
  try {
    const h = await api(esAlumno ? "/historial/mi" : `/historial/alumnos/${alumnoId}`);
    academicas.innerHTML = h.academico.length ? h.academico.map(x => `
      <tr><td>${esc(x.fecha)}</td><td>${esc(x.paquete)}</td><td>${estadoHtml(x.estado)}</td>
      <td>${esc(x.clase)}</td><td>${esc(x.asistencia)}</td><td>${esc(x.calificacion ?? "-")}</td></tr>`).join("")
      : tablaVacia(6, "No hay información académica.");
    financieras.innerHTML = h.financiero.length ? h.financiero.map(x => `
      <tr><td>${esc(x.fecha)}</td><td>${estadoHtml(x.estado)}</td><td>${dinero(x.monto)}</td>
      <td>${esc(x.comprobante || "-")}</td></tr>`).join("")
      : tablaVacia(4, "No hay información financiera.");
  } catch (error) { notificar(error.message, "error"); }
}

/* ---------- Notificaciones ---------- */
async function renderNotificaciones() {
  const notificaciones = await api("/notificaciones");
  contenedor().innerHTML = `
    <div class="section-shell">
      <div class="data-table-card"><div class="table-header"><h3>Notificaciones</h3></div>
        <table><thead><tr><th>Tipo</th><th>Título</th><th>Mensaje</th><th>Fecha</th><th>Estado</th><th>Acciones</th></tr></thead>
          <tbody>${notificaciones.length ? notificaciones.map(n => `
            <tr><td>${esc(etiqueta(n.tipo))}</td><td>${esc(n.titulo)}</td><td>${esc(n.mensaje)}</td>
            <td>${esc(n.fecha)}</td><td>${estadoHtml(n.leida ? "LEIDA" : "PENDIENTE")}</td>
            <td>${n.leida ? "-" : `<button class="edit-btn" data-action="marcar-leida" data-id="${n.id}">Marcar leída</button>`}</td></tr>`).join("")
            : tablaVacia(6, "No hay notificaciones.")}</tbody></table>
      </div>
    </div>`;
}

/* ---------- Construcción de payloads ---------- */
const PAYLOADS = {
  alumnoForm: d => ({ nombre: d.nombre, apellidos: d.apellidos, dni: d.dni, email: d.email, telefono: d.telefono, direccion: d.direccion, fechaRegistro: d.fechaRegistro || hoy() }),
  matriculaForm: d => ({ alumnoId: Number(d.alumnoId), paqueteId: Number(d.paqueteId), estado: d.estado, fecha: d.fecha, codigoPromocion: d.codigoPromocion?.trim() || null }),
  pagoForm: d => ({ matriculaId: Number(d.matriculaId), monto: Number(d.monto), metodo: d.metodo, comprobante: d.comprobante || "", fecha: d.fecha }),
  paqueteForm: d => ({ nombre: d.nombre, categoria: d.categoria, numeroClases: Number(d.numeroClases), precio: Number(d.precio) }),
  promocionForm: d => ({ codigo: d.codigo, descripcion: d.descripcion, porcentajeDescuento: Number(d.porcentajeDescuento), fechaInicio: d.fechaInicio, fechaFin: d.fechaFin }),
  claseForm: d => ({ nombre: d.nombre, fecha: d.fecha, horaInicio: d.horaInicio, horaFin: d.horaFin, alumnoId: Number(d.alumnoId), instructorId: Number(d.instructorId), vehiculoId: Number(d.vehiculoId), estado: d.estado }),
  vehiculoForm: d => ({ placa: d.placa, marca: d.marca, modelo: d.modelo, estado: d.estado }),
  instructorForm: d => ({ nombre: d.nombre, especialidad: d.especialidad, telefono: d.telefono, disponible: d.disponible === "true" }),
  asistenciaForm: d => ({ claseId: Number(d.claseId), asistio: d.asistio === "true", calificacion: Number(d.calificacion), observaciones: d.observaciones || "" }),
  mantenimientoForm: d => ({ vehiculoId: Number(d.vehiculoId), tipo: d.tipo, descripcion: d.descripcion, fecha: d.fecha, estado: d.estado, proveedor: d.proveedor, costo: d.costo ? Number(d.costo) : 0 }),
  ausenciaForm: d => ({
    alumnoId: Number(d.alumnoId), claseId: Number(d.claseId), motivo: d.motivo,
    fecha: d.fecha, nuevaFecha: d.nuevaFecha || null, nuevaHoraInicio: d.nuevaHoraInicio || null,
    nuevaHoraFin: d.nuevaHoraFin || null, estado: d.estado
  })
};

const FORM_A_ENTIDAD = {
  alumnoForm: "alumnos", matriculaForm: "matriculas", pagoForm: "pagos", paqueteForm: "paquetes",
  promocionForm: "promociones", claseForm: "clases", vehiculoForm: "vehiculos", instructorForm: "instructores",
  asistenciaForm: "asistencias", mantenimientoForm: "mantenimientos", ausenciaForm: "reprogramaciones"
};

/* ---------- Eventos ---------- */
document.getElementById("loginForm").addEventListener("submit", async function (e) {
  e.preventDefault();
  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;
  const rol = document.getElementById("rol").value;
  const msg = document.getElementById("loginMessage");

  if (!email || !password || !rol) {
    msg.textContent = "Completa todos los campos.";
    msg.className = "login-message error";
    return;
  }

  msg.textContent = "Validando credenciales...";
  msg.className = "login-message";

  try {
    const respuesta = await api("/auth/login", {
      method: "POST",
      body: { username: email, password, rol: ROLES_LOGIN[rol] || rol }
    });
    state.token = respuesta.token;
    state.currentUser = { username: respuesta.username, nombre: respuesta.nombre, rol: respuesta.rol };
    localStorage.setItem(TOKEN_KEY, state.token);
    localStorage.setItem(USER_KEY, JSON.stringify(state.currentUser));
    msg.textContent = "Acceso correcto. Redirigiendo...";
    msg.className = "login-message success";
    setTimeout(mostrarApp, 350);
  } catch (error) {
    msg.textContent = error.message;
    msg.className = "login-message error";
  }
});

document.getElementById("logoutBtn").addEventListener("click", () => cerrarSesion());

document.querySelectorAll(".nav-item").forEach(btn =>
  btn.addEventListener("click", () => setSection(btn.dataset.section)));

document.addEventListener("submit", async function (e) {
  const form = e.target;
  if (!form.id || !PAYLOADS[form.id]) return;
  e.preventDefault();

  const entidad = FORM_A_ENTIDAD[form.id];
  const idEditar = form.dataset.editId;
  const payload = PAYLOADS[form.id](datosFormulario(form));
  const boton = form.querySelector('button[type="submit"]');

  try {
    boton.disabled = true;
    await api(ENTIDADES[entidad].endpoint + (idEditar ? `/${idEditar}` : ""), {
      method: idEditar ? "PUT" : "POST",
      body: payload
    });
    notificar(idEditar ? "Registro actualizado correctamente" : "Registro creado correctamente");
    form.reset();
    delete form.dataset.editId;
    if (boton) boton.textContent = boton.textContent.replace("Actualizar", "Guardar");
    await ENTIDADES[entidad].render();
  } catch (error) {
    notificar(error.message, "error");
  } finally {
    boton.disabled = false;
  }
});

document.addEventListener("click", async function (e) {
  const target = e.target.closest("[data-action]");
  if (!target) return;
  const accion = target.dataset.action;

  if (accion === "cerrar-modal") {
    document.getElementById("modalRoot").innerHTML = "";
    return;
  }

  if (accion === "limpiar") {
    const form = document.getElementById(target.dataset.form);
    if (form) { form.reset(); delete form.dataset.editId; }
    return;
  }

  if (accion === "comprobante") {
    await verComprobante(target.dataset.id);
    return;
  }

  if (accion === "marcar-leida") {
    try {
      await api(`/notificaciones/${target.dataset.id}/leida`, { method: "PATCH" });
      notificar("Notificación marcada como leída");
      await renderNotificaciones();
    } catch (error) { notificar(error.message, "error"); }
    return;
  }

  if (accion === "descargar-csv") {
    const desde = document.getElementById("reporteDesde")?.value || state.filtros.desde;
    const hasta = document.getElementById("reporteHasta")?.value || state.filtros.hasta;
    const params = new URLSearchParams({ formato: "csv" });
    if (desde) params.set("desde", desde);
    if (hasta) params.set("hasta", hasta);
    try {
      const csv = await api("/reportes/ingresos?" + params, { accept: "text/csv" });
      const url = URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8" }));
      const enlace = document.createElement("a");
      enlace.href = url;
      enlace.download = "reporte-ingresos.csv";
      document.body.appendChild(enlace);
      enlace.click();
      enlace.remove();
      URL.revokeObjectURL(url);
      notificar("Reporte CSV descargado correctamente");
    } catch (error) { notificar(error.message, "error"); }
    return;
  }

  if (accion === "filtrar-reporte") {
    state.filtros.desde = document.getElementById("reporteDesde").value;
    state.filtros.hasta = document.getElementById("reporteHasta").value;
    await renderSection();
    return;
  }

  if (accion === "delete") {
    const entidad = target.dataset.entidad;
    if (!ENTIDADES[entidad]) return;
    if (!window.confirm("¿Está seguro de eliminar este registro?")) return;
    try {
      await api(ENTIDADES[entidad].endpoint + "/" + target.dataset.id, { method: "DELETE" });
      notificar("Registro eliminado correctamente");
      await ENTIDADES[entidad].render();
    } catch (error) { notificar(error.message, "error"); }
    return;
  }

  if (accion === "edit") {
    const entidad = ENTIDADES[target.dataset.entidad];
    if (!entidad) return;
    try {
      const item = await api(entidad.endpoint + "/" + target.dataset.id);
      const form = document.getElementById(entidad.form);
      if (!form) return;
      Object.entries(item).forEach(([clave, valor]) => {
        const campo = form.elements[clave];
        if (!campo || campo.type === "submit") return;
        if (campo.type === "date" && valor) campo.value = String(valor).slice(0, 10);
        else if (campo.type === "time" && valor) campo.value = String(valor).slice(0, 5);
        else if (campo.type === "checkbox") campo.checked = Boolean(valor);
        else campo.value = valor ?? "";
      });
      form.dataset.editId = target.dataset.id;
      const boton = form.querySelector('button[type="submit"]');
      if (boton) boton.textContent = "Actualizar registro";
      form.scrollIntoView({ behavior: "smooth", block: "start" });
      notificar("Registro cargado en el formulario. Modifique y guarde.");
    } catch (error) { notificar(error.message, "error"); }
  }
});

document.addEventListener("change", function (e) {
  if (e.target && e.target.id === "historialAlumnoSelect") {
    cargarHistorial(e.target.value);
  }
});

window.addEventListener("DOMContentLoaded", async function () {
  if (!state.token) {
    cerrarSesion(true);
    return;
  }
  try {
    state.currentUser = await api("/auth/me");
    localStorage.setItem(USER_KEY, JSON.stringify(state.currentUser));
    mostrarApp();
  } catch (error) {
    cerrarSesion(true);
  }
});

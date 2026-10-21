const STORAGE_KEY = "sindaco_state_v1";

const defaultState = {
  users: [
    { id: 1, nombre: "Juan Pérez", email: "propietario@sindaco.com", password: "pass123", rol: "propietario" },
    { id: 2, nombre: "María García", email: "admin@sindaco.com", password: "pass123", rol: "administrador" },
    { id: 3, nombre: "Carlos López", email: "instructor@sindaco.com", password: "pass123", rol: "instructor" },
    { id: 4, nombre: "Pedro Martínez", email: "alumno@sindaco.com", password: "pass123", rol: "alumno" }
  ],
  paquetes: [
    { id: 1, nombre: "A-I", categoria: "A-I", numero_clases: 10, precio: 450.00, vigente: true },
    { id: 2, nombre: "A-IIa", categoria: "A-IIa", numero_clases: 15, precio: 620.00, vigente: true },
    { id: 3, nombre: "A-IIb", categoria: "A-IIb", numero_clases: 18, precio: 680.00, vigente: true },
    { id: 4, nombre: "A-IIIb", categoria: "A-IIIb", numero_clases: 20, precio: 760.00, vigente: true }
  ],
  alumnos: [
    { id: 1, nombre: "Ana", apellidos: "Torres", dni: "12345678", email: "ana@correo.com", telefono: "999111222", direccion: "Av. Central 123", fecha_registro: "2026-09-12" },
    { id: 2, nombre: "Luis", apellidos: "Ramírez", dni: "98765432", email: "luis@correo.com", telefono: "998877665", direccion: "Jr. Las Flores 45", fecha_registro: "2026-09-13" },
    { id: 3, nombre: "Carlos", apellidos: "Vega", dni: "45678912", email: "carlos@correo.com", telefono: "955441233", direccion: "Calle Lima 88", fecha_registro: "2026-09-15" }
  ],
  matriculas: [
    { id: 1, alumnoId: 1, paqueteId: 2, fecha: "2026-09-12", estado: "confirmada" },
    { id: 2, alumnoId: 2, paqueteId: 3, fecha: "2026-09-13", estado: "por_pagar" },
    { id: 3, alumnoId: 3, paqueteId: 1, fecha: "2026-09-15", estado: "confirmada" }
  ],
  pagos: [
    { id: 1, matriculaId: 1, monto: 620, metodo: "yape", comprobante: "Y-001", fecha: "2026-09-12" },
    { id: 2, matriculaId: 2, monto: 680, metodo: "transferencia", comprobante: "TR-102", fecha: "2026-09-13" }
  ],
  instructores: [
    { id: 1, nombre: "Carlos López", especialidad: "Manual", telefono: "999555111", disponible: true },
    { id: 2, nombre: "Renzo Castro", especialidad: "Automático", telefono: "980123456", disponible: true }
  ],
  vehiculos: [
    { id: 1, placa: "ABC-123", marca: "Toyota", modelo: "Corolla", estado: "operativo" },
    { id: 2, placa: "XYZ-456", marca: "Honda", modelo: "Civic", estado: "en_taller" },
    { id: 3, placa: "QWE-789", marca: "Hyundai", modelo: "Accent", estado: "operativo" }
  ],
  clases: [
    { id: 1, nombre: "Clase práctica 01", fecha: "2026-09-20", horaInicio: "08:00", horaFin: "09:30", instructorId: 1, vehiculoId: 1, estado: "programada" },
    { id: 2, nombre: "Clase práctica 02", fecha: "2026-09-20", horaInicio: "10:00", horaFin: "11:30", instructorId: 2, vehiculoId: 2, estado: "confirmada" },
    { id: 3, nombre: "Clase práctica 03", fecha: "2026-09-21", horaInicio: "15:00", horaFin: "16:30", instructorId: 1, vehiculoId: 3, estado: "programada" }
  ],
  asistencias: [
    { id: 1, claseId: 1, asistio: true, calificacion: 17, observaciones: "Muy buen desempeño" },
    { id: 2, claseId: 2, asistio: false, calificacion: 0, observaciones: "Falta justificada" }
  ]
};

let appState = loadState();

const state = {
  currentSection: "dashboard",
  currentUser: null
};

function loadState() {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (!stored) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(defaultState));
    return structuredClone(defaultState);
  }
  try {
    return JSON.parse(stored);
  } catch (err) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(defaultState));
    return structuredClone(defaultState);
  }
}

function saveState() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(appState));
}

function getCurrentUser() {
  return state.currentUser;
}

function loginUser(email, password, rol) {
  const user = appState.users.find(u => u.email === email && u.password === password && u.rol === rol);
  if (!user) return null;
  state.currentUser = user;
  return user;
}

function logoutUser() {
  state.currentUser = null;
  document.getElementById("loginView").classList.remove("hidden");
  document.getElementById("appView").classList.add("hidden");
  document.getElementById("loginForm").reset();
  const msg = document.getElementById("loginMessage");
  msg.textContent = "";
  msg.className = "login-message";
}

function validateLogin(email, password, rol) {
  if (!email || !password || !rol) return "Completa todos los campos.";
  return "";
}

function setSection(section) {
  state.currentSection = section;
  renderSection();
}

function renderHeader() {
  const userBadge = document.getElementById("userBadge");
  const title = document.getElementById("pageTitle");

  if (!state.currentUser) return;

  userBadge.textContent = `${state.currentUser.nombre} (${state.currentUser.rol})`;
  const map = {
    dashboard: "Dashboard",
    alumnos: "Alumnos",
    matriculas: "Matrículas",
    clases: "Clases",
    pagos: "Pagos",
    vehiculos: "Vehículos",
    instructores: "Instructores",
    asistencia: "Asistencia"
  };

  title.textContent = map[state.currentSection];
}

function getAlumnoById(id) {
  return appState.alumnos.find(a => a.id === Number(id));
}

function getPaqueteById(id) {
  return appState.paquetes.find(p => p.id === Number(id));
}

function getInstructorById(id) {
  return appState.instructores.find(i => i.id === Number(id));
}

function getVehiculoById(id) {
  return appState.vehiculos.find(v => v.id === Number(id));
}

function getMatriculaById(id) {
  return appState.matriculas.find(m => m.id === Number(id));
}

function renderDashboard() {
  const totalAlumnos = appState.alumnos.length;
  const totalClases = appState.clases.length;
  const totalPagos = appState.pagos.length;
  const totalInstructores = appState.instructores.length;

  const html = `
    <section class="stats-grid">
      <article class="stat-card blue">
        <small>Alumnos</small>
        <h3>${totalAlumnos}</h3>
        <span>Registrados</span>
      </article>

      <article class="stat-card">
        <small>Clases</small>
        <h3>${totalClases}</h3>
        <span>Programadas</span>
      </article>

      <article class="stat-card">
        <small>Pagos</small>
        <h3>${totalPagos}</h3>
        <span>Registrados</span>
      </article>

      <article class="stat-card">
        <small>Instructores</small>
        <h3>${totalInstructores}</h3>
        <span>Activos</span>
      </article>
    </section>

    <section class="content-grid">
      <div class="panel large">
        <div class="panel-header">
          <h3>Resumen de matrículas</h3>
        </div>

        <table>
          <thead>
            <tr>
              <th>Alumno</th>
              <th>Paquete</th>
              <th>Estado</th>
              <th>Fecha</th>
            </tr>
          </thead>
          <tbody>
            ${appState.matriculas.length ? appState.matriculas.map(m => {
              const alumno = getAlumnoById(m.alumnoId);
              const paquete = getPaqueteById(m.paqueteId);
              return `
                <tr>
                  <td>${alumno ? `${alumno.nombre} ${alumno.apellidos}` : "Sin alumno"}</td>
                  <td>${paquete ? paquete.nombre : "Sin paquete"}</td>
                  <td><span class="status ${m.estado}">${m.estado}</span></td>
                  <td>${m.fecha}</td>
                </tr>
              `;
            }).join("") : `<tr><td colspan="4">No hay matrículas aún.</td></tr>`}
          </tbody>
        </table>
      </div>

      <div class="panel">
        <div class="panel-header">
          <h3>Pagos</h3>
        </div>

        <ul class="list-compact">
          ${appState.pagos.length ? appState.pagos.map(p => {
            const matricula = getMatriculaById(p.matriculaId);
            const alumno = matricula ? getAlumnoById(matricula.alumnoId) : null;
            return `<li><strong>${alumno ? `${alumno.nombre} ${alumno.apellidos}` : "Alumno"}</strong><span>S/ ${Number(p.monto).toFixed(2)}</span></li>`;
          }).join("") : `<li><span>No hay pagos.</span></li>`}
        </ul>
      </div>
    </section>

    <section class="content-grid">
      <div class="panel">
        <div class="panel-header">
          <h3>Clases programadas</h3>
        </div>

        <ul class="list-compact">
          ${appState.clases.length ? appState.clases.map(c => {
            const instructor = getInstructorById(c.instructorId);
            return `<li><strong>${c.nombre}</strong><span>${c.horaInicio} - ${c.horaFin}</span></li>`;
          }).join("") : `<li><span>No hay clases.</span></li>`}
        </ul>
      </div>

      <div class="panel">
        <div class="panel-header">
          <h3>Vehículos</h3>
        </div>

        <ul class="list-compact">
          ${appState.vehiculos.length ? appState.vehiculos.map(v => `<li><strong>${v.marca} ${v.modelo}</strong><span>${v.estado}</span></li>`).join("") : `<li><span>No hay vehículos.</span></li>`}
        </ul>
      </div>
    </section>

    <section class="panel">
      <div class="panel-header">
        <h3>Requerimientos funcionales del sistema</h3>
      </div>
      <div class="requirements-grid">
        <div class="requirement-item">
          <span>RF-01</span>
          <p>Control de acceso por roles.</p>
        </div>
        <div class="requirement-item">
          <span>RF-02</span>
          <p>Registro de alumnos y paquetes.</p>
        </div>
        <div class="requirement-item">
          <span>RF-03</span>
          <p>Gestión de matrículas y pagos.</p>
        </div>
        <div class="requirement-item">
          <span>RF-06</span>
          <p>Control de asistencia y evaluaciones.</p>
        </div>
        <div class="requirement-item">
          <span>RF-08</span>
          <p>Control de horarios entre instructores.</p>
        </div>
        <div class="requirement-item">
          <span>RF-09</span>
          <p>Control de horarios entre vehículos.</p>
        </div>
      </div>
    </section>
  `;

  document.getElementById("viewContent").innerHTML = html;
}

function renderAlumnos() {
  const html = `
    <div class="section-shell">
      <div class="form-card">
        <h3>Registrar alumno</h3>
        <form id="alumnoForm">
          <div class="form-grid">
            <div class="form-group">
              <label>Nombre</label>
              <input name="nombre" required />
            </div>
            <div class="form-group">
              <label>Apellidos</label>
              <input name="apellidos" required />
            </div>
            <div class="form-group">
              <label>DNI</label>
              <input name="dni" required />
            </div>
            <div class="form-group">
              <label>Email</label>
              <input name="email" type="email" />
            </div>
            <div class="form-group">
              <label>Teléfono</label>
              <input name="telefono" />
            </div>
            <div class="form-group">
              <label>Fecha registro</label>
              <input name="fecha_registro" type="date" value="${new Date().toISOString().split('T')[0]}" />
            </div>
            <div class="form-group" style="grid-column: span 3;">
              <label>Dirección</label>
              <textarea name="direccion" rows="2"></textarea>
            </div>
          </div>
          <div class="form-actions">
            <button type="submit" class="action-btn">Guardar alumno</button>
          </div>
        </form>
      </div>

      <div class="data-table-card">
        <div class="table-header">
          <h3>Listado de alumnos</h3>
        </div>

        <table>
          <thead>
            <tr>
              <th>Nombre</th>
              <th>DNI</th>
              <th>Email</th>
              <th>Teléfono</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            ${appState.alumnos.length ? appState.alumnos.map(a => `
              <tr>
                <td>${a.nombre} ${a.apellidos}</td>
                <td>${a.dni}</td>
                <td>${a.email}</td>
                <td>${a.telefono}</td>
                <td>
                  <button class="edit-btn" data-action="edit-alumno" data-id="${a.id}">Editar</button>
                  <button class="delete-btn" data-action="delete-alumno" data-id="${a.id}">Eliminar</button>
                </td>
              </tr>
            `).join("") : `<tr><td colspan="5"><div class="empty-state">No hay alumnos registrados.</div></td></tr>`}
          </tbody>
        </table>
      </div>
    </div>
  `;

  document.getElementById("viewContent").innerHTML = html;
}

function renderMatriculas() {
  const html = `
    <div class="section-shell">
      <div class="form-card">
        <h3>Registrar matrícula</h3>
        <form id="matriculaForm">
          <div class="form-grid two">
            <div class="form-group">
              <label>Alumno</label>
              <select name="alumnoId" required>
                <option value="">Seleccione</option>
                ${appState.alumnos.map(a => `<option value="${a.id}">${a.nombre} ${a.apellidos}</option>`).join("")}
              </select>
            </div>
            <div class="form-group">
              <label>Paquete</label>
              <select name="paqueteId" required>
                <option value="">Seleccione</option>
                ${appState.paquetes.map(p => `<option value="${p.id}">${p.nombre} - S/ ${p.precio.toFixed(2)}</option>`).join("")}
              </select>
            </div>
            <div class="form-group">
              <label>Fecha</label>
              <input name="fecha" type="date" value="${new Date().toISOString().split('T')[0]}" required />
            </div>
            <div class="form-group">
              <label>Estado</label>
              <select name="estado">
                <option value="confirmada">Confirmada</option>
                <option value="por_pagar">Por pagar</option>
                <option value="cancelada">Cancelada</option>
              </select>
            </div>
          </div>
          <div class="form-actions">
            <button type="submit" class="action-btn">Guardar matrícula</button>
          </div>
        </form>
      </div>

      <div class="data-table-card">
        <div class="table-header">
          <h3>Listado de matrículas</h3>
        </div>
        <table>
          <thead>
            <tr>
              <th>Alumno</th>
              <th>Paquete</th>
              <th>Estado</th>
              <th>Fecha</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            ${appState.matriculas.length ? appState.matriculas.map(m => {
              const alumno = getAlumnoById(m.alumnoId);
              const paquete = getPaqueteById(m.paqueteId);
              return `
                <tr>
                  <td>${alumno ? alumno.nombre + " " + alumno.apellidos : "Sin alumno"}</td>
                  <td>${paquete ? paquete.nombre : "Sin paquete"}</td>
                  <td><span class="status ${m.estado}">${m.estado}</span></td>
                  <td>${m.fecha}</td>
                  <td>
                    <button class="delete-btn" data-action="delete-matricula" data-id="${m.id}">Eliminar</button>
                  </td>
                </tr>
              `;
            }).join("") : `<tr><td colspan="5"><div class="empty-state">No hay matrículas.</div></td></tr>`}
          </tbody>
        </table>
      </div>
    </div>
  `;

  document.getElementById("viewContent").innerHTML = html;
}

function renderClases() {
  const html = `
    <div class="section-shell">
      <div class="form-card">
        <h3>Programar clase</h3>
        <form id="claseForm">
          <div class="form-grid">
            <div class="form-group">
              <label>Nombre</label>
              <input name="nombre" required />
            </div>
            <div class="form-group">
              <label>Fecha</label>
              <input name="fecha" type="date" required />
            </div>
            <div class="form-group">
              <label>Hora inicio</label>
              <input name="horaInicio" type="time" required />
            </div>
            <div class="form-group">
              <label>Hora fin</label>
              <input name="horaFin" type="time" required />
            </div>
            <div class="form-group">
              <label>Instructor</label>
              <select name="instructorId" required>
                <option value="">Seleccione</option>
                ${appState.instructores.map(i => `<option value="${i.id}">${i.nombre}</option>`).join("")}
              </select>
            </div>
            <div class="form-group">
              <label>Vehículo</label>
              <select name="vehiculoId" required>
                <option value="">Seleccione</option>
                ${appState.vehiculos.map(v => `<option value="${v.id}">${v.marca} ${v.modelo} - ${v.placa}</option>`).join("")}
              </select>
            </div>
            <div class="form-group" style="grid-column: span 3;">
              <label>Estado</label>
              <select name="estado">
                <option value="programada">Programada</option>
                <option value="confirmada">Confirmada</option>
                <option value="realizada">Realizada</option>
                <option value="cancelada">Cancelada</option>
              </select>
            </div>
          </div>
          <div class="form-actions">
            <button type="submit" class="action-btn">Guardar clase</button>
          </div>
        </form>
      </div>

      <div class="data-table-card">
        <div class="table-header">
          <h3>Listado de clases</h3>
        </div>

        <table>
          <thead>
            <tr>
              <th>Clase</th>
              <th>Fecha</th>
              <th>Horario</th>
              <th>Instructor</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            ${appState.clases.length ? appState.clases.map(c => {
              const instructor = getInstructorById(c.instructorId);
              return `
                <tr>
                  <td>${c.nombre}</td>
                  <td>${c.fecha}</td>
                  <td>${c.horaInicio} - ${c.horaFin}</td>
                  <td>${instructor ? instructor.nombre : "Sin instructor"}</td>
                  <td><span class="status ${c.estado}">${c.estado}</span></td>
                  <td>
                    <button class="delete-btn" data-action="delete-clase" data-id="${c.id}">Eliminar</button>
                  </td>
                </tr>
              `;
            }).join("") : `<tr><td colspan="6"><div class="empty-state">No hay clases programadas.</div></td></tr>`}
          </tbody>
        </table>
      </div>
    </div>
  `;

  document.getElementById("viewContent").innerHTML = html;
}

function renderPagos() {
  const html = `
    <div class="section-shell">
      <div class="form-card">
        <h3>Registrar pago</h3>
        <form id="pagoForm">
          <div class="form-grid two">
            <div class="form-group">
              <label>Matrícula</label>
              <select name="matriculaId" required>
                <option value="">Seleccione</option>
                ${appState.matriculas.map(m => {
                  const alumno = getAlumnoById(m.alumnoId);
                  const paquete = getPaqueteById(m.paqueteId);
                  return `<option value="${m.id}">${alumno ? `${alumno.nombre} ${alumno.apellidos}` : "Alumno"} - ${paquete ? paquete.nombre : ""}</option>`;
                }).join("")}
              </select>
            </div>

            <div class="form-group">
              <label>Monto</label>
              <input name="monto" type="number" min="1" step="0.01" required />
            </div>

            <div class="form-group">
              <label>Método de pago</label>
              <select name="metodo" required>
                <option value="efectivo">Efectivo</option>
                <option value="yape">Yape</option>
                <option value="plin">Plin</option>
                <option value="transferencia">Transferencia</option>
              </select>
            </div>

            <div class="form-group">
              <label>Número comprobante</label>
              <input name="comprobante" required />
            </div>

            <div class="form-group" style="grid-column: span 2;">
              <label>Fecha</label>
              <input name="fecha" type="date" value="${new Date().toISOString().split('T')[0]}" required />
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="action-btn">Guardar pago</button>
          </div>
        </form>
      </div>

      <div class="data-table-card">
        <div class="table-header">
          <h3>Listado de pagos</h3>
        </div>

        <table>
          <thead>
            <tr>
              <th>Alumno</th>
              <th>Monto</th>
              <th>Método</th>
              <th>Fecha</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            ${appState.pagos.length ? appState.pagos.map(p => {
              const matricula = getMatriculaById(p.matriculaId);
              const alumno = matricula ? getAlumnoById(matricula.alumnoId) : null;
              return `
                <tr>
                  <td>${alumno ? `${alumno.nombre} ${alumno.apellidos}` : "Sin alumno"}</td>
                  <td>S/ ${Number(p.monto).toFixed(2)}</td>
                  <td>${p.metodo}</td>
                  <td>${p.fecha}</td>
                  <td>
                    <button class="delete-btn" data-action="delete-pago" data-id="${p.id}">Eliminar</button>
                  </td>
                </tr>
              `;
            }).join("") : `<tr><td colspan="5"><div class="empty-state">No hay pagos.</div></td></tr>`}
          </tbody>
        </table>
      </div>
    </div>
  `;

  document.getElementById("viewContent").innerHTML = html;
}

function renderVehiculos() {
  const html = `
    <div class="section-shell">
      <div class="form-card">
        <h3>Registrar vehículo</h3>
        <form id="vehiculoForm">
          <div class="form-grid two">
            <div class="form-group">
              <label>Placa</label>
              <input name="placa" required />
            </div>
            <div class="form-group">
              <label>Marca</label>
              <input name="marca" required />
            </div>
            <div class="form-group">
              <label>Modelo</label>
              <input name="modelo" required />
            </div>
            <div class="form-group">
              <label>Estado</label>
              <select name="estado">
                <option value="operativo">Operativo</option>
                <option value="en_taller">En taller</option>
                <option value="inactivo">Inactivo</option>
              </select>
            </div>
          </div>
          <div class="form-actions">
            <button type="submit" class="action-btn">Guardar vehículo</button>
          </div>
        </form>
      </div>

      <div class="data-table-card">
        <div class="table-header">
          <h3>Inventario de vehículos</h3>
        </div>

        <table>
          <thead>
            <tr>
              <th>Placa</th>
              <th>Marca</th>
              <th>Modelo</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            ${appState.vehiculos.length ? appState.vehiculos.map(v => `
              <tr>
                <td>${v.placa}</td>
                <td>${v.marca}</td>
                <td>${v.modelo}</td>
                <td>${v.estado}</td>
                <td>
                  <button class="delete-btn" data-action="delete-vehiculo" data-id="${v.id}">Eliminar</button>
                </td>
              </tr>
            `).join("") : `<tr><td colspan="5"><div class="empty-state">No hay vehículos.</div></td></tr>`}
          </tbody>
        </table>
      </div>
    </div>
  `;

  document.getElementById("viewContent").innerHTML = html;
}

function renderInstructores() {
  const html = `
    <div class="section-shell">
      <div class="form-card">
        <h3>Registrar instructor</h3>
        <form id="instructorForm">
          <div class="form-grid two">
            <div class="form-group">
              <label>Nombre</label>
              <input name="nombre" required />
            </div>
            <div class="form-group">
              <label>Especialidad</label>
              <input name="especialidad" required />
            </div>
            <div class="form-group">
              <label>Teléfono</label>
              <input name="telefono" />
            </div>
            <div class="form-group">
              <label>Disponible</label>
              <select name="disponible">
                <option value="true">Sí</option>
                <option value="false">No</option>
              </select>
            </div>
          </div>
          <div class="form-actions">
            <button type="submit" class="action-btn">Guardar instructor</button>
          </div>
        </form>
      </div>

      <div class="data-table-card">
        <div class="table-header">
          <h3>Listado de instructores</h3>
        </div>

        <table>
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Especialidad</th>
              <th>Teléfono</th>
              <th>Disponibilidad</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            ${appState.instructores.length ? appState.instructores.map(i => `
              <tr>
                <td>${i.nombre}</td>
                <td>${i.especialidad}</td>
                <td>${i.telefono}</td>
                <td>${i.disponible ? "Disponible" : "No disponible"}</td>
                <td>
                  <button class="delete-btn" data-action="delete-instructor" data-id="${i.id}">Eliminar</button>
                </td>
              </tr>
            `).join("") : `<tr><td colspan="5"><div class="empty-state">No hay instructores.</div></td></tr>`}
          </tbody>
        </table>
      </div>
    </div>
  `;

  document.getElementById("viewContent").innerHTML = html;
}

function renderAsistencia() {
  const html = `
    <div class="section-shell">
      <div class="data-table-card">
        <div class="table-header">
          <h3>Registro de asistencia</h3>
        </div>

        <table>
          <thead>
            <tr>
              <th>Clase</th>
              <th>Fecha</th>
              <th>Instructor</th>
              <th>Asistió</th>
              <th>Calificación</th>
            </tr>
          </thead>
          <tbody>
            ${appState.clases.length ? appState.clases.map(c => {
              const instructor = getInstructorById(c.instructorId);
              const asistencia = appState.asistencias.find(a => a.claseId === c.id);
              return `
                <tr>
                  <td>${c.nombre}</td>
                  <td>${c.fecha}</td>
                  <td>${instructor ? instructor.nombre : "Sin instructor"}</td>
                  <td>${asistencia ? (asistencia.asistio ? "Sí" : "No") : "Sin registro"}</td>
                  <td>${asistencia ? asistencia.calificacion : "--"}</td>
                </tr>
              `;
            }).join("") : `<tr><td colspan="5"><div class="empty-state">No hay clases para asistencia.</div></td></tr>`}
          </tbody>
        </table>
      </div>
    </div>
  `;

  document.getElementById("viewContent").innerHTML = html;
}

function renderSection() {
  renderHeader();

  const navButtons = document.querySelectorAll(".nav-item");
  navButtons.forEach(btn => {
    btn.classList.toggle("active", btn.dataset.section === state.currentSection);
  });

  switch (state.currentSection) {
    case "dashboard":
      renderDashboard();
      break;
    case "alumnos":
      renderAlumnos();
      break;
    case "matriculas":
      renderMatriculas();
      break;
    case "clases":
      renderClases();
      break;
    case "pagos":
      renderPagos();
      break;
    case "vehiculos":
      renderVehiculos();
      break;
    case "instructores":
      renderInstructores();
      break;
    case "asistencia":
      renderAsistencia();
      break;
    default:
      renderDashboard();
  }
}

document.getElementById("loginForm").addEventListener("submit", function (e) {
  e.preventDefault();

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value.trim();
  const rol = document.getElementById("rol").value;

  const validation = validateLogin(email, password, rol);
  const msg = document.getElementById("loginMessage");

  if (validation) {
    msg.textContent = validation;
    msg.className = "login-message error";
    return;
  }

  const user = loginUser(email, password, rol);
  if (!user) {
    msg.textContent = "Credenciales incorrectas. Intenta nuevamente.";
    msg.className = "login-message error";
    return;
  }

  msg.textContent = "Acceso correcto. Redirigiendo...";
  msg.className = "login-message success";

  setTimeout(() => {
    document.getElementById("loginView").classList.add("hidden");
    document.getElementById("appView").classList.remove("hidden");
    renderSection();
  }, 500);
});

document.getElementById("logoutBtn").addEventListener("click", logoutUser);

document.querySelectorAll(".nav-item").forEach(btn => {
  btn.addEventListener("click", () => {
    setSection(btn.dataset.section);
  });
});

document.addEventListener("submit", function (e) {
  if (e.target && e.target.id === "alumnoForm") {
    e.preventDefault();

    const form = e.target;
    const data = Object.fromEntries(new FormData(form).entries());

    const nuevoAlumno = {
      id: Date.now(),
      nombre: data.nombre.trim(),
      apellidos: data.apellidos.trim(),
      dni: data.dni.trim(),
      email: data.email.trim(),
      telefono: data.telefono.trim(),
      direccion: data.direccion.trim(),
      fecha_registro: data.fecha_registro || new Date().toISOString().split("T")[0]
    };

    appState.alumnos.push(nuevoAlumno);
    saveState();
    renderAlumnos();
  }

  if (e.target && e.target.id === "matriculaForm") {
    e.preventDefault();

    const form = e.target;
    const data = Object.fromEntries(new FormData(form).entries());

    const nuevaMatricula = {
      id: Date.now(),
      alumnoId: Number(data.alumnoId),
      paqueteId: Number(data.paqueteId),
      fecha: data.fecha,
      estado: data.estado
    };

    appState.matriculas.push(nuevaMatricula);
    saveState();
    renderMatriculas();
  }

  if (e.target && e.target.id === "claseForm") {
    e.preventDefault();

    const form = e.target;
    const data = Object.fromEntries(new FormData(form).entries());

    const nuevaClase = {
      id: Date.now(),
      nombre: data.nombre.trim(),
      fecha: data.fecha,
      horaInicio: data.horaInicio,
      horaFin: data.horaFin,
      instructorId: Number(data.instructorId),
      vehiculoId: Number(data.vehiculoId),
      estado: data.estado
    };

    appState.clases.push(nuevaClase);
    saveState();
    renderClases();
  }

  if (e.target && e.target.id === "pagoForm") {
    e.preventDefault();

    const form = e.target;
    const data = Object.fromEntries(new FormData(form).entries());

    const nuevoPago = {
      id: Date.now(),
      matriculaId: Number(data.matriculaId),
      monto: Number(data.monto),
      metodo: data.metodo,
      comprobante: data.comprobante.trim(),
      fecha: data.fecha
    };

    appState.pagos.push(nuevoPago);
    saveState();
    renderPagos();
  }

  if (e.target && e.target.id === "vehiculoForm") {
    e.preventDefault();

    const form = e.target;
    const data = Object.fromEntries(new FormData(form).entries());

    const nuevoVehiculo = {
      id: Date.now(),
      placa: data.placa.trim(),
      marca: data.marca.trim(),
      modelo: data.modelo.trim(),
      estado: data.estado
    };

    appState.vehiculos.push(nuevoVehiculo);
    saveState();
    renderVehiculos();
  }

  if (e.target && e.target.id === "instructorForm") {
    e.preventDefault();

    const form = e.target;
    const data = Object.fromEntries(new FormData(form).entries());

    const nuevoInstructor = {
      id: Date.now(),
      nombre: data.nombre.trim(),
      especialidad: data.especialidad.trim(),
      telefono: data.telefono.trim(),
      disponible: data.disponible === "true"
    };

    appState.instructores.push(nuevoInstructor);
    saveState();
    renderInstructores();
  }
});

document.addEventListener("click", function (e) {
  const target = e.target;

  if (target.dataset.action === "delete-alumno") {
    const id = Number(target.dataset.id);
    appState.alumnos = appState.alumnos.filter(a => a.id !== id);
    appState.matriculas = appState.matriculas.filter(m => m.alumnoId !== id);
    saveState();
    renderAlumnos();
  }

  if (target.dataset.action === "delete-matricula") {
    const id = Number(target.dataset.id);
    appState.matriculas = appState.matriculas.filter(m => m.id !== id);
    appState.pagos = appState.pagos.filter(p => p.matriculaId !== id);
    saveState();
    renderMatriculas();
  }

  if (target.dataset.action === "delete-clase") {
    const id = Number(target.dataset.id);
    appState.clases = appState.clases.filter(c => c.id !== id);
    appState.asistencias = appState.asistencias.filter(a => a.claseId !== id);
    saveState();
    renderClases();
  }

  if (target.dataset.action === "delete-pago") {
    const id = Number(target.dataset.id);
    appState.pagos = appState.pagos.filter(p => p.id !== id);
    saveState();
    renderPagos();
  }

  if (target.dataset.action === "delete-vehiculo") {
    const id = Number(target.dataset.id);
    appState.vehiculos = appState.vehiculos.filter(v => v.id !== id);
    saveState();
    renderVehiculos();
  }

  if (target.dataset.action === "delete-instructor") {
    const id = Number(target.dataset.id);
    appState.instructores = appState.instructores.filter(i => i.id !== id);
    saveState();
    renderInstructores();
  }

  if (target.dataset.action === "edit-alumno") {
    const id = Number(target.dataset.id);
    const alumno = getAlumnoById(id);
    if (!alumno) return;

    const form = document.getElementById("alumnoForm");
    if (!form) return;

    form.nombre.value = alumno.nombre;
    form.apellidos.value = alumno.apellidos;
    form.dni.value = alumno.dni;
    form.email.value = alumno.email;
    form.telefono.value = alumno.telefono;
    form.direccion.value = alumno.direccion;
    form.fecha_registro.value = alumno.fecha_registro || new Date().toISOString().split("T")[0];

    const btn = form.querySelector("button[type='submit']");
    btn.textContent = "Actualizar alumno";
    btn.dataset.editId = id;

    form.addEventListener("submit", function handler(e) {
      if (e.target !== form) return;
      e.preventDefault();

      const updated = {
        id,
        nombre: form.nombre.value.trim(),
        apellidos: form.apellidos.value.trim(),
        dni: form.dni.value.trim(),
        email: form.email.value.trim(),
        telefono: form.telefono.value.trim(),
        direccion: form.direccion.value.trim(),
        fecha_registro: form.fecha_registro.value
      };

      const index = appState.alumnos.findIndex(a => a.id === id);
      if (index >= 0) appState.alumnos[index] = updated;

      saveState();
      renderAlumnos();
      form.removeEventListener("submit", handler);
    }, { once: true });
  }
});

window.addEventListener("DOMContentLoaded", function () {
  const appView = document.getElementById("appView");
  const loginView = document.getElementById("loginView");

  if (!state.currentUser) {
    loginView.classList.remove("hidden");
    appView.classList.add("hidden");
  }

  renderSection();
});
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.botica.model.*, com.botica.service.ConsultaService, com.botica.util.Html, com.botica.util.Constantes, java.time.LocalDate, java.util.*, java.net.URLEncoder, java.nio.charset.StandardCharsets" %>
<%
  String titulo = "Panel de Auditoría";
  Usuario yo = (Usuario) session.getAttribute("usuario");
  String pDesde = request.getParameter("desde"), pHasta = request.getParameter("hasta");
  String pUsuario = request.getParameter("usuario"), pAccion = request.getParameter("filtroAccion");
  LocalDate desde = null, hasta = null;
  String errorFecha = null;
  try {
    if (pDesde != null && !pDesde.isBlank()) desde = LocalDate.parse(pDesde);
    if (pHasta != null && !pHasta.isBlank()) hasta = LocalDate.parse(pHasta);
  } catch (RuntimeException e) {
    errorFecha = "Fecha no válida: se muestran todas las fechas";
    desde = null; hasta = null;
  }
  int limite = 500;
  List<Auditoria> registros = ConsultaService.auditoria(desde, hasta, pUsuario, pAccion, limite);
  List<Auditoria> fallidos = ConsultaService.auditoria(null, null, null, Constantes.AUD_LOGIN_FALLIDO, 20);
  String[] acciones = {"LOGIN", "LOGIN_FALLIDO", "LOGIN_BLOQUEADO", "LOGOUT", "ACCESO_DENEGADO", "COMPRA", "PAGO",
      "AVANCE", "REVERSION", "CANCELACION", "REEMBOLSO", "crearProducto", "actualizarProducto", "ajustarStock",
      "eliminarProducto", "cambiarActivoProducto", "aplicarDescuento", "registrarIngreso", "retirarVencidos",
      "cambiarRol", "cambiarActivoUsuario", "resetearContrasena", "crearUsuario"};
  String q = "desde=" + URLEncoder.encode(pDesde == null ? "" : pDesde, StandardCharsets.UTF_8)
      + "&hasta=" + URLEncoder.encode(pHasta == null ? "" : pHasta, StandardCharsets.UTF_8)
      + "&usuario=" + URLEncoder.encode(pUsuario == null ? "" : pUsuario, StandardCharsets.UTF_8)
      + "&filtroAccion=" + URLEncoder.encode(pAccion == null ? "" : pAccion, StandardCharsets.UTF_8);
%>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="panel-banda">
  <div class="container"><h3 class="mb-0 fw-bold"><i class="bi bi-journal-text"></i> Panel de Auditoría</h3><span class="opacity-75">Hola, <%= Html.esc(yo.getNombre()) %> · bitácora del sistema (solo lectura)</span></div>
</div>

<div class="container my-4">
  <%@ include file="/WEB-INF/jspf/flash.jspf" %>
  <% if (errorFecha != null) { %><div class="alert alert-warning"><%= Html.esc(errorFecha) %></div><% } %>

  <div class="seccion mb-4">
    <form method="get" action="<%= ctx %>/panel" class="row g-2 align-items-end">
      <div class="col-6 col-md-2"><label class="form-label small">Desde</label><input type="date" name="desde" value="<%= Html.esc(pDesde) %>" class="form-control form-control-sm"></div>
      <div class="col-6 col-md-2"><label class="form-label small">Hasta</label><input type="date" name="hasta" value="<%= Html.esc(pHasta) %>" class="form-control form-control-sm"></div>
      <div class="col-md-3"><label class="form-label small">Usuario (nombre o correo)</label><input name="usuario" value="<%= Html.esc(pUsuario) %>" class="form-control form-control-sm"></div>
      <div class="col-md-2"><label class="form-label small">Acción</label>
        <select name="filtroAccion" class="form-select form-select-sm">
          <option value="">Todas</option>
          <% for (String a : acciones) { %><option value="<%= a %>" <%= a.equals(pAccion) ? "selected" : "" %>><%= a %></option><% } %>
        </select>
      </div>
      <div class="col-md-3 d-flex gap-1">
        <button class="btn btn-primary btn-sm"><i class="bi bi-funnel"></i> Filtrar</button>
        <a class="btn btn-outline-secondary btn-sm" href="<%= ctx %>/panel">Limpiar</a>
        <a class="btn btn-outline-success btn-sm" href="<%= ctx %>/panel?accion=exportarCsv&<%= q %>"><i class="bi bi-download"></i> CSV</a>
      </div>
    </form>
  </div>

  <div class="row g-4">
    <div class="col-lg-9">
      <div class="seccion">
        <h5 class="mb-3"><i class="bi bi-list-ul text-verde"></i> Bitácora <span class="text-muted small">(<%= registros.size() %><%= registros.size() == limite ? "+, se muestran los " + limite + " más recientes" : "" %>)</span></h5>
        <div class="table-responsive" style="max-height: 560px; overflow-y: auto;">
          <table class="table table-sm table-hover small">
            <thead><tr><th>Fecha</th><th>Usuario</th><th>Rol</th><th>Acción</th><th>Entidad</th><th>Detalle</th><th>IP</th></tr></thead>
            <tbody>
              <% for (Auditoria a : registros) { %>
                <tr class="<%= a.getAccion().startsWith("LOGIN_") || "ACCESO_DENEGADO".equals(a.getAccion()) ? "table-warning" : "" %>">
                  <td class="text-nowrap"><%= Html.fecha(a.getFecha()) %></td>
                  <td><%= a.getCorreoUsuario() == null ? "<span class='text-muted'>-</span>" : Html.esc(a.getCorreoUsuario()) %></td>
                  <td><%= Html.esc(a.getRol()) %></td>
                  <td><span class="badge text-bg-secondary"><%= Html.esc(a.getAccion()) %></span></td>
                  <td><%= Html.esc(a.getEntidad()) %><%= a.getIdEntidad() == null ? "" : " #" + a.getIdEntidad() %></td>
                  <td><%= Html.esc(a.getDetalle()) %></td>
                  <td><%= Html.esc(a.getIp()) %></td>
                </tr>
              <% } %>
              <% if (registros.isEmpty()) { %><tr><td colspan="7" class="text-muted text-center">Sin registros para estos filtros</td></tr><% } %>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <div class="col-lg-3">
      <div class="seccion">
        <h5 class="mb-3"><i class="bi bi-shield-exclamation text-danger"></i> Intentos fallidos</h5>
        <ul class="list-unstyled small mb-2">
          <% for (Auditoria a : fallidos) { %>
            <li class="border-bottom py-1"><strong><%= Html.esc(a.getDetalle()) %></strong><br><span class="text-muted"><%= Html.fecha(a.getFecha()) %> · <%= Html.esc(a.getIp()) %></span></li>
          <% } %>
          <% if (fallidos.isEmpty()) { %><li class="text-muted">No hay intentos fallidos</li><% } %>
        </ul>
        <a class="btn btn-outline-danger btn-sm w-100" href="<%= ctx %>/panel?filtroAccion=<%= Constantes.AUD_LOGIN_FALLIDO %>">Ver todos</a>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>

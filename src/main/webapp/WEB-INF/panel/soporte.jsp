<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.botica.model.*, com.botica.patterns.command.HistorialComandos, com.botica.service.ConsultaService, com.botica.util.Html, java.util.*" %>
<%
  String titulo = "Panel de Soporte";
  Usuario yo = (Usuario) session.getAttribute("usuario");
  List<Reclamo> reclamos = ConsultaService.reclamos();
  Map<Integer, String> nombres = new HashMap<>();
  Map<Integer, String> estadoPedido = new HashMap<>();
  for (Pedido ped : ConsultaService.pedidos()) estadoPedido.put(ped.getId(), ped.getEstado());
  for (Usuario u : ConsultaService.usuarios()) nombres.put(u.getId(), u.getNombre());
  int abiertos = 0, enProceso = 0, resueltos = 0, escalados = 0;
  for (Reclamo r : reclamos) {
    if ("ABIERTO".equals(r.getEstado())) abiertos++;
    else if ("EN_PROCESO".equals(r.getEstado())) enProceso++;
    else if ("RESUELTO".equals(r.getEstado())) resueltos++;
    else if ("ESCALADO".equals(r.getEstado())) escalados++;
  }
  String[] estados = {"ABIERTO", "EN_PROCESO", "ESCALADO", "RESUELTO"};
  HistorialComandos historial = (HistorialComandos) session.getAttribute("historialReclamos");
  String ultimoCambio = historial == null ? null : historial.ultimaDescripcion();
%>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="panel-banda">
  <div class="container"><h3 class="mb-0 fw-bold"><i class="bi bi-headset"></i> Panel de Soporte al Cliente</h3><span class="opacity-75">Hola, <%= Html.esc(yo.getNombre()) %> · atiende los reclamos de los clientes</span></div>
</div>

<div class="container my-4">
  <%@ include file="/WEB-INF/jspf/flash.jspf" %>

  <div class="row g-3 mb-4">
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono" style="background:#fff4d6;color:#b58105;"><i class="bi bi-envelope-exclamation"></i></div><div><div class="valor"><%= abiertos %></div><div class="etq">Reclamos abiertos</div></div></div></div>
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono"><i class="bi bi-hourglass-split"></i></div><div><div class="valor"><%= enProceso %></div><div class="etq">En proceso</div></div></div></div>
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono"><i class="bi bi-check2-circle"></i></div><div><div class="valor"><%= resueltos %></div><div class="etq">Resueltos</div></div></div></div>
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono"><i class="bi bi-collection"></i></div><div><div class="valor"><%= escalados %></div><div class="etq">Escalados (total: <%= reclamos.size() %>)</div></div></div></div>
  </div>

  <div class="seccion">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
      <h5 class="mb-0"><i class="bi bi-chat-left-text text-verde"></i> Bandeja de reclamos</h5>
      <% if (ultimoCambio != null) { %>
        <form method="post" action="<%= ctx %>/panel">
          <input type="hidden" name="accion" value="deshacerReclamo">
          <button class="btn btn-sm btn-outline-secondary"><i class="bi bi-arrow-counterclockwise"></i> Deshacer: <%= Html.esc(ultimoCambio) %></button>
        </form>
      <% } %>
    </div>
    <% if (reclamos.isEmpty()) { %><p class="text-muted mb-0">Todavía no hay reclamos registrados.</p><% } else { %>
    <div class="table-responsive">
      <table class="table table-hover">
        <thead><tr><th>#</th><th>Pedido</th><th>Descripción</th><th>Fecha</th><th>Estado</th><th>Respondió</th><th>Cambiar estado</th></tr></thead>
        <tbody>
          <% for (Reclamo r : reclamos) { %>
            <tr>
              <td>#<%= r.getId() %></td>
              <td><a href="<%= ctx %>/seguimiento.jsp?id=<%= r.getIdPedido() %>">#<%= r.getIdPedido() %></a></td>
              <td style="max-width: 360px;">
                <%= Html.esc(r.getDescripcion()) %>
                <% if (r.getRespuesta() != null && !r.getRespuesta().isBlank()) { %><div class="small text-success mt-1"><i class="bi bi-reply"></i> <%= Html.esc(r.getRespuesta()) %></div><% } %>
                <form method="post" action="<%= ctx %>/panel" class="d-flex gap-1 mt-2">
                  <input type="hidden" name="accion" value="responderReclamo">
                  <input type="hidden" name="id" value="<%= r.getId() %>">
                  <input name="respuesta" class="form-control form-control-sm" placeholder="Responder al cliente..." required>
                  <button class="btn btn-sm btn-primary" title="Enviar respuesta"><i class="bi bi-send"></i></button>
                </form>
              </td>
              <td class="small"><%= Html.fecha(r.getFechaCreacion()) %></td>
              <td><span class="badge text-bg-<%= Html.badge(r.getEstado()) %>"><%= Html.estadoLegible(r.getEstado()) %></span></td>
              <td class="small"><%= r.getIdSoporte() == 0 ? "—" : Html.esc(nombres.get(r.getIdSoporte())) %></td>
              <td>
                <form method="post" action="<%= ctx %>/panel" class="d-flex gap-1">
                  <input type="hidden" name="accion" value="estadoReclamo">
                  <input type="hidden" name="id" value="<%= r.getId() %>">
                  <select name="estado" class="form-select form-select-sm" style="width: 130px;">
                    <% for (String e : estados) { %><option value="<%= e %>" <%= e.equals(r.getEstado()) ? "selected" : "" %>><%= Html.estadoLegible(e) %></option><% } %>
                  </select>
                  <button class="btn btn-sm btn-outline-primary"><i class="bi bi-check2"></i></button>
                </form>
                <% String estPedido = estadoPedido.get(r.getIdPedido());
                   if ("PAGADO".equals(estPedido) || "EN_DESPACHO".equals(estPedido)) { %>
                <form method="post" action="<%= ctx %>/panel" class="mt-1" onsubmit="return confirm('¿Reembolsar el pedido #<%= r.getIdPedido() %> y devolver el stock?');">
                  <input type="hidden" name="accion" value="reembolsarPedido">
                  <input type="hidden" name="idPedido" value="<%= r.getIdPedido() %>">
                  <button class="btn btn-sm btn-outline-danger w-100"><i class="bi bi-arrow-return-left"></i> Reembolsar pedido</button>
                </form>
                <% } %>
              </td>
            </tr>
          <% } %>
        </tbody>
      </table>
    </div>
    <% } %>
  </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>

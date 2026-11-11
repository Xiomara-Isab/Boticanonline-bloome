<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.botica.model.*, com.botica.service.ConsultaService, com.botica.util.Html, java.util.*" %>
<%
  String titulo = "Mi cuenta";
  Usuario yo = (Usuario) session.getAttribute("usuario");
  List<Pedido> pedidos = ConsultaService.pedidosDeCliente(yo.getId());
  List<Reclamo> reclamos = ConsultaService.reclamosDeCliente(yo.getId());
  int enCurso = 0;
  double gastado = 0;
  for (Pedido p : pedidos) {
    if ("CANCELADO".equals(p.getEstado())) continue;
    if (!"ENTREGADO".equals(p.getEstado())) enCurso++;
    if (!"PENDIENTE".equals(p.getEstado())) gastado += p.getTotal(); // solo cuenta lo ya pagado
  }
%>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="panel-banda">
  <div class="container d-flex justify-content-between align-items-center flex-wrap gap-2">
    <div><h3 class="mb-0 fw-bold">Hola, <%= Html.esc(yo.getNombre()) %></h3><span class="opacity-75">Tu cuenta de cliente</span></div>
    <a class="btn btn-light" href="<%= ctx %>/catalogo.jsp"><i class="bi bi-bag"></i> Seguir comprando</a>
  </div>
</div>

<div class="container my-4">
  <%@ include file="/WEB-INF/jspf/flash.jspf" %>

  <div class="row g-3 mb-4">
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono"><i class="bi bi-receipt"></i></div><div><div class="valor"><%= pedidos.size() %></div><div class="etq">Pedidos realizados</div></div></div></div>
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono"><i class="bi bi-truck"></i></div><div><div class="valor"><%= enCurso %></div><div class="etq">Pedidos en curso</div></div></div></div>
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono"><i class="bi bi-wallet2"></i></div><div><div class="valor"><%= Html.money(gastado) %></div><div class="etq">Total comprado</div></div></div></div>
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono"><i class="bi bi-headset"></i></div><div><div class="valor"><%= reclamos.size() %></div><div class="etq">Reclamos registrados</div></div></div></div>
  </div>

  <div class="row g-4">
    <div class="col-lg-8">
      <div class="seccion">
        <h5 class="mb-3"><i class="bi bi-receipt text-verde"></i> Mis pedidos</h5>
        <% if (pedidos.isEmpty()) { %>
          <p class="text-muted mb-0">Aún no tienes pedidos. <a href="<%= ctx %>/catalogo.jsp">Explora el catálogo</a> y haz tu primera compra.</p>
        <% } else { %>
        <div class="table-responsive">
          <table class="table table-hover">
            <thead><tr><th>#</th><th>Fecha</th><th>Estado</th><th class="text-end">Total</th><th></th></tr></thead>
            <tbody>
              <% for (Pedido p : pedidos) { %>
                <tr>
                  <td>#<%= p.getId() %></td>
                  <td><%= Html.fecha(p.getFechaPedido()) %></td>
                  <td><span class="badge text-bg-<%= Html.badge(p.getEstado()) %>"><%= Html.estadoLegible(p.getEstado()) %></span></td>
                  <td class="text-end"><%= Html.money(p.getTotal()) %></td>
                  <td class="text-end">
                    <% if ("PENDIENTE".equals(p.getEstado())) { %>
                      <a class="btn btn-sm btn-primary" href="<%= ctx %>/pago.jsp?id=<%= p.getId() %>"><i class="bi bi-credit-card"></i> Pagar</a>
                      <form method="post" action="<%= ctx %>/cancelarPedido" class="d-inline" onsubmit="return confirm('¿Cancelar este pedido?');">
                        <input type="hidden" name="idPedido" value="<%= p.getId() %>">
                        <button class="btn btn-sm btn-outline-danger">Cancelar</button>
                      </form>
                    <% } %>
                    <a class="btn btn-sm btn-outline-primary" href="<%= ctx %>/seguimiento.jsp?id=<%= p.getId() %>">Seguimiento</a>
                  </td>
                </tr>
              <% } %>
            </tbody>
          </table>
        </div>
        <% } %>
      </div>
    </div>

    <div class="col-lg-4">
      <div class="seccion mb-4">
        <h5 class="mb-3"><i class="bi bi-chat-dots text-verde"></i> Reportar un problema</h5>
        <% if (pedidos.isEmpty()) { %>
          <p class="text-muted small mb-0">Cuando tengas un pedido podrás registrar aquí cualquier inconveniente.</p>
        <% } else { %>
        <form method="post" action="<%= ctx %>/panel">
          <input type="hidden" name="accion" value="crearReclamo">
          <div class="mb-2">
            <select name="idPedido" class="form-select" required>
              <% for (Pedido p : pedidos) { %><option value="<%= p.getId() %>">Pedido #<%= p.getId() %> · <%= Html.money(p.getTotal()) %></option><% } %>
            </select>
          </div>
          <div class="mb-2"><textarea name="descripcion" class="form-control" rows="3" placeholder="Cuéntanos qué ocurrió" required></textarea></div>
          <button class="btn btn-primary w-100">Enviar reclamo</button>
        </form>
        <% } %>
      </div>

      <div class="seccion">
        <h5 class="mb-3"><i class="bi bi-clock-history text-verde"></i> Mis reclamos</h5>
        <% if (reclamos.isEmpty()) { %>
          <p class="text-muted small mb-0">No tienes reclamos registrados.</p>
        <% } %>
        <% for (Reclamo r : reclamos) { %>
          <div class="border-bottom pb-2 mb-2">
            <div class="d-flex justify-content-between"><strong>Pedido #<%= r.getIdPedido() %></strong><span class="badge text-bg-<%= Html.badge(r.getEstado()) %>"><%= Html.estadoLegible(r.getEstado()) %></span></div>
            <div class="small text-muted"><%= Html.esc(r.getDescripcion()) %></div>
            <% if (r.getRespuesta() != null && !r.getRespuesta().isBlank()) { %>
              <div class="small text-success mt-1"><i class="bi bi-reply"></i> Soporte: <%= Html.esc(r.getRespuesta()) %></div>
            <% } %>
          </div>
        <% } %>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>

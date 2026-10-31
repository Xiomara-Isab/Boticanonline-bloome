<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.botica.model.*, com.botica.service.ConsultaService, com.botica.util.Html, java.time.LocalDate, java.util.*" %>
<%
  String titulo = "Panel de Almacén";
  Usuario yo = (Usuario) session.getAttribute("usuario");
  List<Pedido> pedidos = ConsultaService.pedidos();
  List<Producto> productos = ConsultaService.productos();
  Map<Integer, String> nombres = new HashMap<>();
  for (Usuario u : ConsultaService.usuarios()) nombres.put(u.getId(), u.getNombre());

  int porPreparar = 0, enDespacho = 0, enviados = 0;
  List<Pedido> activos = new ArrayList<>();
  for (Pedido p : pedidos) {
    if ("PAGADO".equals(p.getEstado())) porPreparar++;
    if ("EN_DESPACHO".equals(p.getEstado())) enDespacho++;
    if ("ENVIADO".equals(p.getEstado())) enviados++;
    if (!"ENTREGADO".equals(p.getEstado())) activos.add(p);
  }
  LocalDate limite = LocalDate.now().plusDays(com.botica.util.Constantes.DIAS_POR_VENCER);
  List<Producto> stockBajo = new ArrayList<>();
  List<Producto> porVencer = new ArrayList<>();
  for (Producto p : productos) {
    if (p.getStock() <= com.botica.util.Constantes.STOCK_BAJO) stockBajo.add(p);
    if (p.getFechaVencimiento() != null && !p.getFechaVencimiento().isAfter(limite)) porVencer.add(p);
  }
%>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="panel-banda">
  <div class="container"><h3 class="mb-0 fw-bold"><i class="bi bi-boxes"></i> Panel de Almacén</h3><span class="opacity-75">Hola, <%= Html.esc(yo.getNombre()) %> · prepara pedidos y controla el stock</span></div>
</div>

<div class="container my-4">
  <%@ include file="/WEB-INF/jspf/flash.jspf" %>

  <div class="row g-3 mb-4">
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono"><i class="bi bi-inboxes"></i></div><div><div class="valor"><%= porPreparar %></div><div class="etq">Por preparar (pagados)</div></div></div></div>
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono"><i class="bi bi-box-seam"></i></div><div><div class="valor"><%= enDespacho %></div><div class="etq">En despacho</div></div></div></div>
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono"><i class="bi bi-truck"></i></div><div><div class="valor"><%= enviados %></div><div class="etq">Enviados</div></div></div></div>
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono" style="background:#fdecea;color:#e63946;"><i class="bi bi-exclamation-triangle"></i></div><div><div class="valor"><%= stockBajo.size() %></div><div class="etq">Productos con stock bajo</div></div></div></div>
  </div>

  <div class="seccion mb-4">
    <h5 class="mb-3"><i class="bi bi-list-check text-verde"></i> Pedidos por gestionar</h5>
    <% if (activos.isEmpty()) { %><p class="text-muted mb-0">No hay pedidos pendientes de gestión.</p><% } else { %>
    <div class="table-responsive">
      <table class="table table-hover">
        <thead><tr><th>#</th><th>Cliente</th><th>Fecha</th><th>Estado</th><th class="text-end">Total</th><th></th></tr></thead>
        <tbody>
          <% for (Pedido p : activos) { %>
            <tr>
              <td>#<%= p.getId() %></td>
              <td><%= Html.esc(nombres.get(p.getIdCliente())) %></td>
              <td><%= Html.fecha(p.getFechaPedido()) %></td>
              <td><span class="badge text-bg-<%= Html.badge(p.getEstado()) %>"><%= Html.estadoLegible(p.getEstado()) %></span></td>
              <td class="text-end"><%= Html.money(p.getTotal()) %></td>
              <td class="text-end">
                <a class="btn btn-sm btn-outline-secondary" href="<%= ctx %>/seguimiento.jsp?id=<%= p.getId() %>">Ver</a>
                <form method="post" action="<%= ctx %>/avanzarPedido" class="d-inline">
                  <input type="hidden" name="idPedido" value="<%= p.getId() %>">
                  <input type="hidden" name="volver" value="panel">
                  <button class="btn btn-sm btn-primary">Avanzar estado</button>
                </form>
              </td>
            </tr>
          <% } %>
        </tbody>
      </table>
    </div>
    <% } %>
  </div>

  <div class="row g-4">
    <div class="col-lg-7">
      <div class="seccion">
        <h5 class="mb-3"><i class="bi bi-exclamation-triangle text-danger"></i> Stock bajo (reposición)</h5>
        <% if (stockBajo.isEmpty()) { %><p class="text-muted mb-0">Todo el inventario está en niveles saludables.</p><% } %>
        <% if (!stockBajo.isEmpty()) { %>
        <table class="table">
          <thead><tr><th>Producto</th><th>Stock</th><th>Ajustar</th></tr></thead>
          <tbody>
            <% for (Producto p : stockBajo) { %>
              <tr>
                <td><%= Html.esc(p.getNombre()) %></td>
                <td><span class="badge text-bg-<%= p.getStock() == 0 ? "danger" : "warning" %>"><%= p.getStock() %></span></td>
                <td>
                  <form method="post" action="<%= ctx %>/panel" class="d-flex gap-1">
                    <input type="hidden" name="accion" value="ajustarStock">
                    <input type="hidden" name="id" value="<%= p.getId() %>">
                    <input name="stock" type="number" min="0" value="<%= p.getStock() %>" class="form-control form-control-sm" style="width:90px;">
                    <button class="btn btn-sm btn-outline-primary"><i class="bi bi-check2"></i></button>
                  </form>
                </td>
              </tr>
            <% } %>
          </tbody>
        </table>
        <% } %>
      </div>
    </div>
    <div class="col-lg-5">
      <div class="seccion">
        <h5 class="mb-3"><i class="bi bi-calendar-x text-warning"></i> Por vencer (90 días)</h5>
        <% if (porVencer.isEmpty()) { %><p class="text-muted mb-0">Ningún producto vence pronto.</p><% } %>
        <% for (Producto p : porVencer) { %>
          <div class="d-flex justify-content-between border-bottom py-2">
            <span><%= Html.esc(p.getNombre()) %></span>
            <span class="badge text-bg-<%= p.getFechaVencimiento().isBefore(LocalDate.now()) ? "danger" : "warning" %>"><%= p.getFechaVencimiento() %></span>
          </div>
        <% } %>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>

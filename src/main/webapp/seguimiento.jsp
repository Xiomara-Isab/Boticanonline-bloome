<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.botica.model.*, com.botica.service.ConsultaService, com.botica.util.Html, java.util.*" %>
<%
  String titulo = "Seguimiento de pedido";
  Usuario actual = (Usuario) session.getAttribute("usuario");
  Pedido pedido = null;
  try {
    pedido = ConsultaService.pedidoPorId(Integer.parseInt(request.getParameter("id")));
  } catch (RuntimeException e) {
    pedido = null;
  }
  boolean autorizado = pedido != null && actual != null
      && ("Cliente".equals(actual.getRol()) ? pedido.getIdCliente() == actual.getId() : true);
  if (!autorizado) {
    response.sendRedirect(request.getContextPath() + "/panel");
    return;
  }
  String[] pasos = {"PENDIENTE", "PAGADO", "EN_DESPACHO", "ENVIADO", "ENTREGADO"};
  int indice = Arrays.asList(pasos).indexOf(pedido.getEstado());
  List<DetallePedido> detalles = ConsultaService.detallesDePedido(pedido.getId());
  boolean puedeAvanzar = ("Almacen".equals(actual.getRol()) || "Administrador".equals(actual.getRol())) && indice < pasos.length - 1;
%>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="container my-4" style="max-width: 900px;">
  <a href="<%= ctx %>/panel" class="text-decoration-none"><i class="bi bi-arrow-left"></i> Volver a mi panel</a>
  <div class="row g-4 mt-1">
    <div class="col-md-5">
      <div class="seccion h-100">
        <h5 class="mb-3">Pedido #<%= pedido.getId() %></h5>
        <p class="mb-1 text-muted small"><%= Html.fecha(pedido.getFechaPedido()) %></p>
        <span class="badge text-bg-<%= Html.badge(pedido.getEstado()) %> mb-3"><%= Html.estadoLegible(pedido.getEstado()) %></span>
        <ul class="timeline mt-3">
          <% for (int i = 0; i < pasos.length; i++) { %>
            <li class="<%= i <= indice ? "hecho" : "" %> <%= i == indice ? "actual" : "" %>"><span class="punto"></span><%= Html.estadoLegible(pasos[i]) %></li>
          <% } %>
        </ul>
        <% if (puedeAvanzar) { %>
          <form method="post" action="<%= ctx %>/avanzarPedido" class="mt-4">
            <input type="hidden" name="idPedido" value="<%= pedido.getId() %>">
            <button type="submit" class="btn btn-primary w-100">Avanzar al siguiente estado</button>
          </form>
        <% } %>
      </div>
    </div>
    <div class="col-md-7">
      <div class="seccion h-100">
        <h5 class="mb-3">Detalle del pedido</h5>
        <div class="table-responsive">
          <table class="table">
            <thead><tr><th>Producto</th><th class="text-center">Cant.</th><th class="text-end">Subtotal</th></tr></thead>
            <tbody>
              <% for (DetallePedido d : detalles) {
                   Producto pr = ConsultaService.productoPorId(d.getIdProducto()); %>
                <tr><td><%= Html.esc(pr == null ? "Producto #" + d.getIdProducto() : pr.getNombre()) %></td><td class="text-center"><%= d.getCantidad() %></td><td class="text-end"><%= Html.money(d.getSubtotal()) %></td></tr>
              <% } %>
            </tbody>
            <tfoot><tr><th colspan="2" class="text-end">Total</th><th class="text-end"><%= Html.money(pedido.getTotal()) %></th></tr></tfoot>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>

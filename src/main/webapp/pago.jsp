<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.botica.model.*, com.botica.patterns.pago.*, com.botica.service.ConsultaService, com.botica.util.Html, java.util.*" %>
<%
  String titulo = "Pagar pedido";
  Usuario actual = (Usuario) session.getAttribute("usuario");
  Pedido pedido = null;
  try {
    pedido = ConsultaService.pedidoPorId(Integer.parseInt(request.getParameter("id")));
  } catch (RuntimeException e) {
    pedido = null;
  }
  boolean puedePagar = pedido != null && actual != null && "Cliente".equals(actual.getRol())
      && pedido.getIdCliente() == actual.getId() && "PENDIENTE".equals(pedido.getEstado());
  if (!puedePagar) {
    response.sendRedirect(request.getContextPath() + "/panel");
    return;
  }
  List<DetallePedido> detalles = ConsultaService.detallesDePedido(pedido.getId());
  List<MetodoPago> metodos = MetodoPagoFactory.disponibles();
%>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="container my-4" style="max-width: 980px;">
  <a href="<%= ctx %>/panel" class="text-decoration-none"><i class="bi bi-arrow-left"></i> Volver a mi cuenta</a>
  <%@ include file="/WEB-INF/jspf/flash.jspf" %>
  <div class="row g-4 mt-1">
    <div class="col-md-5">
      <div class="seccion h-100">
        <h5 class="mb-3"><i class="bi bi-receipt text-verde"></i> Resumen del pedido #<%= pedido.getId() %></h5>
        <table class="table table-sm">
          <tbody>
            <% for (DetallePedido d : detalles) {
                 Producto pr = ConsultaService.productoPorId(d.getIdProducto()); %>
              <tr><td><%= Html.esc(pr == null ? "Producto #" + d.getIdProducto() : pr.getNombre()) %> × <%= d.getCantidad() %></td><td class="text-end"><%= Html.money(d.getSubtotal()) %></td></tr>
            <% } %>
          </tbody>
          <tfoot><tr><th>Total a pagar</th><th class="text-end"><%= Html.money(pedido.getTotal()) %></th></tr></tfoot>
        </table>
      </div>
    </div>
    <div class="col-md-7">
      <div class="seccion">
        <h5 class="mb-3"><i class="bi bi-credit-card text-verde"></i> Elige cómo pagar</h5>
        <form method="post" action="<%= ctx %>/pagar">
          <input type="hidden" name="idPedido" value="<%= pedido.getId() %>">
          <% for (int i = 0; i < metodos.size(); i++) {
               MetodoPago m = metodos.get(i); %>
            <div class="form-check metodo-pago mb-2">
              <input class="form-check-input" type="radio" name="metodo" id="metodo<%= i %>" value="<%= m.codigo() %>" data-ayuda="<%= Html.esc(m.ayuda()) %>" <%= i == 0 ? "checked" : "" %> required>
              <label class="form-check-label w-100" for="metodo<%= i %>"><%= Html.esc(m.etiqueta()) %></label>
            </div>
          <% } %>
          <div class="mb-3 mt-3">
            <label class="form-label" for="dato" id="ayudaDato"><%= Html.esc(metodos.get(0).ayuda()) %></label>
            <input type="text" name="dato" id="dato" class="form-control form-control-lg" autocomplete="off" required>
          </div>
          <button class="btn btn-primary btn-lg w-100"><i class="bi bi-lock"></i> Pagar <%= Html.money(pedido.getTotal()) %></button>
        </form>
      </div>
    </div>
  </div>
</div>

<script>
  document.querySelectorAll('input[name="metodo"]').forEach(function (radio) {
    radio.addEventListener('change', function () {
      document.getElementById('ayudaDato').textContent = radio.dataset.ayuda;
      document.getElementById('dato').value = '';
    });
  });
</script>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>

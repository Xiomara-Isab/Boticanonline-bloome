<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.botica.model.*, com.botica.service.ConsultaService, com.botica.util.Html, java.util.*" %>
<%
  String titulo = "Mi carrito";
  Usuario actual = (Usuario) session.getAttribute("usuario");
  if (actual == null || !"Cliente".equals(actual.getRol())) {
    response.sendRedirect(request.getContextPath() + "/panel");
    return;
  }
  Carrito carrito = (Carrito) session.getAttribute("carrito");
  if (carrito == null) carrito = new Carrito();

  double total = 0;
  boolean hayProblemas = false;
  List<Object[]> lineas = new ArrayList<>();   // {producto, cantidad, subtotal, aviso}
  for (Map.Entry<Integer, Integer> linea : carrito.getLineas().entrySet()) {
    Producto prod = ConsultaService.productoPorId(linea.getKey());
    int cant = linea.getValue();
    String aviso = null;
    if (prod == null || !prod.isActivo()) aviso = "Este producto ya no está disponible";
    else if (cant > prod.getStock()) aviso = "Solo quedan " + prod.getStock() + " unidades";
    double sub = prod == null ? 0 : prod.getPrecioFinal() * cant;
    if (aviso == null) total += sub; else hayProblemas = true;
    lineas.add(new Object[]{prod, cant, sub, aviso, linea.getKey()});
  }
%>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="container my-4" style="max-width: 980px;">
  <%@ include file="/WEB-INF/jspf/flash.jspf" %>
  <h2 class="fw-bold mb-3"><i class="bi bi-cart3 text-verde"></i> Mi carrito</h2>

  <% if (lineas.isEmpty()) { %>
    <div class="seccion text-center py-5">
      <i class="bi bi-cart-x display-4 text-muted"></i>
      <p class="mt-3 mb-3">Tu carrito está vacío.</p>
      <a href="<%= ctx %>/catalogo.jsp" class="btn btn-primary">Ver catálogo</a>
    </div>
  <% } else { %>
    <div class="seccion">
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead><tr><th>Producto</th><th class="text-end">Precio</th><th style="width: 190px;">Cantidad</th><th class="text-end">Subtotal</th><th></th></tr></thead>
          <tbody>
            <% for (Object[] l : lineas) {
                 Producto prod = (Producto) l[0];
                 int cant = (Integer) l[1];
                 double sub = (Double) l[2];
                 String aviso = (String) l[3];
                 int idProd = (Integer) l[4]; %>
              <tr>
                <td>
                  <strong><%= Html.esc(prod == null ? "Producto #" + idProd : prod.getNombre()) %></strong>
                  <% if (aviso != null) { %><div class="small text-danger"><i class="bi bi-exclamation-triangle"></i> <%= Html.esc(aviso) %></div><% } %>
                </td>
                <td class="text-end">
                  <% if (prod != null && prod.getDescuento() > 0) { %><span class="text-muted text-decoration-line-through small"><%= Html.money(prod.getPrecio()) %></span> <span class="badge text-bg-danger">-<%= prod.getDescuento() %>%</span><br><% } %>
                  <%= prod == null ? "—" : Html.money(prod.getPrecioFinal()) %>
                </td>
                <td>
                  <form method="post" action="<%= ctx %>/carrito" class="d-flex gap-1">
                    <input type="hidden" name="accion" value="cambiar">
                    <input type="hidden" name="idProducto" value="<%= idProd %>">
                    <input type="number" name="cantidad" min="0" value="<%= cant %>" class="form-control form-control-sm" style="width: 80px;">
                    <button class="btn btn-sm btn-outline-primary" title="Actualizar cantidad"><i class="bi bi-arrow-repeat"></i></button>
                  </form>
                </td>
                <td class="text-end"><%= Html.money(sub) %></td>
                <td class="text-end">
                  <form method="post" action="<%= ctx %>/carrito">
                    <input type="hidden" name="accion" value="quitar">
                    <input type="hidden" name="idProducto" value="<%= idProd %>">
                    <button class="btn btn-sm btn-outline-danger" title="Quitar del carrito"><i class="bi bi-trash"></i></button>
                  </form>
                </td>
              </tr>
            <% } %>
          </tbody>
          <tfoot>
            <tr><th colspan="3" class="text-end fs-5">Total</th><th class="text-end fs-5"><%= Html.money(total) %></th><th></th></tr>
          </tfoot>
        </table>
      </div>

      <div class="d-flex flex-wrap gap-2 justify-content-between mt-4">
        <div class="d-flex gap-2">
          <a href="<%= ctx %>/catalogo.jsp" class="btn btn-outline-primary"><i class="bi bi-arrow-left"></i> Seguir comprando</a>
          <form method="post" action="<%= ctx %>/carrito" onsubmit="return confirm('¿Vaciar el carrito?');">
            <input type="hidden" name="accion" value="vaciar">
            <button class="btn btn-outline-secondary">Vaciar carrito</button>
          </form>
        </div>
        <form method="post" action="<%= ctx %>/carrito">
          <input type="hidden" name="accion" value="finalizar">
          <button class="btn btn-primary btn-lg" <%= hayProblemas ? "disabled" : "" %>><i class="bi bi-bag-check"></i> Finalizar compra</button>
        </form>
      </div>
      <% if (hayProblemas) { %><p class="small text-danger mt-2 mb-0">Corrige o quita los productos marcados para poder finalizar la compra.</p><% } %>
    </div>
  <% } %>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>

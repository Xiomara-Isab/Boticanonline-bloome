<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.botica.model.*, com.botica.service.ConsultaService, com.botica.util.Html, java.time.LocalDate, java.util.*" %>
<%
  String titulo = "Panel de Vendedor";
  Usuario yo = (Usuario) session.getAttribute("usuario");
  List<Producto> productos = ConsultaService.productos();
  Set<Integer> misProductos = new HashSet<>();
  for (Producto p : productos) if (p.getIdVendedor() == yo.getId()) misProductos.add(p.getId());
  Map<Integer, String> nombresProd = new HashMap<>();
  for (Producto p : productos) nombresProd.put(p.getId(), p.getNombre());
  List<String[]> misVentas = new ArrayList<>();
  double totalVendido = 0;
  for (Pedido ped : ConsultaService.pedidos()) {
    for (DetallePedido d : ConsultaService.detallesDePedido(ped.getId())) {
      if (misProductos.contains(d.getIdProducto())) {
        misVentas.add(new String[]{String.valueOf(ped.getId()), nombresProd.get(d.getIdProducto()), String.valueOf(d.getCantidad()), Html.money(d.getSubtotal()), ped.getEstado()});
        totalVendido += d.getSubtotal();
      }
    }
  }
  int unidades = 0, bajo = 0;
  double valor = 0;
  for (Producto p : productos) {
    unidades += p.getStock();
    valor += p.getStock() * p.getPrecio();
    if (p.getStock() <= com.botica.util.Constantes.STOCK_BAJO) bajo++;
  }
%>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="panel-banda">
  <div class="container"><h3 class="mb-0 fw-bold"><i class="bi bi-shop"></i> Panel de Vendedor</h3><span class="opacity-75">Hola, <%= Html.esc(yo.getNombre()) %> · gestiona el catálogo y los precios</span></div>
</div>

<div class="container my-4">
  <%@ include file="/WEB-INF/jspf/flash.jspf" %>

  <div class="row g-3 mb-4">
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono"><i class="bi bi-box-seam"></i></div><div><div class="valor"><%= productos.size() %></div><div class="etq">Productos en catálogo</div></div></div></div>
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono"><i class="bi bi-stack"></i></div><div><div class="valor"><%= unidades %></div><div class="etq">Unidades en stock</div></div></div></div>
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono"><i class="bi bi-cash-coin"></i></div><div><div class="valor"><%= Html.money(valor) %></div><div class="etq">Valor del inventario</div></div></div></div>
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono" style="background:#fdecea;color:#e63946;"><i class="bi bi-exclamation-triangle"></i></div><div><div class="valor"><%= bajo %></div><div class="etq">Con stock bajo (≤10)</div></div></div></div>
  </div>

  <div class="row g-4">
    <div class="col-lg-4">
      <div class="seccion">
        <h5 class="mb-3"><i class="bi bi-plus-circle text-verde"></i> Agregar producto</h5>
        <form method="post" action="<%= ctx %>/panel">
          <input type="hidden" name="accion" value="crearProducto">
          <div class="mb-2"><input name="nombre" class="form-control" placeholder="Nombre" required></div>
          <div class="mb-2"><textarea name="descripcion" class="form-control" rows="2" placeholder="Descripción"></textarea></div>
          <div class="row g-2 mb-2">
            <div class="col-6"><input name="precio" type="number" step="0.01" min="0" class="form-control" placeholder="Precio S/" required></div>
            <div class="col-6"><input name="stock" type="number" min="0" class="form-control" placeholder="Stock" required></div>
          </div>
          <div class="mb-2">
            <select name="categoria" class="form-select">
              <option>Analgésicos</option><option>Vitaminas</option><option>Cuidado personal</option>
              <option>Primeros auxilios</option><option>Bebés</option><option>Dermocosmética</option>
            </select>
          </div>
          <div class="mb-3"><label class="form-label small text-muted mb-0">Fecha de vencimiento (opcional)</label><input name="fechaVencimiento" type="date" class="form-control"></div>
          <button class="btn btn-primary w-100">Guardar producto</button>
        </form>
      </div>
    </div>

    <div class="col-lg-8">
      <div class="seccion">
        <h5 class="mb-3"><i class="bi bi-list-ul text-verde"></i> Inventario y precios</h5>
        <div class="table-responsive">
          <table class="table table-hover">
            <thead><tr><th>Producto</th><th>Vence</th><th>Precio (S/) y stock</th><th></th></tr></thead>
            <tbody>
              <% for (Producto p : productos) { %>
                <tr>
                  <td><strong><%= Html.esc(p.getNombre()) %></strong><br><span class="badge text-bg-light"><%= Html.esc(p.getCategoria()) %></span></td>
                  <td class="small"><%= p.getFechaVencimiento() == null ? "-" : p.getFechaVencimiento() %></td>
                  <td>
                    <form method="post" action="<%= ctx %>/panel" class="d-flex gap-1">
                      <input type="hidden" name="accion" value="actualizarProducto">
                      <input type="hidden" name="id" value="<%= p.getId() %>">
                      <input name="precio" type="number" step="0.01" min="0" value="<%= p.getPrecio() %>" class="form-control form-control-sm" style="width:90px;">
                      <input name="stock" type="number" min="0" value="<%= p.getStock() %>" class="form-control form-control-sm" style="width:80px;">
                      <button class="btn btn-sm btn-outline-primary" title="Guardar"><i class="bi bi-check2"></i></button>
                    </form>
                  </td>
                  <td>
                    <form method="post" action="<%= ctx %>/panel" onsubmit="return confirm('¿Eliminar este producto?');">
                      <input type="hidden" name="accion" value="eliminarProducto">
                      <input type="hidden" name="id" value="<%= p.getId() %>">
                      <button class="btn btn-sm btn-outline-danger" title="Eliminar"><i class="bi bi-trash"></i></button>
                    </form>
                  </td>
                </tr>
              <% } %>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <div class="seccion mt-4">
    <h5 class="mb-3"><i class="bi bi-receipt text-verde"></i> Pedidos con mis productos <small class="text-muted">· vendido: <%= Html.money(totalVendido) %></small></h5>
    <% if (misVentas.isEmpty()) { %><p class="text-muted mb-0">Todavía no hay pedidos con tus productos.</p><% } else { %>
    <div class="table-responsive">
      <table class="table table-sm">
        <thead><tr><th>Pedido</th><th>Producto</th><th class="text-center">Cant.</th><th class="text-end">Subtotal</th><th>Estado</th></tr></thead>
        <tbody>
          <% for (String[] f : misVentas) { %>
            <tr><td><a href="<%= ctx %>/seguimiento.jsp?id=<%= f[0] %>">#<%= f[0] %></a></td><td><%= Html.esc(f[1]) %></td><td class="text-center"><%= f[2] %></td><td class="text-end"><%= f[3] %></td><td><span class="badge text-bg-<%= Html.badge(f[4]) %>"><%= Html.estadoLegible(f[4]) %></span></td></tr>
          <% } %>
        </tbody>
      </table>
    </div>
    <% } %>
  </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.botica.model.*, com.botica.service.ConsultaService, com.botica.util.Html, java.util.*" %>
<%
  String titulo = "Panel de Administración";
  Usuario yo = (Usuario) session.getAttribute("usuario");
  List<Usuario> usuarios = ConsultaService.usuarios();
  List<Producto> productos = ConsultaService.productos();
  List<Pedido> pedidos = ConsultaService.pedidos();
  List<Reclamo> reclamos = ConsultaService.reclamos();
  Map<Integer, String> nombres = new HashMap<>();
  Map<String, Integer> porRol = new LinkedHashMap<>();
  for (String r : new String[]{"Cliente", "Vendedor", "Almacen", "Soporte", "Administrador"}) porRol.put(r, 0);
  for (Usuario u : usuarios) {
    nombres.put(u.getId(), u.getNombre());
    porRol.merge(u.getRol(), 1, Integer::sum);
  }
  double ventas = 0;
  for (Pedido p : pedidos) ventas += p.getTotal();
  int reclamosAbiertos = 0;
  for (Reclamo r : reclamos) if (!"RESUELTO".equals(r.getEstado())) reclamosAbiertos++;
%>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="panel-banda">
  <div class="container"><h3 class="mb-0 fw-bold"><i class="bi bi-shield-check"></i> Panel de Administración</h3><span class="opacity-75">Hola, <%= Html.esc(yo.getNombre()) %> · visión general del negocio y gestión de usuarios</span></div>
</div>

<div class="container my-4">
  <%@ include file="/WEB-INF/jspf/flash.jspf" %>

  <div class="row g-3 mb-4">
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono"><i class="bi bi-people"></i></div><div><div class="valor"><%= usuarios.size() %></div><div class="etq">Usuarios registrados</div></div></div></div>
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono"><i class="bi bi-box-seam"></i></div><div><div class="valor"><%= productos.size() %></div><div class="etq">Productos</div></div></div></div>
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono"><i class="bi bi-receipt"></i></div><div><div class="valor"><%= pedidos.size() %></div><div class="etq">Pedidos · <%= Html.money(ventas) %></div></div></div></div>
    <div class="col-6 col-lg-3"><div class="kpi"><div class="icono" style="background:#fff4d6;color:#b58105;"><i class="bi bi-headset"></i></div><div><div class="valor"><%= reclamosAbiertos %></div><div class="etq">Reclamos sin resolver</div></div></div></div>
  </div>

  <div class="row g-4 mb-4">
    <div class="col-lg-8">
      <div class="seccion h-100">
        <h5 class="mb-3"><i class="bi bi-people text-verde"></i> Usuarios del sistema</h5>
        <div class="d-flex flex-wrap gap-2 mb-3">
          <% for (Map.Entry<String, Integer> e : porRol.entrySet()) { %>
            <span class="badge text-bg-light border"><%= e.getKey() %>: <%= e.getValue() %></span>
          <% } %>
        </div>
        <div class="table-responsive" style="max-height: 320px; overflow-y: auto;">
          <table class="table table-sm table-hover">
            <thead><tr><th>#</th><th>Nombre</th><th>Correo</th><th>Rol</th><th>Cambiar rol</th></tr></thead>
            <tbody>
              <% for (Usuario u : usuarios) { %>
                <tr><td><%= u.getId() %></td><td><%= Html.esc(u.getNombre()) %></td><td><%= Html.esc(u.getCorreo()) %></td><td><span class="badge text-bg-secondary"><%= Html.esc(u.getRol()) %></span></td>
                  <td>
                    <% if (u.getId() != yo.getId()) { %>
                    <form method="post" action="<%= ctx %>/panel" class="d-flex gap-1">
                      <input type="hidden" name="accion" value="cambiarRol">
                      <input type="hidden" name="id" value="<%= u.getId() %>">
                      <select name="rol" class="form-select form-select-sm" style="width:130px;">
                        <% for (String r : porRol.keySet()) { %><option value="<%= r %>" <%= r.equals(u.getRol()) ? "selected" : "" %>><%= r %></option><% } %>
                      </select>
                      <button class="btn btn-sm btn-outline-primary"><i class="bi bi-check2"></i></button>
                    </form>
                    <% } else { %><span class="text-muted small">(tú)</span><% } %>
                  </td></tr>
              <% } %>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <div class="col-lg-4">
      <div class="seccion h-100">
        <h5 class="mb-3"><i class="bi bi-person-plus text-verde"></i> Crear usuario</h5>
        <form method="post" action="<%= ctx %>/panel">
          <input type="hidden" name="accion" value="crearUsuario">
          <div class="mb-2"><input name="nombre" class="form-control" placeholder="Nombre completo" required></div>
          <div class="mb-2"><input name="correo" type="email" class="form-control" placeholder="Correo" required></div>
          <div class="mb-2"><input name="contrasena" type="password" minlength="6" class="form-control" placeholder="Contraseña (mín. 6)" required></div>
          <div class="mb-3">
            <select name="rol" class="form-select">
              <option value="Vendedor">Vendedor</option>
              <option value="Almacen">Encargado de Almacén</option>
              <option value="Soporte">Soporte al Cliente</option>
              <option value="Administrador">Administrador</option>
              <option value="Cliente">Cliente</option>
            </select>
          </div>
          <button class="btn btn-primary w-100">Crear usuario</button>
        </form>
      </div>
    </div>
  </div>

  <div class="row g-4">
    <div class="col-lg-7">
      <div class="seccion">
        <h5 class="mb-3"><i class="bi bi-receipt text-verde"></i> Últimos pedidos</h5>
        <div class="table-responsive">
          <table class="table table-hover">
            <thead><tr><th>#</th><th>Cliente</th><th>Estado</th><th class="text-end">Total</th><th></th></tr></thead>
            <tbody>
              <% int n = 0; for (Pedido p : pedidos) { if (n++ >= 8) break; %>
                <tr>
                  <td>#<%= p.getId() %></td>
                  <td><%= Html.esc(nombres.get(p.getIdCliente())) %></td>
                  <td><span class="badge text-bg-<%= Html.badge(p.getEstado()) %>"><%= Html.estadoLegible(p.getEstado()) %></span></td>
                  <td class="text-end"><%= Html.money(p.getTotal()) %></td>
                  <td class="text-end"><a class="btn btn-sm btn-outline-primary" href="<%= ctx %>/seguimiento.jsp?id=<%= p.getId() %>">Ver</a></td>
                </tr>
              <% } %>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <div class="col-lg-5">
      <div class="seccion">
        <h5 class="mb-3"><i class="bi bi-exclamation-triangle text-danger"></i> Productos con stock bajo</h5>
        <%
          int bajos = 0;
          for (Producto p : productos) {
            if (p.getStock() <= com.botica.util.Constantes.STOCK_BAJO) { bajos++; %>
              <div class="d-flex justify-content-between border-bottom py-2"><span><%= Html.esc(p.getNombre()) %></span><span class="badge text-bg-<%= p.getStock() == 0 ? "danger" : "warning" %>"><%= p.getStock() %> u.</span></div>
        <%  }
          }
          if (bajos == 0) { %><p class="text-muted mb-0">Sin alertas de stock.</p><% } %>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.botica.model.Usuario, com.botica.model.Producto, com.botica.service.ConsultaService, com.botica.util.Html, java.util.*" %>
<%
  String titulo = "Catálogo";
  String q = request.getParameter("q");
  String cat = request.getParameter("categoria");
  List<Producto> todos = new ArrayList<>();
  String errorBD = null;
  try {
    todos = ConsultaService.productos();
  } catch (RuntimeException e) {
    errorBD = "No se pudo cargar el catálogo en este momento.";
  }
  TreeSet<String> categorias = new TreeSet<>();
  List<Producto> productos = new ArrayList<>();
  for (Producto p : todos) {
    if (p.getCategoria() != null && !p.getCategoria().isBlank()) categorias.add(p.getCategoria());
    boolean okCat = cat == null || cat.isBlank() || cat.equals(p.getCategoria());
    String texto = (p.getNombre() + " " + (p.getDescripcion() == null ? "" : p.getDescripcion())).toLowerCase();
    boolean okQ = q == null || q.isBlank() || texto.contains(q.toLowerCase());
    if (okCat && okQ) productos.add(p);
  }
%>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="container my-4">
  <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-3">
    <h2 class="fw-bold mb-0">Catálogo de productos</h2>
    <form method="get" class="d-flex gap-2">
      <% if (cat != null && !cat.isBlank()) { %><input type="hidden" name="categoria" value="<%= Html.esc(cat) %>"><% } %>
      <input type="text" name="q" value="<%= Html.esc(q) %>" class="form-control" placeholder="Buscar producto...">
      <button class="btn btn-primary"><i class="bi bi-search"></i></button>
    </form>
  </div>

  <div class="mb-4 d-flex flex-wrap gap-2">
    <a href="catalogo.jsp" class="btn btn-sm <%= (cat == null || cat.isBlank()) ? "btn-primary" : "btn-outline-primary" %>">Todos</a>
    <% for (String c : categorias) { %>
      <a href="catalogo.jsp?categoria=<%= java.net.URLEncoder.encode(c, "UTF-8") %>" class="btn btn-sm <%= c.equals(cat) ? "btn-primary" : "btn-outline-primary" %>"><%= Html.esc(c) %></a>
    <% } %>
  </div>

  <% if (request.getAttribute("error") != null) { %>
    <div class="alert alert-danger"><%= Html.esc(request.getAttribute("error")) %></div>
  <% } %>
  <% if (errorBD != null) { %>
    <div class="alert alert-warning"><%= errorBD %></div>
  <% } %>
  <% if (productos.isEmpty() && errorBD == null) { %>
    <div class="alert alert-light border">No encontramos productos con ese criterio.</div>
  <% } %>

  <div class="row g-4">
    <% for (Producto p : productos) {
         boolean agotado = p.getStock() <= 0;
    %>
      <div class="col-md-6 col-lg-4">
        <div class="card prod-card h-100">
          <div class="prod-banner" style="background: <%= Html.color(p.getCategoria()) %>;"><i class="bi <%= Html.icono(p.getCategoria()) %>"></i></div>
          <div class="card-body d-flex flex-column">
            <div class="d-flex justify-content-between mb-2">
              <span class="badge text-bg-light"><%= Html.esc(p.getCategoria()) %></span>
              <% if (agotado) { %><span class="badge text-bg-danger">Agotado</span>
              <% } else if (p.getStock() <= com.botica.util.Constantes.STOCK_BAJO) { %><span class="badge text-bg-warning">Últimas <%= p.getStock() %> unidades</span>
              <% } else { %><span class="badge text-bg-success">En stock</span><% } %>
            </div>
            <h5 class="card-title"><%= Html.esc(p.getNombre()) %></h5>
            <p class="card-text text-muted small flex-grow-1"><%= Html.esc(p.getDescripcion()) %></p>
            <% if (p.getFechaVencimiento() != null) { %>
              <p class="small text-muted mb-2"><i class="bi bi-calendar-event"></i> Vence: <%= p.getFechaVencimiento() %></p>
            <% } %>
            <div class="precio mb-2"><%= Html.money(p.getPrecio()) %></div>

            <% if (agotado) { %>
              <button class="btn btn-secondary w-100" disabled>Sin stock</button>
            <% } else if (navU == null) { %>
              <a class="btn btn-primary w-100" href="login.jsp?compra=1"><i class="bi bi-person"></i> Inicia sesión para comprar</a>
            <% } else if (!"Cliente".equals(navU.getRol())) { %>
              <button class="btn btn-secondary w-100" disabled>Solo clientes pueden comprar</button>
            <% } else { %>
              <form method="post" action="comprar" class="d-flex gap-2">
                <input type="hidden" name="idProducto" value="<%= p.getId() %>">
                <input type="number" name="cantidad" value="1" min="1" max="<%= p.getStock() %>" class="form-control" style="width: 90px;">
                <button type="submit" class="btn btn-primary flex-grow-1"><i class="bi bi-bag-plus"></i> Comprar</button>
              </form>
            <% } %>
          </div>
        </div>
      </div>
    <% } %>
  </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>

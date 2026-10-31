<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.botica.model.Producto, com.botica.service.ConsultaService, com.botica.util.Html, java.util.*" %>
<%
  String titulo = "Inicio";
  List<Producto> destacados = new ArrayList<>();
  try {
    for (Producto p : ConsultaService.productos()) {
      if (p.getStock() > 0 && destacados.size() < 6) destacados.add(p);
    }
  } catch (RuntimeException e) {
    // Si la base de datos no responde, la portada igual se muestra.
  }
  String[][] categorias = {
    {"Analgésicos", "bi-capsule", "#e76f51"},
    {"Vitaminas", "bi-brightness-high", "#f4a261"},
    {"Cuidado personal", "bi-droplet-half", "#2a9d8f"},
    {"Primeros auxilios", "bi-bandaid", "#e63946"},
    {"Bebés", "bi-balloon-heart", "#6c8ebf"},
    {"Dermocosmética", "bi-stars", "#9b72cf"}
  };
%>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<section class="hero">
  <div class="container">
    <div class="row align-items-center">
      <div class="col-lg-7">
        <h1 class="display-4 mb-3">Tu botica de confianza, ahora en línea</h1>
        <p class="lead mb-4">Medicamentos, vitaminas y cuidado personal con seguimiento de tu pedido paso a paso.</p>
        <form action="catalogo.jsp" method="get" class="buscador d-flex" style="max-width: 520px;">
          <input type="text" name="q" class="form-control" placeholder="Busca un producto: paracetamol, vitamina C...">
          <button class="btn btn-primary rounded-pill px-4" type="submit"><i class="bi bi-search"></i></button>
        </form>
        <% if (navU == null) { %>
          <p class="mt-4 mb-0">¿Primera vez? <a class="text-white fw-bold" href="registro.jsp">Crea tu cuenta gratis</a> o <a class="text-white fw-bold" href="login.jsp">inicia sesión</a>.</p>
        <% } %>
      </div>
      <div class="col-lg-5 d-none d-lg-block text-center"><i class="bi bi-capsule-pill hero-icono"></i></div>
    </div>
  </div>
</section>

<div class="container my-5">
  <h3 class="fw-bold mb-3">Compra por categoría</h3>
  <div class="row g-3">
    <% for (String[] c : categorias) { %>
      <div class="col-6 col-md-4 col-lg-2">
        <a class="cat-tile" href="catalogo.jsp?categoria=<%= java.net.URLEncoder.encode(c[0], "UTF-8") %>">
          <i class="bi <%= c[1] %>" style="color: <%= c[2] %>;"></i>
          <span class="fw-semibold small"><%= c[0] %></span>
        </a>
      </div>
    <% } %>
  </div>
</div>

<div class="container my-5">
  <div class="d-flex justify-content-between align-items-end mb-3">
    <h3 class="fw-bold mb-0">Productos destacados</h3>
    <a href="catalogo.jsp" class="fw-semibold text-decoration-none">Ver todo el catálogo <i class="bi bi-arrow-right"></i></a>
  </div>
  <% if (destacados.isEmpty()) { %>
    <div class="alert alert-light border">Pronto tendremos productos disponibles.</div>
  <% } %>
  <div class="row g-4">
    <% for (Producto p : destacados) { %>
      <div class="col-md-6 col-lg-4">
        <div class="card prod-card h-100">
          <div class="prod-banner" style="background: <%= Html.color(p.getCategoria()) %>;"><i class="bi <%= Html.icono(p.getCategoria()) %>"></i></div>
          <div class="card-body d-flex flex-column">
            <span class="badge text-bg-light align-self-start mb-2"><%= Html.esc(p.getCategoria()) %></span>
            <h5 class="card-title"><%= Html.esc(p.getNombre()) %></h5>
            <p class="card-text text-muted small flex-grow-1"><%= Html.esc(p.getDescripcion()) %></p>
            <div class="d-flex justify-content-between align-items-center">
              <span class="precio"><%= Html.money(p.getPrecio()) %></span>
              <a class="btn btn-outline-primary btn-sm" href="catalogo.jsp?q=<%= java.net.URLEncoder.encode(p.getNombre(), "UTF-8") %>">Ver</a>
            </div>
          </div>
        </div>
      </div>
    <% } %>
  </div>
</div>

<div class="container my-5">
  <div class="row g-4 text-center">
    <div class="col-md-4"><div class="beneficio"><i class="bi bi-bag-check"></i><h5 class="mt-2">Compra fácil</h5><p class="text-muted mb-0">Elige tus productos y confirma tu pedido en segundos.</p></div></div>
    <div class="col-md-4"><div class="beneficio"><i class="bi bi-truck"></i><h5 class="mt-2">Seguimiento del pedido</h5><p class="text-muted mb-0">Pagado, en despacho, enviado y entregado: siempre sabes dónde está.</p></div></div>
    <div class="col-md-4"><div class="beneficio"><i class="bi bi-headset"></i><h5 class="mt-2">Soporte cercano</h5><p class="text-muted mb-0">¿Algún problema? Registra un reclamo y te ayudamos.</p></div></div>
  </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>

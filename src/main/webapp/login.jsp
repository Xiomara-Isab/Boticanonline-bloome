<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.botica.util.Html" %>
<% String titulo = "Iniciar sesión"; %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="container auth-wrap">
  <div class="card auth-card mx-auto" style="max-width: 860px; width: 100%;">
    <div class="row g-0">
      <div class="col-md-5 auth-lado d-none d-md-flex">
        <i class="bi bi-capsule-pill mb-3"></i>
        <h3 class="fw-bold">Bienvenido a Botica Bloome</h3>
        <p class="mb-0">Inicia sesión para comprar, ver el seguimiento de tus pedidos o acceder a tu panel de trabajo.</p>
      </div>
      <div class="col-md-7 p-4 p-md-5 bg-white">
        <h4 class="fw-bold mb-3">Iniciar sesión</h4>

        <% if (request.getParameter("registrado") != null) { %>
          <div class="alert alert-success">Cuenta creada, ya puedes iniciar sesión.</div>
        <% } %>
        <% if (request.getParameter("compra") != null) { %>
          <div class="alert alert-info">Inicia sesión o crea una cuenta para completar tu compra.</div>
        <% } %>
        <% if (request.getParameter("sesion") != null) { %>
          <div class="alert alert-info">Necesitas iniciar sesión para ver esa página.</div>
        <% } %>
        <% if (request.getAttribute("error") != null) { %>
          <div class="alert alert-danger"><%= Html.esc(request.getAttribute("error")) %></div>
        <% } %>

        <form method="post" action="<%= ctx %>/login">
          <div class="mb-3">
            <label class="form-label">Correo</label>
            <input type="email" name="correo" class="form-control form-control-lg" required autofocus>
          </div>
          <div class="mb-3">
            <label class="form-label">Contraseña</label>
            <input type="password" name="contrasena" class="form-control form-control-lg" required>
          </div>
          <button type="submit" class="btn btn-primary btn-lg w-100">Entrar</button>
        </form>
        <p class="mt-3 text-center mb-0">¿No tienes cuenta? <a href="registro.jsp" class="fw-semibold">Crear una cuenta</a></p>
        <p class="text-center mb-0 mt-2"><a href="index.jsp" class="small text-muted text-decoration-none"><i class="bi bi-arrow-left"></i> Volver a la tienda</a></p>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>

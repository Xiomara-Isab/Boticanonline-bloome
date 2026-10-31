<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.botica.util.Html" %>
<% String titulo = "Crear cuenta"; %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="container auth-wrap">
  <div class="card auth-card mx-auto" style="max-width: 860px; width: 100%;">
    <div class="row g-0">
      <div class="col-md-5 auth-lado d-none d-md-flex">
        <i class="bi bi-person-plus mb-3"></i>
        <h3 class="fw-bold">Únete a Botica Bloome</h3>
        <p class="mb-0">Crea tu cuenta de cliente para comprar online, seguir tus pedidos y reportar cualquier inconveniente.</p>
      </div>
      <div class="col-md-7 p-4 p-md-5 bg-white">
        <h4 class="fw-bold mb-3">Crear cuenta</h4>

        <% if (request.getAttribute("error") != null) { %>
          <div class="alert alert-danger"><%= Html.esc(request.getAttribute("error")) %></div>
        <% } %>

        <form method="post" action="<%= ctx %>/registro" onsubmit="return validar();">
          <div class="mb-3">
            <label class="form-label">Nombre completo</label>
            <input type="text" name="nombre" class="form-control" required>
          </div>
          <div class="mb-3">
            <label class="form-label">Correo</label>
            <input type="email" name="correo" class="form-control" required>
          </div>
          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label">Contraseña</label>
              <input type="password" name="contrasena" id="pass1" class="form-control" minlength="6" required>
            </div>
            <div class="col-md-6 mb-3">
              <label class="form-label">Repite la contraseña</label>
              <input type="password" id="pass2" class="form-control" minlength="6" required>
            </div>
          </div>
          <div id="msgPass" class="text-danger small mb-2" style="display:none;">Las contraseñas no coinciden.</div>
          <button type="submit" class="btn btn-primary btn-lg w-100">Registrarme</button>
        </form>
        <p class="mt-3 text-center mb-0">¿Ya tienes cuenta? <a href="login.jsp" class="fw-semibold">Inicia sesión</a></p>
        <p class="text-center mb-0 mt-2"><a href="index.jsp" class="small text-muted text-decoration-none"><i class="bi bi-arrow-left"></i> Volver a la tienda</a></p>
      </div>
    </div>
  </div>
</div>

<script>
  function validar() {
    var ok = document.getElementById('pass1').value === document.getElementById('pass2').value;
    document.getElementById('msgPass').style.display = ok ? 'none' : 'block';
    return ok;
  }
</script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>

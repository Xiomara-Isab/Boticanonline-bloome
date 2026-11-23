<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ page import="com.botica.util.Html" %>
<%
  Integer codigo = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
  if (codigo == null) codigo = 500;
  String titulo = codigo == 403 ? "Acceso denegado" : codigo == 404 ? "Página no encontrada" : "Error del servidor";
  String mensaje = codigo == 403 ? "No tienes permiso para esta acción, o el formulario caducó. Recarga la página e inténtalo de nuevo."
      : codigo == 404 ? "La página que buscas no existe o fue movida."
      : "Ocurrió un problema inesperado. Inténtalo nuevamente en unos minutos.";
%>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="container my-5 text-center" style="max-width: 640px;">
  <div class="display-1 fw-bold text-verde"><%= codigo %></div>
  <h3 class="fw-bold"><%= Html.esc(titulo) %></h3>
  <p class="text-muted"><%= Html.esc(mensaje) %></p>
  <a class="btn btn-primary" href="<%= ctx %>/index.jsp"><i class="bi bi-house"></i> Volver al inicio</a>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>

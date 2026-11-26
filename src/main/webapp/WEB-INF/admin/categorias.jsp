<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.botica.model.Categoria, com.botica.util.Html, java.util.*" %>
<%
  String titulo = "Categorías";
  @SuppressWarnings("unchecked")
  List<Categoria> categorias = (List<Categoria>) request.getAttribute("categorias");
%>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="panel-banda">
  <div class="container d-flex flex-wrap justify-content-between align-items-center gap-2">
    <div><h3 class="mb-0 fw-bold"><i class="bi bi-tags"></i> Categorías del catálogo</h3><span class="opacity-75">Las categorías activas son las que el vendedor puede asignar y las que filtran el catálogo</span></div>
    <a class="btn btn-light btn-sm" href="<%= ctx %>/panel"><i class="bi bi-arrow-left"></i> Volver al panel</a>
  </div>
</div>

<div class="container my-4">
  <%@ include file="/WEB-INF/jspf/flash.jspf" %>

  <div class="row g-4">
    <div class="col-lg-8">
      <div class="seccion">
        <h5 class="mb-3"><i class="bi bi-list-ul text-verde"></i> Categorías <span class="text-muted small">(<%= categorias.size() %>)</span></h5>
        <div class="table-responsive">
          <table class="table align-middle">
            <thead><tr><th>Nombre</th><th class="text-center">Productos</th><th>Estado</th><th class="text-end">Acciones</th></tr></thead>
            <tbody>
              <% for (Categoria c : categorias) { %>
                <tr class="<%= c.isActiva() ? "" : "table-light text-muted" %>">
                  <td>
                    <form method="post" action="<%= ctx %>/admin/categorias" class="d-flex gap-1">
                      <input type="hidden" name="accion" value="renombrar">
                      <input type="hidden" name="id" value="<%= c.getId() %>">
                      <input name="nombre" value="<%= Html.esc(c.getNombre()) %>" maxlength="50" class="form-control form-control-sm" required>
                      <button class="btn btn-sm btn-outline-primary" title="Guardar nombre"><i class="bi bi-check2"></i></button>
                    </form>
                  </td>
                  <td class="text-center"><%= c.getCantidadProductos() %></td>
                  <td><span class="badge text-bg-<%= c.isActiva() ? "success" : "secondary" %>"><%= c.isActiva() ? "Activa" : "Inactiva" %></span></td>
                  <td>
                    <div class="d-flex gap-1 justify-content-end">
                      <form method="post" action="<%= ctx %>/admin/categorias">
                        <input type="hidden" name="accion" value="cambiarActiva">
                        <input type="hidden" name="id" value="<%= c.getId() %>">
                        <input type="hidden" name="activa" value="<%= !c.isActiva() %>">
                        <button class="btn btn-sm btn-outline-<%= c.isActiva() ? "secondary" : "success" %>"><%= c.isActiva() ? "Desactivar" : "Activar" %></button>
                      </form>
                      <form method="post" action="<%= ctx %>/admin/categorias" data-confirmar="¿Eliminar la categoría &quot;<%= Html.esc(c.getNombre()) %>&quot;?" onsubmit="return confirm(this.dataset.confirmar);">
                        <input type="hidden" name="accion" value="eliminar">
                        <input type="hidden" name="id" value="<%= c.getId() %>">
                        <button class="btn btn-sm btn-outline-danger" title="Eliminar"><i class="bi bi-trash"></i></button>
                      </form>
                    </div>
                  </td>
                </tr>
              <% } %>
              <% if (categorias.isEmpty()) { %><tr><td colspan="4" class="text-muted text-center">Todavía no hay categorías</td></tr><% } %>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <div class="col-lg-4">
      <div class="seccion mb-4">
        <h5 class="mb-3"><i class="bi bi-plus-circle text-verde"></i> Nueva categoría</h5>
        <form method="post" action="<%= ctx %>/admin/categorias">
          <input type="hidden" name="accion" value="crear">
          <div class="mb-2"><input name="nombre" maxlength="50" class="form-control" placeholder="Ej.: Higiene bucal" required></div>
          <button class="btn btn-primary w-100">Crear categoría</button>
        </form>
      </div>
      <div class="seccion small text-muted">
        <p class="mb-2"><i class="bi bi-info-circle"></i> <strong>Renombrar</strong> actualiza también todos sus productos.</p>
        <p class="mb-0"><i class="bi bi-shield-lock"></i> Una categoría <strong>con productos no se puede eliminar</strong>: ningún producto puede quedar con una categoría que no existe. Si ya no se usa, desactívala.</p>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>

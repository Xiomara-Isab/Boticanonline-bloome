<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.botica.model.*, com.botica.service.ReporteService, com.botica.util.Html, java.util.*, java.time.format.DateTimeFormatter" %>
<%
  String titulo = "Reportes de ventas";
  ReporteService.ResumenVentas hoy = (ReporteService.ResumenVentas) request.getAttribute("hoy");
  ReporteService.ResumenVentas ventas = (ReporteService.ResumenVentas) request.getAttribute("ventas");
  @SuppressWarnings("unchecked")
  List<ProductoVendido> masVendidos = (List<ProductoVendido>) request.getAttribute("masVendidos");
  DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  int maxUnidades = masVendidos.isEmpty() ? 1 : masVendidos.get(0).unidades();
%>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="panel-banda">
  <div class="container d-flex flex-wrap justify-content-between align-items-center gap-2">
    <div><h3 class="mb-0 fw-bold"><i class="bi bi-graph-up"></i> Reportes de ventas</h3><span class="opacity-75">Solo cuentan los pedidos pagados: no se incluyen pendientes, cancelados ni reembolsados</span></div>
    <a class="btn btn-light btn-sm" href="<%= ctx %>/panel"><i class="bi bi-arrow-left"></i> Volver al panel</a>
  </div>
</div>

<div class="container my-4">
  <% if (request.getAttribute("error") != null) { %>
    <div class="alert alert-warning"><%= Html.esc(request.getAttribute("error")) %>. Se muestran los últimos 30 días.</div>
  <% } %>

  <div class="row g-3 mb-4">
    <div class="col-md-4"><div class="kpi"><div class="icono"><i class="bi bi-cash-coin"></i></div><div><div class="valor"><%= Html.money(hoy.total()) %></div><div class="etq">Ventas de hoy · <%= hoy.pedidos() %> pedido(s)</div></div></div></div>
    <div class="col-md-4"><div class="kpi"><div class="icono"><i class="bi bi-calendar-range"></i></div><div><div class="valor"><%= Html.money(ventas.total()) %></div><div class="etq">Del <%= ventas.desde().format(fmt) %> al <%= ventas.hasta().format(fmt) %></div></div></div></div>
    <div class="col-md-4"><div class="kpi"><div class="icono"><i class="bi bi-receipt"></i></div><div><div class="valor"><%= ventas.pedidos() %></div><div class="etq">Pedidos vendidos en el rango</div></div></div></div>
  </div>

  <div class="seccion mb-4">
    <form method="get" action="<%= ctx %>/admin/reportes" class="row g-2 align-items-end">
      <div class="col-6 col-md-3"><label class="form-label small">Desde</label><input type="date" name="desde" value="<%= ventas.desde() %>" class="form-control form-control-sm" required></div>
      <div class="col-6 col-md-3"><label class="form-label small">Hasta</label><input type="date" name="hasta" value="<%= ventas.hasta() %>" class="form-control form-control-sm" required></div>
      <div class="col-md-6 d-flex gap-1">
        <button class="btn btn-primary btn-sm"><i class="bi bi-funnel"></i> Ver reporte</button>
        <a class="btn btn-outline-secondary btn-sm" href="<%= ctx %>/admin/reportes">Últimos 30 días</a>
      </div>
    </form>
  </div>

  <div class="row g-4">
    <div class="col-lg-6">
      <div class="seccion h-100">
        <h5 class="mb-3"><i class="bi bi-calendar3 text-verde"></i> Ventas por día</h5>
        <div class="table-responsive" style="max-height: 460px; overflow-y: auto;">
          <table class="table table-sm">
            <thead><tr><th>Fecha</th><th class="text-center">Pedidos</th><th class="text-end">Total</th></tr></thead>
            <tbody>
              <% for (VentaDia d : ventas.dias()) { %>
                <tr><td><%= d.fecha().format(fmt) %></td><td class="text-center"><%= d.pedidos() %></td><td class="text-end"><%= Html.money(d.total()) %></td></tr>
              <% } %>
              <% if (ventas.dias().isEmpty()) { %><tr><td colspan="3" class="text-muted text-center">No hubo ventas en este rango</td></tr><% } %>
            </tbody>
            <tfoot><tr><th>Total</th><th class="text-center"><%= ventas.pedidos() %></th><th class="text-end"><%= Html.money(ventas.total()) %></th></tr></tfoot>
          </table>
        </div>
      </div>
    </div>
    <div class="col-lg-6">
      <div class="seccion h-100">
        <h5 class="mb-3"><i class="bi bi-trophy text-verde"></i> Productos más vendidos <span class="text-muted small">(top <%= ReporteService.TOP_PRODUCTOS %>)</span></h5>
        <table class="table table-sm align-middle">
          <thead><tr><th>#</th><th>Producto</th><th style="width: 35%;">Unidades</th><th class="text-end">Total</th></tr></thead>
          <tbody>
            <% int puesto = 1; for (ProductoVendido p : masVendidos) { %>
              <tr>
                <td><%= puesto++ %></td>
                <td><%= Html.esc(p.nombre()) %></td>
                <td>
                  <div class="d-flex align-items-center gap-2">
                    <div class="progress flex-grow-1" style="height: 8px;"><div class="progress-bar bg-success" style="width: <%= p.unidades() * 100 / maxUnidades %>%;"></div></div>
                    <span class="small"><%= p.unidades() %></span>
                  </div>
                </td>
                <td class="text-end"><%= Html.money(p.total()) %></td>
              </tr>
            <% } %>
            <% if (masVendidos.isEmpty()) { %><tr><td colspan="4" class="text-muted text-center">No hubo ventas en este rango</td></tr><% } %>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>

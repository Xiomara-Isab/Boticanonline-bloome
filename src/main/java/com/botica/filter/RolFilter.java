package com.botica.filter;

import com.botica.model.Usuario;
import com.botica.service.AuditoriaService;
import com.botica.util.Constantes;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Control de acceso por rol en un solo lugar: mapa ruta -> roles permitidos.
 * En /panel la clave es "/panel:accion" porque todas las acciones comparten la misma URL.
 * Las acciones de productos no están aquí: las revisa ProductoServiceProxy (patrón Proxy).
 * También fija el contexto de auditoría (usuario e IP) para toda la petición.
 */
@WebFilter("/*")
public class RolFilter implements Filter {
    private static final List<String> CLIENTE = List.of(Constantes.ROL_CLIENTE);
    private static final List<String> ALMACEN = List.of(Constantes.ROL_ALMACEN, Constantes.ROL_ADMINISTRADOR);
    private static final List<String> SOPORTE = List.of(Constantes.ROL_SOPORTE, Constantes.ROL_ADMINISTRADOR);
    private static final List<String> ADMIN = List.of(Constantes.ROL_ADMINISTRADOR);
    private static final List<String> AUDITOR = List.of(Constantes.ROL_AUDITOR);

    static final Map<String, List<String>> PERMISOS = Map.ofEntries(
            Map.entry("/carrito", CLIENTE),
            Map.entry("/carrito.jsp", CLIENTE),
            Map.entry("/pago.jsp", CLIENTE),
            Map.entry("/comprar", CLIENTE),
            Map.entry("/pagar", CLIENTE),
            Map.entry("/cancelarPedido", CLIENTE),
            Map.entry("/avanzarPedido", ALMACEN),
            Map.entry("/revertirPedido", ALMACEN),
            Map.entry("/panel:crearReclamo", CLIENTE),
            Map.entry("/panel:estadoReclamo", SOPORTE),
            Map.entry("/panel:responderReclamo", SOPORTE),
            Map.entry("/panel:reembolsarPedido", SOPORTE),
            Map.entry("/panel:deshacerReclamo", SOPORTE),
            Map.entry("/panel:cambiarRol", ADMIN),
            Map.entry("/panel:cambiarActivoUsuario", ADMIN),
            Map.entry("/panel:resetearContrasena", ADMIN),
            Map.entry("/panel:crearUsuario", ADMIN),
            Map.entry("/panel:exportarCsv", AUDITOR));

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession sesion = req.getSession(false);
        Usuario usuario = sesion == null ? null : (Usuario) sesion.getAttribute("usuario");

        AuditoriaService.fijarContexto(usuario, req.getRemoteAddr());
        try {
            String clave = clave(req);
            List<String> roles = PERMISOS.get(clave);
            if (roles != null && usuario == null) {
                boolean compra = clave.startsWith("/carrito") || "/comprar".equals(clave);
                resp.sendRedirect(req.getContextPath() + "/login.jsp?" + (compra ? "compra=1" : "sesion=1"));
                return;
            }
            if (roles != null && !roles.contains(usuario.getRol())) {
                new AuditoriaService().registrar(Constantes.AUD_ACCESO_DENEGADO, "RUTA", null, clave);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            chain.doFilter(request, response);
        } finally {
            AuditoriaService.limpiarContexto();
        }
    }

    static String clave(HttpServletRequest req) {
        String ruta = req.getServletPath();
        String accion = "/panel".equals(ruta) ? req.getParameter("accion") : null;
        return accion == null ? ruta : ruta + ":" + accion;
    }
}

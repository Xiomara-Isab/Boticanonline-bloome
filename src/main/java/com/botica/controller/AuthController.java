package com.botica.controller;

import com.botica.model.Usuario;
import com.botica.service.AuditoriaService;
import com.botica.service.UsuarioService;
import com.botica.util.BloqueoLogin;
import com.botica.util.Constantes;
import com.botica.util.ReglaNegocioException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** GRASP Controlador: recibe las peticiones de login/registro/logout y delega las reglas en UsuarioService. */
@WebServlet({"/login", "/registro", "/logout"})
public class AuthController extends HttpServlet {
    private final UsuarioService usuarioService = new UsuarioService();
    private final AuditoriaService auditoria = new AuditoriaService();
    private final BloqueoLogin bloqueo = new BloqueoLogin();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ruta = req.getServletPath();
        if ("/logout".equals(ruta)) {
            HttpSession sesion = req.getSession(false);
            if (sesion != null) {
                if (sesion.getAttribute("usuario") != null) {
                    auditoria.registrar(Constantes.AUD_LOGOUT, "USUARIO", ((Usuario) sesion.getAttribute("usuario")).getId(), null);
                }
                sesion.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }
        if ("/registro".equals(ruta)) {
            req.getRequestDispatcher("registro.jsp").forward(req, resp);
        } else {
            req.getRequestDispatcher("login.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String ruta = req.getServletPath();
        if ("/registro".equals(ruta)) {
            procesarRegistro(req, resp);
        } else {
            procesarLogin(req, resp);
        }
    }

    private void procesarRegistro(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            usuarioService.registrarCliente(req.getParameter("nombre"), req.getParameter("correo"),
                    req.getParameter("contrasena"));
            resp.sendRedirect(req.getContextPath() + "/login.jsp?registrado=1");
        } catch (ReglaNegocioException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("registro.jsp").forward(req, resp);
        }
    }

    private void procesarLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String correo = req.getParameter("correo");
        long minutos = bloqueo.minutosRestantes(correo);
        if (minutos > 0) {
            auditoria.registrar(Constantes.AUD_LOGIN_BLOQUEADO, "USUARIO", null, correo);
            req.setAttribute("error", "Cuenta bloqueada temporalmente por " + Constantes.LOGIN_MAX_INTENTOS
                    + " intentos fallidos. Intenta de nuevo en " + minutos + " minuto(s)");
            req.getRequestDispatcher("login.jsp").forward(req, resp);
            return;
        }

        Usuario usuario;
        try {
            usuario = usuarioService.autenticar(correo, req.getParameter("contrasena"));
        } catch (ReglaNegocioException e) {
            auditoria.registrar(Constantes.AUD_LOGIN_FALLIDO, "USUARIO", null, correo);
            int quedan = bloqueo.registrarFallo(correo);
            req.setAttribute("error", quedan == 0
                    ? "Demasiados intentos fallidos. Tu cuenta quedó bloqueada por " + Constantes.LOGIN_MINUTOS_BLOQUEO + " minutos"
                    : e.getMessage() + ". Te quedan " + quedan + " intento(s) antes del bloqueo temporal");
            req.getRequestDispatcher("login.jsp").forward(req, resp);
            return;
        }
        bloqueo.registrarExito(correo);
        auditoria.registrar(usuario, Constantes.AUD_LOGIN, "USUARIO", usuario.getId(), null);

        // Se garantiza que exista sesión antes de renovar su id (evita fijación de sesión).
        HttpSession sesion = req.getSession();
        req.changeSessionId();
        sesion.setAttribute("usuario", usuario);

        // Cliente vuelve a la tienda; el personal entra directo a su panel de trabajo.
        if (Constantes.ROL_CLIENTE.equals(usuario.getRol())) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
        } else {
            resp.sendRedirect(req.getContextPath() + "/panel");
        }
    }
}

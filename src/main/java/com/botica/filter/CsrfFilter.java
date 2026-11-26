package com.botica.filter;

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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Token CSRF por sesión: todo POST debe traer el parámetro _csrf igual al de la sesión.
 * El campo oculto lo agrega a todos los formularios el script de footer.jspf.
 */
@WebFilter("/*")
public class CsrfFilter implements Filter {
    public static final String ATRIBUTO = "csrfToken";
    public static final String PARAMETRO = "_csrf";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession sesion = req.getSession();
        String token = (String) sesion.getAttribute(ATRIBUTO);
        if (token == null) {
            byte[] bytes = new byte[32];
            ALEATORIO.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            sesion.setAttribute(ATRIBUTO, token);
        }
        if ("POST".equalsIgnoreCase(req.getMethod()) && !iguales(token, req.getParameter(PARAMETRO))) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }

    private static boolean iguales(String esperado, String recibido) {
        return recibido != null && MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8),
                recibido.getBytes(StandardCharsets.UTF_8));
    }
}

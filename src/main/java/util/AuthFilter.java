package util;

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
import util.PerfilUtil;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req = (HttpServletRequest)  request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();

        // ── Recursos públicos — libera sem verificar sessão ───────────────────
        if (uri.contains("index.html")
                || uri.contains("login")
                || uri.contains("logout")
                || uri.endsWith(".css")
                || uri.endsWith(".js")) {
            chain.doFilter(request, response);
            return;
        }

        // ── Verifica sessão ───────────────────────────────────────────────────
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("usuario") == null) {
            // Requisições AJAX não devem receber um redirect para HTML
            String xRequestedWith = req.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(xRequestedWith)) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                res.sendRedirect(req.getContextPath() + "/index.html");
            }
            return;
        }

        // ── Cadastro de usuários — somente ADMIN ─────────────────────────────
        if (isRotaCadastroUsuario(uri)) {
            String perfil = (String) session.getAttribute("perfil");
            if (!PerfilUtil.podeCadastrarUsuario(perfil)) {
                if (uri.endsWith("/pages/cadastro")) {
                    res.sendError(HttpServletResponse.SC_FORBIDDEN);
                } else {
                    res.sendRedirect(req.getContextPath() + "/pages/dashboard.html");
                }
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isRotaCadastroUsuario(String uri) {
        return uri.endsWith("/pages/cadastro")
                || uri.contains("/pages/cadastro.html");
    }
}
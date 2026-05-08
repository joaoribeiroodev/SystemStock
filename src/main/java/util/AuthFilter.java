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

        // ── Protege APENAS o cadastro de usuários (requer ADMIN) ──────────────
        // Usa endsWith para evitar falso-positivo em /cadastroProdutos
        boolean isRotaAdminExclusiva = uri.endsWith("/pages/cadastro");

        if (isRotaAdminExclusiva) {
            String perfil = (String) session.getAttribute("perfil");
            if (!"ADMIN".equalsIgnoreCase(perfil)) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
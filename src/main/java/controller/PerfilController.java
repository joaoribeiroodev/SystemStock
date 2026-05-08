package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/api/perfil")
public class PerfilController extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {


        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String perfil = (String) session.getAttribute("perfil");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");


        response.getWriter().write("{\"perfil\":\"" + (perfil != null ? perfil : "") + "\"}");
    }
}
package controller;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.UserModel;

@WebServlet("/login")
public class LoginServlet extends HttpServlet{
    
    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException {
        
        String usuario = request.getParameter("users");
        String senha = request.getParameter("passw");
    
        UserModel userModel = new UserModel();
        userModel.setUsername(usuario);
        userModel.setPassword(senha);
        
        UserDAO dao = new UserDAO();
        
        if(dao.validarLogin(userModel)){
            HttpSession session =
                    request.getSession();
            
            session.setAttribute("usuario", usuario);
            
            System.out.println("Login OK");
            response.sendRedirect(request.getContextPath() + "/pages/dashboard.html");
        }else{
            System.out.println("Login inválido");
            response.sendRedirect(request.getContextPath() + "/index.html");
        }
        
    }    
}

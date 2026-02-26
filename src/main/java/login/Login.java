/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package login;

import connection.ConnectionFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author 232.969762
 */
@WebServlet("/login")
public class Login extends HttpServlet{
   
    private static final long serialVersionUID = 1L;
   
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       
            String usuario = request.getParameter("user");
            String senha = request.getParameter("password");
           
            response.setContentType("text/html");
            PrintWriter out  = response.getWriter();
            
            try (var con = ConnectionFactory.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND psw = ?";
        } catch (Exception e) {
        }
            
    }
    
}


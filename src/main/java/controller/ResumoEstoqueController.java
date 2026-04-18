package controller;

import com.google.gson.Gson;
import connection.ConnectionFactory;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/resumo")
public class ResumoEstoqueController extends HttpServlet{
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException{
        
        String sql = """
                     SELECT
                        SUM(CASE WHEN status = 'entrada' THEN quantidade ELSE 0 END) AS entrada,
                        SUM(CASE WHEN status = 'saida' THEN quantidade ELSE 0 END) AS saida
                     FROM produtos
                     """;
        
        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            
            int entrada = 0;
            int saida = 0;
            
            if(rs.next()) {
                entrada = rs.getInt("entrada");
                saida = rs.getInt("saida");
            }
            
            int total = entrada - saida;
            
            Map<String, Integer> resultado = new HashMap<>();
            resultado.put("entrada", entrada);
            resultado.put("saida", saida);
            resultado.put("total", total);
  
            String json = new Gson().toJson(resultado);
            
            response.setContentType("application/json");
            response.getWriter().write(json);
        } catch (Exception e) {
        }
    }
}

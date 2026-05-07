package controller;

import com.google.gson.Gson;
import dao.MovimentacaoDAO;
import model.GraficoModel;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// Esta é a rota que o teu JS vai chamar no fetch
@WebServlet("/api/dadosGrafico")
public class GraficoController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        // 1. Instanciar o DAO para buscar os dados do banco
        MovimentacaoDAO dao = new MovimentacaoDAO();
        
        // 2. Obter o modelo preenchido com as listas de entradas e saídas
        GraficoModel dados = dao.buscarDadosGrafico();

        // 3. Converter o objeto Java para uma String JSON usando Gson
        String json = new Gson().toJson(dados);

        // 4. Configurar a resposta para ser entendida como JSON pelo navegador
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // 5. Enviar os dados de volta
        response.getWriter().write(json);
    }
}
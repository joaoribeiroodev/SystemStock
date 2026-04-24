package controller;

import com.google.gson.Gson;
import dao.CadastroProdutoDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import model.CadastroProdutoModel;

@WebServlet("/api/estoque")
public class EstoqueController extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        String nome = request.getParameter("nome");
        String tipo = request.getParameter("tipo");
        String data = request.getParameter("data");

        CadastroProdutoDAO dao = new CadastroProdutoDAO();
        List<CadastroProdutoModel> lista = dao.listarComFiltro(nome, tipo, data);
        
        String json = new Gson().toJson(lista);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
    
}

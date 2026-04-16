package controller;

import dao.CadastroProdutosDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.CadastroProdutoModel;

@WebServlet("/cadastroProdutos")
public class CadastroProdutosController extends HttpServlet {

    public void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
            
            CadastroProdutoModel produto = new CadastroProdutoModel();
            
            produto.setCodigoBarras(request.getParameter("codigoBarras"));
            produto.setNomeProduto(request.getParameter("nomeProduto"));
            produto.setFabricante(request.getParameter("fabricante"));
            produto.setMarca(request.getParameter("marca"));
            produto.setDatafabricacao(request.getParameter("dataFabricacao"));
            produto.setDataVencimento(request.getParameter("dataVencimento"));
            produto.setQuantidade(Long.parseLong(request.getParameter("quantidade")));
            produto.setValor(request.getParameter("valor"));
            produto.setTotal(request.getParameter("total"));
            
            CadastroProdutosDAO dao = new CadastroProdutosDAO();
            
            if(dao.salvar(produto)){
                response.sendRedirect("pages/dashboard.html");
            }else{
                response.sendRedirect("pages/cadastroProdutos.html");
            }
    }

}
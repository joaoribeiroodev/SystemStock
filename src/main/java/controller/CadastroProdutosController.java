package controller;

import dao.CadastroProdutoDAO;
import dao.MovimentacaoDAO; // Você deve criar esta classe conforme o plano anterior
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.CadastroProdutoModel;

@WebServlet("/cadastroProdutos")
public class CadastroProdutosController extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        CadastroProdutoModel produto = new CadastroProdutoModel();

        // Dados vindos do seu formulário HTML
        produto.setCodigoBarras(request.getParameter("codigoBarras"));
        produto.setNomeProduto(request.getParameter("nomeProduto"));
        produto.setFabricante(request.getParameter("fabricante"));
        produto.setMarca(request.getParameter("marca"));
        produto.setDataFabricacao(request.getParameter("dataFabricacao"));
        produto.setDataVencimento(request.getParameter("dataVencimento"));
        produto.setQuantidade(Long.parseLong(request.getParameter("quantidade")));
        produto.setValor(request.getParameter("valor"));
        produto.setTotal(request.getParameter("total"));
        produto.setStatus(request.getParameter("status"));
        
        CadastroProdutoDAO dao = new CadastroProdutoDAO();

        // 1. Salva o produto na tabela 'produtos'
        if(dao.salvar(produto)) {
            
            // 2. Tenta registrar a movimentação para o gráfico
            try {
                int idGerado = dao.buscarIdPorCodigo(produto.getCodigoBarras());
                
                if (idGerado > 0) {
                    MovimentacaoDAO movDAO = new MovimentacaoDAO();
                    // Registra se foi ENTRADA ou SAIDA com a quantidade informada
                    movDAO.registrar(idGerado, produto.getStatus(), produto.getQuantidade());
                }
            } catch (Exception e) {
                // Se falhar o log, o sistema continua funcionando para o usuário
                System.err.println("Erro ao registrar movimentação: " + e.getMessage());
            }

            response.sendRedirect("pages/dashboard.html");
        } else {
            response.sendRedirect("pages/cadastroProduto.html");
        }
    }
}
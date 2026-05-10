package controller;

import com.google.gson.Gson;
import dao.CadastroProdutoDAO;
import dao.MovimentacaoDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.CadastroProdutoModel;

import java.io.IOException;

    @WebServlet("/api/produtos/*")
    public class GerenciamentoProdutosController extends HttpServlet {

    private final CadastroProdutoDAO dao = new CadastroProdutoDAO();
    private final MovimentacaoDAO movDAO = new MovimentacaoDAO();
    private final Gson gson = new Gson();



      // Atualiza os dados de um produto existente.

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo(); // "/atualizar"

        if (!"/atualizar".equals(pathInfo)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"erro\":\"Endpoint não encontrado.\"}");
            return;
        }

        try {
            String codigoBarras = request.getParameter("codigoBarras");

            if (codigoBarras == null || codigoBarras.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"erro\":\"Parâmetro 'codigoBarras' ausente.\"}");
                return;
            }

            CadastroProdutoModel produto = new CadastroProdutoModel();
            produto.setCodigoBarras(codigoBarras);
            produto.setNomeProduto(request.getParameter("nomeProduto"));
            produto.setFabricante(request.getParameter("fabricante"));
            produto.setMarca(request.getParameter("marca"));
            produto.setDataFabricacao(request.getParameter("dataFabricacao"));
            produto.setDataVencimento(request.getParameter("dataVencimento"));
            produto.setQuantidade(Long.parseLong(request.getParameter("quantidade")));
            produto.setValor(request.getParameter("valor"));
            produto.setTotal(request.getParameter("total"));
            produto.setStatus(request.getParameter("status"));

            boolean sucesso = dao.atualizar(produto);

            if (sucesso) {
                // Registra a movimentação após atualização
                int produtoId = dao.buscarIdPorCodigo(codigoBarras);
                if (produtoId > 0) {
                    movDAO.registrar(produtoId, produto.getStatus(), produto.getQuantidade());
                }

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"mensagem\":\"Produto atualizado com sucesso.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"erro\":\"Nenhum produto encontrado para o código de barras informado.\"}");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\":\"Parâmetro 'quantidade' inválido.\"}");
        } catch (Exception e) {
            System.err.println("[GerenciamentoProdutosController] Erro ao atualizar: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"erro\":\"Erro interno ao atualizar o produto.\"}");
        }
    }


    // metodo de deletar produtos sem exclusão total no banco
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo(); // "/excluir"

        if (!"/excluir".equals(pathInfo)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"erro\":\"Endpoint não encontrado.\"}");
            return;
        }

        try {
            String codigoBarras = request.getParameter("codigoBarras");

            if (codigoBarras == null || codigoBarras.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"erro\":\"Parâmetro 'codigoBarras' ausente.\"}");
                return;
            }

            boolean sucesso = dao.excluirPorCodigo(codigoBarras);

            if (sucesso) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"mensagem\":\"Produto excluído com sucesso.\"}");
            } else {

                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"erro\":\"Produto já foi excluído ou não existe.\"}");
            }

        } catch (java.sql.SQLException e) {

            System.err.println("[GerenciamentoProdutosController] Erro SQL: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"erro\":\"Erro no Banco de Dados: " + e.getMessage() + "\"}");
        }
    }

}


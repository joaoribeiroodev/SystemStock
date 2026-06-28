package controller;

import dao.CadastroProdutoDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.CadastroProdutoModel;

import java.io.IOException;
import java.math.BigDecimal;

    @WebServlet("/api/produtos/*")
    public class GerenciamentoProdutosController extends HttpServlet {

    private final CadastroProdutoDAO dao = new CadastroProdutoDAO();



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

            String nomeProduto = request.getParameter("nomeProduto");
            String fabricante = request.getParameter("fabricante");
            String marca = request.getParameter("marca");
            String dataFabricacao = request.getParameter("dataFabricacao");
            String dataVencimento = request.getParameter("dataVencimento");
            String valorStr = request.getParameter("valor");
            String qtdMinStr = request.getParameter("quantidadeMinima");

            if (isBlank(nomeProduto) || isBlank(fabricante) || isBlank(marca)
                    || isBlank(dataFabricacao) || isBlank(dataVencimento)
                    || isBlank(valorStr) || isBlank(qtdMinStr)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"erro\":\"Preencha todos os campos obrigatórios.\"}");
                return;
            }

            CadastroProdutoModel existente = dao.buscarPorCodigoBarras(codigoBarras);
            if (existente == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"erro\":\"Nenhum produto encontrado para o código de barras informado.\"}");
                return;
            }

            long quantidadeMinima = parsePositivo(qtdMinStr);
            BigDecimal valor = parseValorPositivo(valorStr);

            if (quantidadeMinima <= 0 || valor == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"erro\":\"Quantidade mínima e valor devem ser maiores que zero.\"}");
                return;
            }

            long quantidadeAtual = existente.getQuantidade();

            CadastroProdutoModel produto = new CadastroProdutoModel();
            produto.setCodigoBarras(codigoBarras);
            produto.setNomeProduto(nomeProduto.trim());
            produto.setFabricante(fabricante.trim());
            produto.setMarca(marca.trim());
            produto.setDataFabricacao(dataFabricacao);
            produto.setDataVencimento(dataVencimento);
            produto.setQuantidade(quantidadeAtual);
            produto.setQuantidadeMinima(quantidadeMinima);
            produto.setValor(valor.toPlainString());
            produto.setTotal(valor.multiply(BigDecimal.valueOf(quantidadeAtual)).toPlainString());
            produto.setStatus(existente.getStatus() != null ? existente.getStatus() : "ENTRADA");

            boolean sucesso = dao.atualizar(produto);

            if (sucesso) {
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


    // metodo de deletar produtos sem exclusão da movimentação no banco
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

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static long parsePositivo(String value) throws NumberFormatException {
        return Long.parseLong(value.trim());
    }

    private static BigDecimal parseValorPositivo(String value) {
        try {
            BigDecimal bd = new BigDecimal(value.trim());
            return bd.compareTo(BigDecimal.ZERO) > 0 ? bd : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

}


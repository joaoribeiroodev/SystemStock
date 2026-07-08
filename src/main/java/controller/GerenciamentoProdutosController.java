package controller;

import dao.CadastroProdutoDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.CadastroProdutoModel;
import util.ValidacaoProduto;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/api/produtos/*")
public class GerenciamentoProdutosController extends HttpServlet {

    private final CadastroProdutoDAO dao = new CadastroProdutoDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (!"/atualizar".equals(pathInfo)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"erro\":\"Endpoint não encontrado.\"}");
            return;
        }

        try {
            String codigoBarras = request.getParameter("codigoBarras");

            if (ValidacaoProduto.isBlank(codigoBarras)) {
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
            String prateleira = request.getParameter("prateleira");
            String localArmazenamento = request.getParameter("localArmazenamento");

            if (ValidacaoProduto.isBlank(nomeProduto) || ValidacaoProduto.isBlank(fabricante)
                    || ValidacaoProduto.isBlank(marca) || ValidacaoProduto.isBlank(dataFabricacao)
                    || ValidacaoProduto.isBlank(dataVencimento) || ValidacaoProduto.isBlank(valorStr)
                    || ValidacaoProduto.isBlank(qtdMinStr)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"erro\":\"Preencha todos os campos obrigatórios.\"}");
                return;
            }

            String erroData = ValidacaoProduto.validarDatas(dataFabricacao, dataVencimento);
            if (erroData != null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"erro\":\"" + erroData.replace("\"", "\\\"") + "\"}");
                return;
            }

            CadastroProdutoModel existente = dao.buscarPorCodigoBarras(codigoBarras);
            if (existente == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"erro\":\"Nenhum produto encontrado para o código de barras informado.\"}");
                return;
            }

            Long quantidadeMinima = ValidacaoProduto.parseLongPositivo(qtdMinStr);
            BigDecimal valor = ValidacaoProduto.parseValorPositivo(valorStr);

            if (quantidadeMinima == null || valor == null) {
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
            produto.setPrateleira(ValidacaoProduto.isBlank(prateleira) ? null : prateleira.trim());
            produto.setLocalArmazenamento(ValidacaoProduto.isBlank(localArmazenamento) ? null : localArmazenamento.trim());
            produto.setStatus(existente.getStatus() != null ? existente.getStatus() : "ENTRADA");

            boolean sucesso = dao.atualizar(produto);

            if (sucesso) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"mensagem\":\"Produto atualizado com sucesso.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"erro\":\"Nenhum produto encontrado para o código de barras informado.\"}");
            }

        } catch (Exception e) {
            System.err.println("[GerenciamentoProdutosController] Erro ao atualizar: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"erro\":\"Erro interno ao atualizar o produto.\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (!"/excluir".equals(pathInfo)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"erro\":\"Endpoint não encontrado.\"}");
            return;
        }

        try {
            String codigoBarras = request.getParameter("codigoBarras");

            if (ValidacaoProduto.isBlank(codigoBarras)) {
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
            response.getWriter().write("{\"erro\":\"Erro ao excluir produto. Tente novamente.\"}");
        }
    }
}

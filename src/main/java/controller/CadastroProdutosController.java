package controller;

import dao.CadastroProdutoDAO;
import dao.CadastroProdutoDAO.ExistenciaCodigoBarras;
import dao.MovimentacaoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import model.CadastroProdutoModel;
import util.ValidacaoProduto;

@WebServlet("/cadastroProdutos")
public class CadastroProdutosController extends HttpServlet {

    private static String redirectErroCadastro(HttpServletRequest request, String codigoErro) {
        return request.getContextPath() + "/pages/cadastroProduto.html?erro=" + codigoErro;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String codigo = new CadastroProdutoDAO().gerarCodigoBarrasUnico();
        response.getWriter().write("{\"codigoBarras\":\"" + codigo + "\"}");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nomeProduto = request.getParameter("nomeProduto");
        String fabricante = request.getParameter("fabricante");
        String marca = request.getParameter("marca");
        String dataFabricacao = request.getParameter("dataFabricacao");
        String dataVencimento = request.getParameter("dataVencimento");
        String qtdStr = request.getParameter("quantidade");
        String qtdMinStr = request.getParameter("quantidadeMinima");
        String valorStr = request.getParameter("valor");

        if (ValidacaoProduto.isBlank(nomeProduto) || ValidacaoProduto.isBlank(fabricante)
                || ValidacaoProduto.isBlank(marca) || ValidacaoProduto.isBlank(dataFabricacao)
                || ValidacaoProduto.isBlank(dataVencimento) || ValidacaoProduto.isBlank(qtdStr)
                || ValidacaoProduto.isBlank(qtdMinStr) || ValidacaoProduto.isBlank(valorStr)) {
            response.sendRedirect(redirectErroCadastro(request, "cadastro_falhou"));
            return;
        }

        String erroData = ValidacaoProduto.validarDatas(dataFabricacao, dataVencimento);
        if (erroData != null) {
            response.sendRedirect(redirectErroCadastro(request, "cadastro_falhou"));
            return;
        }

        Long quantidade = ValidacaoProduto.parseLongPositivo(qtdStr);
        Long quantidadeMinima = ValidacaoProduto.parseLongPositivo(qtdMinStr);
        BigDecimal valor = ValidacaoProduto.parseValorPositivo(valorStr);

        if (quantidade == null || quantidadeMinima == null || valor == null) {
            response.sendRedirect(redirectErroCadastro(request, "cadastro_falhou"));
            return;
        }

        CadastroProdutoDAO dao = new CadastroProdutoDAO();
        MovimentacaoDAO movDAO = new MovimentacaoDAO();

        String codigoBarras = request.getParameter("codigoBarras");
        if (ValidacaoProduto.isBlank(codigoBarras)
                || dao.consultarExistenciaCodigoBarras(codigoBarras.trim()) != ExistenciaCodigoBarras.DISPONIVEL) {
            codigoBarras = dao.gerarCodigoBarrasUnico();
        } else {
            codigoBarras = codigoBarras.trim();
        }

        CadastroProdutoModel produto = new CadastroProdutoModel();
        produto.setCodigoBarras(codigoBarras);
        produto.setNomeProduto(nomeProduto.trim());
        produto.setFabricante(fabricante.trim());
        produto.setMarca(marca.trim());
        produto.setDataFabricacao(dataFabricacao);
        produto.setDataVencimento(dataVencimento);
        produto.setQuantidade(quantidade);
        produto.setQuantidadeMinima(quantidadeMinima);
        produto.setValor(valor.toPlainString());
        produto.setTotal(valor.multiply(BigDecimal.valueOf(quantidade)).toPlainString());
        produto.setStatus("ENTRADA");

        if (dao.salvar(produto)) {

            int produtoId = dao.buscarIdPorCodigo(produto.getCodigoBarras());

            if (produtoId > 0) {
                boolean movOk = movDAO.registrar(produtoId, produto.getStatus(), produto.getQuantidade());
                if (!movOk) {
                    System.err.println("[CadastroProdutosController] Produto salvo, mas falha ao registrar movimentação. produtoId=" + produtoId);
                }
            } else {
                System.err.println("[CadastroProdutosController] Não foi possível recuperar o ID do produto recém-inserido.");
            }

            response.sendRedirect(request.getContextPath() + "/pages/dashboard.html");
        } else {
            response.sendRedirect(redirectErroCadastro(request, "cadastro_falhou"));
        }
    }
}

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

@WebServlet("/cadastroProdutos")
public class CadastroProdutosController extends HttpServlet {

    private static String redirectErroCadastro(HttpServletRequest request, String codigoErro) {
        return request.getContextPath() + "/pages/cadastroProduto.html?erro=" + codigoErro;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String codigoBarras = request.getParameter("codigoBarras");
        String nomeProduto = request.getParameter("nomeProduto");
        String fabricante = request.getParameter("fabricante");
        String marca = request.getParameter("marca");
        String dataFabricacao = request.getParameter("dataFabricacao");
        String dataVencimento = request.getParameter("dataVencimento");
        String qtdStr = request.getParameter("quantidade");
        String qtdMinStr = request.getParameter("quantidadeMinima");
        String valorStr = request.getParameter("valor");
        String status = request.getParameter("status");

        if (isBlank(codigoBarras) || isBlank(nomeProduto) || isBlank(fabricante) || isBlank(marca)
                || isBlank(dataFabricacao) || isBlank(dataVencimento)
                || isBlank(qtdStr) || isBlank(qtdMinStr) || isBlank(valorStr) || isBlank(status)) {
            response.sendRedirect(redirectErroCadastro(request, "cadastro_falhou"));
            return;
        }

        long quantidade;
        long quantidadeMinima;
        BigDecimal valor;
        try {
            quantidade = Long.parseLong(qtdStr.trim());
            quantidadeMinima = Long.parseLong(qtdMinStr.trim());
            valor = new BigDecimal(valorStr.trim());
            if (quantidade <= 0 || quantidadeMinima <= 0 || valor.compareTo(BigDecimal.ZERO) <= 0) {
                response.sendRedirect(redirectErroCadastro(request, "cadastro_falhou"));
                return;
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(redirectErroCadastro(request, "cadastro_falhou"));
            return;
        }

        CadastroProdutoModel produto = new CadastroProdutoModel();

        produto.setCodigoBarras(codigoBarras.trim());
        produto.setNomeProduto(nomeProduto.trim());
        produto.setFabricante(fabricante.trim());
        produto.setMarca(marca.trim());
        produto.setDataFabricacao(dataFabricacao);
        produto.setDataVencimento(dataVencimento);
        produto.setQuantidade(quantidade);
        produto.setQuantidadeMinima(quantidadeMinima);
        produto.setValor(valor.toPlainString());
        produto.setTotal(valor.multiply(BigDecimal.valueOf(quantidade)).toPlainString());
        produto.setStatus(status);

        CadastroProdutoDAO dao    = new CadastroProdutoDAO();
        MovimentacaoDAO    movDAO = new MovimentacaoDAO();

        ExistenciaCodigoBarras existencia = dao.consultarExistenciaCodigoBarras(produto.getCodigoBarras());
        if (existencia == ExistenciaCodigoBarras.JA_REGISTRADO_ATIVO) {
            response.sendRedirect(redirectErroCadastro(request, "codigo_duplicado_ativo"));
            return;
        }
        if (existencia == ExistenciaCodigoBarras.JA_REGISTRADO_INATIVO) {
            response.sendRedirect(redirectErroCadastro(request, "codigo_duplicado_inativo"));
            return;
        }

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

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
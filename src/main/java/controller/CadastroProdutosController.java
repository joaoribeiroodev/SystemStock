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
import model.CadastroProdutoModel;

@WebServlet("/cadastroProdutos")
public class CadastroProdutosController extends HttpServlet {

    private static String redirectErroCadastro(HttpServletRequest request, String codigoErro) {
        return request.getContextPath() + "/pages/cadastroProduto.html?erro=" + codigoErro;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        CadastroProdutoModel produto = new CadastroProdutoModel();

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
}
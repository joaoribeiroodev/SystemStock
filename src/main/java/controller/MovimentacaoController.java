package controller;

import dao.CadastroProdutoDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/movimentacao")
public class MovimentacaoController extends HttpServlet {

    private final CadastroProdutoDAO dao = new CadastroProdutoDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String codigoBarras = request.getParameter("codigoBarras");
        String tipo = request.getParameter("tipo");
        String qtdStr = request.getParameter("quantidade");

        if (codigoBarras == null || codigoBarras.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\":\"Selecione um produto.\"}");
            return;
        }

        if (tipo == null || tipo.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\":\"Selecione o tipo de movimentação.\"}");
            return;
        }

        long quantidade;
        try {
            quantidade = Long.parseLong(qtdStr);
            if (quantidade <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\":\"Informe uma quantidade maior que zero.\"}");
            return;
        }

        String erro = dao.registrarMovimentacao(codigoBarras, tipo, quantidade);

        if (erro != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\":\"" + erro.replace("\"", "\\\"") + "\"}");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("{\"mensagem\":\"Movimentação registrada com sucesso.\"}");
    }
}

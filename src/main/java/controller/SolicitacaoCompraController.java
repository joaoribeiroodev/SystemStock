package controller;

import com.google.gson.Gson;
import dao.SolicitacaoCompraDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.SolicitacaoCompraModel;
import util.ValidacaoProduto;

import java.io.IOException;
import java.util.List;

/**
 * API de solicitações de compra.
 *
 * GET  /api/solicitacoes              -> lista (aceita ?status=PENDENTE|EM_ANDAMENTO|ATENDIDA|CANCELADA)
 * GET  /api/solicitacoes/contagem     -> {"pendentes": N}
 * POST /api/solicitacoes/atualizar    -> params: id, status, observacao (opcional)
 *
 * As solicitações em si são sempre emitidas/encerradas automaticamente pelo
 * backend (CadastroProdutoDAO) quando o estoque cruza a quantidade mínima;
 * este controller só expõe consulta e mudança manual de status (ex.: marcar
 * "Em andamento" ao fazer o pedido ao fornecedor, ou "Cancelada").
 */
@WebServlet("/api/solicitacoes/*")
public class SolicitacaoCompraController extends HttpServlet {

    private final SolicitacaoCompraDAO dao = new SolicitacaoCompraDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (pathInfo != null && pathInfo.equals("/contagem")) {
            int pendentes = dao.contarPendentes();
            response.getWriter().write("{\"pendentes\":" + pendentes + "}");
            return;
        }

        String status = request.getParameter("status");
        List<SolicitacaoCompraModel> lista = dao.listar(status);
        response.getWriter().write(gson.toJson(lista));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (!"/atualizar".equals(pathInfo)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"erro\":\"Endpoint não encontrado.\"}");
            return;
        }

        String idStr = request.getParameter("id");
        String status = request.getParameter("status");
        String observacao = request.getParameter("observacao");

        if (ValidacaoProduto.isBlank(idStr) || ValidacaoProduto.isBlank(status)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\":\"Informe 'id' e 'status'.\"}");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\":\"'id' inválido.\"}");
            return;
        }

        boolean sucesso = dao.atualizarStatus(id, status.trim().toUpperCase(), observacao);

        if (sucesso) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"mensagem\":\"Solicitação atualizada com sucesso.\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\":\"Não foi possível atualizar. Verifique o id e o status informados.\"}");
        }
    }
}

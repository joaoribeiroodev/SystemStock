package controller;

import com.google.gson.Gson;
import dao.MovimentacaoDAO;
import model.GraficoModel;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Year;

@WebServlet("/api/dadosGrafico")
public class GraficoController extends HttpServlet {

    private static final Gson GSON = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // ── Configurar cabeçalhos da resposta ─────────────────────────────────
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // ── Ler parâmetro ?ano=2025 (opcional) ────────────────────────────────
        int ano = parseAno(request.getParameter("ano"));

        // ── Buscar dados e tratar erros de forma centralizada ─────────────────
        try {
            MovimentacaoDAO dao = new MovimentacaoDAO();
            GraficoModel dados = dao.buscarDadosGrafico(ano);

            if (dados.getErro() != null) {
                // Retorna HTTP 500 com mensagem legível ao JS
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

            response.getWriter().write(GSON.toJson(dados));

        } catch (Exception e) {
            System.err.println("[GraficoController] Erro inesperado: " + e.getMessage());
            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            // Retorna um JSON de erro estruturado para o frontend
            GraficoModel erro = new GraficoModel();
            erro.setErro("Erro interno do servidor. Tente novamente mais tarde.");
            erro.garantirDozeMeses();

            response.getWriter().write(GSON.toJson(erro));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER: Parsear o ano com fallback seguro para o ano atual
    // ─────────────────────────────────────────────────────────────────────────
    private int parseAno(String anoParam) {
        int anoAtual = Year.now().getValue();
        if (anoParam == null || anoParam.isBlank()) {
            return anoAtual;
        }
        try {
            int ano = Integer.parseInt(anoParam.trim());
            // Bloqueia anos fora de um intervalo razoável
            return (ano >= 2000 && ano <= anoAtual) ? ano : anoAtual;
        } catch (NumberFormatException e) {
            return anoAtual;
        }
    }
}
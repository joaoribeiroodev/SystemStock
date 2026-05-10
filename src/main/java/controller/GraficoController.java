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


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        int ano = parseAno(request.getParameter("ano"));
        String codigoBarras = request.getParameter("codigoBarras");

        try {
            MovimentacaoDAO dao = new MovimentacaoDAO();
            GraficoModel dados;

            if (codigoBarras != null && !codigoBarras.isBlank()) {
                dados = dao.buscarDadosGraficoPorCodigoBarras(codigoBarras.trim(), ano);
                if (dados.getErro() != null) {
                    boolean naoEncontrado = "Produto não encontrado para este código de barras.".equals(dados.getErro());
                    response.setStatus(
                        naoEncontrado ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                    );
                }
            } else {
                dados = dao.buscarDadosGrafico(ano);
                if (dados.getErro() != null) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }

            response.getWriter().write(GSON.toJson(dados));

        } catch (Exception e) {
            System.err.println("[GraficoController] Erro inesperado: " + e.getMessage());
            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            GraficoModel erro = new GraficoModel();
            erro.setErro("Erro interno do servidor. Tente novamente mais tarde.");
            erro.garantirDozeMeses();

            response.getWriter().write(GSON.toJson(erro));
        }
    }


    private int parseAno(String anoParam) {
        int anoAtual = Year.now().getValue();
        if (anoParam == null || anoParam.isBlank()) {
            return anoAtual;
        }
        try {
            int ano = Integer.parseInt(anoParam.trim());
            return (ano >= 2000 && ano <= anoAtual) ? ano : anoAtual;
        } catch (NumberFormatException e) {
            return anoAtual;
        }
    }
}
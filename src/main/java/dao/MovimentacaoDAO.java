package dao;

import connection.ConnectionFactory;
import model.GraficoModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MovimentacaoDAO {

    public boolean registrar(int produtoId, String tipo, long qtd) {
        if (produtoId <= 0 || tipo == null || tipo.isBlank() || qtd <= 0) {
            System.err.println("[MovimentacaoDAO] Parâmetros inválidos para registrar movimentação.");
            return false;
        }

        String tipoNormalizado = tipo.trim().toUpperCase();
        if (!tipoNormalizado.equals("ENTRADA") && !tipoNormalizado.equals("SAIDA")) {
            System.err.println("[MovimentacaoDAO] Tipo de movimentação inválido: " + tipo);
            return false;
        }

        String sql = "INSERT INTO movimentacoes (produto_id, tipo, quantidade) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, produtoId);
            ps.setString(2, tipoNormalizado);
            ps.setLong(3, qtd);
            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            System.err.println("[MovimentacaoDAO] Erro ao registrar movimentação: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }




    public GraficoModel buscarDadosGrafico(int ano) {
        GraficoModel grafico = new GraficoModel();
        grafico.setAno(ano > 0 ? ano : java.time.Year.now().getValue());

        String sql =
            "SELECT " +
            "  m.mes, " +
            "  COALESCE(SUM(CASE WHEN mov.tipo = 'ENTRADA' THEN mov.quantidade ELSE 0 END), 0) AS total_entrada, " +
            "  COALESCE(SUM(CASE WHEN mov.tipo = 'SAIDA'   THEN mov.quantidade ELSE 0 END), 0) AS total_saida " +
            "FROM ( " +
            "  SELECT 1 AS mes UNION ALL SELECT 2 UNION ALL SELECT 3  UNION ALL " +
            "  SELECT 4        UNION ALL SELECT 5 UNION ALL SELECT 6  UNION ALL " +
            "  SELECT 7        UNION ALL SELECT 8 UNION ALL SELECT 9  UNION ALL " +
            "  SELECT 10       UNION ALL SELECT 11 UNION ALL SELECT 12 " +
            ") AS m " +
            "LEFT JOIN movimentacoes mov " +
            "  ON MONTH(mov.data_movimentacao) = m.mes " +
            " AND YEAR(mov.data_movimentacao)  = ? " +
            "GROUP BY m.mes " +
            "ORDER BY m.mes";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, grafico.getAno());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    grafico.getEntradas().add(rs.getLong("total_entrada"));
                    grafico.getSaidas().add(rs.getLong("total_saida"));
                }
            }

        } catch (Exception e) {
            System.err.println("[MovimentacaoDAO] Erro ao buscar dados do gráfico: " + e.getMessage());
            e.printStackTrace();
            grafico.setErro("Falha ao carregar dados do banco de dados.");
        }

        grafico.garantirDozeMeses();
        return grafico;
    }
}
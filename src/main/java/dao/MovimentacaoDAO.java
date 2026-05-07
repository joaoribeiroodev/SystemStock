package dao;

import connection.Conexao; // Certifique-se que o nome da sua classe de conexão está correto
import java.sql.*;
import model.GraficoModel;

public class MovimentacaoDAO {

    public void registrar(int produtoId, String tipo, long qtd) {
        String sql = "INSERT INTO movimentacoes (produto_id, tipo, quantidade) VALUES (?, ?, ?)";
        try (Connection conn = Conexao.conectar(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, produtoId);
            ps.setString(2, tipo.toUpperCase());
            ps.setLong(3, qtd);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public GraficoModel buscarDadosGrafico() {
        GraficoModel grafico = new GraficoModel();
        String sql = "SELECT m.mes, " +
                     "COALESCE(SUM(CASE WHEN mov.tipo = 'ENTRADA' THEN mov.quantidade ELSE 0 END), 0) AS total_in, " +
                     "COALESCE(SUM(CASE WHEN mov.tipo = 'SAIDA' THEN mov.quantidade ELSE 0 END), 0) AS total_out " +
                     "FROM (SELECT 1 AS mes UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 " +
                     "UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 " +
                     "UNION SELECT 11 UNION SELECT 12) AS m " +
                     "LEFT JOIN movimentacoes mov ON m.mes = MONTH(mov.data_movimentacao) " +
                     "AND YEAR(mov.data_movimentacao) = YEAR(CURDATE()) " +
                     "GROUP BY m.mes ORDER BY m.mes";

        try (Connection conn = Conexao.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                grafico.getEntradas().add(rs.getLong("total_in"));
                grafico.getSaidas().add(rs.getLong("total_out"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return grafico;
    }
}
package dao;

import connection.ConnectionFactory;
import model.SolicitacaoCompraModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;


public class SolicitacaoCompraDAO {

    public static final String STATUS_PENDENTE     = "PENDENTE";
    public static final String STATUS_EM_ANDAMENTO = "EM_ANDAMENTO";
    public static final String STATUS_ATENDIDA     = "ATENDIDA";
    public static final String STATUS_CANCELADA    = "CANCELADA";

    private static boolean isStatusValido(String status) {
        return STATUS_PENDENTE.equals(status) || STATUS_EM_ANDAMENTO.equals(status)
                || STATUS_ATENDIDA.equals(status) || STATUS_CANCELADA.equals(status);
    }

    
    public void verificarNivelEstoque(Connection conn, int produtoId, String codigoBarras,
                                       String nomeProduto, long quantidadeAtual, long quantidadeMinima)
            throws SQLException {

        if (produtoId <= 0) {
            return;
        }

        if (quantidadeMinima > 0 && quantidadeAtual < quantidadeMinima) {
            gerarSeNaoExistirAberta(conn, produtoId, codigoBarras, nomeProduto, quantidadeAtual, quantidadeMinima);
        } else {
            encerrarAbertasPorReposicao(conn, produtoId);
        }
    }

    public void verificarNivelEstoque(int produtoId, String codigoBarras, String nomeProduto,
                                       long quantidadeAtual, long quantidadeMinima) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            if (conn == null) {
                return;
            }
            conn.setAutoCommit(false);
            try {
                verificarNivelEstoque(conn, produtoId, codigoBarras, nomeProduto, quantidadeAtual, quantidadeMinima);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[SolicitacaoCompraDAO] Erro ao verificar nível de estoque: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean existeSolicitacaoAberta(Connection conn, int produtoId) throws SQLException {
        String sql = "SELECT id FROM solicitacoes_compra WHERE produto_id = ? AND status IN (?, ?) LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, produtoId);
            ps.setString(2, STATUS_PENDENTE);
            ps.setString(3, STATUS_EM_ANDAMENTO);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void gerarSeNaoExistirAberta(Connection conn, int produtoId, String codigoBarras, String nomeProduto,
                                          long quantidadeAtual, long quantidadeMinima) throws SQLException {

        if (existeSolicitacaoAberta(conn, produtoId)) {
            return;
        }

        
        long quantidadeSugerida = Math.max((quantidadeMinima * 2) - quantidadeAtual, quantidadeMinima);

        String sql = "INSERT INTO solicitacoes_compra "
                + "(produto_id, codigo_barras, nome_produto, quantidade_atual, quantidade_minima, "
                + " quantidade_sugerida, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, produtoId);
            ps.setString(2, codigoBarras);
            ps.setString(3, nomeProduto);
            ps.setLong(4, quantidadeAtual);
            ps.setLong(5, quantidadeMinima);
            ps.setLong(6, quantidadeSugerida);
            ps.setString(7, STATUS_PENDENTE);
            ps.executeUpdate();
        }
    }

    private void encerrarAbertasPorReposicao(Connection conn, int produtoId) throws SQLException {
        String sql = "UPDATE solicitacoes_compra SET status = ?, "
                + "observacao = COALESCE(observacao, 'Encerrada automaticamente: estoque reabastecido.'), "
                + "atualizado_em = CURRENT_TIMESTAMP "
                + "WHERE produto_id = ? AND status IN (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, STATUS_ATENDIDA);
            ps.setInt(2, produtoId);
            ps.setString(3, STATUS_PENDENTE);
            ps.setString(4, STATUS_EM_ANDAMENTO);
            ps.executeUpdate();
        }
    }

    public List<SolicitacaoCompraModel> listar(String status) {
        List<SolicitacaoCompraModel> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT id, produto_id, codigo_barras, nome_produto, quantidade_atual, "
                        + "quantidade_minima, quantidade_sugerida, status, observacao, criado_em, atualizado_em "
                        + "FROM solicitacoes_compra");

        boolean temFiltro = status != null && !status.isBlank();
        if (temFiltro) {
            sql.append(" WHERE status = ?");
        }
        sql.append(" ORDER BY criado_em DESC");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (temFiltro) {
                ps.setString(1, status.trim().toUpperCase());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }

        } catch (Exception e) {
            System.err.println("[SolicitacaoCompraDAO] Erro ao listar solicitações: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    public boolean atualizarStatus(int id, String novoStatus, String observacao) {
        if (id <= 0 || !isStatusValido(novoStatus)) {
            return false;
        }

        String sql = "UPDATE solicitacoes_compra SET status = ?, observacao = ?, "
                + "atualizado_em = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, novoStatus);
            if (observacao == null || observacao.isBlank()) {
                ps.setNull(2, java.sql.Types.VARCHAR);
            } else {
                ps.setString(2, observacao.trim());
            }
            ps.setInt(3, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[SolicitacaoCompraDAO] Erro ao atualizar status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public int contarPendentes() {
        String sql = "SELECT COUNT(*) FROM solicitacoes_compra WHERE status = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, STATUS_PENDENTE);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            System.err.println("[SolicitacaoCompraDAO] Erro ao contar pendentes: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private SolicitacaoCompraModel mapear(ResultSet rs) throws SQLException {
        SolicitacaoCompraModel s = new SolicitacaoCompraModel();
        s.setId(rs.getInt("id"));
        s.setProdutoId(rs.getInt("produto_id"));
        s.setCodigoBarras(rs.getString("codigo_barras"));
        s.setNomeProduto(rs.getString("nome_produto"));
        s.setQuantidadeAtual(rs.getLong("quantidade_atual"));
        s.setQuantidadeMinima(rs.getLong("quantidade_minima"));
        s.setQuantidadeSugerida(rs.getLong("quantidade_sugerida"));
        s.setStatus(rs.getString("status"));
        s.setObservacao(rs.getString("observacao"));

        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Timestamp criadoEm = rs.getTimestamp("criado_em", utc);
        s.setCriadoEm(criadoEm != null ? criadoEm.toInstant().toString() : null);

        Timestamp atualizadoEm = rs.getTimestamp("atualizado_em", utc);
        s.setAtualizadoEm(atualizadoEm != null ? atualizadoEm.toInstant().toString() : null);

        return s;
    }
}

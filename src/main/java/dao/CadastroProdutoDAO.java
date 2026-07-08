package dao;

import connection.ConnectionFactory;
import model.CadastroProdutoModel;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CadastroProdutoDAO {

    public enum ExistenciaCodigoBarras {
        DISPONIVEL,
        JA_REGISTRADO_ATIVO,
        JA_REGISTRADO_INATIVO
    }

    public ExistenciaCodigoBarras consultarExistenciaCodigoBarras(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.isBlank()) {
            return ExistenciaCodigoBarras.DISPONIVEL;
        }
        String sql = "SELECT ativo FROM produtos WHERE codigo_barras = ?";
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) {
                return ExistenciaCodigoBarras.DISPONIVEL;
            }
            ps.setString(1, codigoBarras.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return ExistenciaCodigoBarras.DISPONIVEL;
                }
                return rs.getBoolean("ativo")
                        ? ExistenciaCodigoBarras.JA_REGISTRADO_ATIVO
                        : ExistenciaCodigoBarras.JA_REGISTRADO_INATIVO;
            }
        } catch (Exception e) {
            System.err.println("[CadastroProdutoDAO] Erro ao consultar código de barras: " + e.getMessage());
            e.printStackTrace();
            return ExistenciaCodigoBarras.DISPONIVEL;
        }
    }

    public String gerarCodigoBarrasUnico() {
        for (long n = 1; n <= 99_999_999L; n++) {
            String codigo = String.format("%08d", n);
            ExistenciaCodigoBarras existencia = consultarExistenciaCodigoBarras(codigo);

            if (existencia == ExistenciaCodigoBarras.DISPONIVEL) {
                return codigo;
            }

            if (existencia == ExistenciaCodigoBarras.JA_REGISTRADO_INATIVO) {
                try {
                    if (removerProdutoDefinitivamente(codigo)) {
                        return codigo;
                    }
                } catch (SQLException e) {
                    System.err.println("[CadastroProdutoDAO] Erro ao limpar produto inativo: " + e.getMessage());
                }
            }
        }

        return String.format("%08d", System.currentTimeMillis() % 100_000_000L);
    }


    public boolean salvar(CadastroProdutoModel produto) {
        String sql = "INSERT INTO produtos " + "(codigo_barras, nome_produto, fabricante, marca, "
                + " data_fabricacao, data_vencimento, quantidade, quantidade_minima, valor, total, "
                + " prateleira, local_armazenamento, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, produto.getCodigoBarras());
            stmt.setString(2, produto.getNomeProduto());
            stmt.setString(3, produto.getFabricante());
            stmt.setString(4, produto.getMarca());
            stmt.setDate(5, Date.valueOf(produto.getDataFabricacao()));
            stmt.setDate(6, Date.valueOf(produto.getDataVencimento()));
            stmt.setLong(7, produto.getQuantidade());
            stmt.setLong(8, produto.getQuantidadeMinima());
            stmt.setBigDecimal(9, new BigDecimal(produto.getValor()));
            stmt.setBigDecimal(10, new BigDecimal(produto.getTotal()));
            stmt.setString(11, produto.getPrateleira());
            stmt.setString(12, produto.getLocalArmazenamento());
            stmt.setString(13, produto.getStatus().toUpperCase());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("[CadastroProdutoDAO] Erro ao salvar produto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean salvarComMovimentacaoInicial(CadastroProdutoModel produto) {
        String sqlProduto = "INSERT INTO produtos "
                + "(codigo_barras, nome_produto, fabricante, marca, data_fabricacao, data_vencimento, "
                + "quantidade, quantidade_minima, valor, total, prateleira, local_armazenamento, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlMov = "INSERT INTO movimentacoes (produto_id, tipo, quantidade) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection()) {
            if (conn == null) {
                return false;
            }

            conn.setAutoCommit(false);
            try {
                int produtoId;
                try (PreparedStatement stmt = conn.prepareStatement(sqlProduto, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, produto.getCodigoBarras());
                    stmt.setString(2, produto.getNomeProduto());
                    stmt.setString(3, produto.getFabricante());
                    stmt.setString(4, produto.getMarca());
                    stmt.setDate(5, Date.valueOf(produto.getDataFabricacao()));
                    stmt.setDate(6, Date.valueOf(produto.getDataVencimento()));
                    stmt.setLong(7, produto.getQuantidade());
                    stmt.setLong(8, produto.getQuantidadeMinima());
                    stmt.setBigDecimal(9, new BigDecimal(produto.getValor()));
                    stmt.setBigDecimal(10, new BigDecimal(produto.getTotal()));
                    stmt.setString(11, produto.getPrateleira());
                    stmt.setString(12, produto.getLocalArmazenamento());
                    stmt.setString(13, produto.getStatus().toUpperCase());
                    stmt.executeUpdate();

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            conn.rollback();
                            return false;
                        }
                        produtoId = keys.getInt(1);
                    }
                }

                try (PreparedStatement psMov = conn.prepareStatement(sqlMov)) {
                    psMov.setInt(1, produtoId);
                    psMov.setString(2, produto.getStatus().toUpperCase());
                    psMov.setLong(3, produto.getQuantidade());
                    psMov.executeUpdate();
                }

                new SolicitacaoCompraDAO().verificarNivelEstoque(conn, produtoId, produto.getCodigoBarras(),
                        produto.getNomeProduto(), produto.getQuantidade(), produto.getQuantidadeMinima());

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[CadastroProdutoDAO] Erro ao salvar produto com movimentação: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }



    public List<CadastroProdutoModel> listarComFiltro(String nome, String tipo, String data) {
        List<CadastroProdutoModel> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM produtos WHERE ativo = TRUE");


        if (nome != null && !nome.isBlank()) sql.append(" AND LOWER(nome_produto) LIKE ?");
        if (tipo != null && !tipo.isBlank()) {
            sql.append(" AND EXISTS (SELECT 1 FROM movimentacoes m WHERE m.produto_id = produtos.id AND m.tipo = ?)");
        }
        if (data != null && !data.isBlank()) sql.append(" AND data_fabricacao = ?");

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (nome != null && !nome.isBlank()) stmt.setString(index++, "%" + nome.toLowerCase() + "%");
            if (tipo != null && !tipo.isBlank()) stmt.setString(index++, tipo.toUpperCase());
            if (data != null && !data.isBlank()) stmt.setString(index++, data);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CadastroProdutoModel p = new CadastroProdutoModel();
                    p.setCodigoBarras(rs.getString("codigo_barras"));
                    p.setNomeProduto(rs.getString("nome_produto"));
                    p.setFabricante(rs.getString("fabricante"));
                    p.setMarca(rs.getString("marca"));
                    p.setDataFabricacao(rs.getDate("data_fabricacao").toLocalDate().toString());
                    p.setDataVencimento(rs.getDate("data_vencimento").toLocalDate().toString());
                    p.setQuantidade(rs.getLong("quantidade"));
                    p.setQuantidadeMinima(rs.getLong("quantidade_minima"));
                    p.setValor(rs.getBigDecimal("valor").toPlainString());
                    p.setTotal(rs.getBigDecimal("total").toPlainString());
                    p.setPrateleira(rs.getString("prateleira"));
                    p.setLocalArmazenamento(rs.getString("local_armazenamento"));
                    p.setStatus(rs.getString("status"));

                    p.setAtivo(rs.getBoolean("ativo"));

                    lista.add(p);
                }
            }

        } catch (Exception e) {
            System.err.println("[CadastroProdutoDAO] Erro ao listar produtos: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }



    public boolean atualizar(CadastroProdutoModel produto) {
        String sql = "UPDATE produtos SET " + "nome_produto = ?, fabricante = ?, marca = ?, "
                + "data_fabricacao = ?, data_vencimento = ?, "
                + "quantidade = ?, quantidade_minima = ?, valor = ?, total = ?, "
                + "prateleira = ?, local_armazenamento = ?, status = ? "
                + "WHERE codigo_barras = ?";

        try (Connection conn = ConnectionFactory.getConnection()) {
            if (conn == null) {
                return false;
            }

            conn.setAutoCommit(false);
            try {
                boolean atualizado;
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, produto.getNomeProduto());
                    stmt.setString(2, produto.getFabricante());
                    stmt.setString(3, produto.getMarca());
                    stmt.setDate(4, Date.valueOf(produto.getDataFabricacao()));
                    stmt.setDate(5, Date.valueOf(produto.getDataVencimento()));
                    stmt.setLong(6, produto.getQuantidade());
                    stmt.setLong(7, produto.getQuantidadeMinima());
                    stmt.setBigDecimal(8, new BigDecimal(produto.getValor()));
                    stmt.setBigDecimal(9, new BigDecimal(produto.getTotal()));
                    stmt.setString(10, produto.getPrateleira());
                    stmt.setString(11, produto.getLocalArmazenamento());
                    stmt.setString(12, produto.getStatus().toUpperCase());
                    stmt.setString(13, produto.getCodigoBarras());

                    atualizado = stmt.executeUpdate() > 0;
                }

                if (atualizado) {
                    int produtoId = buscarIdPorCodigo(conn, produto.getCodigoBarras());
                    if (produtoId > 0) {
                        new SolicitacaoCompraDAO().verificarNivelEstoque(conn, produtoId, produto.getCodigoBarras(),
                                produto.getNomeProduto(), produto.getQuantidade(), produto.getQuantidadeMinima());
                    }
                }

                conn.commit();
                return atualizado;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[CadastroProdutoDAO] Erro ao atualizar produto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }



    public boolean excluirPorCodigo(String codigoBarras) throws SQLException {
        if (codigoBarras == null || codigoBarras.isBlank()) {
            return false;
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            if (conn == null) {
                return false;
            }

            conn.setAutoCommit(false);
            try {
                int produtoId = buscarIdPorCodigo(conn, codigoBarras.trim());
                if (produtoId <= 0 || !produtoEstaAtivo(conn, produtoId)) {
                    conn.rollback();
                    return false;
                }

                boolean removido = removerProdutoDefinitivamentePorId(conn, produtoId);
                if (removido) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
                return removido;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private boolean removerProdutoDefinitivamente(String codigoBarras) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection()) {
            if (conn == null) {
                return false;
            }

            conn.setAutoCommit(false);
            try {
                int produtoId = buscarIdPorCodigo(conn, codigoBarras.trim());
                if (produtoId <= 0) {
                    conn.rollback();
                    return false;
                }

                boolean removido = removerProdutoDefinitivamentePorId(conn, produtoId);
                if (removido) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
                return removido;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private boolean removerProdutoDefinitivamentePorId(Connection conn, int produtoId) throws SQLException {
        try (PreparedStatement psMov = conn.prepareStatement("DELETE FROM movimentacoes WHERE produto_id = ?")) {
            psMov.setInt(1, produtoId);
            psMov.executeUpdate();
        }

        try (PreparedStatement psProd = conn.prepareStatement("DELETE FROM produtos WHERE id = ?")) {
            psProd.setInt(1, produtoId);
            return psProd.executeUpdate() > 0;
        }
    }

    private boolean produtoEstaAtivo(Connection conn, int produtoId) throws SQLException {
        String sql = "SELECT ativo FROM produtos WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, produtoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean("ativo");
            }
        }
    }


    public int buscarIdPorCodigo(String codigoBarras) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            return buscarIdPorCodigo(conn, codigoBarras);
        } catch (SQLException e) {
            System.err.println("[CadastroProdutoDAO] Erro ao buscar ID por código: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private int buscarIdPorCodigo(Connection conn, String codigoBarras) throws SQLException {
        if (codigoBarras == null || codigoBarras.isBlank()) {
            return 0;
        }

        String sql = "SELECT id FROM produtos WHERE codigo_barras = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoBarras.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        return 0;
    }

    public CadastroProdutoModel buscarPorCodigoBarras(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.isBlank()) {
            return null;
        }

        String sql = "SELECT * FROM produtos WHERE codigo_barras = ? AND ativo = TRUE";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigoBarras.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                CadastroProdutoModel p = new CadastroProdutoModel();
                p.setCodigoBarras(rs.getString("codigo_barras"));
                p.setNomeProduto(rs.getString("nome_produto"));
                p.setFabricante(rs.getString("fabricante"));
                p.setMarca(rs.getString("marca"));
                p.setDataFabricacao(rs.getDate("data_fabricacao").toLocalDate().toString());
                p.setDataVencimento(rs.getDate("data_vencimento").toLocalDate().toString());
                p.setQuantidade(rs.getLong("quantidade"));
                p.setQuantidadeMinima(rs.getLong("quantidade_minima"));
                p.setValor(rs.getBigDecimal("valor").toPlainString());
                p.setTotal(rs.getBigDecimal("total").toPlainString());
                p.setPrateleira(rs.getString("prateleira"));
                p.setLocalArmazenamento(rs.getString("local_armazenamento"));
                p.setStatus(rs.getString("status"));
                p.setAtivo(rs.getBoolean("ativo"));
                return p;
            }

        } catch (Exception e) {
            System.err.println("[CadastroProdutoDAO] Erro ao buscar produto por código: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

  
    public String registrarMovimentacao(String codigoBarras, String tipo, long qtd) {
        if (codigoBarras == null || codigoBarras.isBlank() || tipo == null || tipo.isBlank() || qtd <= 0) {
            return "Informe produto, tipo e quantidade válidos (maior que zero).";
        }

        String tipoNorm = tipo.trim().toUpperCase();
        if (!tipoNorm.equals("ENTRADA") && !tipoNorm.equals("SAIDA")) {
            return "Tipo de movimentação inválido.";
        }

        String sqlSelect = "SELECT id, quantidade, valor, quantidade_minima, nome_produto "
                + "FROM produtos WHERE codigo_barras = ? AND ativo = TRUE FOR UPDATE";
        String sqlUpdate = "UPDATE produtos SET quantidade = ?, total = ?, status = ?, atualizado_em = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection()) {
            if (conn == null) {
                return "Falha na conexão com o banco de dados.";
            }

            conn.setAutoCommit(false);

            int produtoId;
            long quantidadeAtual;
            long quantidadeMinima;
            String nomeProduto;
            BigDecimal valorUnit;

            try (PreparedStatement psSelect = conn.prepareStatement(sqlSelect)) {
                psSelect.setString(1, codigoBarras.trim());
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return "Produto não encontrado ou inativo.";
                    }
                    produtoId = rs.getInt("id");
                    quantidadeAtual = rs.getLong("quantidade");
                    valorUnit = rs.getBigDecimal("valor");
                    quantidadeMinima = rs.getLong("quantidade_minima");
                    nomeProduto = rs.getString("nome_produto");
                }
            }

            long novaQuantidade = tipoNorm.equals("ENTRADA")
                    ? quantidadeAtual + qtd
                    : quantidadeAtual - qtd;

            if (novaQuantidade < 0) {
                conn.rollback();
                return "Quantidade em estoque insuficiente para esta saída.";
            }

            BigDecimal novoTotal = valorUnit.multiply(BigDecimal.valueOf(novaQuantidade));

            try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                psUpdate.setLong(1, novaQuantidade);
                psUpdate.setBigDecimal(2, novoTotal);
                psUpdate.setString(3, tipoNorm);
                psUpdate.setInt(4, produtoId);
                psUpdate.executeUpdate();
            }

            String sqlMov = "INSERT INTO movimentacoes (produto_id, tipo, quantidade) VALUES (?, ?, ?)";
            try (PreparedStatement psMov = conn.prepareStatement(sqlMov)) {
                psMov.setInt(1, produtoId);
                psMov.setString(2, tipoNorm);
                psMov.setLong(3, qtd);
                psMov.executeUpdate();
            }

        
            new SolicitacaoCompraDAO().verificarNivelEstoque(conn, produtoId, codigoBarras.trim(),
                    nomeProduto, novaQuantidade, quantidadeMinima);

            conn.commit();
            return null;

        } catch (SQLException e) {
            System.err.println("[CadastroProdutoDAO] Erro ao registrar movimentação: " + e.getMessage());
            e.printStackTrace();
            return "Erro ao registrar movimentação.";
        }
    }
}
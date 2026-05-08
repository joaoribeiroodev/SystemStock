package dao;

import connection.ConnectionFactory;
import model.CadastroProdutoModel;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CadastroProdutoDAO {



    public boolean salvar(CadastroProdutoModel produto) {
        String sql =
                "INSERT INTO produtos " +
                        "(codigo_barras, nome_produto, fabricante, marca, " +
                        " data_fabricacao, data_vencimento, quantidade, valor, total, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, produto.getCodigoBarras());
            stmt.setString(2, produto.getNomeProduto());
            stmt.setString(3, produto.getFabricante());
            stmt.setString(4, produto.getMarca());
            stmt.setDate(5, Date.valueOf(produto.getDataFabricacao()));
            stmt.setDate(6, Date.valueOf(produto.getDataVencimento()));
            stmt.setLong(7, produto.getQuantidade());
            stmt.setBigDecimal(8, new BigDecimal(produto.getValor()));
            stmt.setBigDecimal(9, new BigDecimal(produto.getTotal()));
            stmt.setString(10, produto.getStatus().toUpperCase());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("[CadastroProdutoDAO] Erro ao salvar produto: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }



    public List<CadastroProdutoModel> listarComFiltro(String nome, String tipo, String data) {
        List<CadastroProdutoModel> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM produtos WHERE 1=1");

        if (nome != null && !nome.isBlank()) sql.append(" AND LOWER(nome_produto) LIKE ?");
        if (tipo != null && !tipo.isBlank())  sql.append(" AND status = ?");
        if (data != null && !data.isBlank())  sql.append(" AND data_fabricacao = ?");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (nome != null && !nome.isBlank()) stmt.setString(index++, "%" + nome.toLowerCase() + "%");
            if (tipo != null && !tipo.isBlank())  stmt.setString(index++, tipo.toUpperCase());
            if (data != null && !data.isBlank())  stmt.setString(index++, data);

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

                    p.setValor(rs.getBigDecimal("valor").toPlainString());
                    p.setTotal(rs.getBigDecimal("total").toPlainString());

                    p.setStatus(rs.getString("status"));
                    lista.add(p);
                }
            }

        } catch (Exception e) {
            System.err.println("[CadastroProdutoDAO] Erro ao listar produtos: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }



    public int buscarIdPorCodigo(String codigoBarras) {
        String sql = "SELECT id FROM produtos WHERE codigo_barras = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigoBarras);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }

        } catch (Exception e) {
            System.err.println("[CadastroProdutoDAO] Erro ao buscar ID por código: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }
}
package dao;

import connection.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.CadastroProdutoModel;

public class CadastroProdutoDAO {

    public boolean salvar(CadastroProdutoModel produto) {

        String sql = "INSERT INTO produtos"
                + "(codigoBarras, nomeProduto, fabricante, marca, datafabricacao, dataVencimento, quantidade, valor, total, satus)"
                + "VALUES (?,?,?,?,?,?,?,?,?)";

        try (Connection conn = ConnectionFactory.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, produto.getCodigoBarras());
                stmt.setString(2, produto.getNomeProduto());
                stmt.setString(3, produto.getFabricante());
                stmt.setString(4, produto.getMarca());
                stmt.setDate(5, java.sql.Date.valueOf(produto.getDatafabricacao()));
                stmt.setDate(6, java.sql.Date.valueOf(produto.getDataVencimento()));
                stmt.setLong(7, produto.getQuantidade());
                stmt.setString(8, produto.getValor());
                stmt.setString(9, produto.getTotal());
                stmt.setString(10, produto.getStatus());
                
                stmt.executeUpdate();
                
                return true;
            
        } catch (SQLException e) {          
            e.printStackTrace();
            return false;            
        }
    }
    public List<CadastroProdutoModel> listar() {
        List<CadastroProdutoModel> lista = new ArrayList<>();
        
        String sql = "SELECT * FROM produtos";
        
        try(Connection conn = ConnectionFactory.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
            
            while(rs.next()) {
                CadastroProdutoModel p = new CadastroProdutoModel();
                
                p.setCodigoBarras(rs.getString("codigo_barras"));
                p.setNomeProduto(rs.getString("nome_produto"));
                p.setFabricante(rs.getString("fabricante"));
                p.setMarca(rs.getString("marca"));
                p.setQuantidade(rs.getLong("quantidade"));
                p.setValor(rs.getString("valor"));
                p.setTotal(rs.getString("total"));
                p.setStatus(rs.getString("status"));
                
                lista.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return lista;
    }
}

package dao;

import connection.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import model.CadastroProdutoModel;

public class CadastroProdutosDAO {

    public boolean salvar(CadastroProdutoModel produto) {
        String sql = "INSERT INTO produtos "
                + "(codigo_barras,nome_produto,fabricante,marca,data_fabricacao,data_vencimento,quantidade,valor,total)"
                + "VALUE(?,?,?,?,?,?,?,?,?)";

        try (Connection conn = ConnectionFactory.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, produto.getCodigoBarras());
                stmt.setString(2, produto.getNomeProduto());
                stmt.setString(3, produto.getFabricante());
                stmt.setString(4, produto.getMarca());
                stmt.setDate(5, java.sql.Date.valueOf(produto.getDatafabricacao()));
                stmt.setDate(6, java.sql.Date.valueOf(produto.getDataVencimento()));
                stmt.setLong(7, produto.getQuantidade());
                stmt.setString(8, produto.getValor());
                stmt.setString(9, produto.getTotal());

                stmt.executeUpdate();
                
                return true;
            } catch (SQLException e) {
            e.printStackTrace();
            return false;
            }
        }
    }

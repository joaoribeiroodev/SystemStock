package dao;

import connection.ConnectionFactory;
import java.sql.PreparedStatement;
import model.CadastroUsuarioModel;

public class CadastroUsersDAO {
    
    public boolean cadastrar(CadastroUsuarioModel user) {
    String sql = "INSERT INTO users " +
            "(username, psw, nameFirst, sobreNome, matricula, cpf, sexo, data_nascimento, email, telefone, funcao, cep, endereco, bairro, cidade, estado, numero, complemento) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    try (var con = ConnectionFactory.getConnection()) {
        PreparedStatement stmt = con.prepareStatement(sql);

        stmt.setString(1, user.getNomeUsuario());
        stmt.setString(2, user.getPsw());
        stmt.setString(3, user.getNome());
        stmt.setString(4, user.getSobrenome());
        stmt.setString(5, user.getMatricula());
        stmt.setString(6, user.getCpf());
        stmt.setString(7, user.getSexo());
        stmt.setString(8, user.getData());
        stmt.setString(9, user.getEmail());
        stmt.setString(10, user.getTelefone());
        stmt.setString(11, user.getFuncao());
        stmt.setString(12, user.getCep());
        stmt.setString(13, user.getEndereco());
        stmt.setString(14, user.getBairro());
        stmt.setString(15, user.getCidade());
        stmt.setString(16, user.getEstado());
        stmt.setLong(17, user.getNumero());
        stmt.setString(18, user.getComplemento());

        stmt.executeUpdate(); // ← ESSENCIAL
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
}

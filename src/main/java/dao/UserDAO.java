
package dao;

import connection.ConnectionFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import model.UserModel;
import util.SenhaUtil;

public class UserDAO {

    public UserModel validarLogin(UserModel userModel) {
        String sql = "SELECT * FROM users WHERE BINARY username = ?";
        try (var con = ConnectionFactory.getConnection()) {
            PreparedStatement stmt
                    = con.prepareStatement(sql);
            stmt.setString(1, userModel.getUsername());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashBanco = rs.getString("psw");

                boolean senhaValida = SenhaUtil.verificarSenha(
                        userModel.getPassword(), 
                        hashBanco
                );
                
                if(senhaValida) {
                    UserModel user = new UserModel();
                    user.setUsername(rs.getString("username"));
                    user.setPassword(hashBanco);
                    user.setFuncao(rs.getString("funcao"));
                    
                    return user;
                }
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

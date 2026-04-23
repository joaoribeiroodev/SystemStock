/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import connection.ConnectionFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import model.UserModel;
import util.SenhaUtil;

public class UserDAO {

    public boolean validarLogin(UserModel userModel) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (var con = ConnectionFactory.getConnection()) {
            PreparedStatement stmt
                    = con.prepareStatement(sql);
            stmt.setString(1, userModel.getUsername());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashBanco = rs.getString("psw");

                return SenhaUtil.verificarSenha(
                        userModel.getPassword(),
                        hashBanco
                );
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

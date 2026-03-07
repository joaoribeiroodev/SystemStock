/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import connection.ConnectionFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import model.UserModel;

public class UserDAO {
    
    public boolean validarLogin(UserModel userModel){
         String sql = "SELECT * FROM users WHERE username = ? AND psw = ?";
         try (var con = ConnectionFactory.getConnection()) {
             PreparedStatement stmt = 
                     con.prepareStatement(sql);
            stmt.setString(1, userModel.getUsername());
            stmt.setString(2, userModel.getPassword());
            
             ResultSet rs = stmt.executeQuery();
             
             return rs.next();
           
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}


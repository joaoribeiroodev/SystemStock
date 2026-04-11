/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package connection;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author 232.004057
 */
public class ConnectionFactory {

    private static final String URL = System.getenv("DB_URL");
    private static final String USER = System.getenv("DB_USER");
    private static final String PASSWORD = System.getenv("DB_PASS");
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    
    //métodos
    public static Connection getConnection() {
        Connection con = null;
        
        try {
            if(URL == null || USER == null || PASSWORD == null) {
                System.out.println("variavel de ambiente");
                return null;
            }
            Class.forName(DRIVER);
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Bando de Dados Conectado");
        }catch (Exception e) {
            System.out.println("Banco de Dados Não Conectado");
            e.printStackTrace();
        }
        
        return con;
    }
            
    
}

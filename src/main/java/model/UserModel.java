package model;

/**
 *
 * @author 232.004057
 */
public class UserModel {
    
    private int id;
    private String username;
    private String password;
    
    public UserModel() {
        
    }
    
     public String getUsername() {
        return username;
    }
    public void setUsername(String username){
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    public void setPassword(String password){
        this.password = password;
    }

}

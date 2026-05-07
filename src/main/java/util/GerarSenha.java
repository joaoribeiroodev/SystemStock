
package util;

import org.mindrot.jbcrypt.BCrypt;


public class GerarSenha {
    
    public static void main (String[] args) {
        String senha = "123";
        String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());
        
        System.out.println("Senha hash:");
        System.out.print(senhaHash);
    }
}

package controller;

import dao.CadastroUsersDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import model.CadastroUsuarioModel;
import util.PerfilUtil;
import util.ValidacaoUsuario;

@WebServlet("/pages/cadastro")
public class CadastroController extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String perfil = session != null ? (String) session.getAttribute("perfil") : null;

        if (!PerfilUtil.podeCadastrarUsuario(perfil)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        CadastroUsuarioModel user = new CadastroUsuarioModel();

        user.setNome(request.getParameter("nameFirst"));
        user.setSobrenome(request.getParameter("sobreNome"));
        user.setMatricula(request.getParameter("matricula"));
        user.setCpf(request.getParameter("cpf"));
        user.setSexo(request.getParameter("opcao"));

        String dataNascimento = request.getParameter("dtaNascimento");
        if (ValidacaoUsuario.validarDataNascimento(dataNascimento) != null) {
            response.sendRedirect(request.getContextPath() + "/pages/cadastro.html");
            return;
        }
        user.setData(dataNascimento);
        user.setEmail(request.getParameter("email"));
        user.setTelefone(request.getParameter("telefone"));
        user.setNomeUsuario(request.getParameter("usuario"));
        user.setPsw(request.getParameter("psw"));
        user.setFuncao(request.getParameter("funcao"));
        user.setCep(request.getParameter("cep"));
        user.setEndereco(request.getParameter("endereco"));
        user.setCidade(request.getParameter("cidade"));
        user.setBairro(request.getParameter("bairro"));
        user.setEstado(request.getParameter("estado"));
        user.setComplemento(request.getParameter("complemento"));


        user.setNumero(request.getParameter("numero"));

        CadastroUsersDAO dao = new CadastroUsersDAO();

        if (dao.cadastrar(user)) {
            response.sendRedirect(request.getContextPath() + "/pages/dashboard.html");
        } else {
            response.sendRedirect(request.getContextPath() + "/pages/cadastro.html");
        }
    }
}
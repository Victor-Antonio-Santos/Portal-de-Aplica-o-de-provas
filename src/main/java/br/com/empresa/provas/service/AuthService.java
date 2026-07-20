package br.com.empresa.provas.service;

import br.com.empresa.provas.dao.LogOperacaoDAO;
import br.com.empresa.provas.dao.UsuarioDAO;
import br.com.empresa.provas.entity.LogOperacao;
import br.com.empresa.provas.entity.Usuario;
import br.com.empresa.provas.entity.enums.PerfilUsuario;
import br.com.empresa.provas.util.CPFUtil;
import br.com.empresa.provas.util.PasswordUtil;

public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final LogOperacaoDAO logOperacaoDAO = new LogOperacaoDAO();

    public Usuario autenticar(String cpf, String senha, PerfilUsuario perfil) {
        if (!CPFUtil.isValido(cpf)) {
            throw new IllegalArgumentException("CPF invalido.");
        }

        Usuario usuario = usuarioDAO.buscarPorCpf(cpf);
        if (usuario == null || !usuario.isAtivo()) {
            registrarLog("LOGIN_NEGADO", CPFUtil.formatar(cpf), "Tentativa de login para usuario inexistente/inativo.");
            throw new IllegalArgumentException("Usuario nao encontrado ou inativo.");
        }
        if (!usuario.getPerfil().equals(perfil)) {
            registrarLog("LOGIN_NEGADO", usuario.getCpf(), "Perfil informado nao corresponde ao usuario.");
            throw new IllegalArgumentException("Perfil de acesso invalido.");
        }
        if (!PasswordUtil.matches(senha, usuario.getSenha())) {
            registrarLog("LOGIN_NEGADO", usuario.getCpf(), "Senha invalida.");
            throw new IllegalArgumentException("Senha invalida.");
        }

        registrarLog("LOGIN_SUCESSO", usuario.getCpf(), "Login realizado com sucesso.");
        return usuario;
    }

    public void registrarLogout(Usuario usuario) {
        if (usuario != null) {
            registrarLog("LOGOUT", usuario.getCpf(), "Sessao encerrada.");
        }
    }

    private void registrarLog(String acao, String usuario, String descricao) {
        LogOperacao log = new LogOperacao();
        log.setAcao(acao);
        log.setUsuario(usuario);
        log.setDescricao(descricao);
        logOperacaoDAO.salvar(log);
    }
}


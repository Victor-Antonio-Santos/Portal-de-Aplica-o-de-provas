package br.com.empresa.provas.service;

import br.com.empresa.provas.dao.LogOperacaoDAO;
import br.com.empresa.provas.dao.UsuarioDAO;
import br.com.empresa.provas.entity.Turma;
import br.com.empresa.provas.entity.LogOperacao;
import br.com.empresa.provas.entity.Usuario;
import br.com.empresa.provas.entity.enums.PerfilUsuario;
import br.com.empresa.provas.util.CPFUtil;
import br.com.empresa.provas.util.PasswordUtil;

import java.util.List;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final LogOperacaoDAO logOperacaoDAO = new LogOperacaoDAO();
    private final TurmaService turmaService = new TurmaService();

    public Usuario salvar(Usuario usuario, String senhaPura) {
        validarUsuario(usuario, senhaPura);
        Usuario existente = usuarioDAO.buscarPorCpf(usuario.getCpf());
        if (existente != null && (usuario.getId() == null || !existente.getId().equals(usuario.getId()))) {
            throw new IllegalArgumentException("Ja existe usuario cadastrado com este CPF.");
        }

        usuario.setCpf(CPFUtil.somenteDigitos(usuario.getCpf()));
        if (senhaPura != null && !senhaPura.trim().isEmpty()) {
            usuario.setSenha(PasswordUtil.hash(senhaPura));
        } else if (usuario.getId() == null) {
            throw new IllegalArgumentException("Senha obrigatoria para novos cadastros.");
        }

        Usuario salvo = usuarioDAO.salvar(usuario);
        registrarLog("USUARIO_SALVO", usuario.getCpf(), "Cadastro/atualizacao de usuario " + usuario.getNome());
        return salvo;
    }

    public Usuario autoCadastrarColaborador(Usuario usuario, Long turmaId, String senhaPura, String confirmacaoSenha) {
        validarAutoCadastro(usuario, senhaPura, confirmacaoSenha);
        Usuario existente = usuarioDAO.buscarPorCpf(usuario.getCpf());
        if (existente != null) {
            throw new IllegalArgumentException("Ja existe colaborador cadastrado com este CPF.");
        }

        Turma turma = turmaService.validarTurmaAtiva(turmaId);
        usuario.setCpf(CPFUtil.somenteDigitos(usuario.getCpf()));
        usuario.setPerfil(PerfilUsuario.ROLE_COLABORADOR);
        usuario.setStatus(br.com.empresa.provas.entity.enums.StatusUsuario.ATIVO);
        usuario.setTurma(turma);
        usuario.setAplicadorResponsavel(turma.getAplicadorResponsavel());
        usuario.setSenha(PasswordUtil.hash(senhaPura));

        Usuario salvo = usuarioDAO.salvar(usuario);
        registrarLog("AUTO_CADASTRO_COLABORADOR", usuario.getCpf(), "Colaborador realizou auto cadastro no portal.");
        return salvo;
    }

    public void excluir(Long id) {
        usuarioDAO.excluir(id);
    }

    public List<Usuario> listarColaboradores() {
        return usuarioDAO.listarPorPerfil(PerfilUsuario.ROLE_COLABORADOR);
    }

    public List<Usuario> listarColaboradoresPorResponsavel(Usuario responsavel) {
        if (responsavel == null || responsavel.getId() == null) {
            return listarColaboradores();
        }
        return usuarioDAO.listarColaboradoresPorResponsavel(responsavel.getId());
    }

    public List<Usuario> listarAdministradores() {
        return usuarioDAO.listarPorPerfil(PerfilUsuario.ROLE_ADMIN);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioDAO.buscarPorId(id);
    }

    private void validarUsuario(Usuario usuario, String senhaPura) {
        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome obrigatorio.");
        }
        if (!CPFUtil.isValido(usuario.getCpf())) {
            throw new IllegalArgumentException("CPF invalido.");
        }
        if (usuario.getPerfil() == null) {
            throw new IllegalArgumentException("Perfil obrigatorio.");
        }
        if (PerfilUsuario.ROLE_COLABORADOR.equals(usuario.getPerfil())) {
            if (usuario.getTurma() == null || usuario.getTurma().getId() == null) {
                throw new IllegalArgumentException("Turma obrigatoria para colaboradores.");
            }
            if (usuario.getAplicadorResponsavel() == null || usuario.getAplicadorResponsavel().getId() == null) {
                throw new IllegalArgumentException("Defina o treinador responsavel pelo colaborador.");
            }
        }
        if (usuario.getId() == null && (senhaPura == null || senhaPura.trim().isEmpty())) {
            throw new IllegalArgumentException("Senha obrigatoria.");
        }
    }

    private void validarAutoCadastro(Usuario usuario, String senhaPura, String confirmacaoSenha) {
        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome obrigatorio.");
        }
        if (!CPFUtil.isValido(usuario.getCpf())) {
            throw new IllegalArgumentException("CPF invalido.");
        }
        if (senhaPura == null || senhaPura.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha obrigatoria.");
        }
        if (confirmacaoSenha == null || confirmacaoSenha.trim().isEmpty()) {
            throw new IllegalArgumentException("Confirme a senha informada.");
        }
        if (!senhaPura.equals(confirmacaoSenha)) {
            throw new IllegalArgumentException("A confirmacao da senha nao confere.");
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


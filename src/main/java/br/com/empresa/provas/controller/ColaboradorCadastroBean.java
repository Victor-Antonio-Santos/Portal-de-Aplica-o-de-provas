package br.com.empresa.provas.controller;

import br.com.empresa.provas.entity.Turma;
import br.com.empresa.provas.entity.Usuario;
import br.com.empresa.provas.entity.enums.PerfilUsuario;
import br.com.empresa.provas.entity.enums.StatusUsuario;
import br.com.empresa.provas.service.TurmaService;
import br.com.empresa.provas.service.UsuarioService;
import br.com.empresa.provas.util.FacesUtil;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.List;

@Named("colaboradorCadastroBean")
@ViewScoped
public class ColaboradorCadastroBean implements Serializable {

    private final UsuarioService usuarioService = new UsuarioService();
    private final TurmaService turmaService = new TurmaService();

    @Inject
    private SessionBean sessionBean;

    private Usuario colaborador;
    private Long turmaId;
    private String senha;
    private List<Usuario> colaboradores;
    private List<Turma> turmas;

    @PostConstruct
    public void init() {
        novo();
        carregarLista();
        carregarTurmas();
    }

    public void salvar() {
        try {
            colaborador.setPerfil(PerfilUsuario.ROLE_COLABORADOR);
            aplicarResponsavelPadrao();
            colaborador.setTurma(turmaService.validarTurmaAtiva(turmaId));
            Usuario salvo = usuarioService.salvar(colaborador, senha);
            FacesUtil.addInfoMessage("Colaborador salvo com sucesso.");
            colaborador = salvo;
            turmaId = salvo.getTurma() == null ? null : salvo.getTurma().getId();
            senha = null;
            carregarLista();
            novo();
        } catch (IllegalArgumentException e) {
            FacesUtil.addErrorMessage(e.getMessage());
        }
    }

    public void editar(Usuario usuario) {
        this.colaborador = usuario;
        this.turmaId = usuario.getTurma() == null ? null : usuario.getTurma().getId();
    }

    public void excluir(Long id) {
        usuarioService.excluir(id);
        carregarLista();
    }

    public void novo() {
        colaborador = new Usuario();
        colaborador.setStatus(StatusUsuario.ATIVO);
        colaborador.setPerfil(PerfilUsuario.ROLE_COLABORADOR);
        aplicarResponsavelPadrao();
        turmaId = null;
    }

    public StatusUsuario[] getStatusDisponiveis() {
        return StatusUsuario.values();
    }

    private void carregarLista() {
        colaboradores = usuarioService.listarColaboradoresPorResponsavel(sessionBean == null ? null : sessionBean.getUsuarioLogado());
    }

    private void carregarTurmas() {
        turmas = turmaService.listarPorResponsavel(sessionBean == null ? null : sessionBean.getUsuarioLogado());
    }

    private void aplicarResponsavelPadrao() {
        if (colaborador != null && colaborador.getAplicadorResponsavel() == null
                && sessionBean != null && sessionBean.getUsuarioLogado() != null) {
            colaborador.setAplicadorResponsavel(sessionBean.getUsuarioLogado());
        }
    }

    public Usuario getColaborador() {
        return colaborador;
    }

    public void setColaborador(Usuario colaborador) {
        this.colaborador = colaborador;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public List<Usuario> getColaboradores() {
        return colaboradores;
    }

    public Long getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(Long turmaId) {
        this.turmaId = turmaId;
    }

    public List<Turma> getTurmas() {
        return turmas;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }
}


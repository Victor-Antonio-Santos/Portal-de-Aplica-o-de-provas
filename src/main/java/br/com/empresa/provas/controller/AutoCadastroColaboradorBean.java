package br.com.empresa.provas.controller;

import br.com.empresa.provas.entity.Turma;
import br.com.empresa.provas.entity.Usuario;
import br.com.empresa.provas.service.TurmaService;
import br.com.empresa.provas.service.UsuarioService;
import br.com.empresa.provas.util.FacesUtil;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@Named("autoCadastroColaboradorBean")
@ViewScoped
public class AutoCadastroColaboradorBean implements Serializable {

    private final UsuarioService usuarioService = new UsuarioService();
    private final TurmaService turmaService = new TurmaService();

    private Usuario colaborador = new Usuario();
    private Long turmaId;
    private String senha;
    private String confirmacaoSenha;
    private List<Turma> turmasAtivas;

    @PostConstruct
    public void init() {
        turmasAtivas = turmaService.listarAtivas();
    }

    public String cadastrar() {
        try {
            usuarioService.autoCadastrarColaborador(colaborador, turmaId, senha, confirmacaoSenha);
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesUtil.addInfoMessage("Cadastro realizado com sucesso. Agora voce ja pode acessar o portal.");
            return "/pages/login.xhtml?faces-redirect=true";
        } catch (IllegalArgumentException e) {
            FacesUtil.addErrorMessage(e.getMessage());
            return null;
        }
    }

    public void irParaLogin() {
        try {
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/pages/login.xhtml");
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel redirecionar para o login.", e);
        }
    }

    public Usuario getColaborador() {
        return colaborador;
    }

    public Long getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(Long turmaId) {
        this.turmaId = turmaId;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getConfirmacaoSenha() {
        return confirmacaoSenha;
    }

    public void setConfirmacaoSenha(String confirmacaoSenha) {
        this.confirmacaoSenha = confirmacaoSenha;
    }

    public List<Turma> getTurmasAtivas() {
        return turmasAtivas;
    }
}


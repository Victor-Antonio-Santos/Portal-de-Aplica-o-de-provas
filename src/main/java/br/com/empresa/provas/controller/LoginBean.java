package br.com.empresa.provas.controller;

import br.com.empresa.provas.entity.Usuario;
import br.com.empresa.provas.entity.enums.PerfilUsuario;
import br.com.empresa.provas.service.AuthService;
import br.com.empresa.provas.util.FacesUtil;

import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;

@Named("loginBean")
@ViewScoped
public class LoginBean implements Serializable {

    private String cpf;
    private String senha;
    private String tipoAcesso = PerfilUsuario.ROLE_COLABORADOR.name();

    private final AuthService authService = new AuthService();

    @Inject
    private SessionBean sessionBean;

    public String login() {
        try {
            Usuario usuario = authService.autenticar(cpf, senha, getPerfilSelecionado());
            sessionBean.registrarLogin(usuario);
            return sessionBean.getPaginaInicial();
        } catch (IllegalArgumentException e) {
            FacesUtil.addErrorMessage(e.getMessage());
            return null;
        }
    }

    private PerfilUsuario getPerfilSelecionado() {
        return PerfilUsuario.valueOf(tipoAcesso);
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTipoAcesso() {
        return tipoAcesso;
    }

    public void setTipoAcesso(String tipoAcesso) {
        this.tipoAcesso = tipoAcesso;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }
}


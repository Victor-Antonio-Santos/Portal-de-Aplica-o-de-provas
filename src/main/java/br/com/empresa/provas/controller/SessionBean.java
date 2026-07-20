package br.com.empresa.provas.controller;

import br.com.empresa.provas.entity.Usuario;
import br.com.empresa.provas.entity.enums.PerfilUsuario;
import br.com.empresa.provas.service.AuthService;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;

@Named("sessionBean")
@SessionScoped
public class SessionBean implements Serializable {

    private Usuario usuarioLogado;
    private final AuthService authService = new AuthService();

    public void registrarLogin(Usuario usuario) {
        this.usuarioLogado = usuario;
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("usuarioLogado", usuario);
    }

    public String logout() {
        authService.registrarLogout(usuarioLogado);
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/pages/login.xhtml?faces-redirect=true";
    }

    public String getPaginaInicial() {
        if (usuarioLogado == null) {
            return "/pages/login.xhtml?faces-redirect=true";
        }
        return isAdmin()
                ? "/pages/admin/dashboard.xhtml?faces-redirect=true"
                : "/pages/colaborador/minhas-provas.xhtml?faces-redirect=true";
    }

    public boolean isAdmin() {
        return usuarioLogado != null && PerfilUsuario.ROLE_ADMIN.equals(usuarioLogado.getPerfil());
    }

    public boolean isColaborador() {
        return usuarioLogado != null && PerfilUsuario.ROLE_COLABORADOR.equals(usuarioLogado.getPerfil());
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }
}


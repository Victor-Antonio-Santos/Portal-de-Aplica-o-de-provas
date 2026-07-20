package br.com.empresa.provas.controller;

import br.com.empresa.provas.entity.Usuario;
import br.com.empresa.provas.entity.enums.PerfilUsuario;
import br.com.empresa.provas.entity.enums.StatusUsuario;
import br.com.empresa.provas.service.UsuarioService;
import br.com.empresa.provas.util.FacesUtil;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.List;

@Named("aplicadorCadastroBean")
@ViewScoped
public class AplicadorCadastroBean implements Serializable {

    private final UsuarioService usuarioService = new UsuarioService();

    private Usuario aplicador;
    private String senha;
    private List<Usuario> aplicadores;

    @PostConstruct
    public void init() {
        novo();
        carregarLista();
    }

    public void salvar() {
        try {
            aplicador.setPerfil(PerfilUsuario.ROLE_ADMIN);
            Usuario salvo = usuarioService.salvar(aplicador, senha);
            FacesUtil.addInfoMessage("Aplicador salvo com sucesso.");
            aplicador = salvo;
            senha = null;
            carregarLista();
            novo();
        } catch (IllegalArgumentException e) {
            FacesUtil.addErrorMessage(e.getMessage());
        }
    }

    public void editar(Usuario usuario) {
        this.aplicador = usuario;
    }

    public void excluir(Long id) {
        usuarioService.excluir(id);
        carregarLista();
    }

    public void novo() {
        aplicador = new Usuario();
        aplicador.setStatus(StatusUsuario.ATIVO);
        aplicador.setPerfil(PerfilUsuario.ROLE_ADMIN);
    }

    public StatusUsuario[] getStatusDisponiveis() {
        return StatusUsuario.values();
    }

    private void carregarLista() {
        aplicadores = usuarioService.listarAdministradores();
    }

    public Usuario getAplicador() {
        return aplicador;
    }

    public void setAplicador(Usuario aplicador) {
        this.aplicador = aplicador;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public List<Usuario> getAplicadores() {
        return aplicadores;
    }
}


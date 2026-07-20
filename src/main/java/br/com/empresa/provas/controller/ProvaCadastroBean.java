package br.com.empresa.provas.controller;

import br.com.empresa.provas.entity.Prova;
import br.com.empresa.provas.entity.enums.StatusProva;
import br.com.empresa.provas.service.ProvaService;
import br.com.empresa.provas.util.FacesUtil;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Named("provaCadastroBean")
@ViewScoped
public class ProvaCadastroBean implements Serializable {

    private final ProvaService provaService = new ProvaService();

    private Prova prova;
    private List<Prova> provas;

    @PostConstruct
    public void init() {
        novo();
        carregarLista();
    }

    public void salvar() {
        try {
            provaService.salvar(prova);
            FacesUtil.addInfoMessage("Prova salva com sucesso.");
            carregarLista();
            novo();
        } catch (IllegalArgumentException e) {
            FacesUtil.addErrorMessage(e.getMessage());
        }
    }

    public void editar(Prova prova) {
        this.prova = prova;
    }

    public void excluir(Long id) {
        try {
            provaService.excluir(id);
            carregarLista();
            if (prova != null && id != null && id.equals(prova.getId())) {
                novo();
            }
            FacesUtil.addInfoMessage("Prova excluida.");
        } catch (IllegalArgumentException e) {
            FacesUtil.addErrorMessage(e.getMessage());
        }
    }

    public void novo() {
        prova = new Prova();
        prova.setStatus(StatusProva.RASCUNHO);
        prova.setNotaMinima(BigDecimal.valueOf(7));
        prova.setTempoMinutos(30);
    }

    public StatusProva[] getStatusDisponiveis() {
        return StatusProva.values();
    }

    private void carregarLista() {
        provas = provaService.listarTodas();
    }

    public Prova getProva() {
        return prova;
    }

    public void setProva(Prova prova) {
        this.prova = prova;
    }

    public List<Prova> getProvas() {
        return provas;
    }
}


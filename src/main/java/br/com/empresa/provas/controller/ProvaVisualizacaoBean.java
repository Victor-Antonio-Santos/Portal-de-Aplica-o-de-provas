package br.com.empresa.provas.controller;

import br.com.empresa.provas.entity.Prova;
import br.com.empresa.provas.entity.Questao;
import br.com.empresa.provas.service.ProvaService;
import br.com.empresa.provas.util.FacesUtil;

import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;

@Named("provaVisualizacaoBean")
@ViewScoped
public class ProvaVisualizacaoBean implements Serializable {

    private final ProvaService provaService = new ProvaService();

    private Long id;
    private Prova prova;

    public void carregar() {
        if (id == null) {
            String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
            if (idParam != null && !idParam.trim().isEmpty()) {
                id = Long.valueOf(idParam);
            }
        }

        if (id == null) {
            FacesUtil.addErrorMessage("Selecione uma prova para visualizar.");
            return;
        }

        prova = provaService.buscarPorIdComEstrutura(id);
        if (prova == null) {
            FacesUtil.addErrorMessage("Prova nao encontrada.");
        }
    }

    public String getTipoFormatado(Questao questao) {
        return questao.getTipo().name().replace('_', ' ');
    }

    public String tipoFormatado(Questao questao) {
        return getTipoFormatado(questao);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Prova getProva() {
        return prova;
    }
}


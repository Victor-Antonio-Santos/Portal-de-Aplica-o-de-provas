package br.com.empresa.provas.controller;

import br.com.empresa.provas.entity.Turma;
import br.com.empresa.provas.entity.enums.StatusTurma;
import br.com.empresa.provas.service.TurmaService;
import br.com.empresa.provas.util.FacesUtil;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.List;

@Named("turmaCadastroBean")
@ViewScoped
public class TurmaCadastroBean implements Serializable {

    private final TurmaService turmaService = new TurmaService();

    @Inject
    private SessionBean sessionBean;

    private Turma turma;
    private List<Turma> turmas;

    @PostConstruct
    public void init() {
        novo();
        carregarLista();
    }

    public void salvar() {
        try {
            Turma salva = turmaService.salvar(turma, sessionBean == null ? null : sessionBean.getUsuarioLogado());
            FacesUtil.addInfoMessage("Turma salva com sucesso.");
            turma = salva;
            carregarLista();
            novo();
        } catch (IllegalArgumentException e) {
            FacesUtil.addErrorMessage(e.getMessage());
        }
    }

    public void editar(Turma turmaSelecionada) {
        this.turma = turmaSelecionada;
    }

    public void novo() {
        turma = new Turma();
        turma.setStatus(StatusTurma.ATIVA);
    }

    public StatusTurma[] getStatusDisponiveis() {
        return StatusTurma.values();
    }

    public Turma getTurma() {
        return turma;
    }

    public List<Turma> getTurmas() {
        return turmas;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    private void carregarLista() {
        turmas = turmaService.listarPorResponsavel(sessionBean == null ? null : sessionBean.getUsuarioLogado());
    }
}


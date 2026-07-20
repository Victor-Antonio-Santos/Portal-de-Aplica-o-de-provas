package br.com.empresa.provas.controller;

import br.com.empresa.provas.entity.ProvaColaborador;
import br.com.empresa.provas.entity.Turma;
import br.com.empresa.provas.entity.enums.StatusProvaColaborador;
import br.com.empresa.provas.service.ProvaColaboradorService;
import br.com.empresa.provas.service.RelatorioService;
import br.com.empresa.provas.service.TurmaService;
import br.com.empresa.provas.util.FacesUtil;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.List;

@Named("relatorioBean")
@ViewScoped
public class RelatorioBean implements Serializable {

    private final RelatorioService relatorioService = new RelatorioService();
    private final ProvaColaboradorService provaColaboradorService = new ProvaColaboradorService();
    private final TurmaService turmaService = new TurmaService();

    @Inject
    private SessionBean sessionBean;

    private String filtroTexto;
    private String status;
    private Long turmaId;
    private List<ProvaColaborador> registros;
    private List<Turma> turmas;

    @PostConstruct
    public void init() {
        turmas = turmaService.listarPorResponsavel(sessionBean == null ? null : sessionBean.getUsuarioLogado());
        pesquisar();
    }

    public void pesquisar() {
        registros = relatorioService.filtrar(filtroTexto, status, turmaId, sessionBean == null ? null : sessionBean.getUsuarioLogado());
    }

    public StatusProvaColaborador[] getStatusDisponiveis() {
        return StatusProvaColaborador.values();
    }

    public void excluirEnvio(Long id) {
        try {
            provaColaboradorService.excluirEnvio(id);
            pesquisar();
            FacesUtil.addInfoMessage("Envio excluido com sucesso.");
        } catch (IllegalArgumentException e) {
            FacesUtil.addErrorMessage(e.getMessage());
        }
    }

    public String getFiltroTexto() {
        return filtroTexto;
    }

    public void setFiltroTexto(String filtroTexto) {
        this.filtroTexto = filtroTexto;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ProvaColaborador> getRegistros() {
        return registros;
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


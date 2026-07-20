package br.com.empresa.provas.controller;

import br.com.empresa.provas.entity.ProvaColaborador;
import br.com.empresa.provas.entity.Turma;
import br.com.empresa.provas.service.DashboardService;
import br.com.empresa.provas.service.ProvaColaboradorService;
import br.com.empresa.provas.service.TurmaService;
import br.com.empresa.provas.service.dto.DashboardResumo;
import br.com.empresa.provas.util.FacesUtil;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("adminDashboardBean")
@ViewScoped
public class AdminDashboardBean implements Serializable {

    private final DashboardService dashboardService = new DashboardService();
    private final ProvaColaboradorService provaColaboradorService = new ProvaColaboradorService();
    private final TurmaService turmaService = new TurmaService();

    @Inject
    private SessionBean sessionBean;

    private DashboardResumo resumo;
    private List<ProvaColaborador> enviosRecentes;
    private List<Turma> turmas;
    private Long turmaId;
    private String filtroIndicador;

    @PostConstruct
    public void init() {
        turmas = turmaService.listarPorResponsavel(sessionBean == null ? null : sessionBean.getUsuarioLogado());
        carregarDados();
    }

    public DashboardResumo getResumo() {
        return resumo;
    }

    public List<ProvaColaborador> getEnviosRecentes() {
        return enviosRecentes;
    }

    public List<Turma> getTurmas() {
        return turmas;
    }

    public Long getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(Long turmaId) {
        this.turmaId = turmaId;
    }

    public void filtrar() {
        carregarDados();
    }

    public void filtrarIndicador(String filtro) {
        this.filtroIndicador = filtro;
        carregarDados();
    }

    public void limparFiltroIndicador() {
        this.filtroIndicador = null;
        carregarDados();
    }

    public boolean isFiltroAtivo(String filtro) {
        return filtro != null && filtro.equals(filtroIndicador);
    }

    public boolean filtroAtivo(String filtro) {
        return isFiltroAtivo(filtro);
    }

    public boolean isExisteFiltroIndicador() {
        return filtroIndicador != null && !filtroIndicador.trim().isEmpty();
    }

    public boolean existeFiltroIndicador() {
        return isExisteFiltroIndicador();
    }

    public String getDescricaoFiltro() {
        if ("PENDENTE".equals(filtroIndicador)) {
            return "Colaboradores com prova pendente";
        }
        if ("FINALIZADA".equals(filtroIndicador)) {
            return "Colaboradores com prova finalizada";
        }
        if ("APROVADOS".equals(filtroIndicador)) {
            return "Colaboradores aprovados";
        }
        if ("REPROVADOS".equals(filtroIndicador)) {
            return "Colaboradores reprovados";
        }
        return "Visao geral";
    }

    public void excluirEnvio(Long id) {
        try {
            provaColaboradorService.excluirEnvio(id);
            carregarDados();
            FacesUtil.addInfoMessage("Envio excluido com sucesso.");
        } catch (IllegalArgumentException e) {
            FacesUtil.addErrorMessage(e.getMessage());
        }
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    private void carregarDados() {
        resumo = dashboardService.carregarResumo(sessionBean == null ? null : sessionBean.getUsuarioLogado(), turmaId);
        List<ProvaColaborador> enviosBase = provaColaboradorService.listarTodosPorResponsavel(sessionBean == null ? null : sessionBean.getUsuarioLogado());
        enviosRecentes = new ArrayList<ProvaColaborador>();
        for (ProvaColaborador envio : enviosBase) {
            if ((turmaId == null || (envio.getColaborador() != null && envio.getColaborador().getTurma() != null
                    && turmaId.equals(envio.getColaborador().getTurma().getId())))
                    && correspondeAoFiltroIndicador(envio)) {
                enviosRecentes.add(envio);
            }
        }
    }

    private boolean correspondeAoFiltroIndicador(ProvaColaborador envio) {
        if (filtroIndicador == null || filtroIndicador.trim().isEmpty()) {
            return true;
        }
        if ("PENDENTE".equals(filtroIndicador)) {
            return envio.getStatus() != null && "PENDENTE".equals(envio.getStatus().name());
        }
        if ("FINALIZADA".equals(filtroIndicador)) {
            return envio.getStatus() != null && "FINALIZADA".equals(envio.getStatus().name());
        }
        if ("APROVADOS".equals(filtroIndicador)) {
            return envio.isFinalizada() && envio.getNota() != null
                    && envio.getProva() != null && envio.getProva().getNotaMinima() != null
                    && envio.getNota().compareTo(envio.getProva().getNotaMinima()) >= 0;
        }
        if ("REPROVADOS".equals(filtroIndicador)) {
            return envio.isFinalizada() && envio.getNota() != null
                    && envio.getProva() != null && envio.getProva().getNotaMinima() != null
                    && envio.getNota().compareTo(envio.getProva().getNotaMinima()) < 0;
        }
        return true;
    }
}


package br.com.empresa.provas.controller;

import br.com.empresa.provas.entity.Prova;
import br.com.empresa.provas.entity.Turma;
import br.com.empresa.provas.entity.Usuario;
import br.com.empresa.provas.entity.enums.StatusProva;
import br.com.empresa.provas.service.ProvaColaboradorService;
import br.com.empresa.provas.service.ProvaService;
import br.com.empresa.provas.service.TurmaService;
import br.com.empresa.provas.service.UsuarioService;
import br.com.empresa.provas.util.FacesUtil;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Named("envioProvaBean")
@ViewScoped
public class EnvioProvaBean implements Serializable {

    private final ProvaService provaService = new ProvaService();
    private final UsuarioService usuarioService = new UsuarioService();
    private final ProvaColaboradorService provaColaboradorService = new ProvaColaboradorService();
    private final TurmaService turmaService = new TurmaService();

    @Inject
    private SessionBean sessionBean;

    private Long provaId;
    private List<Long> colaboradoresSelecionados;
    private Date dataLimite;
    private Integer tentativasPermitidas = 1;
    private boolean disponivelImediatamente = true;
    private boolean selecionarTodos;
    private boolean enviarParaTodaTurma;
    private Long turmaId;

    private List<Prova> provas;
    private List<Usuario> colaboradores;
    private List<Turma> turmas;

    @PostConstruct
    public void init() {
        provas = carregarProvasDisponiveis();
        turmas = turmaService.listarPorResponsavel(sessionBean == null ? null : sessionBean.getUsuarioLogado());
        colaboradores = carregarColaboradoresAtivos();
        colaboradoresSelecionados = new ArrayList<Long>();
    }

    public void enviar() {
        try {
            validarFormulario();
            if (enviarParaTodaTurma) {
                selecionarTodosColaboradores();
            }
            String tituloProva = localizarTituloProva();
            int quantidadeColaboradores = colaboradoresSelecionados == null ? 0 : colaboradoresSelecionados.size();
            provaColaboradorService.enviarProva(provaId, colaboradoresSelecionados, dataLimite,
                    tentativasPermitidas, disponivelImediatamente, sessionBean == null ? null : sessionBean.getUsuarioLogado());
            FacesUtil.addInfoMessage("Prova enviada com sucesso para " + quantidadeColaboradores
                    + " colaborador(es): " + tituloProva + ".");
            colaboradoresSelecionados = new ArrayList<Long>();
            dataLimite = null;
            tentativasPermitidas = 1;
            selecionarTodos = false;
            enviarParaTodaTurma = false;
        } catch (IllegalArgumentException e) {
            FacesUtil.addErrorMessage(e.getMessage());
        }
    }

    private void validarFormulario() {
        if (provaId == null) {
            throw new IllegalArgumentException("Selecione uma prova para enviar.");
        }
        if (dataLimite == null) {
            throw new IllegalArgumentException("Informe a data limite da prova.");
        }
        if (tentativasPermitidas == null || tentativasPermitidas.intValue() <= 0) {
            throw new IllegalArgumentException("Informe a quantidade de tentativas permitidas.");
        }
        if (enviarParaTodaTurma && turmaId == null) {
            throw new IllegalArgumentException("Selecione uma turma para enviar a prova para todos os colaboradores.");
        }
        if ((colaboradoresSelecionados == null || colaboradoresSelecionados.isEmpty()) && !enviarParaTodaTurma) {
            throw new IllegalArgumentException("Selecione ao menos um colaborador ou utilize o envio por turma.");
        }
    }

    public void alternarSelecaoTodos() {
        if (selecionarTodos) {
            selecionarTodosColaboradores();
            return;
        }
        limparSelecaoColaboradores();
    }

    public void selecionarTodosColaboradores() {
        colaboradoresSelecionados = new ArrayList<Long>();
        if (colaboradores != null) {
            for (Usuario colaborador : colaboradores) {
                colaboradoresSelecionados.add(colaborador.getId());
            }
        }
        selecionarTodos = colaboradores != null && !colaboradores.isEmpty();
    }

    public void limparSelecaoColaboradores() {
        colaboradoresSelecionados = new ArrayList<Long>();
        selecionarTodos = false;
    }

    public void filtrarPorTurma() {
        colaboradores = carregarColaboradoresAtivos();
        limparSelecaoColaboradores();
        if (enviarParaTodaTurma && (colaboradores == null || colaboradores.isEmpty())) {
            enviarParaTodaTurma = false;
        }
    }

    private String localizarTituloProva() {
        if (provaId == null || provas == null) {
            return "Prova selecionada";
        }
        for (Prova prova : provas) {
            if (provaId.equals(prova.getId())) {
                return prova.getTitulo();
            }
        }
        return "Prova selecionada";
    }

    public Long getProvaId() {
        return provaId;
    }

    public void setProvaId(Long provaId) {
        this.provaId = provaId;
    }

    public List<Long> getColaboradoresSelecionados() {
        return colaboradoresSelecionados;
    }

    public void setColaboradoresSelecionados(List<Long> colaboradoresSelecionados) {
        this.colaboradoresSelecionados = colaboradoresSelecionados;
    }

    public Date getDataLimite() {
        return dataLimite;
    }

    public void setDataLimite(Date dataLimite) {
        this.dataLimite = dataLimite;
    }

    public Integer getTentativasPermitidas() {
        return tentativasPermitidas;
    }

    public void setTentativasPermitidas(Integer tentativasPermitidas) {
        this.tentativasPermitidas = tentativasPermitidas;
    }

    public boolean isDisponivelImediatamente() {
        return disponivelImediatamente;
    }

    public void setDisponivelImediatamente(boolean disponivelImediatamente) {
        this.disponivelImediatamente = disponivelImediatamente;
    }

    public List<Prova> getProvas() {
        return provas;
    }

    public List<Usuario> getColaboradores() {
        return colaboradores;
    }

    public List<Turma> getTurmas() {
        return turmas;
    }

    public boolean isSelecionarTodos() {
        return selecionarTodos;
    }

    public void setSelecionarTodos(boolean selecionarTodos) {
        this.selecionarTodos = selecionarTodos;
    }

    public boolean isEnviarParaTodaTurma() {
        return enviarParaTodaTurma;
    }

    public void setEnviarParaTodaTurma(boolean enviarParaTodaTurma) {
        this.enviarParaTodaTurma = enviarParaTodaTurma;
    }

    public Long getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(Long turmaId) {
        this.turmaId = turmaId;
    }

    private List<Prova> carregarProvasDisponiveis() {
        List<Prova> provasDisponiveis = new ArrayList<Prova>();
        for (Prova prova : provaService.listarTodas()) {
            if (!StatusProva.ENCERRADA.equals(prova.getStatus())) {
                provasDisponiveis.add(prova);
            }
        }
        return provasDisponiveis;
    }

    private List<Usuario> carregarColaboradoresAtivos() {
        List<Usuario> colaboradoresAtivos = new ArrayList<Usuario>();
        for (Usuario colaborador : usuarioService.listarColaboradoresPorResponsavel(sessionBean == null ? null : sessionBean.getUsuarioLogado())) {
            if (colaborador.isAtivo() && (turmaId == null
                    || (colaborador.getTurma() != null && turmaId.equals(colaborador.getTurma().getId())))) {
                colaboradoresAtivos.add(colaborador);
            }
        }
        return colaboradoresAtivos;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }
}


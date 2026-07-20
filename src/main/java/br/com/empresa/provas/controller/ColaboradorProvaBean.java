package br.com.empresa.provas.controller;

import br.com.empresa.provas.entity.ProvaColaborador;
import br.com.empresa.provas.entity.Alternativa;
import br.com.empresa.provas.entity.Questao;
import br.com.empresa.provas.entity.enums.StatusProvaColaborador;
import br.com.empresa.provas.entity.enums.TipoQuestao;
import br.com.empresa.provas.service.ProvaColaboradorService;
import br.com.empresa.provas.util.FacesUtil;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

@Named("colaboradorProvaBean")
@SessionScoped
public class ColaboradorProvaBean implements Serializable {

    private final ProvaColaboradorService provaColaboradorService = new ProvaColaboradorService();

    @Inject
    private SessionBean sessionBean;

    private List<ProvaColaborador> minhasProvas;
    private ProvaColaborador provaAtual;
    private Map<Long, Object> respostasUnicas = new HashMap<Long, Object>();
    private Map<Long, Object> respostasMultiplas = new HashMap<Long, Object>();

    @PostConstruct
    public void init() {
        carregarMinhasProvas();
    }

    public void carregarMinhasProvas() {
        if (sessionBean != null && sessionBean.getUsuarioLogado() != null) {
            minhasProvas = provaColaboradorService.listarPorColaborador(sessionBean.getUsuarioLogado());
        }
    }

    public void carregarRealizacao() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        if (idParam != null) {
            provaAtual = provaColaboradorService.buscarPorId(Long.valueOf(idParam));
            if (provaAtual != null) {
                provaColaboradorService.expirarSeNecessario(provaAtual);
                if (provaAtual.getDataLimite() != null && provaAtual.getDataLimite().before(new Date()) && !provaAtual.isFinalizada()) {
                    provaAtual.setStatus(StatusProvaColaborador.EXPIRADA);
                }
                if (isRealizacaoBloqueada()) {
                    FacesUtil.addErrorMessage("Esta prova esta expirada e nao pode mais ser realizada.");
                }
                carregarMapasResposta(provaColaboradorService.mapearRespostas(provaAtual));
            }
        }
    }

    public void iniciar() {
        try {
            provaAtual = provaColaboradorService.iniciarProva(provaAtual.getId());
            FacesUtil.addInfoMessage("Prova iniciada.");
        } catch (IllegalArgumentException e) {
            FacesUtil.addErrorMessage(e.getMessage());
        }
    }

    public void salvarProgresso() {
        try {
            provaAtual = provaColaboradorService.salvarRespostas(provaAtual.getId(), montarMapaRespostas());
            FacesUtil.addInfoMessage("Respostas salvas.");
        } catch (IllegalArgumentException e) {
            FacesUtil.addErrorMessage(e.getMessage());
        }
    }

    public void finalizar() {
        try {
            provaAtual = provaColaboradorService.finalizarProva(provaAtual.getId(), montarMapaRespostas());
            carregarMinhasProvas();
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesUtil.addInfoMessage("Prova concluida. Sua nota foi " + provaAtual.getNota() + ".");
            redirecionarResultado(provaAtual.getId());
        } catch (IllegalArgumentException e) {
            FacesUtil.addErrorMessage(e.getMessage());
        }
    }

    public void verificarTempo() {
        if (provaAtual == null || provaAtual.getDataInicio() == null || provaAtual.isFinalizada() || isRealizacaoBloqueada()) {
            return;
        }
        long minutosDecorridos = (System.currentTimeMillis() - provaAtual.getDataInicio().getTime()) / 60000L;
        if (minutosDecorridos >= provaAtual.getProva().getTempoMinutos()) {
            finalizar();
        }
    }

    public boolean isAprovado() {
        return provaAtual != null && provaAtual.getNota().compareTo(provaAtual.getProva().getNotaMinima()) >= 0;
    }

    public int getTempoRestanteMinutos() {
        if (provaAtual == null || provaAtual.getDataInicio() == null) {
            return provaAtual == null ? 0 : provaAtual.getProva().getTempoMinutos();
        }
        long minutosDecorridos = (System.currentTimeMillis() - provaAtual.getDataInicio().getTime()) / 60000L;
        int restante = provaAtual.getProva().getTempoMinutos() - (int) minutosDecorridos;
        return Math.max(restante, 0);
    }

    public String getTituloStatus(ProvaColaborador provaColaborador) {
        return provaColaborador.getStatus().name().replace('_', ' ');
    }

    public boolean isRealizacaoBloqueada() {
        return provaAtual != null
                && !provaAtual.isFinalizada()
                && (StatusProvaColaborador.EXPIRADA.equals(provaAtual.getStatus())
                || (provaAtual.getDataLimite() != null && provaAtual.getDataLimite().before(new Date())))
                && !provaAtual.isFinalizada();
    }

    public List<ProvaColaborador> getMinhasProvas() {
        return minhasProvas;
    }

    public ProvaColaborador getProvaAtual() {
        return provaAtual;
    }

    public Map<Long, Object> getRespostasUnicas() {
        return respostasUnicas;
    }

    public Map<Long, Object> getRespostasMultiplas() {
        return respostasMultiplas;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public void redirecionarResultado(Long id) {
        try {
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
                            + "/pages/colaborador/resultado.xhtml?id=" + id);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao redirecionar para resultado.", e);
        }
    }

    public void carregarResultado() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        if (idParam != null) {
            provaAtual = provaColaboradorService.buscarPorId(Long.valueOf(idParam));
            if (provaAtual != null) {
                carregarMapasResposta(provaColaboradorService.mapearRespostas(provaAtual));
            }
        }
    }

    public boolean isQuestaoMultipla(Questao questao) {
        return TipoQuestao.MULTIPLA_ESCOLHA.equals(questao.getTipo());
    }

    public boolean questaoMultipla(Questao questao) {
        return isQuestaoMultipla(questao);
    }

    public boolean isAlternativaSelecionada(Questao questao, Alternativa alternativa) {
        if (isQuestaoMultipla(questao)) {
            List<Long> ids = converterParaListaLong(respostasMultiplas.get(questao.getId()));
            return ids != null && ids.contains(alternativa.getId());
        }
        Long selecionada = converterParaLong(respostasUnicas.get(questao.getId()));
        return selecionada != null && selecionada.equals(alternativa.getId());
    }

    public boolean alternativaSelecionada(Questao questao, Alternativa alternativa) {
        return isAlternativaSelecionada(questao, alternativa);
    }

    public boolean isQuestaoCorreta(Questao questao) {
        if (questao == null || questao.getAlternativas() == null) {
            return false;
        }

        List<Long> respostasMarcadas = isQuestaoMultipla(questao)
                ? converterParaListaLong(respostasMultiplas.get(questao.getId()))
                : converterParaListaLong(respostasUnicas.get(questao.getId()));

        List<Long> respostasCorretas = new ArrayList<Long>();
        for (Alternativa alternativa : questao.getAlternativas()) {
            if (alternativa.isCorreta()) {
                respostasCorretas.add(alternativa.getId());
            }
        }

        Collections.sort(respostasMarcadas);
        Collections.sort(respostasCorretas);
        return respostasMarcadas.equals(respostasCorretas);
    }

    public boolean questaoCorreta(Questao questao) {
        return isQuestaoCorreta(questao);
    }

    public boolean isAlternativaCorreta(Alternativa alternativa) {
        return alternativa != null && alternativa.isCorreta();
    }

    public boolean alternativaCorreta(Alternativa alternativa) {
        return isAlternativaCorreta(alternativa);
    }

    public boolean isAlternativaMarcadaIncorreta(Questao questao, Alternativa alternativa) {
        return isAlternativaSelecionada(questao, alternativa) && !isAlternativaCorreta(alternativa);
    }

    public boolean alternativaMarcadaIncorreta(Questao questao, Alternativa alternativa) {
        return isAlternativaMarcadaIncorreta(questao, alternativa);
    }

    private void carregarMapasResposta(Map<Long, List<Long>> respostas) {
        respostasUnicas = new HashMap<Long, Object>();
        respostasMultiplas = new HashMap<Long, Object>();
        if (provaAtual == null || respostas == null) {
            return;
        }
        for (Questao questao : provaAtual.getProva().getQuestoes()) {
            List<Long> ids = respostas.get(questao.getId());
            if (ids == null || ids.isEmpty()) {
                continue;
            }
            if (TipoQuestao.MULTIPLA_ESCOLHA.equals(questao.getTipo())) {
                respostasMultiplas.put(questao.getId(), new ArrayList<Long>(ids));
            } else {
                respostasUnicas.put(questao.getId(), ids.get(0));
            }
        }
    }

    private Map<Long, List<Long>> montarMapaRespostas() {
        Map<Long, List<Long>> respostas = new HashMap<Long, List<Long>>();
        for (Map.Entry<Long, Object> entry : respostasUnicas.entrySet()) {
            Long valor = converterParaLong(entry.getValue());
            if (valor != null) {
                List<Long> ids = new ArrayList<Long>();
                ids.add(valor);
                respostas.put(entry.getKey(), ids);
            }
        }
        for (Map.Entry<Long, Object> entry : respostasMultiplas.entrySet()) {
            List<Long> ids = converterParaListaLong(entry.getValue());
            if (ids != null && !ids.isEmpty()) {
                respostas.put(entry.getKey(), ids);
            }
        }
        return respostas;
    }

    private Long converterParaLong(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Long) {
            return (Long) valor;
        }
        if (valor instanceof Number) {
            return ((Number) valor).longValue();
        }
        if (valor instanceof String) {
            String texto = ((String) valor).trim();
            return texto.isEmpty() ? null : Long.valueOf(texto);
        }
        return null;
    }

    private List<Long> converterParaListaLong(Object valor) {
        List<Long> ids = new ArrayList<Long>();
        if (valor == null) {
            return ids;
        }
        if (valor instanceof List<?>) {
            for (Object item : (List<?>) valor) {
                Long id = converterParaLong(item);
                if (id != null) {
                    ids.add(id);
                }
            }
            return ids;
        }
        if (valor instanceof Object[]) {
            for (Object item : (Object[]) valor) {
                Long id = converterParaLong(item);
                if (id != null) {
                    ids.add(id);
                }
            }
            return ids;
        }
        Long id = converterParaLong(valor);
        if (id != null) {
            ids.add(id);
        }
        return ids;
    }
}


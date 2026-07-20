package br.com.empresa.provas.controller;

import br.com.empresa.provas.entity.Alternativa;
import br.com.empresa.provas.entity.Prova;
import br.com.empresa.provas.entity.Questao;
import br.com.empresa.provas.entity.enums.TipoQuestao;
import br.com.empresa.provas.service.ProvaService;
import br.com.empresa.provas.util.FacesUtil;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Named("questaoCadastroBean")
@ViewScoped
public class QuestaoCadastroBean implements Serializable {

    private final ProvaService provaService = new ProvaService();

    private Long provaId;
    private Questao questao;
    private List<AlternativaItem> alternativas;
    private List<Prova> provas;
    private List<Questao> questoesDaProvaSelecionada;

    @PostConstruct
    public void init() {
        provas = provaService.listarTodas();
        carregarProvaInformadaNaUrl();
        novo();
        carregarQuestoesDaProvaSelecionada();
    }

    public void salvar() {
        try {
            validarFormulario();
            List<Alternativa> alternativasEntidade = new ArrayList<Alternativa>();
            for (AlternativaItem item : alternativas) {
                if (item.getTexto() != null && !item.getTexto().trim().isEmpty()) {
                    Alternativa alternativa = new Alternativa();
                    alternativa.setId(item.getId());
                    alternativa.setTexto(item.getTexto());
                    alternativa.setCorreta(item.isCorreta());
                    alternativasEntidade.add(alternativa);
                }
            }
            boolean editando = isEditando();
            provaService.salvarQuestao(provaId, questao, alternativasEntidade);
            FacesUtil.addInfoMessage(editando ? "Questao atualizada com sucesso." : "Questao salva com sucesso.");
            provas = provaService.listarTodas();
            carregarQuestoesDaProvaSelecionada();
            novo();
        } catch (IllegalArgumentException e) {
            FacesUtil.addErrorMessage(e.getMessage());
        } catch (RuntimeException e) {
            FacesUtil.addErrorMessage("Nao foi possivel salvar a questao.");
        }
    }

    public String salvarQuestao() {
        salvar();
        return null;
    }

    public void adicionarAlternativa() {
        alternativas.add(criarAlternativaItem("", false));
    }

    public void removerAlternativaEmEdicao(AlternativaItem item) {
        if (alternativas == null || alternativas.size() <= 2) {
            FacesUtil.addErrorMessage("A questao deve manter ao menos duas alternativas.");
            return;
        }
        alternativas.remove(item);
        garantirAlternativaCorreta();
    }

    public void novo() {
        questao = new Questao();
        questao.setTipo(TipoQuestao.ESCOLHA_UNICA);
        questao.setPeso(BigDecimal.ONE);
        questao.setAtivo(true);
        atualizarProximaOrdem();
        prepararAlternativasPadrao();
    }

    public void editarQuestao(Questao questaoSalva) {
        Questao novaQuestao = new Questao();
        novaQuestao.setId(questaoSalva.getId());
        novaQuestao.setEnunciado(questaoSalva.getEnunciado());
        novaQuestao.setTipo(questaoSalva.getTipo());
        novaQuestao.setPeso(questaoSalva.getPeso());
        novaQuestao.setOrdemExibicao(questaoSalva.getOrdemExibicao());
        novaQuestao.setAtivo(questaoSalva.isAtivo());
        questao = novaQuestao;

        alternativas = new ArrayList<AlternativaItem>();
        for (Alternativa alternativa : questaoSalva.getAlternativas()) {
            alternativas.add(new AlternativaItem(alternativa.getId(), alternativa.getTexto(), alternativa.isCorreta()));
        }
        garantirAlternativaCorreta();
    }

    public void excluirQuestao(Long questaoId) {
        try {
            provaService.excluirQuestao(provaId, questaoId);
            if (questao != null && questaoId != null && questaoId.equals(questao.getId())) {
                novo();
            }
            carregarQuestoesDaProvaSelecionada();
            FacesUtil.addInfoMessage("Questao excluida com sucesso.");
        } catch (IllegalArgumentException e) {
            FacesUtil.addErrorMessage(e.getMessage());
        }
    }

    public void cancelarEdicao() {
        novo();
    }

    public void aplicarModeloQuestao() {
        prepararAlternativasPadrao();
        garantirAlternativaCorreta();
    }

    public void atualizarMarcacaoCorreta(AlternativaItem item) {
        if (item == null || TipoQuestao.MULTIPLA_ESCOLHA.equals(questao.getTipo())) {
            return;
        }

        if (item.isCorreta()) {
            for (AlternativaItem alternativa : alternativas) {
                if (alternativa != item) {
                    alternativa.setCorreta(false);
                }
            }
            return;
        }

        garantirAlternativaCorreta();
    }

    public boolean isSelecaoMultipla() {
        return TipoQuestao.MULTIPLA_ESCOLHA.equals(questao.getTipo());
    }

    public boolean isEditando() {
        return questao != null && questao.getId() != null;
    }

    public String getDescricaoModelo() {
        if (TipoQuestao.MULTIPLA_ESCOLHA.equals(questao.getTipo())) {
            return "Modelo com multiplas respostas corretas. O colaborador podera marcar mais de uma alternativa.";
        }
        if (TipoQuestao.VERDADEIRO_FALSO.equals(questao.getTipo())) {
            return "Modelo com duas alternativas fixas: Verdadeiro e Falso.";
        }
        return "Modelo de escolha unica. Apenas uma alternativa podera ser marcada como correta.";
    }

    public String getTituloFormulario() {
        return isEditando() ? "Editar questao" : "Nova questao";
    }

    public List<Questao> getQuestoesDaProvaSelecionada() {
        if (questoesDaProvaSelecionada == null) {
            carregarQuestoesDaProvaSelecionada();
        }
        return questoesDaProvaSelecionada;
    }

    private void carregarQuestoesDaProvaSelecionada() {
        if (provaId == null) {
            questoesDaProvaSelecionada = Collections.emptyList();
            return;
        }
        Prova provaSelecionada = provaService.buscarPorIdComEstrutura(provaId);
        if (provaSelecionada == null || provaSelecionada.getQuestoes() == null) {
            questoesDaProvaSelecionada = Collections.emptyList();
            return;
        }
        List<Questao> questoes = new ArrayList<Questao>(provaSelecionada.getQuestoes());
        Collections.sort(questoes, new Comparator<Questao>() {
            @Override
            public int compare(Questao q1, Questao q2) {
                Integer ordem1 = q1.getOrdemExibicao() == null ? 0 : q1.getOrdemExibicao();
                Integer ordem2 = q2.getOrdemExibicao() == null ? 0 : q2.getOrdemExibicao();
                return ordem1.compareTo(ordem2);
            }
        });
        questoesDaProvaSelecionada = questoes;
    }

    private void carregarProvaInformadaNaUrl() {
        String provaIdParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("provaId");
        if (provaIdParam != null && !provaIdParam.trim().isEmpty()) {
            provaId = Long.valueOf(provaIdParam);
        }
    }

    private void validarFormulario() {
        if (provaId == null) {
            throw new IllegalArgumentException("Selecione a prova antes de salvar a questao.");
        }
        if (questao == null) {
            throw new IllegalArgumentException("Questao nao informada.");
        }
        if (questao.getPeso() == null) {
            throw new IllegalArgumentException("Informe o peso da questao.");
        }
    }

    private void prepararAlternativasPadrao() {
        alternativas = new ArrayList<AlternativaItem>();
        if (TipoQuestao.VERDADEIRO_FALSO.equals(questao.getTipo())) {
            AlternativaItem verdadeiro = criarAlternativaItem("Verdadeiro", true);
            AlternativaItem falso = criarAlternativaItem("Falso", false);
            alternativas.add(verdadeiro);
            alternativas.add(falso);
            return;
        }

        alternativas.add(criarAlternativaItem("", true));
        alternativas.add(criarAlternativaItem("", false));
        alternativas.add(criarAlternativaItem("", false));
        alternativas.add(criarAlternativaItem("", false));
        garantirAlternativaCorreta();
    }

    private AlternativaItem criarAlternativaItem(String texto, boolean correta) {
        return new AlternativaItem(System.nanoTime() + alternativasSizeSeed(), texto, correta);
    }

    private long alternativasSizeSeed() {
        return alternativas == null ? 0L : alternativas.size();
    }

    public TipoQuestao[] getTiposQuestao() {
        return TipoQuestao.values();
    }

    public Long getProvaId() {
        return provaId;
    }

    public void setProvaId(Long provaId) {
        this.provaId = provaId;
        atualizarProximaOrdem();
        carregarQuestoesDaProvaSelecionada();
    }

    public Questao getQuestao() {
        return questao;
    }

    public void setQuestao(Questao questao) {
        this.questao = questao;
    }

    public List<AlternativaItem> getAlternativas() {
        return alternativas;
    }

    public List<Prova> getProvas() {
        return provas;
    }

    public int getProximaOrdem() {
        return questao == null || questao.getOrdemExibicao() == null ? 1 : questao.getOrdemExibicao();
    }

    private void atualizarProximaOrdem() {
        if (questao == null) {
            return;
        }
        questao.setOrdemExibicao(provaService.calcularProximaOrdemQuestao(provaId));
    }

    public String getResumoAlternativas(Questao questaoSalva) {
        int total = questaoSalva.getAlternativas() == null ? 0 : questaoSalva.getAlternativas().size();
        int corretas = 0;
        if (questaoSalva.getAlternativas() != null) {
            for (Alternativa alternativa : questaoSalva.getAlternativas()) {
                if (alternativa.isCorreta()) {
                    corretas++;
                }
            }
        }
        return total + " alternativa(s) / " + corretas + " correta(s)";
    }

    public String resumoAlternativas(Questao questaoSalva) {
        return getResumoAlternativas(questaoSalva);
    }

    private void garantirAlternativaCorreta() {
        if (alternativas == null || alternativas.isEmpty()) {
            return;
        }
        if (TipoQuestao.MULTIPLA_ESCOLHA.equals(questao.getTipo())) {
            return;
        }
        boolean encontrouCorreta = false;
        for (AlternativaItem item : alternativas) {
            if (item.isCorreta()) {
                if (!encontrouCorreta) {
                    encontrouCorreta = true;
                } else {
                    item.setCorreta(false);
                }
            }
        }
        if (!encontrouCorreta) {
            alternativas.get(0).setCorreta(true);
        }
    }
}


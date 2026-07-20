package br.com.empresa.provas.service;

import br.com.empresa.provas.dao.LogOperacaoDAO;
import br.com.empresa.provas.dao.ProvaDAO;
import br.com.empresa.provas.entity.Alternativa;
import br.com.empresa.provas.entity.LogOperacao;
import br.com.empresa.provas.entity.Prova;
import br.com.empresa.provas.entity.Questao;
import br.com.empresa.provas.entity.enums.StatusProva;
import br.com.empresa.provas.entity.enums.TipoQuestao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProvaService {

    private final ProvaDAO provaDAO = new ProvaDAO();
    private final LogOperacaoDAO logOperacaoDAO = new LogOperacaoDAO();

    public Prova salvar(Prova prova) {
        validarProva(prova);
        Prova salva = provaDAO.salvar(prova);
        registrarLog("PROVA_SALVA", null, "Prova " + prova.getTitulo() + " salva.");
        return salva;
    }

    public Prova adicionarQuestao(Long provaId, Questao questao, List<Alternativa> alternativas) {
        return salvarQuestao(provaId, questao, alternativas);
    }

    public Prova salvarQuestao(Long provaId, Questao questao, List<Alternativa> alternativas) {
        Prova prova = provaDAO.buscarPorIdComEstrutura(provaId);
        if (prova == null) {
            throw new IllegalArgumentException("Prova nao encontrada.");
        }
        if (StatusProva.ENCERRADA.equals(prova.getStatus())) {
            throw new IllegalArgumentException("Provas encerradas nao podem ser editadas.");
        }
        validarQuestao(questao, alternativas);

        boolean novaQuestao = questao.getId() == null;
        Questao questaoPersistida = novaQuestao ? questao : localizarQuestao(prova, questao.getId());
        if (questaoPersistida == null) {
            throw new IllegalArgumentException("Questao nao encontrada.");
        }

        questaoPersistida.setProva(prova);
        questaoPersistida.setEnunciado(questao.getEnunciado());
        questaoPersistida.setTipo(questao.getTipo());
        questaoPersistida.setPeso(questao.getPeso());
        questaoPersistida.setAtivo(questao.isAtivo());
        if (novaQuestao) {
            questaoPersistida.setOrdemExibicao(calcularProximaOrdem(prova));
            prova.getQuestoes().add(questaoPersistida);
        }

        sincronizarAlternativas(questaoPersistida, alternativas);

        Prova salva = provaDAO.salvar(prova);
        registrarLog(novaQuestao ? "QUESTAO_SALVA" : "QUESTAO_ATUALIZADA", null,
                "Questao " + (novaQuestao ? "incluida" : "atualizada") + " na prova " + prova.getTitulo());
        return salva;
    }

    public void excluir(Long id) {
        Prova prova = provaDAO.buscarPorId(id);
        if (prova == null) {
            throw new IllegalArgumentException("Prova nao encontrada.");
        }
        if (prova != null && StatusProva.ENCERRADA.equals(prova.getStatus())) {
            throw new IllegalArgumentException("Provas encerradas nao podem ser excluidas.");
        }
        try {
            provaDAO.excluir(id);
            registrarLog("PROVA_EXCLUIDA", null, "Prova " + prova.getTitulo() + " excluida.");
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Nao foi possivel excluir a prova. Verifique se ela ja foi enviada a colaboradores.", e);
        }
    }

    public void excluirAlternativa(Long provaId, Long questaoId, Long alternativaId) {
        Prova prova = provaDAO.buscarPorIdComEstrutura(provaId);
        if (prova == null) {
            throw new IllegalArgumentException("Prova nao encontrada.");
        }

        Questao questaoEncontrada = null;
        Alternativa alternativaEncontrada = null;
        for (Questao questao : prova.getQuestoes()) {
            if (!questaoId.equals(questao.getId())) {
                continue;
            }
            questaoEncontrada = questao;
            for (Alternativa alternativa : questao.getAlternativas()) {
                if (alternativaId.equals(alternativa.getId())) {
                    alternativaEncontrada = alternativa;
                    break;
                }
            }
            break;
        }

        if (questaoEncontrada == null || alternativaEncontrada == null) {
            throw new IllegalArgumentException("Alternativa nao encontrada.");
        }

        List<Alternativa> alternativasRestantes = new ArrayList<Alternativa>(questaoEncontrada.getAlternativas());
        alternativasRestantes.remove(alternativaEncontrada);
        validarQuestao(questaoEncontrada, alternativasRestantes);
        questaoEncontrada.getAlternativas().remove(alternativaEncontrada);
        provaDAO.salvar(prova);
        registrarLog("ALTERNATIVA_EXCLUIDA", null,
                "Alternativa removida da questao " + questaoEncontrada.getEnunciado() + " na prova " + prova.getTitulo());
    }

    public void excluirQuestao(Long provaId, Long questaoId) {
        Prova prova = provaDAO.buscarPorIdComEstrutura(provaId);
        if (prova == null) {
            throw new IllegalArgumentException("Prova nao encontrada.");
        }
        if (StatusProva.ENCERRADA.equals(prova.getStatus())) {
            throw new IllegalArgumentException("Provas encerradas nao podem ser editadas.");
        }

        Questao questaoEncontrada = localizarQuestao(prova, questaoId);
        if (questaoEncontrada == null) {
            throw new IllegalArgumentException("Questao nao encontrada.");
        }

        prova.getQuestoes().remove(questaoEncontrada);
        reordenarQuestoes(prova);
        provaDAO.salvar(prova);
        registrarLog("QUESTAO_EXCLUIDA", null,
                "Questao removida da prova " + prova.getTitulo() + ": " + questaoEncontrada.getEnunciado());
    }

    public Prova buscarPorId(Long id) {
        return provaDAO.buscarPorId(id);
    }

    public Prova buscarPorIdComEstrutura(Long id) {
        return provaDAO.buscarPorIdComEstrutura(id);
    }

    public List<Prova> listarTodas() {
        return provaDAO.listarTodos("dataCriacao");
    }

    public List<Prova> listarAtivas() {
        return provaDAO.listarAtivas();
    }

    public int calcularProximaOrdemQuestao(Long provaId) {
        if (provaId == null) {
            return 1;
        }
        Prova prova = provaDAO.buscarPorIdComEstrutura(provaId);
        return prova == null ? 1 : calcularProximaOrdem(prova);
    }

    private void validarProva(Prova prova) {
        if (prova.getTitulo() == null || prova.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("Titulo da prova obrigatorio.");
        }
        if (prova.getTempoMinutos() == null || prova.getTempoMinutos() <= 0) {
            throw new IllegalArgumentException("Tempo limite deve ser maior que zero.");
        }
        if (prova.getNotaMinima() == null) {
            throw new IllegalArgumentException("Nota minima obrigatoria.");
        }
    }

    private void validarQuestao(Questao questao, List<Alternativa> alternativas) {
        if (questao.getEnunciado() == null || questao.getEnunciado().trim().isEmpty()) {
            throw new IllegalArgumentException("Enunciado obrigatorio.");
        }
        if (questao.getTipo() == null) {
            throw new IllegalArgumentException("Tipo da questao obrigatorio.");
        }
        if (alternativas == null || alternativas.size() < 2) {
            throw new IllegalArgumentException("Informe ao menos duas alternativas.");
        }
        int corretas = 0;
        for (Alternativa alternativa : alternativas) {
            if (alternativa.getTexto() == null || alternativa.getTexto().trim().isEmpty()) {
                throw new IllegalArgumentException("Texto da alternativa obrigatorio.");
            }
            if (alternativa.isCorreta()) {
                corretas++;
            }
        }
        if (corretas == 0) {
            throw new IllegalArgumentException("Ao menos uma alternativa deve ser correta.");
        }
        if (TipoQuestao.ESCOLHA_UNICA.equals(questao.getTipo()) && corretas != 1) {
            throw new IllegalArgumentException("Questoes de escolha unica devem possuir exatamente uma alternativa correta.");
        }
        if (TipoQuestao.VERDADEIRO_FALSO.equals(questao.getTipo())) {
            if (alternativas.size() != 2) {
                throw new IllegalArgumentException("Questoes de verdadeiro ou falso devem possuir exatamente duas alternativas.");
            }
            if (corretas != 1) {
                throw new IllegalArgumentException("Questoes de verdadeiro ou falso devem possuir uma unica alternativa correta.");
            }
        }
    }

    private void registrarLog(String acao, String usuario, String descricao) {
        LogOperacao log = new LogOperacao();
        log.setAcao(acao);
        log.setUsuario(usuario);
        log.setDescricao(descricao);
        logOperacaoDAO.salvar(log);
    }

    private int calcularProximaOrdem(Prova prova) {
        if (prova.getQuestoes() == null || prova.getQuestoes().isEmpty()) {
            return 1;
        }
        List<Questao> questoes = new ArrayList<Questao>(prova.getQuestoes());
        Collections.sort(questoes, new Comparator<Questao>() {
            @Override
            public int compare(Questao q1, Questao q2) {
                Integer ordem1 = q1.getOrdemExibicao() == null ? 0 : q1.getOrdemExibicao();
                Integer ordem2 = q2.getOrdemExibicao() == null ? 0 : q2.getOrdemExibicao();
                return ordem1.compareTo(ordem2);
            }
        });
        Integer ultimaOrdem = questoes.get(questoes.size() - 1).getOrdemExibicao();
        return (ultimaOrdem == null ? 0 : ultimaOrdem) + 1;
    }

    private Questao localizarQuestao(Prova prova, Long questaoId) {
        if (prova.getQuestoes() == null) {
            return null;
        }
        for (Questao questao : prova.getQuestoes()) {
            if (questaoId.equals(questao.getId())) {
                return questao;
            }
        }
        return null;
    }

    private void reordenarQuestoes(Prova prova) {
        if (prova.getQuestoes() == null || prova.getQuestoes().isEmpty()) {
            return;
        }
        List<Questao> questoes = new ArrayList<Questao>(prova.getQuestoes());
        Collections.sort(questoes, new Comparator<Questao>() {
            @Override
            public int compare(Questao q1, Questao q2) {
                Integer ordem1 = q1.getOrdemExibicao() == null ? 0 : q1.getOrdemExibicao();
                Integer ordem2 = q2.getOrdemExibicao() == null ? 0 : q2.getOrdemExibicao();
                return ordem1.compareTo(ordem2);
            }
        });
        int ordem = 1;
        for (Questao questao : questoes) {
            questao.setOrdemExibicao(ordem++);
        }
    }

    private void sincronizarAlternativas(Questao questaoPersistida, List<Alternativa> alternativasAtualizadas) {
        List<Alternativa> alternativasExistentes = questaoPersistida.getAlternativas();
        List<Alternativa> alternativasSincronizadas = new ArrayList<Alternativa>();

        for (Alternativa alternativaAtualizada : alternativasAtualizadas) {
            Alternativa alternativaPersistida = localizarAlternativa(alternativasExistentes, alternativaAtualizada.getId());
            if (alternativaPersistida == null) {
                alternativaPersistida = new Alternativa();
                alternativaPersistida.setQuestao(questaoPersistida);
            }
            alternativaPersistida.setTexto(alternativaAtualizada.getTexto());
            alternativaPersistida.setCorreta(alternativaAtualizada.isCorreta());
            alternativasSincronizadas.add(alternativaPersistida);
        }

        List<Alternativa> alternativasParaRemover = new ArrayList<Alternativa>();
        for (Alternativa alternativaExistente : alternativasExistentes) {
            if (!alternativasSincronizadas.contains(alternativaExistente)) {
                alternativasParaRemover.add(alternativaExistente);
            }
        }

        alternativasExistentes.removeAll(alternativasParaRemover);
        for (Alternativa alternativaSincronizada : alternativasSincronizadas) {
            if (!alternativasExistentes.contains(alternativaSincronizada)) {
                alternativasExistentes.add(alternativaSincronizada);
            }
        }
    }

    private Alternativa localizarAlternativa(List<Alternativa> alternativas, Long alternativaId) {
        if (alternativas == null || alternativaId == null) {
            return null;
        }
        for (Alternativa alternativa : alternativas) {
            if (alternativaId.equals(alternativa.getId())) {
                return alternativa;
            }
        }
        return null;
    }
}


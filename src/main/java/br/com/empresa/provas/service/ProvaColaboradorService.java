package br.com.empresa.provas.service;

import br.com.empresa.provas.dao.LogOperacaoDAO;
import br.com.empresa.provas.dao.ProvaColaboradorDAO;
import br.com.empresa.provas.dao.ProvaDAO;
import br.com.empresa.provas.dao.UsuarioDAO;
import br.com.empresa.provas.entity.Alternativa;
import br.com.empresa.provas.entity.LogOperacao;
import br.com.empresa.provas.entity.Prova;
import br.com.empresa.provas.entity.ProvaColaborador;
import br.com.empresa.provas.entity.Questao;
import br.com.empresa.provas.entity.RespostaColaborador;
import br.com.empresa.provas.entity.Usuario;
import br.com.empresa.provas.entity.enums.StatusProvaColaborador;
import br.com.empresa.provas.entity.enums.TipoQuestao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProvaColaboradorService {

    private final ProvaColaboradorDAO provaColaboradorDAO = new ProvaColaboradorDAO();
    private final ProvaDAO provaDAO = new ProvaDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final LogOperacaoDAO logOperacaoDAO = new LogOperacaoDAO();

    public void enviarProva(Long provaId, List<Long> colaboradoresIds, Date dataLimite, Integer tentativasPermitidas,
                            boolean disponivelImediatamente, Usuario aplicadorLogado) {
        if (provaId == null) {
            throw new IllegalArgumentException("Selecione uma prova para enviar.");
        }
        Prova prova = provaDAO.buscarPorId(provaId);
        if (prova == null) {
            throw new IllegalArgumentException("Prova nao encontrada.");
        }
        if (colaboradoresIds == null || colaboradoresIds.isEmpty()) {
            throw new IllegalArgumentException("Selecione ao menos um colaborador.");
        }
        if (dataLimite == null) {
            throw new IllegalArgumentException("Informe a data limite da prova.");
        }
        if (tentativasPermitidas == null || tentativasPermitidas.intValue() <= 0) {
            throw new IllegalArgumentException("Informe a quantidade de tentativas permitidas.");
        }

        for (Long colaboradorId : colaboradoresIds) {
            Usuario colaborador = usuarioDAO.buscarPorIdComResponsavel(colaboradorId);
            if (aplicadorLogado != null && colaborador != null && colaborador.getAplicadorResponsavel() != null
                    && !aplicadorLogado.getId().equals(colaborador.getAplicadorResponsavel().getId())) {
                throw new IllegalArgumentException("Voce so pode enviar provas para colaboradores da sua turma.");
            }
            ProvaColaborador vinculo = new ProvaColaborador();
            vinculo.setProva(prova);
            vinculo.setColaborador(colaborador);
            vinculo.setDataLimite(dataLimite);
            vinculo.setTentativasPermitidas(tentativasPermitidas == null ? 1 : tentativasPermitidas);
            vinculo.setDisponivelImediatamente(disponivelImediatamente);
            provaColaboradorDAO.salvar(vinculo);
            registrarLog("PROVA_ENVIADA", colaborador.getCpf(), "Prova " + prova.getTitulo() + " enviada.");
        }
    }

    public List<ProvaColaborador> listarPorColaborador(Usuario colaborador) {
        return provaColaboradorDAO.listarPorColaborador(colaborador);
    }

    public List<ProvaColaborador> listarTodos() {
        return provaColaboradorDAO.listarTodosComRelacionamentos();
    }

    public List<ProvaColaborador> listarTodosPorResponsavel(Usuario responsavel) {
        if (responsavel == null || responsavel.getId() == null) {
            return listarTodos();
        }
        return provaColaboradorDAO.listarTodosPorResponsavel(responsavel.getId());
    }

    public void excluirEnvio(Long id) {
        ProvaColaborador vinculo = provaColaboradorDAO.buscarPorIdParaRealizacao(id);
        if (vinculo == null) {
            throw new IllegalArgumentException("Envio nao encontrado.");
        }
        provaColaboradorDAO.excluir(id);
        registrarLog("ENVIO_PROVA_EXCLUIDO", vinculo.getColaborador().getCpf(),
                "Envio da prova " + vinculo.getProva().getTitulo() + " excluido.");
    }

    public ProvaColaborador buscarPorId(Long id) {
        return carregarVinculoCompleto(id);
    }

    public ProvaColaborador iniciarProva(Long provaColaboradorId) {
        ProvaColaborador vinculo = carregarVinculoCompleto(provaColaboradorId);
        validarInicio(vinculo);
        vinculo.setStatus(StatusProvaColaborador.EM_ANDAMENTO);
        vinculo.setTentativa(vinculo.getTentativa() + 1);
        if (vinculo.getDataInicio() == null) {
            vinculo.setDataInicio(new Date());
        }
        ProvaColaborador salvo = provaColaboradorDAO.salvar(vinculo);
        registrarLog("PROVA_INICIADA", vinculo.getColaborador().getCpf(), "Prova iniciada: " + vinculo.getProva().getTitulo());
        return carregarVinculoCompleto(salvo.getId());
    }

    public ProvaColaborador salvarRespostas(Long provaColaboradorId, Map<Long, List<Long>> respostasMap) {
        ProvaColaborador vinculo = carregarVinculoCompleto(provaColaboradorId);
        if (vinculo == null || vinculo.isFinalizada()) {
            throw new IllegalArgumentException("Prova nao pode receber novas respostas.");
        }

        vinculo.getRespostas().clear();
        for (Questao questao : vinculo.getProva().getQuestoes()) {
            List<Long> alternativasIds = respostasMap.get(questao.getId());
            validarQuantidadeRespostasPorTipo(questao, alternativasIds);
            if (alternativasIds == null) {
                continue;
            }
            for (Long alternativaId : alternativasIds) {
                if (alternativaId == null) {
                    continue;
                }
                Alternativa alternativaEscolhida = localizarAlternativa(questao, alternativaId);
                RespostaColaborador resposta = new RespostaColaborador();
                resposta.setProvaColaborador(vinculo);
                resposta.setQuestao(questao);
                resposta.setAlternativa(alternativaEscolhida);
                vinculo.getRespostas().add(resposta);
            }
        }
        ProvaColaborador salvo = provaColaboradorDAO.salvar(vinculo);
        return carregarVinculoCompleto(salvo.getId());
    }

    public ProvaColaborador finalizarProva(Long provaColaboradorId, Map<Long, List<Long>> respostasMap) {
        ProvaColaborador vinculo = salvarRespostas(provaColaboradorId, respostasMap);
        BigDecimal nota = calcularNota(vinculo);
        vinculo.setNota(nota);
        vinculo.setDataFim(new Date());
        vinculo.setStatus(StatusProvaColaborador.FINALIZADA);
        if (vinculo.getDataInicio() != null) {
            long minutos = (vinculo.getDataFim().getTime() - vinculo.getDataInicio().getTime()) / 60000L;
            vinculo.setTempoGasto((int) minutos);
        }
        ProvaColaborador salvo = provaColaboradorDAO.salvar(vinculo);
        registrarLog("PROVA_FINALIZADA", vinculo.getColaborador().getCpf(), "Prova finalizada com nota " + nota);
        return salvo;
    }

    public void expirarSeNecessario(ProvaColaborador vinculo) {
        if (vinculo != null && !vinculo.isFinalizada() && vinculo.getDataLimite() != null
                && vinculo.getDataLimite().before(new Date())) {
            vinculo.setStatus(StatusProvaColaborador.EXPIRADA);
            provaColaboradorDAO.salvar(vinculo);
        }
    }

    public Map<Long, List<Long>> mapearRespostas(ProvaColaborador vinculo) {
        Map<Long, List<Long>> mapa = new HashMap<Long, List<Long>>();
        if (vinculo != null && vinculo.getRespostas() != null) {
            for (RespostaColaborador resposta : vinculo.getRespostas()) {
                if (!mapa.containsKey(resposta.getQuestao().getId())) {
                    mapa.put(resposta.getQuestao().getId(), new ArrayList<Long>());
                }
                mapa.get(resposta.getQuestao().getId()).add(resposta.getAlternativa().getId());
            }
        }
        return mapa;
    }

    private void validarQuantidadeRespostasPorTipo(Questao questao, List<Long> alternativasIds) {
        if (!TipoQuestao.MULTIPLA_ESCOLHA.equals(questao.getTipo())
                && alternativasIds != null && alternativasIds.size() > 1) {
            throw new IllegalArgumentException("A questao \"" + questao.getEnunciado() + "\" permite apenas uma resposta.");
        }
    }

    private void validarInicio(ProvaColaborador vinculo) {
        if (vinculo == null) {
            throw new IllegalArgumentException("Prova nao encontrada.");
        }
        if (vinculo.isFinalizada()) {
            throw new IllegalArgumentException("A prova ja foi finalizada.");
        }
        if (vinculo.getDataLimite() != null && vinculo.getDataLimite().before(new Date())) {
            throw new IllegalArgumentException("Prazo da prova expirado.");
        }
        if (vinculo.getTentativa() >= vinculo.getTentativasPermitidas()) {
            throw new IllegalArgumentException("Limite de tentativas excedido.");
        }
    }

    private Alternativa localizarAlternativa(Questao questao, Long alternativaId) {
        for (Alternativa alternativa : questao.getAlternativas()) {
            if (alternativa.getId().equals(alternativaId)) {
                return alternativa;
            }
        }
        throw new IllegalArgumentException("Alternativa nao encontrada para a questao.");
    }

    private BigDecimal calcularNota(ProvaColaborador vinculo) {
        BigDecimal somaPesos = BigDecimal.ZERO;
        BigDecimal somaAcertos = BigDecimal.ZERO;
        List<RespostaColaborador> respostas = vinculo.getRespostas() == null ? new ArrayList<RespostaColaborador>() : vinculo.getRespostas();
        for (Questao questao : vinculo.getProva().getQuestoes()) {
            somaPesos = somaPesos.add(questao.getPeso());
            Set<Long> marcadas = new HashSet<Long>();
            Set<Long> corretas = new HashSet<Long>();
            for (Alternativa alternativa : questao.getAlternativas()) {
                if (alternativa.isCorreta()) {
                    corretas.add(alternativa.getId());
                }
            }
            for (RespostaColaborador resposta : respostas) {
                if (resposta.getQuestao().getId().equals(questao.getId())) {
                    marcadas.add(resposta.getAlternativa().getId());
                }
            }
            if (!marcadas.isEmpty() && marcadas.equals(corretas)) {
                somaAcertos = somaAcertos.add(questao.getPeso());
            }
        }
        if (BigDecimal.ZERO.compareTo(somaPesos) == 0) {
            return BigDecimal.ZERO;
        }
        return somaAcertos.multiply(BigDecimal.valueOf(10))
                .divide(somaPesos, 2, RoundingMode.HALF_UP);
    }

    private void registrarLog(String acao, String usuario, String descricao) {
        LogOperacao log = new LogOperacao();
        log.setAcao(acao);
        log.setUsuario(usuario);
        log.setDescricao(descricao);
        logOperacaoDAO.salvar(log);
    }

    private ProvaColaborador carregarVinculoCompleto(Long id) {
        ProvaColaborador vinculo = provaColaboradorDAO.buscarPorIdParaRealizacao(id);
        if (vinculo != null && vinculo.getProva() != null) {
            vinculo.setProva(provaDAO.buscarPorIdComEstrutura(vinculo.getProva().getId()));
        }
        return vinculo;
    }
}


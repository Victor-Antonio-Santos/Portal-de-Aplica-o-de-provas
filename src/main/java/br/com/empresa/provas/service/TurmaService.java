package br.com.empresa.provas.service;

import br.com.empresa.provas.dao.LogOperacaoDAO;
import br.com.empresa.provas.dao.TurmaDAO;
import br.com.empresa.provas.entity.LogOperacao;
import br.com.empresa.provas.entity.Turma;
import br.com.empresa.provas.entity.Usuario;
import br.com.empresa.provas.entity.enums.StatusTurma;

import java.util.Collections;
import java.util.List;

public class TurmaService {

    private final TurmaDAO turmaDAO = new TurmaDAO();
    private final LogOperacaoDAO logOperacaoDAO = new LogOperacaoDAO();

    public Turma salvar(Turma turma, Usuario responsavel) {
        validarTurma(turma, responsavel);
        Turma existente = turmaDAO.buscarPorNome(turma.getNome().trim());
        if (existente != null && (turma.getId() == null || !existente.getId().equals(turma.getId()))) {
            throw new IllegalArgumentException("Ja existe uma turma cadastrada com este nome.");
        }

        turma.setNome(turma.getNome().trim());
        turma.setAplicadorResponsavel(responsavel);
        Turma salva = turmaDAO.salvar(turma);
        registrarLog("TURMA_SALVA", responsavel.getCpf(), "Turma cadastrada/atualizada: " + turma.getNome());
        return salva;
    }

    public List<Turma> listarPorResponsavel(Usuario responsavel) {
        if (responsavel == null || responsavel.getId() == null) {
            return Collections.emptyList();
        }
        return turmaDAO.listarPorResponsavel(responsavel.getId());
    }

    public List<Turma> listarAtivas() {
        return turmaDAO.listarAtivas();
    }

    public Turma buscarPorId(Long id) {
        return turmaDAO.buscarPorIdComResponsavel(id);
    }

    public Turma validarTurmaAtiva(Long turmaId) {
        if (turmaId == null) {
            throw new IllegalArgumentException("Selecione uma turma valida.");
        }
        Turma turma = turmaDAO.buscarPorIdComResponsavel(turmaId);
        if (turma == null) {
            throw new IllegalArgumentException("A turma informada nao existe.");
        }
        if (!StatusTurma.ATIVA.equals(turma.getStatus())) {
            throw new IllegalArgumentException("A turma informada esta inativa.");
        }
        return turma;
    }

    public Turma validarTurmaAtivaPorNomeEResponsavel(String nomeTurma, Usuario responsavel) {
        if (nomeTurma == null || nomeTurma.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe a turma do colaborador na planilha.");
        }
        if (responsavel == null || responsavel.getId() == null) {
            throw new IllegalArgumentException("Nao foi possivel identificar o treinador responsavel.");
        }
        Turma turma = turmaDAO.buscarPorNomeEResponsavel(nomeTurma.trim(), responsavel.getId());
        if (turma == null) {
            throw new IllegalArgumentException("A turma \"" + nomeTurma + "\" nao existe para o treinador logado.");
        }
        if (!StatusTurma.ATIVA.equals(turma.getStatus())) {
            throw new IllegalArgumentException("A turma \"" + nomeTurma + "\" esta inativa.");
        }
        return turma;
    }

    private void validarTurma(Turma turma, Usuario responsavel) {
        if (responsavel == null || responsavel.getId() == null) {
            throw new IllegalArgumentException("Nao foi possivel identificar o treinador responsavel.");
        }
        if (turma == null) {
            throw new IllegalArgumentException("Turma nao informada.");
        }
        if (turma.getNome() == null || turma.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da turma obrigatorio.");
        }
        if (turma.getStatus() == null) {
            throw new IllegalArgumentException("Status da turma obrigatorio.");
        }
    }

    private void registrarLog(String acao, String usuario, String descricao) {
        LogOperacao log = new LogOperacao();
        log.setAcao(acao);
        log.setUsuario(usuario);
        log.setDescricao(descricao);
        logOperacaoDAO.salvar(log);
    }
}


package br.com.empresa.provas.service;

import br.com.empresa.provas.dao.ProvaColaboradorDAO;
import br.com.empresa.provas.entity.ProvaColaborador;
import br.com.empresa.provas.entity.Usuario;

import java.util.List;
import java.util.stream.Collectors;

public class RelatorioService {

    private final ProvaColaboradorDAO provaColaboradorDAO = new ProvaColaboradorDAO();

    public List<ProvaColaborador> listarTodos() {
        return provaColaboradorDAO.listarTodosComRelacionamentos();
    }

    public List<ProvaColaborador> filtrar(String nomeOuCpf, String status, Usuario responsavel) {
        return filtrar(nomeOuCpf, status, null, responsavel);
    }

    public List<ProvaColaborador> filtrar(String nomeOuCpf, String status, Long turmaId, Usuario responsavel) {
        List<ProvaColaborador> base = responsavel == null || responsavel.getId() == null
                ? listarTodos()
                : provaColaboradorDAO.listarTodosPorResponsavel(responsavel.getId());
        return base.stream()
                .filter(v -> nomeOuCpf == null || nomeOuCpf.trim().isEmpty()
                        || v.getColaborador().getNome().toLowerCase().contains(nomeOuCpf.toLowerCase())
                        || v.getColaborador().getCpf().contains(nomeOuCpf.replaceAll("\\D", "")))
                .filter(v -> status == null || status.trim().isEmpty() || v.getStatus().name().equals(status))
                .filter(v -> turmaId == null
                        || (v.getColaborador().getTurma() != null && turmaId.equals(v.getColaborador().getTurma().getId())))
                .collect(Collectors.toList());
    }
}


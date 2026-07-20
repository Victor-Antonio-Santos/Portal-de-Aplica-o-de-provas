package br.com.empresa.provas.service;

import br.com.empresa.provas.dao.ProvaColaboradorDAO;
import br.com.empresa.provas.dao.ProvaDAO;
import br.com.empresa.provas.dao.UsuarioDAO;
import br.com.empresa.provas.entity.ProvaColaborador;
import br.com.empresa.provas.entity.Usuario;
import br.com.empresa.provas.entity.enums.PerfilUsuario;
import br.com.empresa.provas.entity.enums.StatusProvaColaborador;
import br.com.empresa.provas.service.dto.DashboardResumo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class DashboardService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ProvaDAO provaDAO = new ProvaDAO();
    private final ProvaColaboradorDAO provaColaboradorDAO = new ProvaColaboradorDAO();

    public DashboardResumo carregarResumo() {
        return carregarResumo(null, null);
    }

    public DashboardResumo carregarResumo(Usuario responsavel) {
        return carregarResumo(responsavel, null);
    }

    public DashboardResumo carregarResumo(Usuario responsavel, Long turmaId) {
        DashboardResumo resumo = new DashboardResumo();
        List<ProvaColaborador> vinculosBase = responsavel == null || responsavel.getId() == null
                ? provaColaboradorDAO.listarTodosComRelacionamentos()
                : provaColaboradorDAO.listarTodosPorResponsavel(responsavel.getId());
        List<ProvaColaborador> vinculos = filtrarPorTurma(vinculosBase, turmaId);
        resumo.setTotalColaboradores(filtrarColaboradores(responsavel, turmaId).size());
        resumo.setTotalProvas(provaDAO.listarTodos("dataCriacao").size());
        resumo.setProvasPendentes(contarPorStatus(vinculos, StatusProvaColaborador.PENDENTE));
        resumo.setProvasFinalizadas(contarPorStatus(vinculos, StatusProvaColaborador.FINALIZADA));
        resumo.setAprovados(contarAprovados(vinculos, true));
        resumo.setReprovados(contarAprovados(vinculos, false));
        resumo.setMediaNotas(calcularMedia(vinculos));
        return resumo;
    }

    private List<Usuario> filtrarColaboradores(Usuario responsavel, Long turmaId) {
        List<Usuario> colaboradores = responsavel == null || responsavel.getId() == null
                ? usuarioDAO.listarPorPerfil(PerfilUsuario.ROLE_COLABORADOR)
                : usuarioDAO.listarColaboradoresPorResponsavel(responsavel.getId());
        if (turmaId == null) {
            return colaboradores;
        }
        return colaboradores.stream()
                .filter(colaborador -> colaborador.getTurma() != null && turmaId.equals(colaborador.getTurma().getId()))
                .collect(java.util.stream.Collectors.toList());
    }

    private List<ProvaColaborador> filtrarPorTurma(List<ProvaColaborador> vinculos, Long turmaId) {
        if (turmaId == null) {
            return vinculos;
        }
        return vinculos.stream()
                .filter(vinculo -> vinculo.getColaborador() != null
                        && vinculo.getColaborador().getTurma() != null
                        && turmaId.equals(vinculo.getColaborador().getTurma().getId()))
                .collect(java.util.stream.Collectors.toList());
    }

    private long contarPorStatus(List<ProvaColaborador> vinculos, StatusProvaColaborador status) {
        return vinculos.stream().filter(v -> status.equals(v.getStatus())).count();
    }

    private long contarAprovados(List<ProvaColaborador> vinculos, boolean aprovado) {
        return vinculos.stream()
                .filter(v -> StatusProvaColaborador.FINALIZADA.equals(v.getStatus()))
                .filter(v -> aprovado == (v.getNota().compareTo(v.getProva().getNotaMinima()) >= 0))
                .count();
    }

    private BigDecimal calcularMedia(List<ProvaColaborador> vinculos) {
        BigDecimal soma = BigDecimal.ZERO;
        long total = 0;
        for (ProvaColaborador vinculo : vinculos) {
            if (StatusProvaColaborador.FINALIZADA.equals(vinculo.getStatus())) {
                soma = soma.add(vinculo.getNota() == null ? BigDecimal.ZERO : vinculo.getNota());
                total++;
            }
        }
        return total == 0 ? BigDecimal.ZERO : soma.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
}


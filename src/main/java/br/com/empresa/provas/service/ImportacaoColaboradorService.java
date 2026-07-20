package br.com.empresa.provas.service;

import br.com.empresa.provas.dao.UsuarioDAO;
import br.com.empresa.provas.entity.Turma;
import br.com.empresa.provas.entity.Usuario;
import br.com.empresa.provas.entity.enums.PerfilUsuario;
import br.com.empresa.provas.entity.enums.StatusUsuario;
import br.com.empresa.provas.service.dto.ImportacaoColaboradorLinhaDTO;
import br.com.empresa.provas.service.dto.ImportacaoColaboradorLinhaResultado;
import br.com.empresa.provas.service.dto.ImportacaoColaboradorResultado;
import br.com.empresa.provas.util.CPFUtil;
import br.com.empresa.provas.util.PlanilhaColaboradorUtil;

import java.io.InputStream;
import java.util.List;

public class ImportacaoColaboradorService {

    private final UsuarioService usuarioService = new UsuarioService();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final TurmaService turmaService = new TurmaService();

    public ImportacaoColaboradorResultado importar(InputStream inputStream, Long turmaId, String senhaPadrao, Usuario responsavel) {
        if (responsavel == null || responsavel.getId() == null) {
            throw new IllegalArgumentException("Nao foi possivel identificar o treinador responsavel.");
        }

        Turma turmaPadrao = turmaId == null ? null : turmaService.validarTurmaAtiva(turmaId);
        if (turmaPadrao != null && !responsavel.getId().equals(turmaPadrao.getAplicadorResponsavel().getId())) {
            throw new IllegalArgumentException("A turma selecionada nao pertence ao treinador logado.");
        }
        List<ImportacaoColaboradorLinhaDTO> linhas = PlanilhaColaboradorUtil.ler(inputStream);
        ImportacaoColaboradorResultado resultado = new ImportacaoColaboradorResultado();

        for (ImportacaoColaboradorLinhaDTO linha : linhas) {
            ImportacaoColaboradorLinhaResultado itemResultado = new ImportacaoColaboradorLinhaResultado();
            itemResultado.setNumeroLinha(linha.getNumeroLinha());
            itemResultado.setNome(linha.getNome());
            itemResultado.setCpf(linha.getCpf());
            try {
                processarLinha(linha, turmaPadrao, senhaPadrao, responsavel);
                itemResultado.setSucesso(true);
                itemResultado.setMensagem("Colaborador cadastrado/atualizado com sucesso.");
                resultado.setTotalSucesso(resultado.getTotalSucesso() + 1);
            } catch (IllegalArgumentException e) {
                itemResultado.setSucesso(false);
                itemResultado.setMensagem(e.getMessage());
                resultado.setTotalErro(resultado.getTotalErro() + 1);
            }
            resultado.getLinhas().add(itemResultado);
        }

        resultado.setTotalProcessado(resultado.getLinhas().size());
        return resultado;
    }

    private void processarLinha(ImportacaoColaboradorLinhaDTO linha, Turma turmaPadrao, String senhaPadrao, Usuario responsavel) {
        if (linha.getNome() == null || linha.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome obrigatorio.");
        }
        if (!CPFUtil.isValido(linha.getCpf())) {
            throw new IllegalArgumentException("CPF invalido.");
        }

        Turma turma = resolverTurmaLinha(linha, turmaPadrao, responsavel);

        String cpf = CPFUtil.somenteDigitos(linha.getCpf());
        Usuario existente = usuarioDAO.buscarPorCpf(cpf);
        Usuario colaborador = existente == null ? new Usuario() : existente;

        if (existente != null && !PerfilUsuario.ROLE_COLABORADOR.equals(existente.getPerfil())) {
            throw new IllegalArgumentException("O CPF informado ja pertence a um usuario administrativo.");
        }
        if (existente != null && existente.getAplicadorResponsavel() != null
                && !responsavel.getId().equals(existente.getAplicadorResponsavel().getId())) {
            throw new IllegalArgumentException("O colaborador ja esta vinculado a outro treinador.");
        }

        colaborador.setNome(linha.getNome());
        colaborador.setCpf(cpf);
        colaborador.setEmail(linha.getEmail());
        colaborador.setTurma(turma);
        colaborador.setPerfil(PerfilUsuario.ROLE_COLABORADOR);
        colaborador.setStatus(StatusUsuario.ATIVO);
        colaborador.setAplicadorResponsavel(responsavel);

        String senha = linha.getSenha() == null || linha.getSenha().trim().isEmpty() ? senhaPadrao : linha.getSenha().trim();
        if ((senha == null || senha.trim().isEmpty()) && colaborador.getId() == null) {
            throw new IllegalArgumentException("Defina uma senha na planilha ou informe uma senha padrao.");
        }

        usuarioService.salvar(colaborador, senha);
    }

    private Turma resolverTurmaLinha(ImportacaoColaboradorLinhaDTO linha, Turma turmaPadrao, Usuario responsavel) {
        if (linha.getTurma() != null && !linha.getTurma().trim().isEmpty()) {
            return turmaService.validarTurmaAtivaPorNomeEResponsavel(linha.getTurma(), responsavel);
        }
        if (turmaPadrao != null) {
            return turmaPadrao;
        }
        throw new IllegalArgumentException("Informe a turma na planilha ou selecione uma turma padrao para a importacao.");
    }
}


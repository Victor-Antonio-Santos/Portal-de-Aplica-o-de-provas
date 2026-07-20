package br.com.empresa.provas.listener;

import br.com.empresa.provas.dao.UsuarioDAO;
import br.com.empresa.provas.entity.Alternativa;
import br.com.empresa.provas.entity.Prova;
import br.com.empresa.provas.entity.Questao;
import br.com.empresa.provas.entity.Turma;
import br.com.empresa.provas.entity.Usuario;
import br.com.empresa.provas.entity.enums.PerfilUsuario;
import br.com.empresa.provas.entity.enums.StatusProva;
import br.com.empresa.provas.entity.enums.StatusTurma;
import br.com.empresa.provas.entity.enums.StatusUsuario;
import br.com.empresa.provas.entity.enums.TipoQuestao;
import br.com.empresa.provas.service.ProvaColaboradorService;
import br.com.empresa.provas.service.ProvaService;
import br.com.empresa.provas.service.TurmaService;
import br.com.empresa.provas.service.UsuarioService;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@WebListener
public class ApplicationBootstrapListener implements ServletContextListener {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final UsuarioService usuarioService = new UsuarioService();
    private final TurmaService turmaService = new TurmaService();
    private final ProvaService provaService = new ProvaService();
    private final ProvaColaboradorService provaColaboradorService = new ProvaColaboradorService();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (!usuarioDAO.listarTodos("nome").isEmpty()) {
            return;
        }

        Usuario admin = criarUsuario("Administrador Master", "52998224725", "admin@empresa.com",
                PerfilUsuario.ROLE_ADMIN, "admin123");
        Usuario colaborador = criarUsuario("Colaborador Teste", "11144477735", "colaborador@empresa.com",
                PerfilUsuario.ROLE_COLABORADOR, "colab123");
        Turma turma = new Turma();
        turma.setNome("Turma Master");
        turma.setStatus(StatusTurma.ATIVA);
        turma = turmaService.salvar(turma, admin);
        colaborador.setTurma(turma);
        colaborador.setAplicadorResponsavel(turma.getAplicadorResponsavel());
        colaborador = usuarioService.salvar(colaborador, null);

        Prova prova = new Prova();
        prova.setTitulo("Integracao Corporativa");
        prova.setDescricao("Prova inicial para novos colaboradores.");
        prova.setTempoMinutos(30);
        prova.setNotaMinima(BigDecimal.valueOf(7));
        prova.setStatus(StatusProva.ATIVA);
        prova.setMostrarResultado(true);
        prova = provaService.salvar(prova);

        Questao questao1 = new Questao();
        questao1.setEnunciado("A empresa valoriza comportamento etico no ambiente de trabalho?");
        questao1.setTipo(TipoQuestao.VERDADEIRO_FALSO);
        questao1.setPeso(BigDecimal.valueOf(2));
        questao1.setOrdemExibicao(1);
        provaService.adicionarQuestao(prova.getId(), questao1, criarAlternativas("Verdadeiro", true, "Falso", false));

        Questao questao2 = new Questao();
        questao2.setEnunciado("Qual atitude e esperada durante a integracao?");
        questao2.setTipo(TipoQuestao.ESCOLHA_UNICA);
        questao2.setPeso(BigDecimal.valueOf(3));
        questao2.setOrdemExibicao(2);
        List<Alternativa> alternativasQuestao2 = new ArrayList<Alternativa>();
        alternativasQuestao2.add(criarAlternativa("Seguir procedimentos e participar dos treinamentos.", true));
        alternativasQuestao2.add(criarAlternativa("Ignorar as orientacoes iniciais.", false));
        alternativasQuestao2.add(criarAlternativa("Compartilhar credenciais com colegas.", false));
        provaService.adicionarQuestao(prova.getId(), questao2, alternativasQuestao2);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        List<Long> colaboradores = new ArrayList<Long>();
        colaboradores.add(colaborador.getId());
        provaColaboradorService.enviarProva(prova.getId(), colaboradores, calendar.getTime(), 1, true, admin);
    }

    private Usuario criarUsuario(String nome, String cpf, String email, PerfilUsuario perfil, String senha) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setCpf(cpf);
        usuario.setEmail(email);
        usuario.setPerfil(perfil);
        usuario.setStatus(StatusUsuario.ATIVO);
        return usuarioService.salvar(usuario, senha);
    }

    private List<Alternativa> criarAlternativas(String texto1, boolean correta1, String texto2, boolean correta2) {
        List<Alternativa> alternativas = new ArrayList<Alternativa>();
        alternativas.add(criarAlternativa(texto1, correta1));
        alternativas.add(criarAlternativa(texto2, correta2));
        return alternativas;
    }

    private Alternativa criarAlternativa(String texto, boolean correta) {
        Alternativa alternativa = new Alternativa();
        alternativa.setTexto(texto);
        alternativa.setCorreta(correta);
        return alternativa;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}


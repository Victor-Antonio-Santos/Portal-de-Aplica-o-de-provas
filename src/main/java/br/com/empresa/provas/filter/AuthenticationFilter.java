package br.com.empresa.provas.filter;

import br.com.empresa.provas.entity.Usuario;
import br.com.empresa.provas.entity.enums.PerfilUsuario;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = "/pages/*")
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        Usuario usuario = session == null ? null : (Usuario) session.getAttribute("usuarioLogado");
        String uri = req.getRequestURI();
        boolean paginaPublica = uri.endsWith("/pages/login.xhtml")
                || uri.endsWith("/pages/cadastro-inicial.xhtml");

        if (paginaPublica) {
            chain.doFilter(request, response);
            return;
        }

        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/login.xhtml");
            return;
        }

        if (uri.contains("/pages/admin/") && !PerfilUsuario.ROLE_ADMIN.equals(usuario.getPerfil())) {
            resp.sendRedirect(req.getContextPath() + "/pages/colaborador/minhas-provas.xhtml");
            return;
        }
        if (uri.contains("/pages/colaborador/") && !PerfilUsuario.ROLE_COLABORADOR.equals(usuario.getPerfil())) {
            resp.sendRedirect(req.getContextPath() + "/pages/admin/dashboard.xhtml");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}


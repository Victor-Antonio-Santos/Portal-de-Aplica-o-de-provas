package br.com.empresa.provas.dao;

import br.com.empresa.provas.entity.Usuario;
import br.com.empresa.provas.entity.enums.PerfilUsuario;
import br.com.empresa.provas.util.CPFUtil;
import br.com.empresa.provas.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.Collections;
import java.util.List;

public class UsuarioDAO extends GenericDAO<Usuario> {

    public UsuarioDAO() {
        super(Usuario.class);
    }

    public Usuario buscarPorCpf(String cpf) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("select u from Usuario u left join fetch u.aplicadorResponsavel left join fetch u.turma where u.cpf = :cpf", Usuario.class)
                    .setParameter("cpf", CPFUtil.somenteDigitos(cpf))
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public List<Usuario> listarPorPerfil(PerfilUsuario perfil) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("select u from Usuario u left join fetch u.aplicadorResponsavel left join fetch u.turma where u.perfil = :perfil order by u.nome", Usuario.class)
                    .setParameter("perfil", perfil)
                    .getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public List<Usuario> listarColaboradoresPorResponsavel(Long responsavelId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "select u from Usuario u left join fetch u.aplicadorResponsavel left join fetch u.turma " +
                                    "where u.perfil = :perfil and (u.aplicadorResponsavel.id = :responsavelId or u.aplicadorResponsavel is null) order by u.nome",
                            Usuario.class)
                    .setParameter("perfil", PerfilUsuario.ROLE_COLABORADOR)
                    .setParameter("responsavelId", responsavelId)
                    .getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public Usuario buscarPorIdComResponsavel(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("select u from Usuario u left join fetch u.aplicadorResponsavel left join fetch u.turma where u.id = :id", Usuario.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}


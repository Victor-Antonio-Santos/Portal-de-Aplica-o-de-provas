package br.com.empresa.provas.dao;

import br.com.empresa.provas.entity.Turma;
import br.com.empresa.provas.entity.enums.StatusTurma;
import br.com.empresa.provas.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.Collections;
import java.util.List;

public class TurmaDAO extends GenericDAO<Turma> {

    public TurmaDAO() {
        super(Turma.class);
    }

    public List<Turma> listarPorResponsavel(Long responsavelId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "select t from Turma t join fetch t.aplicadorResponsavel where t.aplicadorResponsavel.id = :responsavelId order by t.nome",
                            Turma.class)
                    .setParameter("responsavelId", responsavelId)
                    .getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public List<Turma> listarAtivas() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "select t from Turma t join fetch t.aplicadorResponsavel where t.status = :status order by t.nome",
                            Turma.class)
                    .setParameter("status", StatusTurma.ATIVA)
                    .getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public Turma buscarPorIdComResponsavel(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "select t from Turma t join fetch t.aplicadorResponsavel where t.id = :id",
                            Turma.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public Turma buscarPorNomeEResponsavel(String nome, Long responsavelId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "select t from Turma t join fetch t.aplicadorResponsavel where lower(t.nome) = :nome and t.aplicadorResponsavel.id = :responsavelId",
                            Turma.class)
                    .setParameter("nome", nome.toLowerCase())
                    .setParameter("responsavelId", responsavelId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public Turma buscarPorNome(String nome) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "select t from Turma t join fetch t.aplicadorResponsavel where lower(t.nome) = :nome",
                            Turma.class)
                    .setParameter("nome", nome.toLowerCase())
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}


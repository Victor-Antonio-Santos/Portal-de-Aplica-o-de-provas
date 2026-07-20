package br.com.empresa.provas.dao;

import br.com.empresa.provas.entity.ProvaColaborador;
import br.com.empresa.provas.entity.Usuario;
import br.com.empresa.provas.entity.enums.StatusProvaColaborador;
import br.com.empresa.provas.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;

public class ProvaColaboradorDAO extends GenericDAO<ProvaColaborador> {

    public ProvaColaboradorDAO() {
        super(ProvaColaborador.class);
    }

    public List<ProvaColaborador> listarPorColaborador(Usuario colaborador) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("select pc from ProvaColaborador pc join fetch pc.prova where pc.colaborador.id = :colaboradorId order by pc.dataEnvio desc", ProvaColaborador.class)
                    .setParameter("colaboradorId", colaborador.getId())
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<ProvaColaborador> listarPendentes() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("select pc from ProvaColaborador pc where pc.status = :status", ProvaColaborador.class)
                    .setParameter("status", StatusProvaColaborador.PENDENTE)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<ProvaColaborador> listarTodosComRelacionamentos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("select pc from ProvaColaborador pc join fetch pc.prova join fetch pc.colaborador c left join fetch c.aplicadorResponsavel left join fetch c.turma order by pc.dataEnvio desc", ProvaColaborador.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<ProvaColaborador> listarTodosPorResponsavel(Long responsavelId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "select pc from ProvaColaborador pc " +
                                    "join fetch pc.prova " +
                                    "join fetch pc.colaborador c " +
                                    "left join fetch c.aplicadorResponsavel " +
                                    "left join fetch c.turma " +
                                    "where c.aplicadorResponsavel.id = :responsavelId order by pc.dataEnvio desc",
                            ProvaColaborador.class)
                    .setParameter("responsavelId", responsavelId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public ProvaColaborador buscarPorIdParaRealizacao(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "select distinct pc from ProvaColaborador pc " +
                                    "join fetch pc.prova " +
                                    "join fetch pc.colaborador " +
                                    "left join fetch pc.respostas r " +
                                    "left join fetch r.questao " +
                                    "left join fetch r.alternativa " +
                                    "where pc.id = :id",
                            ProvaColaborador.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}


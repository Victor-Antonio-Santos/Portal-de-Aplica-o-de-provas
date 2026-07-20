package br.com.empresa.provas.dao;

import br.com.empresa.provas.entity.Prova;
import br.com.empresa.provas.entity.enums.StatusProva;
import br.com.empresa.provas.util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;

public class ProvaDAO extends GenericDAO<Prova> {

    public ProvaDAO() {
        super(Prova.class);
    }

    public List<Prova> listarAtivas() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("select p from Prova p where p.status = :status order by p.dataCriacao desc", Prova.class)
                    .setParameter("status", StatusProva.ATIVA)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Prova buscarPorIdComEstrutura(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Prova prova = em.createQuery(
                            "select distinct p from Prova p " +
                                    "left join fetch p.questoes q " +
                                    "where p.id = :id " +
                                    "order by q.ordemExibicao",
                            Prova.class)
                    .setParameter("id", id)
                    .getSingleResult();

            em.createQuery(
                            "select distinct q from Questao q " +
                                    "left join fetch q.alternativas a " +
                                    "where q.prova.id = :id " +
                                    "order by q.ordemExibicao, a.id",
                            br.com.empresa.provas.entity.Questao.class)
                    .setParameter("id", id)
                    .getResultList();

            prova.getQuestoes().size();
            return prova;
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}


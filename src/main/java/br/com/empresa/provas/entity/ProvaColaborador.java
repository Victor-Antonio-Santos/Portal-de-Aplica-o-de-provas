package br.com.empresa.provas.entity;

import br.com.empresa.provas.entity.enums.StatusProvaColaborador;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "prova_colaborador")
public class ProvaColaborador implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prova_id", nullable = false)
    private Prova prova;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Usuario colaborador;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_envio", nullable = false)
    private Date dataEnvio = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_inicio")
    private Date dataInicio;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_fim")
    private Date dataFim;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_limite", nullable = false)
    private Date dataLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusProvaColaborador status = StatusProvaColaborador.PENDENTE;

    @Column(precision = 10, scale = 2)
    private BigDecimal nota = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer tentativa = 0;

    @Column(name = "tentativas_permitidas", nullable = false)
    private Integer tentativasPermitidas = 1;

    @Column(name = "tempo_gasto")
    private Integer tempoGasto;

    @Column(name = "disponivel_imediatamente", nullable = false)
    private boolean disponivelImediatamente = true;

    @OneToMany(mappedBy = "provaColaborador", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RespostaColaborador> respostas = new ArrayList<RespostaColaborador>();

    public boolean isFinalizada() {
        return StatusProvaColaborador.FINALIZADA.equals(status);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Prova getProva() {
        return prova;
    }

    public void setProva(Prova prova) {
        this.prova = prova;
    }

    public Usuario getColaborador() {
        return colaborador;
    }

    public void setColaborador(Usuario colaborador) {
        this.colaborador = colaborador;
    }

    public Date getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(Date dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public Date getDataLimite() {
        return dataLimite;
    }

    public void setDataLimite(Date dataLimite) {
        this.dataLimite = dataLimite;
    }

    public StatusProvaColaborador getStatus() {
        return status;
    }

    public void setStatus(StatusProvaColaborador status) {
        this.status = status;
    }

    public BigDecimal getNota() {
        return nota;
    }

    public void setNota(BigDecimal nota) {
        this.nota = nota;
    }

    public Integer getTentativa() {
        return tentativa;
    }

    public void setTentativa(Integer tentativa) {
        this.tentativa = tentativa;
    }

    public Integer getTentativasPermitidas() {
        return tentativasPermitidas;
    }

    public void setTentativasPermitidas(Integer tentativasPermitidas) {
        this.tentativasPermitidas = tentativasPermitidas;
    }

    public Integer getTempoGasto() {
        return tempoGasto;
    }

    public void setTempoGasto(Integer tempoGasto) {
        this.tempoGasto = tempoGasto;
    }

    public boolean isDisponivelImediatamente() {
        return disponivelImediatamente;
    }

    public void setDisponivelImediatamente(boolean disponivelImediatamente) {
        this.disponivelImediatamente = disponivelImediatamente;
    }

    public List<RespostaColaborador> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<RespostaColaborador> respostas) {
        this.respostas = respostas;
    }
}


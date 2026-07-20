package br.com.empresa.provas.service.dto;

import java.math.BigDecimal;

public class DashboardResumo {

    private long totalColaboradores;
    private long totalProvas;
    private long provasPendentes;
    private long provasFinalizadas;
    private BigDecimal mediaNotas = BigDecimal.ZERO;
    private long aprovados;
    private long reprovados;

    public long getTotalColaboradores() {
        return totalColaboradores;
    }

    public void setTotalColaboradores(long totalColaboradores) {
        this.totalColaboradores = totalColaboradores;
    }

    public long getTotalProvas() {
        return totalProvas;
    }

    public void setTotalProvas(long totalProvas) {
        this.totalProvas = totalProvas;
    }

    public long getProvasPendentes() {
        return provasPendentes;
    }

    public void setProvasPendentes(long provasPendentes) {
        this.provasPendentes = provasPendentes;
    }

    public long getProvasFinalizadas() {
        return provasFinalizadas;
    }

    public void setProvasFinalizadas(long provasFinalizadas) {
        this.provasFinalizadas = provasFinalizadas;
    }

    public BigDecimal getMediaNotas() {
        return mediaNotas;
    }

    public void setMediaNotas(BigDecimal mediaNotas) {
        this.mediaNotas = mediaNotas;
    }

    public long getAprovados() {
        return aprovados;
    }

    public void setAprovados(long aprovados) {
        this.aprovados = aprovados;
    }

    public long getReprovados() {
        return reprovados;
    }

    public void setReprovados(long reprovados) {
        this.reprovados = reprovados;
    }
}


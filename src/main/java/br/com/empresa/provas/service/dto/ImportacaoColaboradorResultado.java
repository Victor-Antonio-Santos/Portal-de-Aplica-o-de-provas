package br.com.empresa.provas.service.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportacaoColaboradorResultado {

    private int totalProcessado;
    private int totalSucesso;
    private int totalErro;
    private List<ImportacaoColaboradorLinhaResultado> linhas = new ArrayList<ImportacaoColaboradorLinhaResultado>();

    public int getTotalProcessado() {
        return totalProcessado;
    }

    public void setTotalProcessado(int totalProcessado) {
        this.totalProcessado = totalProcessado;
    }

    public int getTotalSucesso() {
        return totalSucesso;
    }

    public void setTotalSucesso(int totalSucesso) {
        this.totalSucesso = totalSucesso;
    }

    public int getTotalErro() {
        return totalErro;
    }

    public void setTotalErro(int totalErro) {
        this.totalErro = totalErro;
    }

    public List<ImportacaoColaboradorLinhaResultado> getLinhas() {
        return linhas;
    }

    public void setLinhas(List<ImportacaoColaboradorLinhaResultado> linhas) {
        this.linhas = linhas;
    }
}


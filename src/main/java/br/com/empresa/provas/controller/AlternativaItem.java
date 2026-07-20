package br.com.empresa.provas.controller;

import java.io.Serializable;

public class AlternativaItem implements Serializable {

    private Long id;
    private String texto;
    private boolean correta;

    public AlternativaItem() {
    }

    public AlternativaItem(Long id, String texto, boolean correta) {
        this.id = id;
        this.texto = texto;
        this.correta = correta;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public boolean isCorreta() {
        return correta;
    }

    public void setCorreta(boolean correta) {
        this.correta = correta;
    }
}


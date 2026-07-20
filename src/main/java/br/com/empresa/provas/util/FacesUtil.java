package br.com.empresa.provas.util;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import java.io.IOException;

public final class FacesUtil {

    private FacesUtil() {
    }

    public static void addInfoMessage(String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, mensagem, mensagem));
    }

    public static void addErrorMessage(String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, mensagem, mensagem));
    }

    public static void redirect(String pagina) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        try {
            externalContext.redirect(externalContext.getRequestContextPath() + pagina);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao redirecionar.", e);
        }
    }
}


package br.com.empresa.provas.controller;

import br.com.empresa.provas.entity.Turma;
import br.com.empresa.provas.service.ImportacaoColaboradorService;
import br.com.empresa.provas.service.TurmaService;
import br.com.empresa.provas.service.dto.ImportacaoColaboradorResultado;
import br.com.empresa.provas.util.FacesUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

@Named("importacaoColaboradorBean")
@ViewScoped
public class ImportacaoColaboradorBean implements Serializable {

    private final ImportacaoColaboradorService importacaoColaboradorService = new ImportacaoColaboradorService();
    private final TurmaService turmaService = new TurmaService();

    @Inject
    private SessionBean sessionBean;

    private Long turmaId;
    private String senhaPadrao;
    private UploadedFile arquivo;
    private ImportacaoColaboradorResultado resultado;
    private List<Turma> turmas;

    @PostConstruct
    public void init() {
        turmas = turmaService.listarPorResponsavel(sessionBean == null ? null : sessionBean.getUsuarioLogado());
    }

    public void importar() {
        try {
            if (arquivo == null || arquivo.getFileName() == null || arquivo.getFileName().trim().isEmpty()) {
                throw new IllegalArgumentException("Selecione a planilha de colaboradores.");
            }
            try (InputStream inputStream = arquivo.getInputStream()) {
                resultado = importacaoColaboradorService.importar(inputStream, turmaId, senhaPadrao,
                        sessionBean == null ? null : sessionBean.getUsuarioLogado());
            }
            FacesUtil.addInfoMessage("Importacao concluida. " + resultado.getTotalSucesso()
                    + " colaborador(es) processado(s) com sucesso.");
            arquivo = null;
        } catch (IllegalArgumentException e) {
            FacesUtil.addErrorMessage(e.getMessage());
        } catch (IOException e) {
            FacesUtil.addErrorMessage("Nao foi possivel ler o arquivo enviado.");
        }
    }

    public boolean isTemResultado() {
        return resultado != null && resultado.getTotalProcessado() > 0;
    }

    public StreamedContent getModeloPlanilha() {
        try {
            byte[] arquivoModelo = gerarModeloPlanilha();
            return DefaultStreamedContent.builder()
                    .name("modelo-colaboradores.xls")
                    .contentType("application/vnd.ms-excel")
                    .stream(() -> new ByteArrayInputStream(arquivoModelo))
                    .build();
        } catch (IOException e) {
            FacesContext.getCurrentInstance().validationFailed();
            FacesUtil.addErrorMessage("Nao foi possivel gerar o arquivo modelo.");
            return null;
        }
    }

    public Long getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(Long turmaId) {
        this.turmaId = turmaId;
    }

    public String getSenhaPadrao() {
        return senhaPadrao;
    }

    public void setSenhaPadrao(String senhaPadrao) {
        this.senhaPadrao = senhaPadrao;
    }

    public UploadedFile getArquivo() {
        return arquivo;
    }

    public void setArquivo(UploadedFile arquivo) {
        this.arquivo = arquivo;
    }

    public ImportacaoColaboradorResultado getResultado() {
        return resultado;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public List<Turma> getTurmas() {
        return turmas;
    }

    private byte[] gerarModeloPlanilha() throws IOException {
        Workbook workbook = new HSSFWorkbook();
        try {
            Sheet sheet = workbook.createSheet("Colaboradores");
            Row cabecalho = sheet.createRow(0);
            cabecalho.createCell(0).setCellValue("Nome");
            cabecalho.createCell(1).setCellValue("CPF");
            cabecalho.createCell(2).setCellValue("E-mail");
            cabecalho.createCell(3).setCellValue("Senha");
            cabecalho.createCell(4).setCellValue("Turma");

            Row exemplo = sheet.createRow(1);
            exemplo.createCell(0).setCellValue("Maria da Silva");
            exemplo.createCell(1).setCellValue("123.456.789-09");
            exemplo.createCell(2).setCellValue("maria.silva@empresa.com");
            exemplo.createCell(3).setCellValue("SenhaInicial123");
            exemplo.createCell(4).setCellValue("D2");

            for (int coluna = 0; coluna < 5; coluna++) {
                sheet.autoSizeColumn(coluna);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } finally {
            workbook.close();
        }
    }
}


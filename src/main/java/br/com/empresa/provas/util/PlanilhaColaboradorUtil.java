package br.com.empresa.provas.util;

import br.com.empresa.provas.service.dto.ImportacaoColaboradorLinhaDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class PlanilhaColaboradorUtil {

    private PlanilhaColaboradorUtil() {
    }

    public static List<ImportacaoColaboradorLinhaDTO> ler(InputStream inputStream) {
        List<ImportacaoColaboradorLinhaDTO> linhas = new ArrayList<ImportacaoColaboradorLinhaDTO>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new IllegalArgumentException("A planilha enviada nao possui abas.");
            }

            for (int indice = 1; indice <= sheet.getLastRowNum(); indice++) {
                Row row = sheet.getRow(indice);
                if (row == null || linhaVazia(row, formatter)) {
                    continue;
                }

                ImportacaoColaboradorLinhaDTO linha = new ImportacaoColaboradorLinhaDTO();
                linha.setNumeroLinha(indice + 1);
                linha.setNome(valor(row.getCell(0), formatter));
                linha.setCpf(valor(row.getCell(1), formatter));
                linha.setEmail(valor(row.getCell(2), formatter));
                linha.setSenha(valor(row.getCell(3), formatter));
                linha.setTurma(valor(row.getCell(4), formatter));
                linhas.add(linha);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Nao foi possivel ler a planilha. Verifique se o arquivo esta em formato XLS ou XLSX.", e);
        }

        if (linhas.isEmpty()) {
            throw new IllegalArgumentException("Nenhum colaborador valido foi encontrado na planilha.");
        }
        return linhas;
    }

    private static boolean linhaVazia(Row row, DataFormatter formatter) {
        return valor(row.getCell(0), formatter).isEmpty()
                && valor(row.getCell(1), formatter).isEmpty()
                && valor(row.getCell(2), formatter).isEmpty()
                && valor(row.getCell(3), formatter).isEmpty()
                && valor(row.getCell(4), formatter).isEmpty();
    }

    private static String valor(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }
}


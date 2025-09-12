package com.fullcars.restapi.service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fullcars.restapi.model.ProviderMapping;
import com.fullcars.restapi.model.ProviderPart;

public class FileMapper {

    public static List<ProviderPart> mapFile(InputStream inputStream, ProviderMapping mapping, String filename) throws IOException {
        if (filename.endsWith(".csv")) {
            return leerCSV(inputStream, mapping);
        } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            return leerExcel(inputStream, filename, mapping);
        } else {
            throw new IllegalArgumentException("Tipo de archivo no soportado: " + filename);
        }
    }

    private static List<ProviderPart> leerExcel(InputStream inputStream, String filename, ProviderMapping mapping) throws IOException {
        List<ProviderPart> partes = new ArrayList<>();
        Workbook workbook = filename.endsWith(".xlsx") ? new XSSFWorkbook(inputStream) : new HSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        int nameIdx = columnLetterToIndex(mapping.getNameColumn());
        int brandIdx = columnLetterToIndex(mapping.getBrandColumn());
        int priceIdx = columnLetterToIndex(mapping.getPriceColumn());

        boolean primeraLinea = true;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (primeraLinea) { primeraLinea = false; continue; }
            if (isRowEmpty(row)) continue;

            ProviderPart parte = new ProviderPart();
            parte.setProviderMapping(mapping);
            parte.setNombre(getCellValueAsString(row.getCell(nameIdx)));
            parte.setMarca(getCellValueAsString(row.getCell(brandIdx)));
            parte.setPrecio(getCellValueAsBigDecimal(row.getCell(priceIdx)));
            partes.add(parte);
        }

        workbook.close();
        return partes;
    }

    private static List<ProviderPart> leerCSV(InputStream inputStream, ProviderMapping mapping) throws IOException {
        List<ProviderPart> partes = new ArrayList<>();

        int nameIdx = columnLetterToIndex(mapping.getNameColumn());
        int brandIdx = columnLetterToIndex(mapping.getBrandColumn());
        int priceIdx = columnLetterToIndex(mapping.getPriceColumn());

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean primeraLinea = true;

            while ((line = br.readLine()) != null) {
                if (primeraLinea) { primeraLinea = false; continue; }
                if (line.isBlank()) continue;

                String[] columnas = line.split(";|,");
                if (columnas.length <= Math.max(nameIdx, Math.max(brandIdx, priceIdx))) continue;

                ProviderPart parte = new ProviderPart();
                parte.setProviderMapping(mapping);
                parte.setNombre(columnas[nameIdx].trim());
                parte.setMarca(columnas[brandIdx].trim());
                parte.setPrecio(new BigDecimal(columnas[priceIdx].trim()));
                partes.add(parte);
            }
        }

        return partes;
    }

    // ------------------------------------------------------
    // Helpers
    // ------------------------------------------------------
    private static boolean isRowEmpty(Row row) {
        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && !getCellValueAsString(cell).isBlank()) return false;
        }
        return true;
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }

    private static BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;
        switch (cell.getCellType()) {
            case NUMERIC: return BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING:
                try { return new BigDecimal(cell.getStringCellValue()); }
                catch (NumberFormatException e) { return BigDecimal.ZERO; }
            default: return BigDecimal.ZERO;
        }
    }

    /**
     * Convierte letras de columna Excel (A, B, ..., Z, AA, AB, ...) a índice numérico (0,1,...)
     */
    public static int columnLetterToIndex(String letter) {
        if (letter == null || letter.isBlank()) return 0;
        letter = letter.toUpperCase();
        int index = 0;
        for (int i = 0; i < letter.length(); i++) {
            index *= 26;
            index += letter.charAt(i) - 'A' + 1;
        }
        return index - 1; // ajuste a índice 0
    }
}

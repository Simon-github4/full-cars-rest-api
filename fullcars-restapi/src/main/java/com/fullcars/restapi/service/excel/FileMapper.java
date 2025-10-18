package com.fullcars.restapi.service.excel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import com.fullcars.restapi.model.ProviderMapping;
import com.fullcars.restapi.model.ProviderPart;
import com.monitorjbl.xlsx.StreamingReader;

@Component
public class FileMapper {

	private static final int batchSize = 500;
    private final static DataFormatter dataFormatter = new DataFormatter();

	public void mapFile(File tempFile, ProviderMapping mapping, Consumer<List<ProviderPart>> batchConsumer) throws Exception {
		String extension = tempFile.getName().substring(tempFile.getName().lastIndexOf('.'));
		System.err.println(extension);
		if (extension.endsWith(".csv")) {
			mapCSV(tempFile, mapping, batchConsumer);
		} else if (extension.endsWith(".xlsx") || extension.endsWith(".xls")) {
			mapStreaming(tempFile, mapping, batchConsumer);
		} else {
			throw new IllegalArgumentException("Tipo de archivo no soportado: " + extension);
		}
	}

	public void mapStreaming(File tempFile, ProviderMapping mapping, Consumer<List<ProviderPart>> batchConsumer)
			throws Exception {

		String filename = tempFile.getName().toLowerCase();
		int batchSize = 500;
		List<ProviderPart> batch = new ArrayList<>(batchSize);

	    try (InputStream is = new FileInputStream(tempFile)) {
	        Workbook workbook = null;

	        // Intento inicial según extensión
	        try {
	            if (filename.endsWith(".xls")) {
	                workbook = new HSSFWorkbook(is);
	            } else if (filename.endsWith(".xlsx")) {
	                workbook = StreamingReader.builder()
	                        .rowCacheSize(100)
	                        .bufferSize(4096)
	                        .open(is);
	            } else {
	                throw new IllegalArgumentException("Formato de archivo no soportado: " + filename);
	            }
	        } catch (NotOLE2FileException | NotOfficeXmlFileException | ZipException e1) {
	            System.err.println("Archivo no coincide con su extensión, intentando abrir con otro formato...");

	            // Reabrir InputStream para intentar con el otro tipo
	            try (InputStream is2 = new FileInputStream(tempFile)) {
	                if (filename.endsWith(".xls")) {
	                    workbook = StreamingReader.builder()
	                            .rowCacheSize(100)
	                            .bufferSize(4096)
	                            .open(is2);
	                } else if (filename.endsWith(".xlsx")) {
	                    workbook = new HSSFWorkbook(is2);
	                }
	            } catch (Exception e2) {
	                // Si falla de nuevo, entonces es corrupto
	                throw new IllegalArgumentException("Archivo Excel corrupto o inválido: " + filename, e2);
	            }
	        }
	        
			Sheet sheet = workbook.getSheetAt(0);
			int nameIdx = columnLetterToIndex(mapping.getNameColumn());
			int brandIdx = columnLetterToIndex(mapping.getBrandColumn());
			int priceIdx = columnLetterToIndex(mapping.getPriceColumn());
			int catIdx = columnLetterToIndex(mapping.getCategoryColumn());
			//int qualIdx = columnLetterToIndex(mapping.getQualityColumn());
			int provCodIdx = columnLetterToIndex(mapping.getProvCodColumn());

			boolean primeraLinea = true;

			for (Row row : sheet) {
				if (primeraLinea) {
					primeraLinea = false;
					continue;
				}
				String name = getCellValueAsString(row.getCell(nameIdx)).trim();
			    String price = getCellValueAsString(row.getCell(priceIdx)).trim();
				if (!name.isBlank() || !price.isBlank()) {
					String brand = getCellValueAsString(row.getCell(brandIdx)).trim();
					
					ProviderPart parte = new ProviderPart();
					parte.setProviderMapping(mapping);
					parte.setNombre(name);
					parte.setMarca(brand);
					parte.setPrecio(getCellValueAsBigDecimal(row.getCell(priceIdx)));
					parte.setCategory(getCellValueAsString(row.getCell(catIdx)));
					//parte.setQuality(getCellValueAsString(row.getCell(qualIdx)));
					parte.setProvCod(getCellValueAsString(row.getCell(provCodIdx)));
					
					batch.add(parte);
	
					if (batch.size() >= batchSize) {
						batchConsumer.accept(new ArrayList<>(batch));
						batch.clear();
					}
				}
			}

			if (!batch.isEmpty()) {
				batchConsumer.accept(new ArrayList<>(batch));
				batch.clear();
			}
	    }
	}
	
	private void mapCSV(File tempFile, ProviderMapping mapping, Consumer<List<ProviderPart>> batchConsumer)throws IOException {
	    List<ProviderPart> batch = new ArrayList<>(batchSize);

	    int nameIdx = columnLetterToIndex(mapping.getNameColumn());
	    int brandIdx = columnLetterToIndex(mapping.getBrandColumn());
	    int priceIdx = columnLetterToIndex(mapping.getPriceColumn());
	    int catIdx = columnLetterToIndex(mapping.getCategoryColumn());
	    //int qualIdx = columnLetterToIndex(mapping.getQualityColumn());
	    int provCodIdx = columnLetterToIndex(mapping.getProvCodColumn());

	    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile), StandardCharsets.UTF_8))) {

	        String line;
	        int lineNumber = 0;

	        while ((line = br.readLine()) != null) {
	            lineNumber++;
	            if (line.isBlank()) continue;

	            String[] columnas = line.split(";|,");
	            if (columnas.length <= Math.max(nameIdx, Math.max(brandIdx, priceIdx))) continue;

	            // Validar que la columna precio sea numérica
	            String precioStr = columnas[priceIdx].trim().replace(",", "."); // por si viene con coma
	            if (!precioStr.matches("-?\\d+(\\.\\d+)?")) {
	                System.out.println("⚠ Línea " + lineNumber + " ignorada (precio no numérico): " + precioStr);
	                continue; // saltar encabezados u otras líneas no válidas
	            }

	            try {
	                ProviderPart parte = new ProviderPart();
	                parte.setProviderMapping(mapping);
	                parte.setNombre(getSafe(columnas, nameIdx));
	                parte.setMarca(getSafe(columnas, brandIdx));
	                parte.setPrecio(new BigDecimal(precioStr));
	                parte.setCategory(getSafe(columnas, catIdx));
	                //parte.setQuality(getSafe(columnas, qualIdx));
	                parte.setProvCod(getSafe(columnas, provCodIdx));

	                batch.add(parte);
	            } catch (Exception e) {
	                System.err.println("⚠ Error procesando línea " + lineNumber + ": " + e.getMessage());
	            }

	            if (batch.size() >= batchSize) {
	                batchConsumer.accept(new ArrayList<>(batch)); // pasa copia
	                batch.clear();
	            }
	        }

	        if (!batch.isEmpty()) {
	            batchConsumer.accept(new ArrayList<>(batch));
	        }
	    }
	}

	private static String getSafe(String[] arr, int idx) {
	    return (idx >= 0 && idx < arr.length) ? arr[idx] : "";
	}


    // ------------------------------------------------------
    // Helpers
    // ------------------------------------------------------
	private static boolean isRowEmpty(Row row) {
	    if (row == null) return true;
	    for (int c = 0; c < row.getLastCellNum(); c++) {
	        Cell cell = row.getCell(c);
	        if (cell != null && !getCellValueAsString(cell).isBlank()) {
	            return false; // Encontró algo
	        }
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

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return BigDecimal.valueOf(cell.getNumericCellValue());

                case STRING:
                    String value = cell.getStringCellValue().trim();
                    if (value.isEmpty()) return BigDecimal.ZERO;

                    // Manejo estilo europeo / separador decimal
                    if (value.contains(",") && value.contains(".")) {
                        value = value.replace(".", "").replace(",", ".");
                    } else if (value.contains(",") && !value.contains(".")) {
                        value = value.replace(",", ".");
                    }
                    return new BigDecimal(value);

                case FORMULA:
                    // si querés, podés intentar evaluar la fórmula a numérico
                    try {
                        return BigDecimal.valueOf(cell.getNumericCellValue());
                    } catch (Exception e) {
                        return BigDecimal.ZERO;
                    }
                case BLANK:
                case BOOLEAN:
                case ERROR:
                default:
                    return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
/*
    private static BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;
         
        //DataFormatter lee el valor tal como lo muestra Excel
        String value = dataFormatter.formatCellValue(cell).trim();

        if (value.isEmpty()) return BigDecimal.ZERO;

        try {
            // Caso 1: estilo europeo -> "40.000,00"
            if (value.contains(",") && value.contains(".")) {
                value = value.replace(".", "");   // quita separador de miles
                value = value.replace(",", "."); // cambia decimal a punto
            }
            // Caso 2: solo coma decimal -> "123,45"
            else if (value.contains(",") && !value.contains(".")) 
                value = value.replace(",", ".");
            // Caso 3: solo punto decimal -> "12345.67" (no cambia nada)
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }*/

    //Convierte letras de columna Excel (A, B, ..., Z, AA, AB, ...) a índice numérico (0,1,...)
    public static int columnLetterToIndex(String letter) {
        if (letter == null || letter.isBlank()) return -1;
        letter = letter.toUpperCase();
        int index = 0;
        for (int i = 0; i < letter.length(); i++) {
            index *= 26;
            index += letter.charAt(i) - 'A' + 1;
        }
        return index - 1; // ajuste a índice 0
    }
}

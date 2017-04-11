package ru.kabor.demand.prediction.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.monitorjbl.xlsx.StreamingReader;

import ru.kabor.demand.prediction.service.RequestServiceImpl;
import ru.kabor.demand.prediction.utils.exceptions.InvalidHeaderException;

/** It contains methods for working with MS Excel */
public class ExcelUtils {

	private static final Logger LOG = LoggerFactory.getLogger(RequestServiceImpl.class);

	/** List for validating csv headers*/
	public static List<String> validHeaders = new ArrayList<>();
	public static List<String> validHeadersPrices = new ArrayList<>();

	static {
		validHeaders.add(0, "whs_id");
		validHeaders.add(1, "art_id");
		validHeaders.add(2, "day_id");
		validHeaders.add(3, "sale_qnty");
		
		validHeadersPrices.add(0, "whs_id");
		validHeadersPrices.add(1, "art_id");
		validHeadersPrices.add(2, "day_id");
		validHeadersPrices.add(3, "sale_qnty");
		validHeadersPrices.add(4, "price");
	}

	/** Validation xls file headers
	 *
	 * @param file
	 * @param request_TYPE 
	 * @throws Exception
     */
	public static void validateXLSHeaders(MultipartFile file, String requestType) throws InvalidHeaderException {
		Workbook workbook = null;
		try {
			workbook = WorkbookFactory.create(file.getInputStream());
			Row headerRow = workbook.getSheetAt(0).getRow(0);
			List<String> requestHeaders = new ArrayList<>();

			if (headerRow == null) {
				throw new InvalidHeaderException("Excel file is empty");
			}

			if(requestType.equals(ConstantUtils.REQUEST_TYPE_FORECAST)){
				for (int i = 0; i < validHeaders.size(); i++) {
					requestHeaders.add(readValueFromXls(workbook, headerRow, i));
				}
				
    			if (!validHeaders.equals(requestHeaders)) {
    				LOG.error("Invalid header in Excel File:" + requestHeaders);
    				throw new InvalidHeaderException("Invalid header in Excel File:" + requestHeaders);
    			}
			} else {
				for (int i = 0; i < validHeadersPrices.size(); i++) {
					requestHeaders.add(readValueFromXls(workbook, headerRow, i));
				}

				
    			if (!validHeadersPrices.equals(requestHeaders)) {
    				LOG.error("Invalid header in Excel File:" + requestHeaders);
    				throw new InvalidHeaderException("Invalid header in Excel File:" + requestHeaders);
    			}
			}
			
		} catch (InvalidFormatException | IOException e) {
			throw new InvalidHeaderException("Invalid header in Excel File:" + e.toString());
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					LOG.error("Can't close workbook:");
				}
			}
		}
	}
	
	/** Validation xlsx file headers
	 *
	 * @param file
	 * @param request_TYPE 
	 * @throws Exception
    */
	public static void validateXLSXHeaders(MultipartFile file, String requestType) throws InvalidHeaderException {
		StreamingReader reader = null;
		try {
			reader = StreamingReader.builder()
			        .rowCacheSize(100)
			        .bufferSize(4096)
			        .sheetIndex(0) 
			        .read(file.getInputStream());
			
			List<String> requestHeaders = new ArrayList<>();
			Iterator<Row> rowIterator = reader.iterator();

			if (rowIterator.hasNext() == false) {
				throw new InvalidHeaderException("Excel file is empty");
			}
			
			Row row = rowIterator.next();
			for (Cell cellInRow : row) {
				String cellValue = readCellWithoutFormulas(cellInRow,null);
			    requestHeaders.add(cellValue);
			}
			
			if(requestType.equals(ConstantUtils.REQUEST_TYPE_FORECAST)){
				requestHeaders.remove("price");
    			if (!validHeaders.equals(requestHeaders)) {
    				LOG.error("Invalid header in Excel File:" + requestHeaders);
    				throw new InvalidHeaderException("Invalid header in Excel File:" + requestHeaders);
    			}
			} else {
    			if (!validHeadersPrices.equals(requestHeaders)) {
    				LOG.error("Invalid header in Excel File:" + requestHeaders);
    				throw new InvalidHeaderException("Invalid header in Excel File:" + requestHeaders);
    			}
			}
			
		} catch (IOException e) {
			throw new InvalidHeaderException("Invalid header in Excel File:" + e.toString());
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	/** Find column in row with  by content*/
	public static Integer findColumnNumberByContent(Workbook workbook, Sheet sheet, Integer rowNumber, String content) {
		if(content!=null){
			content = content.trim();
			content = content.toLowerCase();
		}
		
		Row row = sheet.getRow(rowNumber);
		Integer currentColumnNumber = 0;
		do {
			String currentCellContent = ExcelUtils.readValueFromXls(workbook, row, currentColumnNumber);
			if (currentCellContent != null && currentCellContent.trim().toLowerCase().equals(content)) {
				return currentColumnNumber;
			}
			currentColumnNumber++;
		} while (currentColumnNumber < 120);
		return null;
	}

	/** Reading cell from Excel workbook */
	public static String readValueFromXls(Workbook workbook, Row row, Integer columnNumber) {
		return readValueFromXls(workbook, row, columnNumber, null);
	}

	/** Reading cell froxm Excel workbook */
	@SuppressWarnings("deprecation")
	public static String readValueFromXls(Workbook workbook, Row row, Integer columnNumber, SimpleDateFormat dateFormat) {
		if (workbook == null || row == null) {
			return null;
		}
		String cellValue = "";
		Cell cell = row.getCell(columnNumber, Row.CREATE_NULL_AS_BLANK);
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			cellValue = cell.getStringCellValue();
			break;
		case Cell.CELL_TYPE_NUMERIC:
			if ( dateFormat != null && HSSFDateUtil.isCellDateFormatted(cell)) {
				Date cellDate = cell.getDateCellValue();
				if (cellDate != null) {
					cellValue = dateFormat.format(cellDate);
				}
			} else {
				cellValue = formatDouble(cell.getNumericCellValue());
			}
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			cellValue = String.valueOf(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_FORMULA:
			try {
				CellValue currentCellValue = evaluator.evaluate(cell);
				if (cell.getCachedFormulaResultType() == Cell.CELL_TYPE_STRING) {
					cellValue = String.valueOf(cell.getStringCellValue());
				} else if (cell.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC) {
					String formated = currentCellValue.formatAsString();
					formated = formated.replace("\"", "");
					cellValue = formated;
				} else if (cell.getCachedFormulaResultType() == Cell.CELL_TYPE_BOOLEAN) {
					String formated = currentCellValue.formatAsString();
					formated = formated.replace("\"", "");
					cellValue = formated;
				}
				break;
			} catch (Exception e) {
				//Try to recognize even with exception
				if (cell.getCachedFormulaResultType() == Cell.CELL_TYPE_STRING) {
					cellValue = String.valueOf(cell.getStringCellValue());
				} else if (cell.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC) {
					cellValue = String.valueOf(cell.getNumericCellValue());
				} else if (cell.getCachedFormulaResultType() == Cell.CELL_TYPE_BOOLEAN) {
					cellValue = String.valueOf(cell.getBooleanCellValue());
				}
				break;
			}
		}
		cellValue = cellValue.replace('\n', ' ');
		cellValue = cellValue.replace('\r', ' ');
		return cellValue;
	}
	
	@SuppressWarnings("deprecation")
	public static String readCellWithoutFormulas(Cell cell, SimpleDateFormat dateFormat){
		if (cell == null) {
			return "";
		}
		String cellValue = "";
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			cellValue = cell.getStringCellValue();
			break;
		case Cell.CELL_TYPE_NUMERIC:
			if ( dateFormat != null && HSSFDateUtil.isCellDateFormatted(cell)) {
				Date cellDate = cell.getDateCellValue();
				if (cellDate != null) {
					cellValue = dateFormat.format(cellDate);
				}
			} else {
				cellValue = formatDouble(cell.getNumericCellValue());
			}
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			cellValue = String.valueOf(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_FORMULA:
			return "";
		}
		cellValue = cellValue.replace('\n', ' ');
		cellValue = cellValue.replace('\r', ' ');
		return cellValue;
	}
	
	/** Saving workbook to xls file*/
	public static void saveXls(String folderPath, String fileName, Workbook workbook) throws IOException {
		FileOutputStream fileOutputStream = null;
		String filePath = "";
		try {
			filePath = folderPath + File.separator + fileName;
			fileOutputStream = new FileOutputStream(filePath);
			workbook.write(fileOutputStream);
		} catch (FileNotFoundException e) {
			LOG.error("Can't update Excel file. It' possible that file is already open", e);
			throw new IOException("Can't update Excel file. It' possible that file is already open:" + filePath);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					LOG.error("Can't save Excel file.", e);
				}
			}
			try {
				workbook.close();
			} catch (IOException e) {
				LOG.error("Can't close Excel file", e);
			}
		}
	}

	/** From double to string */
	private static String formatDouble(double d) {
		String result = "";
		if (d == (long) d) {
			result = String.format("%d", (long) d);
		} else {
			result = String.format("%.2f", d);
		}
		result = result.replace(",", ".");
		return result;
	}
	
}

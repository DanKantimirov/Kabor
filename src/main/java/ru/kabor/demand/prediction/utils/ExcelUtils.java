package ru.kabor.demand.prediction.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;


public class ExcelUtils {

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

	/** Reading cell from Excel workbook */
	@SuppressWarnings("deprecation")
	public static String readValueFromXls(Workbook workbook, Row row, Integer columnNumber, SimpleDateFormat dateFormat) {
		String cellValue = "";
		Cell cell = row.getCell(columnNumber, Row.CREATE_NULL_AS_BLANK);
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			cellValue = cell.getStringCellValue();
			break;
		case Cell.CELL_TYPE_NUMERIC:
			if ( !dateFormat.equals(null) && HSSFDateUtil.isCellDateFormatted(cell)) {
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
	
	/** Saving workbook to xls file*/
	public static void saveXls(String folderPath, String fileName, Workbook workbook) throws IOException {
		FileOutputStream fileOutputStream = null;
		String filePath = "";
		try {
			filePath = folderPath + File.separator + fileName;
			fileOutputStream = new FileOutputStream(filePath);
			workbook.write(fileOutputStream);
		} catch (FileNotFoundException e) {
			throw new IOException("Can't update Excel file. It' possible that file is already open:" + filePath);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					new IOException("Can't save Excel file:" + e.toString());
				}
			}
			try {
				workbook.close();
			} catch (IOException e) {
				new IOException("Can't close Excel file:" + e.toString());
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
		result = result.replace(".", ",");
		return result;
	}
	
	/** Place jpeg image into cell 
	 * @throws IOException */
	
	public static void insetJpegImageToCell(String imagePath, Integer rowNumber, Integer columnNumber, Workbook workbook, Sheet sheet) throws IOException  {
		URL url = null;
		if (imagePath == null || imagePath.trim().equals("")) {
			return;
		}
		try {
			imagePath = imagePath.replace('\\', '/');
			url = new URL("file:" + imagePath); //	url = new URL("file:/E:/project-logo3.jpg");
			
			// enlarge image cell
	        sheet.setColumnWidth(columnNumber, 5000);
	        sheet.getRow(rowNumber).setHeightInPoints(100);
	 
	        // read image into memory
	        ByteArrayOutputStream img_bytes = new ByteArrayOutputStream();
	        img_bytes = resizeImage(url,250,250);
	        if(img_bytes!=null && img_bytes.size()>0){
    	        ClientAnchor anchor = new HSSFClientAnchor(0, 0, 0, 0, (short)(columnNumber+0), rowNumber,
                        (short) (columnNumber+1), rowNumber+1);
                int index = workbook.addPicture(img_bytes.toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG);
                Drawing patriarch = sheet.createDrawingPatriarch();
                patriarch.createPicture(anchor, index);
                anchor.setAnchorType(org.apache.poi.ss.usermodel. ClientAnchor.AnchorType.MOVE_DONT_RESIZE );
	        }
		} catch (IOException e) {
			throw new IOException("Can't insert image to cell");
		} 
	}
	
	/** Resize image */
	public static ByteArrayOutputStream resizeImage(URL url, Integer hight, Integer width) throws IOException {
		if (url == null) {
			return null;
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			BufferedImage originalImage = ImageIO.read(url);

			originalImage = Scalr.resize(originalImage, 
										Method.SPEED, 
										Mode.FIT_TO_WIDTH, 
										hight, width, Scalr.OP_ANTIALIAS);
			ImageIO.write(originalImage, "png", byteArrayOutputStream);
			byteArrayOutputStream.flush();
			return byteArrayOutputStream;
		} catch (IOException e) {
			throw new IOException("Can't read image from url:" + url.toString());
		} finally{
			if(byteArrayOutputStream!=null){
				byteArrayOutputStream.close();
			}
		}
	}
}

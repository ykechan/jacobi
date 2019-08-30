package jacobi.test.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.IntFunction;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import jacobi.api.classifier.Column;

public class JacobiDataDef {
	
	public JacobiDataDef(Workbook workbook, List<Class<?>> enumTypes) {
		this.workbook = workbook;
		this.enumTypes = enumTypes;
	}
	
	public Map<Integer, Column<?>> getColumnDefs(String sheetName) {
		Sheet sheet = this.workbook.getSheet(sheetName);
		if(sheet == null){
			throw new IllegalArgumentException("Sheet " + sheetName + " not found.");
		}
		
		return this.readColumnDefs(sheet, this.readTypeDefs(sheet));
	}
	
	protected Map<Integer, Column<?>> readColumnDefs(
			Sheet sheet, 
			Map<String, IntFunction<Column<?>>> typedefs) {
		
		Map<Integer, Column<?>> columnDefs = new TreeMap<>();
		for(int i = sheet.getFirstRowNum(); i < sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			if(row == null 
			|| row.getFirstCellNum() < 0
			|| !"column-def".equals(this.getString(row.getCell(0)))) {
				continue;
			}
			
			try {
				int index = (int) Double.parseDouble(this.getString(row.getCell(1)));
				String typeName = this.getString(row.getCell(2));
				
				if(columnDefs.containsKey(index)) {
					throw new IllegalArgumentException("Duplicated column def of #" +index);
				}
				
				if(!typedefs.containsKey(typeName)){
					throw new IllegalArgumentException("Type " + typeName + " not found.");
				}
				
				columnDefs.put(index, typedefs.get(typeName).apply(index));
			}catch(NumberFormatException ex){
				throw new IllegalArgumentException("Invalid column index at row #"
						+ row.getRowNum(), ex);
			}
		}
		return columnDefs;
	}
	
	protected Map<String, IntFunction<Column<?>>> readTypeDefs(Sheet sheet) {
		Map<String, IntFunction<Column<?>>> typedefs = new TreeMap<>();
		
		int k = sheet.getFirstRowNum();
		while(k <= sheet.getLastRowNum()) {
			Row row = sheet.getRow(k++);
			if(row == null || row.getFirstCellNum() < 0) {
				continue;
			}
			
			String anchor = this.getString(row.getCell(0));
			if(!"typedef".equals(anchor.trim())){
				continue;
			}
			
			String typeName = this.getString(row.getCell(1));
			
			if(typeName.trim().isEmpty()) {
				throw new IllegalArgumentException("Unable to define type with no name on row #" 
					+ row.getRowNum());
			}
			
			List<String> items = this.getItems(sheet, k);
			typedefs.put(typeName, this.columnOf(items));
			k += items.size();
		}
		
		return typedefs;
	}
	
	protected List<String> getItems(Sheet sheet, int begin) {
		Map<Integer, String> map = new TreeMap<>();
		for(int i = begin; i < sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			if(row == null || row.getFirstCellNum() < 0){
				break;
			}
			
			String item = this.getString(row.getCell(0));
			if(item.trim().isEmpty()){
				break;
			}			
			
			String nom = this.getString(row.getCell(1));
			
			try {
				int value = (int) Double.parseDouble(nom);
				if(map.containsKey(value)){
					throw new IllegalArgumentException("Duplicated nomminal value " + value);
				}
				
				map.put(value, item.toUpperCase());
			}catch(NumberFormatException ex){
				throw new IllegalArgumentException("Invalid nominal value " + nom);
			}
		}
		
		for(int i = 0; i < map.size(); i++) {
			if(!map.containsKey(i)){
				throw new IllegalArgumentException("Nominal value " + i + " is undefined.");
			}
		}
		return Arrays.asList(map.values().toArray(new String[0]));
	}
	
	protected IntFunction<Column<?>> columnOf(List<String> items) {
		System.out.println("items = " + items);
		if(items.size() == 2
		&& Boolean.FALSE.toString().equalsIgnoreCase(items.get(0))
		&& Boolean.TRUE.toString().equalsIgnoreCase(items.get(1))){
			return n -> Column.signed(n);
		}
		
		Class<?> clazz = this.enumTypes.stream()
			.filter(t -> t.isEnum())
			.filter(t -> t.getEnumConstants().length == items.size())
			.filter(t -> {
				Object[] enumVals = t.getEnumConstants();
				for(int i = 0; i < enumVals.length; i++){
					if(!items.get(i).equalsIgnoreCase(enumVals[i].toString())){
						System.out.println(items.get(i) + " <> " + enumVals[i]);
						return false;
					}
				}
				return true;
			})
			.findAny()
			.orElse(String.class);
		System.out.println("Type = " + clazz);
		
		return clazz == String.class
			? n -> new Column<>(n, items, v -> (int) v)
			: n -> Column.of(n, clazz);
	}
	
	protected String getString(Cell cell) {
		if(cell == null){
			return "";
		}
		
		return this.getString(cell, 
			cell.getCellType() == Cell.CELL_TYPE_FORMULA
			? cell.getCachedFormulaResultType()
			: cell.getCellType() 
		);
	}
	
	protected String getString(Cell cell, int cellType) {
		switch(cellType){
			case Cell.CELL_TYPE_BLANK :
			case Cell.CELL_TYPE_ERROR :
				return "";
				
			case Cell.CELL_TYPE_BOOLEAN :
				return Boolean.toString(cell.getBooleanCellValue());
				
			case Cell.CELL_TYPE_NUMERIC :
				return Double.toString(cell.getNumericCellValue());				
			
			case Cell.CELL_TYPE_STRING :
				return cell.getStringCellValue();
				
			default :
				break;
		}
		throw new UnsupportedOperationException("Type " + cellType + " not supported");
	}
	
	private Workbook workbook;
	private List<Class<?>> enumTypes;
}

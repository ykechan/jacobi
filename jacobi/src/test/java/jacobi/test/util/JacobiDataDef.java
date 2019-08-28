/* 
 * The MIT License
 *
 * Copyright 2019 Y.K. Chan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jacobi.test.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.IntFunction;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import jacobi.api.classifier.Column;
import jacobi.core.util.Throw;

public class JacobiDataDef {
		
	public JacobiDataDef(Workbook workbook, List<Class<?>> enumTypes) {
		this.workbook = workbook;
		this.enumTypes = enumTypes;
	}
	
	public BiFunction<String, Integer, Column<?>> getDef(String name) {
		Sheet sheet = this.workbook.getSheet(name);
		Throw.when()
	        .isNull(() -> sheet, () -> "Worksheet " + name + " not found.");
	    
		int k = sheet.getFirstRowNum();
		while(k <= sheet.getLastRowNum()){
			Row row = sheet.getRow(k++);
			if(row == null 
			|| row.getFirstCellNum() < 0
			|| !this.getString(row.getCell(0)).contains("#")) {
				continue;
			}
			
			
		}
		return null;
	}
	
	protected List<String> readItems(Sheet sheet, int begin) {
		Map<Integer, String> map = new TreeMap<>();
		return Arrays.asList(map.values().toArray(new String[0]));
	}
	
	protected IntFunction<Column<?>> determine(List<String> items) {
		if(items.size() == 2
		&& Boolean.FALSE.toString().equals(items.get(0))
		&& Boolean.TRUE.toString().equals(items.get(1))) {
			return Column::signed;
		}
		
		Class<?> enumType = this.enumTypes.stream()
			.filter(t -> t.isEnum())
			.filter(t -> {
				Object[] enumConsts = t.getEnumConstants();
				if(items.size() != enumConsts.length) {
					return false;
				}
				
				for(int i = 0; i < enumConsts.length; i++){
					if(!enumConsts[i].toString().equals(items.get(i))){
						return false;
					}
				}
				return true;
			})
			.findAny()
			.orElse(String.class);
		
		return enumType == String.class
			? n -> new Column<>(n, items, v -> (int) v)
			: n -> Column.of(n, enumType);
	}
	
	protected String getString(Cell cell) {
		
		return cell != null
			&& (cell.getCellType() == Cell.CELL_TYPE_STRING 
			||  (cell.getCellType() == Cell.CELL_TYPE_FORMULA 
			  && cell.getCachedFormulaResultType() == Cell.CELL_TYPE_STRING))
			? cell.getStringCellValue()
			: "";
	}
	
	private Workbook workbook;
	private List<Class<?>> enumTypes;
}


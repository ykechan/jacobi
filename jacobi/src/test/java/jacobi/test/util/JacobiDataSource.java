/*
 * Copyright (C) 2015 Y.K. Chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jacobi.test.util;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.util.Throw;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

/**
 * 
 * Read data from an Excel spreadsheet for testing input and expected output.
 * 
 * The spreadsheet can contain multiple worksheets. Each worksheets is expected
 * to take the following format.
 * 
 * - A string value with prefix # indicates a start of a new matrix.
 * - A non-negative integer should follow # to be the matrix's ID, e.g. #7.
 * - The following row should have column A and B as number of rows and columns.
 * - The following n rows would contain the matrix element values, starting
 *   from column A.
 * - Other cell values are ignored.
 * 
 * E.g.
 * A  B  C  D  E  ...
 * #1          This indicates start of a new matrix
 * 3  3        This is the number of rows and columns, respectively
 * 1  0  0     Matrix elements. 
 * 0  1  0     Comments can be written anywhere else
 * 0  0  1    
 * 
 * The data can be queried by the name of the worksheet. A query would return
 * all matrices defined in the worksheet, indexed by their IDs.
 * 
 * @author Y.K. Chan
 */
public class JacobiDataSource {

    /**
     * Construct data source from a spreadsheet workbook.
     * @param workbook   Spreadsheet workbook
     */
    public JacobiDataSource(Workbook workbook) {
        this.workbook = workbook;
    }
    
    /**
     * Get all matrices defined in the specified worksheet.
     * @param name  Name of the worksheet
     * @return   Matrices defined
     */
    public Map<Integer, Matrix> get(String name) {
        Throw.when()
            .isNull(() -> this.workbook.getSheet(name), () -> "Worksheet " + name + " not found.");
        Map<Integer, Matrix> data = new TreeMap<>();
        Sheet sheet = this.workbook.getSheet(name);
        int k = sheet.getFirstRowNum();
        while(k <= sheet.getLastRowNum()){
            Integer id = this.getIdFromAnchor(sheet.getRow(k++));
            if(id == null){
                continue;
            }
            Matrix matrix = this.createMatrix(sheet.getRow(k++));
            k = this.readMatrixElements(sheet, k, matrix);
            data.put(id, matrix);
        }
        return data;
    }
    
    private Integer getIdFromAnchor(Row row) {
        return Optional.ofNullable(row)
            .map((r) -> r.getCell(0))
            .filter((c) -> c != null)
            .filter((c) -> c.getCellType() == Cell.CELL_TYPE_STRING)
            .map((c) -> c.getStringCellValue())
            .filter((s) -> s != null)
            .filter((s) -> s.matches("#[0-9]+") )
            .map((s) -> Integer.valueOf(s.substring(1)))
            .orElse(null);
    }
    
    private Matrix createMatrix(Row row) {
        Throw.when()
            .isNull(() -> row, () -> "No dimension row.")
            .isNull(() -> row.getCell(0), () -> "Undefined number of rows at row #" + row.getRowNum())
            .isNull(() -> row.getCell(1), () -> "Undefined number of columns at row #" + row.getRowNum())
            .isFalse(
                () -> row.getCell(0).getCellType() == Cell.CELL_TYPE_NUMERIC,
                () -> "Invalid number of rows at row #" + row.getRowNum())
            .isFalse(
                () -> row.getCell(1).getCellType() == Cell.CELL_TYPE_NUMERIC,
                () -> "Invalid number of columns at row #" + row.getRowNum());
        
        double numRows = row.getCell(0).getNumericCellValue();
        double numCols = row.getCell(1).getNumericCellValue();
        
        Throw.when()
            .isFalse(
                () -> this.isInteger(numRows),
                () -> "Invalid number of rows " + numRows + " at row #" + row.getRowNum() 
            )
            .isFalse(
                () -> this.isInteger(numCols),
                () -> "Invalid number of columns " + numCols + " at row #" + row.getRowNum() 
            );
        
        return Matrices.zeros((int) numRows, (int) numCols);
    }        
    
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private int readMatrixElements(Sheet sheet, int begin, Matrix matrix) {
        int k = begin;
        for(int i = 0; i < matrix.getRowCount(); i++){
            Row row = sheet.getRow(k++);
            if(row == null) {
                throw new IllegalArgumentException("No matrix element at row #" + (k + 1));
            }            
            double[] v = matrix.getRow(i);
            for(int j = 0; j < matrix.getColCount(); j++){
                int rowNum = k;
                int colNum = j;
                v[j] = Optional.ofNullable(row.getCell(j))
                    .filter((c) -> 
                             c.getCellType() == Cell.CELL_TYPE_NUMERIC
                        || ( c.getCellType() == Cell.CELL_TYPE_FORMULA 
                          && c.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC )
                    )
                    .map((c) -> c.getNumericCellValue())
                    .orElseThrow( () -> 
                        new IllegalArgumentException("Invalid matrix element at row #" // NOPMD
                                + rowNum 
                                + ", column " 
                                + CellReference.convertNumToColString(colNum)) 
                    );                
            }
            matrix.setRow(i, v);
        }
        return k;
    }
    
    private boolean isInteger(double v) {
        return Math.abs(v - Math.floor(v)) < 1e-10;
    }
   
    private Workbook workbook;
}

package jacobi.test.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class JacobiCartData {
    
    public JacobiCartData(Workbook workbook) {
        this.workbook = workbook;
    }
    
    public Map<Integer, DataTable> read(String sheetName) {
        Sheet ws = this.workbook.getSheet(sheetName);
        Map<Integer, DataTable> tableMap = new TreeMap<>();
        
        int rowIndex = ws.getFirstRowNum();
        while(rowIndex <= ws.getLastRowNum()){
            Row row = ws.getRow(rowIndex++);
            Integer id = this.getIdFromAnchor(row);
            if(id == null) {
                continue;
            }
            
            List<Object[]> elements = this.readElements(ws, rowIndex);
            if(elements.isEmpty()) {
                throw new UnsupportedOperationException("Data set #" + id + " is empty.");
            }
            List<Column<?>> columns = this.detectColumns(elements);
            tableMap.put(id, this.toDataTable(columns, elements));
            
            rowIndex += elements.size();
        }
        return tableMap;
    }
    
    protected DataTable toDataTable(List<Column<?>> columns, List<Object[]> elements) {
        Column<?> outcomeCol = columns.get(columns.size() - 1);
        List<Column<?>> features = columns.subList(0, columns.size() - 1);
        
        Matrix dataMat = this.toMatrix(features, elements);
        int[][] noms = features.stream()
            .map(c -> c.isNumeric()
                ? null
                : IntStream.range(0, dataMat.getRowCount())
                    .map(i -> c.getMapping().applyAsInt(dataMat.get(i, c.getIndex())))
                    .toArray() )
            .toArray(n -> new int[n][]);
        
        int[] outcomes = elements.stream()
            .map(r -> r[r.length - 1])
            .mapToInt(obj -> outcomeCol.getItems().indexOf(obj))
            .toArray();
        
        return new DataTable() {

            @Override
            public List<Column<?>> getColumns() {
                return columns;
            }

            @Override
            public Matrix getMatrix() {
                return dataMat;
            }

            @Override
            public int[] nominals(int index) {
                return noms[index];
            }

            @Override
            public Column<?> getOutcomeColumn() {
                return outcomeCol;
            }

            @Override
            public int[] outcomes() {
                return outcomes;
            }
            
        };
    }
    
    protected Matrix toMatrix(List<Column<?>> columns, List<Object[]> elements) {
        Matrix mat = Matrices.zeros(elements.size(), columns.size());
        for(int i = 0; i < mat.getRowCount(); i++) {
            double[] row = mat.getRow(i);
            Object[] elem = elements.get(i);
            
            for(int j = 0; j < columns.size(); j++) {
                Column<?> col = columns.get(j);
                if(col.isNumeric()) {
                    row[j] = (Double) elem[j];
                    continue;
                }
                
                row[j] = col.getItems().indexOf(elem[j]);
            }
            
            mat.setRow(i, row);
        }
        return mat;
    }
    
    protected List<Column<?>> detectColumns(List<Object[]> elements) {
        int numCols = elements.stream().mapToInt(r -> r.length).reduce((i, j) -> {
            if(i != j) {
                throw new IllegalArgumentException("Inconsistent number of columns "
                        + i + " and " + j);
            }
            return i;
        }).orElse(0);
                
        return IntStream.range(0, numCols)
            .mapToObj(i -> this.detectColumn(elements, i))
            .collect(Collectors.toList());
    }
    
    protected Column<?> detectColumn(List<Object[]> elements, int index) {
        boolean isNum = elements.get(0)[index] instanceof Double;
        Set<Object> items = new HashSet<>();
        
        for(Object[] row : elements){
            Object val = row[index];            
            if(isNum != val instanceof Double) {
                throw new UnsupportedOperationException("Inconsistent column type at column " 
                        + index);
            } 
            
            if(isNum) {
                continue;
            }
            
            items.add(val);
        }
        return isNum 
            ? Column.numeric(index) 
            : new Column<>(index, Arrays.asList(items.toArray()), v -> (int) Math.floor(v));
    }
    
    protected List<Object[]> readElements(Sheet worksheet, int begin) {
        List<Object[]> elements = new ArrayList<>();
        for(int i = begin; i <= worksheet.getLastRowNum(); i++){
            Row row = worksheet.getRow(i);
            if(row == null || row.getPhysicalNumberOfCells() == 0){
                // ended by an empty row
                break;
            }
            
            elements.add(this.readRow(row));
        }
        return elements;
    }
    
    protected Object[] readRow(Row row) { 
        if(row.getFirstCellNum() > 0) {
            throw new UnsupportedOperationException("Row " + row.getRowNum() 
                + " does not starts at #" + row.getFirstCellNum() + " , not #0.");
        }
        List<Object> values = new ArrayList<>();
        for(int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if(cell == null) {
                break;
            }
            values.add(this.getValue(cell));
        }
        return values.toArray();
    }        
    
    protected Object getValue(Cell cell) {
        if(cell == null){
            return null;
        }
        int type = cell.getCellType() == Cell.CELL_TYPE_FORMULA
                ? cell.getCachedFormulaResultType()
                : cell.getCellType();
        switch(type) {
            case Cell.CELL_TYPE_BLANK :
            case Cell.CELL_TYPE_ERROR :
                return "";
            case Cell.CELL_TYPE_STRING :
                return cell.getStringCellValue().trim();
            case Cell.CELL_TYPE_BOOLEAN :
                return cell.getBooleanCellValue();
            case Cell.CELL_TYPE_NUMERIC :
                return DateUtil.isCellDateFormatted(cell)
                    ? cell.getDateCellValue()
                    : cell.getNumericCellValue();
            default :
                break;
        }
        throw new IllegalArgumentException("Unknown cell type " + type
                + " at " + cell.getRowIndex() 
                + "," + cell.getColumnIndex());
    }
    
    protected Integer getIdFromAnchor(Row row) {
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

    private Workbook workbook;
}

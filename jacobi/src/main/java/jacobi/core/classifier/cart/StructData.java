package jacobi.core.classifier.cart;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

import jacobi.api.Matrix;

/**
 * This class represents a structured data set in which each column has its type defined.
 * 
 * <p>Columns in the data set can be nominal or numeric. For nominal data, the value is
 * the index of an array of items pre-defined in the column model. For numeric data, the value
 * is the rank of that instance in the column.
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public class StructData {
    
    public static Function<Matrix, StructData> define() {
        return null;
    }

    protected StructData(List<ColumnModel<?>> models, int[][] data) {
        this.models = models;
        this.data = data;
    }
    
    public int getRowCount() {
        return this.data.length;
    }
    
    public int getColCount() {
        return this.models.size();
    }
    
    public int[] getRow(int index) {
        return this.data[index];
    }
    
    public void groupBy(IntUnaryOperator grouper, int begin, int end, int[] dist) {
        int[] mapping = this.indexMapping(grouper, begin, end, dist);
        int[][] group = new int[mapping.length][];
        
        for(int i = 0; i < group.length; i++) {
            group[i] = this.data[mapping[i]];
        }
    }
    
    protected int[] indexMapping(IntUnaryOperator grouper, int begin, int end, int[] dist){        
        int[] mapping = new int[end - begin];
        int[] pos = new int[dist.length];
        
        for(int i = 1; i < pos.length; i++) {
            pos[i] = pos[i - 1] + dist[i - 1];
        }
        for(int i = 0; i < mapping.length; i++) {
            int group = grouper.applyAsInt(begin + i);
            mapping[i] = pos[group]++;
        }
        return mapping;
    }
    
    protected int[] countingSort(int[][] data, int begin, int end, int columnIdx) {
        return null;
    }
    
    private List<ColumnModel<?>> models;
    private int[][] data;
}

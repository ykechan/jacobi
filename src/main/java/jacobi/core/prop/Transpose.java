/* 
 * The MIT License
 *
 * Copyright 2017 Y.K. Chan
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
package jacobi.core.prop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Pure;
import jacobi.core.impl.ColumnVector;
import jacobi.core.impl.Empty;
import jacobi.core.util.Throw;

/**
 * Create the transpose of the input matrix.
 * 
 * <p>The transpose matrix A^t of matrix A can be obtained by reflect A over its diagonal.</p>
 * 
 * <p>Since matrices are assumed to be stored row-wise in memory, column-wise access
 * is costly for it causes a lot of cache miss. This implementation alleviates this
 * by reading a few column elements when fetching a row before moving on to the next
 * row.</p>
 * 
 * @author Y.K. Chan
 */
@Pure
public class Transpose {
    
    /**
     * Create the transpose of the input matrix.
     * @param matrix  Input matrix A
     * @return  Transpose matrix A^T
     */
    public Matrix compute(Matrix matrix) {
    	Throw.when().isNull(() -> matrix, () -> "No matrix to transpose.");
    	
    	return matrix.getRowCount() == 0 
    		? Empty.getInstance()
    		: Matrices.wrap(
    			this.compute(matrix, Function.identity()).toArray(new double[0][])
    		);
    }
    
    /**
     * Compute on each column of the input matrix and get the results
     * @param matrix  Input matrix
     * @param mapper  Mapping function from column vector to result
     * @return  Lists of results
     */
    public <T> List<T> compute(Matrix matrix, Function<double[], T> mapper) {   
    	
    	if(matrix instanceof ColumnVector){
    		double[] vector = ((ColumnVector) matrix).getVector();
    		return Collections.singletonList(
    			mapper.apply(Arrays.copyOf(vector, vector.length))
    		);
    	}
    	
    	if(matrix.getRowCount() == 1) {
    		return Arrays.stream(matrix.getRow(0))
    			.mapToObj(v -> mapper.apply(new double[] {v}))
    			.collect(Collectors.toList());
    	}
    	
    	List<T> results = new ArrayList<>(matrix.getColCount());
    	double[][] buffer = new double[FETCH_SIZE][];
    	
    	for(int j = 0; j < matrix.getColCount(); j += buffer.length) {
    		int span = Math.min(matrix.getColCount() - j, buffer.length);
    		
    		for(int k = 0; k < span; k++) {
    			if(buffer[k] == null) {
    				buffer[k] = new double[matrix.getRowCount()];
    			}
    		}
    		
    		for(int i = 0; i < matrix.getRowCount(); i++) {
    			double[] row = matrix.getRow(i);
    			for(int k = 0; k < span; k++) {
    				buffer[k][i] = row[j + k];
    			}
    		}
    		
    		for(int k = 0; k < span; k++){
    			T yield = mapper.apply(buffer[k]);
    			results.add(yield);
    			
    			if(yield == buffer[k]){
    				buffer[k] = null;
    			}
    		}
    	}
    	return results;
    }
    
    /**
     * Fetch size
     */
    protected static final int FETCH_SIZE = 8;
}

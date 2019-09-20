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
package jacobi.core.classifier;

import java.util.AbstractList;
import java.util.List;

import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.Instance;

/**
 * An implementation of DataTable implements the Reweightable interface to indicate
 * it can create a new DataTable with the original data and associate new weights
 * to the respective instances. 
 * 
 * @author Y.K. Chan
 * @param <T>  Type of outcome
 */
public interface Reweightable<T> {
	
	/**
	 * Construct a reweightable DataTable from a DataTable. If the given DataTable
	 * does not implements Reweightable, weights are associated on-the-fly.
	 * @param dataTab  Input data table
	 * @return A reweightable data table  
	 */
	@SuppressWarnings("unchecked")
	public static <T> Reweightable<T> of(DataTable<T> dataTab) {
		if(dataTab instanceof Reweightable){
			return (Reweightable<T>) dataTab;
		}
				
		return w -> new DataTable<T>() {

			@Override
			public List<Column<?>> getColumns() {
				return dataTab.getColumns();
			}

			@Override
			public Column<T> getOutcomeColumn() {
				return dataTab.getOutcomeColumn();
			}

			@Override
			public Matrix getMatrix() {
				return dataTab.getMatrix();
			}

			@Override
			public List<Instance> getInstances(Column<?> column) {
				List<Instance> instances = dataTab.getInstances(column);
				return new AbstractList<Instance>() {

					@Override
					public Instance get(int index) {
						Instance inst = instances.get(index);
						return new Instance(inst.feature, inst.outcome, w[index]);
					}

					@Override
					public int size() {
						return instances.size();
					}
					
				};
			}
			
		};
	}
	
	/**
	 * Create a new DataTable and associate with new weights
	 * @param weights  New weights of the instances
	 * @return  DataTable with new weights associated
	 */
	public DataTable<T> reweight(double[] weights);

}

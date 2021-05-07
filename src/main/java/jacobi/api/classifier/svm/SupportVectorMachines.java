/*
 * The MIT License
 *
 * Copyright 2021 Y.K. Chan
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
package jacobi.api.classifier.svm;

import java.util.List;
import java.util.stream.IntStream;

import jacobi.api.classifier.Classifier;
import jacobi.api.classifier.Column;

/**
 * A collection of multiple one-against-all SVMs.
 * 
 * @author Y.K. Chan
 * 
 */
public class SupportVectorMachines<T> implements Classifier<T> {
	
	/**
	 * Constructor
	 * @param outCol  Outcome column
	 * @param svms  List of SVM for each outcome
	 */
	public SupportVectorMachines(Column<T> outCol, List<SupportVectorMachine> svms) {
		this.outCol = outCol;
		this.svms = svms;
	}
	
	@Override
	public T apply(double[] t) {
		int ans = 0;
		double max = this.svms.get(0).applyAsDouble(t);
		if(this.outCol.isBoolean()){
			return this.outCol.valueOf(max > 0 ? 1 : 0);
		}
		
		for(int i = 1; i < this.svms.size(); i++){
			double dist = this.svms.get(i).applyAsDouble(t);
			if(dist > max){
				ans = i;
				max = dist;
			}
		}
		return this.outCol.valueOf(ans);
	}
	
	/**
	 * Get outcome column
	 * @return  Outcome column
	 */
	public Column<T> getOutcomeColumn() {
		return this.outCol;
	}
	
	/**
	 * Get normal and bias for classifying an item as positive
	 * @param item  Outcome of the item
	 * @return  SVM for the given outcome
	 */
	public SupportVectorMachine getSVM(T item) {
		int val = this.outCol.getItems().indexOf(item);
		if(val < 0){
			throw new IllegalArgumentException("Class " + item + " not an outcome");
		}
		
		if(this.outCol.isBoolean()){
			SupportVectorMachine svm = this.svms.get(0);
			if(val > 0){
				return svm;
			}
			
			int dim = svm.dim();
			
			double bias = -svm.getBias();
			double[] normal = IntStream.range(0, dim)
				.mapToDouble(svm::getCoeff).map(v -> -v).toArray();
			
			int[] cols = IntStream.range(0, dim).map(svm::getColumn).toArray();
			
			return new SupportVectorMachine(cols, normal, bias);
		}
		
		return this.svms.get(val);
	}

	@Override
	public String toString() {
		return "SupportVectorMachines [outCol=" + outCol + ", svms=" + svms + "]";
	}

	private Column<T> outCol;
	private List<SupportVectorMachine> svms;
}

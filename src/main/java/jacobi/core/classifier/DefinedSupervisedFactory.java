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

import java.util.List;

import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.DefinedSupervised;
import jacobi.core.facade.FacadeProxy;
import jacobi.core.util.Throw;

/**
 * Implementation of creating proxy for supervised learning with column types defined.
 * 
 * @author Y.K. Chan
 */
public class DefinedSupervisedFactory {
	
	@SuppressWarnings("unchecked")
	public <T> DefinedSupervised<T> create(Matrix matrix, List<Column<?>> colDefs, List<T> outcomes) {
		Throw.when()
			.isNull(() -> matrix, () -> "No input matrix.")
			.isNull(() -> colDefs, () -> "No column definition.")
			.isTrue(() -> outcomes == null || outcomes.isEmpty(), () -> "No outcome")
			.isTrue(
				() -> matrix.getRowCount() != outcomes.size(), 
				() -> "Number of instances mismatch."
			);
		
		for(Column<?> cols : colDefs) {
			if(cols.getIndex() < 0 || cols.getIndex() >= matrix.getColCount()) {
				throw new IllegalArgumentException("Invalid column #" + cols.getIndex());
			}
		}
		
		DataTable<T> dataTab = DefinedMatrix.of(matrix, outcomes).apply(colDefs);
		return FacadeProxy.of(DefinedSupervised.class, dataTab);
	}

}

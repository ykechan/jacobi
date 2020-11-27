package jacobi.core.spatial.sort;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

/**
 * 
 * @author Y.K. Chan
 *
 */
@JacobiImport("/jacobi/test/data/KdSortTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class KdSortTest {
	
	protected List<double[]> toList(Matrix matrix) {
		return new AbstractList<double[]>() {

			@Override
			public double[] get(int index) {
				return matrix.getRow(index);
			}

			@Override
			public int size() {
				return matrix.getRowCount();
			}
			
		};
	}
	
	protected Matrix toMatrix(List<double[]> vectors, int[] order) {
		return this.toMatrix(new AbstractList<double[]>() {

			@Override
			public double[] get(int index) {
				return vectors.get(order[index]);
			}

			@Override
			public int size() {
				return order.length;
			}
			
		});
	}
	
	protected Matrix toMatrix(List<double[]> vectors) {
		return new ImmutableMatrix() {

			@Override
			public int getRowCount() {
				return vectors.size();
			}

			@Override
			public int getColCount() {
				return vectors.isEmpty() ? 0 : vectors.get(0).length;
			}

			@Override
			public double[] getRow(int index) {
				return vectors.get(index);
			}
			
		};
	}

}

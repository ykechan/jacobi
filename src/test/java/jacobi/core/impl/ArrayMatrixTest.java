package jacobi.core.impl;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrix;

public class ArrayMatrixTest {
	
	@Test
	public void shouldBeAbleToConstructArrayMatrix() {
		Matrix mat = ArrayMatrix.of(6, 2);
		
		Assert.assertNotNull(mat);
		Assert.assertEquals(6, mat.getRowCount());
		Assert.assertEquals(2, mat.getColCount());
		
		for(int i = 0; i < mat.getRowCount(); i++) {
			Assert.assertEquals(2, mat.getRow(i).length);
		}
	}
	
	@Test
	public void shouldBeInitializeToZeroesWhenConstructArrayMatrix() {
		Matrix mat = ArrayMatrix.of(12, 3);
		for(int i = 0; i < mat.getRowCount(); i++) {
			Assert.assertArrayEquals(new double[] {0.0, 0.0, 0.0}, mat.getRow(i), 1e-12);
		}
	}
	
	@Test
	public void shouldNotLeakArrayWhenGetRowForArrayMatrix() {
		Matrix mat = ArrayMatrix.of(12, 3);
		mat.setRow(2, new double[] {1.0, Math.PI, Math.E});
		
		double[] row = mat.getRow(2);
		row[1] = 0.0;
		
		Assert.assertArrayEquals(new double[] {1.0, Math.PI, Math.E}, mat.getRow(2), 1e-12);
	}
	
	@Test
	public void shouldBeAbleToInitByWrapGivenArray() {
		Matrix mat = ArrayMatrix.wrap(3, 
			1.0, 2.0, 3.0, 
			4.0, 5.0, 6.0, 
			7.0, 8.0, 9.0
		);
		
		Assert.assertEquals(3, mat.getRowCount());
		Assert.assertEquals(3, mat.getColCount());
		Assert.assertArrayEquals(new double[] {1.0, 2.0, 3.0}, mat.getRow(0), 1e-12);
		Assert.assertArrayEquals(new double[] {4.0, 5.0, 6.0}, mat.getRow(1), 1e-12);
		Assert.assertArrayEquals(new double[] {7.0, 8.0, 9.0}, mat.getRow(2), 1e-12);
	}
	
	@Test
	public void shouldBeAbleToSwapRows() {
		Matrix mat = ArrayMatrix.wrap(3, 
				1.0, 2.0, 3.0, 
				4.0, 5.0, 6.0, 
				7.0, 8.0, 9.0
		).swapRow(0, 2);
			
		Assert.assertEquals(3, mat.getRowCount());
		Assert.assertEquals(3, mat.getColCount());
		Assert.assertArrayEquals(new double[] {1.0, 2.0, 3.0}, mat.getRow(2), 1e-12);
		Assert.assertArrayEquals(new double[] {4.0, 5.0, 6.0}, mat.getRow(1), 1e-12);
		Assert.assertArrayEquals(new double[] {7.0, 8.0, 9.0}, mat.getRow(0), 1e-12);
	}
	
	@Test
	public void shouldBeAbleToInit2x3MatrixByWrapGivenArray() {
		Matrix mat = ArrayMatrix.wrap(3, 
			1.0, 2.0, 3.0, 
			4.0, 5.0, 6.0
		);
		
		Assert.assertEquals(2, mat.getRowCount());
		Assert.assertEquals(3, mat.getColCount());
		Assert.assertArrayEquals(new double[] {1.0, 2.0, 3.0}, mat.getRow(0), 1e-12);
		Assert.assertArrayEquals(new double[] {4.0, 5.0, 6.0}, mat.getRow(1), 1e-12);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenNumOfRowsIsZero() {
		ArrayMatrix.of(0, 3);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenNumOfColumnIsZero() {
		ArrayMatrix.of(3, 0);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenNumOfRowsIsNegative() {
		ArrayMatrix.of(-1, 3);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenNumOfColumnIsNegative() {
		ArrayMatrix.of(3, -2);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void shouldFailWhenNumberOfElementsIsGreaterThanMaxInt() {
		ArrayMatrix.of(65536, 65536 / 2);
	}

}

package jacobi.core.solver.nonlin;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.util.Real;
import jacobi.core.util.Weighted;

public class QueuedOptimizerTest {
	
	@Test
	public void shouldBeAbleToReturnDefaultAnsWhenNoOptimizerGiven() {
		Weighted<ColumnVector> ans = new QueuedOptimizer(Collections.emptyList()).optimize(
			new VectorFunction() {

				@Override
				public double at(double[] pos) {
					throw new UnsupportedOperationException();
				}

				@Override
				public ColumnVector grad(double[] pos) {
					return new ColumnVector(new double[] {10.0, 12.0, -16.0});
				}

				@Override
				public Matrix hess(double[] pos) {
					throw new UnsupportedOperationException();
				}
				
			}, 
			() -> new double[] {1.0, 2.0, 3.0}, 
			1L, Real.EPSILON);
		
		Assert.assertEquals(16.0, ans.weight, 1e-12);
		Assert.assertArrayEquals(new double[] {1.0, 2.0, 3.0}, ans.item.getVector(),  1e-12);		
	}
	
	@Test
	public void shouldBeAbleToDoubleTheResourcesForNextOptimizer() {
		AtomicInteger count = new AtomicInteger(0);
		new QueuedOptimizer(Arrays.asList(
					this.mock(128, count),
					this.mock(256, count),
					this.mock(512, count)
				)).optimize(
				new VectorFunction() {

					@Override
					public double at(double[] pos) {
						throw new UnsupportedOperationException();
					}

					@Override
					public ColumnVector grad(double[] pos) {
						return new ColumnVector(new double[] {10.0, 12.0, -16.0});
					}

					@Override
					public Matrix hess(double[] pos) {
						throw new UnsupportedOperationException();
					}
					
				}, 
			() -> new double[] {1.0, 2.0, 3.0}, 
			1024L, Real.EPSILON);
		
		Assert.assertEquals(3, count.get());
	}
	
	@Test
	public void shouldBeAbleToUseTheFirstAnsFoundIn1stOptimizer() {
		AtomicInteger count = new AtomicInteger(0);
		Weighted<ColumnVector> ans = new QueuedOptimizer(Arrays.asList(
			(f, s, lmt, eps) -> new Weighted<>(new ColumnVector(new double[] {3.0, 2.0, 10.0}), 0.0),
			this.mock(256, count),
			this.mock(512, count)
		)).optimize(
			this.mockFunc(), 
			() -> new double[] {1.0, 2.0, 3.0}, 
			1024L, Real.EPSILON
		);
		
		Assert.assertEquals(0, count.get());
		Assert.assertEquals(0.0, ans.weight, 1e-12);
		Assert.assertArrayEquals(new double[] {3.0, 2.0, 10.0}, ans.item.getVector(), 1e-12);
	}
	
	@Test
	public void shouldBeAbleToUseTheFirstAnsFoundIn2ndOptimizer() {
		AtomicInteger count = new AtomicInteger(0);
		Weighted<ColumnVector> ans = new QueuedOptimizer(Arrays.asList(
			this.mock(128, count),
			(f, s, lmt, eps) -> new Weighted<>(new ColumnVector(new double[] {3.0, 2.0, 10.0}), 0.0),			
			this.mock(512, count)
		)).optimize(
			this.mockFunc(), 
			() -> new double[] {1.0, 2.0, 3.0}, 
			1024L, Real.EPSILON
		);
		
		Assert.assertEquals(1, count.get());
		Assert.assertEquals(0.0, ans.weight, 1e-12);
		Assert.assertArrayEquals(new double[] {3.0, 2.0, 10.0}, ans.item.getVector(), 1e-12);
	}
	
	@Test
	public void shouldBeAbleToUseTheFirstAnsFoundIn3rdOptimizer() {
		AtomicInteger count = new AtomicInteger(0);
		Weighted<ColumnVector> ans = new QueuedOptimizer(Arrays.asList(
			this.mock(128, count),
			this.mock(256, count),
			(f, s, lmt, eps) -> new Weighted<>(new ColumnVector(new double[] {3.0, 2.0, 10.0}), 0.0)						
		)).optimize(
			this.mockFunc(), 
			() -> new double[] {1.0, 2.0, 3.0}, 
			1024L, Real.EPSILON
		);
		
		Assert.assertEquals(2, count.get());
		Assert.assertEquals(0.0, ans.weight, 1e-12);
		Assert.assertArrayEquals(new double[] {3.0, 2.0, 10.0}, ans.item.getVector(), 1e-12);
	}
	
	@Test
	public void shouldBeAbleToUseTheBestAnsFoundIn1stOptimizer() {
		Weighted<ColumnVector> ans = new QueuedOptimizer(Arrays.asList(
			(f, s, lmt, eps) -> new Weighted<>(new ColumnVector(new double[] {5.0, 6.0, 9.0}), 10.0),
			(f, s, lmt, eps) -> new Weighted<>(new ColumnVector(new double[] {3.1, 2.4, 1.8}), 20.0),
			(f, s, lmt, eps) -> new Weighted<>(new ColumnVector(new double[] {1.1, 3.2, 7.8}), 30.0)						
		)).optimize(
			this.mockFunc(), 
			() -> new double[] {1.0, 2.0, 3.0}, 
			1024L, Real.EPSILON
		);
		
		Assert.assertEquals(10.0, ans.weight, 1e-12);
		Assert.assertArrayEquals(new double[] {5.0, 6.0, 9.0}, ans.item.getVector(), 1e-12);
	}
	
	@Test
	public void shouldBeAbleToUseTheBestAnsFoundIn2ndOptimizer() {
		Weighted<ColumnVector> ans = new QueuedOptimizer(Arrays.asList(
			(f, s, lmt, eps) -> new Weighted<>(new ColumnVector(new double[] {5.0, 6.0, 9.0}), 21.0),
			(f, s, lmt, eps) -> new Weighted<>(new ColumnVector(new double[] {3.1, 2.4, 1.8}), 20.0),
			(f, s, lmt, eps) -> new Weighted<>(new ColumnVector(new double[] {1.1, 3.2, 7.8}), 30.0)						
		)).optimize(
			this.mockFunc(), 
			() -> new double[] {1.0, 2.0, 3.0}, 
			1024L, Real.EPSILON
		);
		
		Assert.assertEquals(20.0, ans.weight, 1e-12);
		Assert.assertArrayEquals(new double[] {3.1, 2.4, 1.8}, ans.item.getVector(), 1e-12);
	}
	
	@Test
	public void shouldBeAbleToUseTheBestAnsFoundIn3rdOptimizer() {
		Weighted<ColumnVector> ans = new QueuedOptimizer(Arrays.asList(
			(f, s, lmt, eps) -> new Weighted<>(new ColumnVector(new double[] {5.0, 6.0, 9.0}), 21.0),
			this.mock(256, new AtomicInteger(0)),
			(f, s, lmt, eps) -> new Weighted<>(new ColumnVector(new double[] {1.1, 3.2, 7.8}), 15.0)						
		)).optimize(
			this.mockFunc(), 
			() -> new double[] {1.0, 2.0, 3.0}, 
			1024L, Real.EPSILON
		);
		
		Assert.assertEquals(15.0, ans.weight, 1e-12);
		Assert.assertArrayEquals(new double[] {1.1, 3.2, 7.8}, ans.item.getVector(), 1e-12);
	}
	
	protected IterativeOptimizer mock(long expectedLimit, AtomicInteger count) {
		return (f, s, lmt, eps) -> {
			Assert.assertEquals(expectedLimit, lmt);
			count.incrementAndGet();
			return null;
		};
	}
	
	protected VectorFunction mockFunc() {
		return new VectorFunction() {

			@Override
			public double at(double[] pos) {
				throw new UnsupportedOperationException();
			}

			@Override
			public ColumnVector grad(double[] pos) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Matrix hess(double[] pos) {
				throw new UnsupportedOperationException();
			}
			
		};
	}

}

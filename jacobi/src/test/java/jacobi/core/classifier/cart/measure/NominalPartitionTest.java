package jacobi.core.classifier.cart.measure;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrix;
import jacobi.api.classifier.cart.Column;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Instance;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.measure.Impurity;
import jacobi.core.classifier.cart.measure.NominalPartition;
import jacobi.core.classifier.cart.util.JacobiDefCsvReader;
import jacobi.core.classifier.cart.util.JacobiEnums.YesOrNo;
import jacobi.core.util.Weighted;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/NominalPartitionTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class NominalPartitionTest {
	
	@JacobiInject(0)
	public Matrix input;
	
	@Test
	@JacobiImport("test 10x5 radix")
	public void testShouldBeAbleToCount10x5Radix2() {
		double result = new NominalPartition(dist -> {
			Assert.assertTrue(
				(dist[0] == 2.0 && dist[1] == 3.0)
			 || (dist[0] == 3.0 && dist[1] == 2.0) 
			);
			return Math.E;
		}).measure(
			Column.nominal(1, 2, v -> (int) v), 
			Column.signed(0), 
			this.toInstances(this.input, 1, 0, -1));
		
		Assert.assertEquals(10 * Math.E, result, 1e-12);
	}
	
	@Test
	@JacobiImport("test 10x5 radix")
	public void testShouldBeAbleToCount10x5Radix3() {
		AtomicInteger count = new AtomicInteger(0);
		double result = new NominalPartition(dist -> {
			switch(count.getAndIncrement()) {
				case 0 :
					Assert.assertArrayEquals(new double[] {2.0, 2.0}, dist, 1e-12);
					break;
				case 1 :
					Assert.assertArrayEquals(new double[] {2.0, 1.0}, dist, 1e-12);
					break;
				case 2 :
					Assert.assertArrayEquals(new double[] {1.0, 2.0}, dist, 1e-12);
					break;
				default :
					break;
			}
			return Math.E;
		}).measure(
			Column.nominal(2, 3, v -> (int) v), 
			Column.signed(0), 
			this.toInstances(this.input, 2, 0, -1));
		
		Assert.assertEquals(3, count.get());
		Assert.assertEquals(10 * Math.E, result, 1e-12);
	}
	
	/*
	 * sunny,85,85,false,no
		sunny,80,90,true,no
		overcast,83,78,false,yes
		rain,70,96,false,yes
		rain,68,80,false,yes
		rain,65,70,true,no
		overcast,64,65,true,yes
		sunny,72,95,false,no
		sunny,69,70,false,yes
		rain,75,80,false,yes
		sunny,75,70,true,yes
		overcast,72,90,true,yes
		overcast,81,75,false,yes
		rain,71,80,true,no
	 */
	
	@Test
	public void testShouldBeAbleToPartitionGolfDataOnOutlook() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<YesOrNo> dataTab = new JacobiDefCsvReader()
					.read(input, YesOrNo.class);
			
			Weighted<double[]> split = new NominalPartition(Impurity.ENTROPY).measure(
				dataTab, dataTab.getColumns().get(0), new Sequence(
					IntStream.range(0, dataTab.size()).toArray(), 0, dataTab.size()
				) 
			);
						
			// sunny: yes(2) no(3)
			// overcast: yes(4) no(0)
			// rain: yes(3) no(2)
			
			Assert.assertEquals(
				  5 * Impurity.ENTROPY.of(new double[] {2, 3})
				+ 4 * Impurity.ENTROPY.of(new double[] {4, 0})
				+ 5 * Impurity.ENTROPY.of(new double[] {3, 2}), split.weight, 1e-12);
			
			Assert.assertEquals(0, split.item.length);
		}
	}
	
	@Test
	public void testShouldBeAbleToPartitionGolfDataOnWindy() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<YesOrNo> dataTab = new JacobiDefCsvReader()
					.read(input, YesOrNo.class);
			
			Weighted<double[]> split = new NominalPartition(Impurity.ENTROPY).measure(
				dataTab, dataTab.getColumns().get(3), new Sequence(
					IntStream.range(0, dataTab.size()).toArray(), 0, dataTab.size()
				) 
			);
						
			// true: yes(3) no(3)
			// false: yes(6) no(2)
			
			Assert.assertEquals(
				  6 * Impurity.ENTROPY.of(new double[] {3, 3})
				+ 8 * Impurity.ENTROPY.of(new double[] {6, 2}), split.weight, 1e-12);
			
			Assert.assertEquals(0, split.item.length);
		}
	}
	
	protected List<Instance> toInstances(Matrix matrix, 
			int featCol, int outcomeCol, int weightCol) {
		return new AbstractList<Instance>() {

			@Override
			public Instance get(int index) {
				double[] row = matrix.getRow(index);
				return new Instance(
					(int) Math.floor(row[featCol]),
					(int) Math.floor(row[outcomeCol]),
					weightCol < 0 ? 1.0 : row[weightCol]);
			}

			@Override
			public int size() {
				return matrix.getRowCount();
			}
			
		};
	}

}

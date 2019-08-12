package jacobi.core.classifier.cart.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrix;
import jacobi.api.classifier.cart.Column;
import jacobi.core.classifier.cart.util.JacobiDefCsvReader;
import jacobi.core.classifier.cart.util.JacobiEnums.Outlook;
import jacobi.core.classifier.cart.util.JacobiEnums.YesOrNo;

public class DataMatrixTest {
	
	@Test
	public void shouldBeAbleToEncodeGolfCsv() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			Matrix matrix = new JacobiDefCsvReader().read(input, YesOrNo.class)
					.getMatrix();
						
			Assert.assertEquals(14, matrix.getRowCount());
			Assert.assertEquals(5, matrix.getColCount());
			
			Assert.assertArrayEquals(
				new double[] {0.0, 85.0, 85.0, 0.0, 1.0}, 
				matrix.getRow(0), 1e-12);
			
			Assert.assertArrayEquals(
				new double[] {0.0, 80.0, 90.0, 1.0, 1.0}, 
				matrix.getRow(1), 1e-12);
			
			Assert.assertArrayEquals(
				new double[] {1.0, 83.0, 78.0, 0.0, 0.0}, 
				matrix.getRow(2), 1e-12);
		}
	}
	
	@Test
	public void shouldBeAbleToInitGolfData() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<YesOrNo> dataMat = new JacobiDefCsvReader().read(input, YesOrNo.class);
			
			List<Instance> insts = dataMat.getInstances(
				new Column<>(0, Arrays.asList(Outlook.values()), v -> (int) v)
			);
			
			Assert.assertEquals(14, insts.size());
			/*
			sunny,85,85,false,no
			sunny,80,90,true,no
			overcast,83,78,false,yes
			rain,70,96,false,yes
			rain,68,80,false,yes
			rain,65,70,true,no
			overcast,64,65,true,yes // 6
			sunny,72,95,false,no
			sunny,69,70,false,yes
			rain,75,80,false,yes
			sunny,75,70,true,yes
			overcast,72,90,true,yes
			overcast,81,75,false,yes
			rain,71,80,true,no
			*/
			this.assertEquals(new Instance(0, 1, 1.0), insts.get(0), 1e-12);
			this.assertEquals(new Instance(0, 1, 1.0), insts.get(1), 1e-12);
			this.assertEquals(new Instance(1, 0, 1.0), insts.get(2), 1e-12);
			this.assertEquals(new Instance(2, 0, 1.0), insts.get(3), 1e-12);
			this.assertEquals(new Instance(2, 0, 1.0), insts.get(4), 1e-12);
			this.assertEquals(new Instance(2, 1, 1.0), insts.get(5), 1e-12);
			this.assertEquals(new Instance(1, 0, 1.0), insts.get(6), 1e-12);
			this.assertEquals(new Instance(0, 1, 1.0), insts.get(7), 1e-12);
			this.assertEquals(new Instance(0, 0, 1.0), insts.get(8), 1e-12);
			this.assertEquals(new Instance(2, 0, 1.0), insts.get(9), 1e-12);
			this.assertEquals(new Instance(0, 0, 1.0), insts.get(10), 1e-12);
			this.assertEquals(new Instance(1, 0, 1.0), insts.get(11), 1e-12);
			this.assertEquals(new Instance(1, 0, 1.0), insts.get(12), 1e-12);
			this.assertEquals(new Instance(2, 1, 1.0), insts.get(13), 1e-12);
		}
	}
	
	@Test
	public void shouldBeAbleToInitGolfDataExcludingOutcomeColInColList() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<YesOrNo> dataMat = new JacobiDefCsvReader().read(input, YesOrNo.class);
			
			List<Instance> insts = dataMat.getInstances(
				new Column<>(3, Arrays.asList(Boolean.FALSE, Boolean.TRUE), v -> (int) v)
			);
			
			Assert.assertEquals(14, insts.size());
			/*
			sunny,85,85,false,no
			sunny,80,90,true,no
			overcast,83,78,false,yes
			rain,70,96,false,yes
			rain,68,80,false,yes
			rain,65,70,true,no
			overcast,64,65,true,yes // 6
			sunny,72,95,false,no
			sunny,69,70,false,yes
			rain,75,80,false,yes
			sunny,75,70,true,yes
			overcast,72,90,true,yes
			overcast,81,75,false,yes
			rain,71,80,true,no
			*/
			this.assertEquals(new Instance(0, 1, 1.0), insts.get(0), 1e-12);
			this.assertEquals(new Instance(1, 1, 1.0), insts.get(1), 1e-12);
			this.assertEquals(new Instance(0, 0, 1.0), insts.get(2), 1e-12);
			this.assertEquals(new Instance(0, 0, 1.0), insts.get(3), 1e-12);
			this.assertEquals(new Instance(0, 0, 1.0), insts.get(4), 1e-12);
			this.assertEquals(new Instance(1, 1, 1.0), insts.get(5), 1e-12);
			this.assertEquals(new Instance(1, 0, 1.0), insts.get(6), 1e-12);
			this.assertEquals(new Instance(0, 1, 1.0), insts.get(7), 1e-12);
			this.assertEquals(new Instance(0, 0, 1.0), insts.get(8), 1e-12);
			this.assertEquals(new Instance(0, 0, 1.0), insts.get(9), 1e-12);
			this.assertEquals(new Instance(1, 0, 1.0), insts.get(10), 1e-12);
			this.assertEquals(new Instance(1, 0, 1.0), insts.get(11), 1e-12);
			this.assertEquals(new Instance(0, 0, 1.0), insts.get(12), 1e-12);
			this.assertEquals(new Instance(1, 1, 1.0), insts.get(13), 1e-12);
		}
	}
	
	@Test
	public void shouldBeAbleToReturnIndexAsFeatureForNumericCol() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<YesOrNo> dataMat = new JacobiDefCsvReader().read(input, YesOrNo.class);			
			
			List<Instance> insts = dataMat.getInstances(
				Column.numeric(1)
			);
			
			Assert.assertEquals(14, insts.size());
			for(int i = 0; i < insts.size(); i++) {
				Assert.assertEquals(i, insts.get(i).feature);
			}
			
			insts = dataMat.getInstances(
				Column.numeric(2)
			);
				
			Assert.assertEquals(14, insts.size());
			for(int i = 0; i < insts.size(); i++) {
				Assert.assertEquals(i, insts.get(i).feature);
			}
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenGetInstancesOfNominalColByNumericDef() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<YesOrNo> dataMat = new JacobiDefCsvReader().read(input, YesOrNo.class);
			
			dataMat.getInstances(Column.numeric(0));
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenGetInstancesOfNumericColByNominalDef() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<YesOrNo> dataMat = new JacobiDefCsvReader().read(input, YesOrNo.class);
			
			dataMat.getInstances(new Column<>(1, Arrays.asList(Outlook.values()), v -> (int) v));
		}
	}
	
	protected void assertEquals(Instance expected, Instance actual, double eps) {
		Assert.assertEquals(expected.feature, actual.feature);
		Assert.assertEquals(expected.outcome, actual.outcome);
		Assert.assertEquals(expected.weight, actual.weight, eps);
	}	

}

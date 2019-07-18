package jacobi.core.classifier.cart.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.function.DoubleToIntFunction;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.classifier.cart.data.JacobiCsvDataTable.Outlook;
import jacobi.core.classifier.cart.data.JacobiCsvDataTable.YesOrNo;

public class DataMatrixTest {
	
	@Test
	public void shouldBeAbleToEncodeGolfCsv() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.csv")){
			Matrix matrix = new JacobiCsvDataTable().encodeCsv(input, Arrays.asList(
				Outlook.class, double.class, double.class, boolean.class, YesOrNo.class
			), true);
						
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
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.csv")){
			Matrix matrix = new JacobiCsvDataTable().encodeCsv(input, Arrays.asList(
				Outlook.class, double.class, double.class, boolean.class, YesOrNo.class
			), true);
			
			DoubleToIntFunction flr = v -> (int) Math.floor(v);
						
			DataMatrix<YesOrNo> dataMat = DataMatrix.of(matrix, 
				new TreeSet<>(Arrays.asList(
					new Column<>(0, Arrays.asList(Outlook.values()), flr),
					Column.numeric(1),
					Column.numeric(2),
					new Column<>(3, Arrays.asList(Boolean.FALSE, Boolean.TRUE), v -> v > 0 ? 1 : 0),
					new Column<>(4, Arrays.asList(YesOrNo.values()), flr)
				)), 
				new Column<>(4, Arrays.asList(YesOrNo.values()), flr)
			);
			
			List<Instance> insts = dataMat.getInstances(
				new Column<>(0, Arrays.asList(Outlook.values()), flr)
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
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.csv")){
			Matrix matrix = new JacobiCsvDataTable().encodeCsv(input, Arrays.asList(
				Outlook.class, double.class, double.class, boolean.class, YesOrNo.class
			), true);
			
			DoubleToIntFunction flr = v -> (int) Math.floor(v);
						
			DataMatrix<YesOrNo> dataMat = DataMatrix.of(matrix, 
				new TreeSet<>(Arrays.asList(
					new Column<>(0, Arrays.asList(Outlook.values()), flr),
					Column.numeric(1),
					Column.numeric(2),
					new Column<>(3, Arrays.asList(Boolean.FALSE, Boolean.TRUE), v -> v > 0 ? 1 : 0)
				)), 
				new Column<>(4, Arrays.asList(YesOrNo.values()), flr)
			);
			
			List<Instance> insts = dataMat.getInstances(
				new Column<>(3, Arrays.asList(Boolean.FALSE, Boolean.TRUE), flr)
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
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.csv")){
			Matrix matrix = new JacobiCsvDataTable().encodeCsv(input, Arrays.asList(
				Outlook.class, double.class, double.class, boolean.class, YesOrNo.class
			), true);
			
			DoubleToIntFunction flr = v -> (int) Math.floor(v);
						
			DataMatrix<YesOrNo> dataMat = DataMatrix.of(matrix, 
				new TreeSet<>(Arrays.asList(
					new Column<>(0, Arrays.asList(Outlook.values()), flr),
					Column.numeric(1),
					Column.numeric(2),
					new Column<>(3, Arrays.asList(Boolean.FALSE, Boolean.TRUE), v -> v > 0 ? 1 : 0),
					new Column<>(4, Arrays.asList(YesOrNo.values()), flr)
				)), 
				new Column<>(4, Arrays.asList(YesOrNo.values()), flr)
			);
			
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
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.csv")){
			Matrix matrix = new JacobiCsvDataTable().encodeCsv(input, Arrays.asList(
				Outlook.class, double.class, double.class, boolean.class, YesOrNo.class
			), true);
			
			DoubleToIntFunction flr = v -> (int) Math.floor(v);
						
			DataMatrix<YesOrNo> dataMat = DataMatrix.of(matrix, 
				new TreeSet<>(Arrays.asList(
					new Column<>(0, Arrays.asList(Outlook.values()), flr),
					Column.numeric(1),
					Column.numeric(2),
					new Column<>(3, Arrays.asList(Boolean.FALSE, Boolean.TRUE), v -> v > 0 ? 1 : 0),
					new Column<>(4, Arrays.asList(YesOrNo.values()), flr)
				)), 
				new Column<>(4, Arrays.asList(YesOrNo.values()), flr)
			);
			
			dataMat.getInstances(Column.numeric(0));
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenGetInstancesOfNumericColByNominalDef() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.csv")){
			Matrix matrix = new JacobiCsvDataTable().encodeCsv(input, Arrays.asList(
				Outlook.class, double.class, double.class, boolean.class, YesOrNo.class
			), true);
			
			DoubleToIntFunction flr = v -> (int) Math.floor(v);
						
			DataMatrix<YesOrNo> dataMat = DataMatrix.of(matrix, 
				new TreeSet<>(Arrays.asList(
					new Column<>(0, Arrays.asList(Outlook.values()), flr),
					Column.numeric(1),
					Column.numeric(2),
					new Column<>(3, Arrays.asList(Boolean.FALSE, Boolean.TRUE), v -> v > 0 ? 1 : 0),
					new Column<>(4, Arrays.asList(YesOrNo.values()), flr)
				)), 
				new Column<>(4, Arrays.asList(YesOrNo.values()), flr)
			);
			
			dataMat.getInstances(new Column<>(1, Arrays.asList(Outlook.values()), flr));
		}
	}
	
	protected void assertEquals(Instance expected, Instance actual, double eps) {
		Assert.assertEquals(expected.feature, actual.feature);
		Assert.assertEquals(expected.outcome, actual.outcome);
		Assert.assertEquals(expected.weight, actual.weight, eps);
	}	

}

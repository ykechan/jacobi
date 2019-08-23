package jacobi.core.classifier.cart.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.classifier.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.DefinedMatrix;
import jacobi.core.classifier.cart.util.JacobiEnums.Lens;
import jacobi.core.classifier.cart.util.JacobiEnums.Outlook;
import jacobi.core.classifier.cart.util.JacobiEnums.YesOrNo;

public class JacobiDefCsvReader {
	
	@Test
	public void shouldBeAbleToCreateNumericColumn() {
		List<Column<?>> cols = Arrays.asList(
			this.defColumn(0, "#"),
			this.defColumn(1, "Double"),
			this.defColumn(2, "Float"),
			this.defColumn(3, Double.class.toString()),
			this.defColumn(4, Integer.class.toString())
		);
		
		Assert.assertEquals(5, cols.size());
		for(int i = 0; i < cols.size(); i++) {
			Assert.assertEquals(i, cols.get(i).getIndex());
			Assert.assertTrue(cols.get(i).isNumeric());
		}
	}
	
	@Test
	public void shouldBeAbleToCreateBoolColumn() {
		List<Column<?>> cols = Arrays.asList(
			this.defColumn(5, "bool"),
			this.defColumn(6, "boolean"),
			this.defColumn(7, Boolean.class.toString()),
			this.defColumn(8, boolean.class.toString())
		);
		
		Assert.assertEquals(4, cols.size());
		for(int i = 0; i < cols.size(); i++) {
			Assert.assertEquals(5 + i, cols.get(i).getIndex());
			Assert.assertFalse(cols.get(i).isNumeric());
			Assert.assertEquals(2, cols.get(i).cardinality());
			Assert.assertEquals(Boolean.FALSE, cols.get(i).getItems().get(0));
			Assert.assertEquals(Boolean.TRUE, cols.get(i).getItems().get(1));
			Assert.assertEquals(Boolean.TRUE, cols.get(i).valueOf(1.0));
			Assert.assertEquals(Boolean.FALSE, cols.get(i).valueOf(0.0));
			Assert.assertEquals(Boolean.FALSE, cols.get(i).valueOf(-1.0));
		}
	}
	
	@Test
	public void shouldBeAbleToCreateEnumColumn() {
		Column<?> col = this.defColumn(9, Color.class.toString());
		Assert.assertEquals(3, col.cardinality());
		Assert.assertEquals(Color.RED, col.getItems().get(0));
		Assert.assertEquals(Color.BLUE, col.getItems().get(1));
		Assert.assertEquals(Color.GREEN, col.getItems().get(2));		
		
		Assert.assertEquals(Color.RED, col.valueOf(0.2));
		Assert.assertEquals(Color.BLUE, col.valueOf(1.1));
		Assert.assertEquals(Color.GREEN, col.valueOf(2.3333));
	}
	
	@Test
	public void shouldBeAbleToCreateStringColumn() {
		@SuppressWarnings("unchecked")
		Column<String> col = (Column<String>) this.defColumn(10, "?");
		Assert.assertEquals(0, col.cardinality());
		col.getItems().add("A");
		
		Assert.assertEquals(1, col.cardinality());
		Assert.assertEquals("A", col.valueOf(0));
		
		col.getItems().add("ABC");
		
		Assert.assertEquals(2, col.cardinality());
		Assert.assertEquals("A", col.valueOf(0));
		Assert.assertEquals("ABC", col.valueOf(1));
	}
	
	@Test
	public void shouldBeAbleToReadGolfDefCsv() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<YesOrNo> dataTab = this.read(input, 4, YesOrNo.class);
			Assert.assertEquals(14, dataTab.size());
		}
	}
	
	@Test
	public void shouldBeAbleToReadGolfDefCsvByOutcomeType() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<YesOrNo> dataTab = this.read(input, YesOrNo.class);
			
			Assert.assertEquals(14, dataTab.size());
			Assert.assertEquals(YesOrNo.YES, dataTab.getOutcomeColumn().getItems().get(0));
			Assert.assertEquals(YesOrNo.NO, dataTab.getOutcomeColumn().getItems().get(1));
		}
	}
	
	@Test
	public void shouldBeAbleToReadGolfNomOnlyDefCsv() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf-nom-only.def.csv")){
			DataTable<YesOrNo> dataTab = this.read(input, YesOrNo.class);
			
			Assert.assertEquals(14, dataTab.size());
			Assert.assertEquals(YesOrNo.YES, dataTab.getOutcomeColumn().getItems().get(0));
			Assert.assertEquals(YesOrNo.NO, dataTab.getOutcomeColumn().getItems().get(1));
			
			Assert.assertArrayEquals(
				Outlook.values(), 
				dataTab.getColumns().get(0).getItems().toArray()
			);
			
			Assert.assertArrayEquals(
				new Object[] {"HOT", "MILD", "COOL"}, 
				dataTab.getColumns().get(1).getItems().toArray()
			);
			
			Assert.assertArrayEquals(
				new Object[] {"HIGH", "NORMAL"}, 
				dataTab.getColumns().get(2).getItems().toArray()
			);
			
			Assert.assertArrayEquals(
				new Object[] {"WEAK", "STRONG"}, 
				dataTab.getColumns().get(3).getItems().toArray()
			);
		}
	}
	
	@Test
	public void shouldBeAbleToReadLensDefCsvByOutcomeType() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/contact-lenses.def.csv")){
			DataTable<Lens> dataTab = this.read(input, Lens.class);
						
			Assert.assertEquals(24, dataTab.size());
			Assert.assertEquals(Lens.SOFT, dataTab.getOutcomeColumn().getItems().get(0));
			Assert.assertEquals(Lens.HARD, dataTab.getOutcomeColumn().getItems().get(1));
		}
	}
	
	public <T> DataTable<T> read(InputStream input, Class<T> outcomeType) 
			throws IOException {
		return this.read(input, -1, outcomeType);
	}
	
	@SuppressWarnings("unchecked")
	public <T> DataTable<T> read(InputStream input, int outcomeIndex, Class<T> outcomeType) 
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		
		List<Column<?>> colDefs = this.readDef(reader);
		Matrix matrix = this.readData(reader, colDefs);		
		Column<?> outcomeCol = outcomeIndex < 0 
				? colDefs.stream()
					.filter(c -> !c.isNumeric())
					.filter(c -> outcomeType.isInstance(c.valueOf(0)))
					.reduce((a, b) -> {
						throw new IllegalArgumentException("Ambiguous outcome column #"
							+ a.getIndex() 
							+ " and " 
							+ b.getIndex()
						);
					})
					.orElseThrow(
						() -> new IllegalArgumentException()
					)
				: colDefs.get(outcomeIndex);
		
		return DefinedMatrix.of(matrix, (Column<T>) outcomeCol).apply(colDefs);
	}
	
	protected Matrix readData(BufferedReader reader, List<Column<?>> colDefs) throws IOException {
		
		List<double[]> rows = new ArrayList<>();
		String line = null;
		while((line = reader.readLine()) != null){
			if(line.trim().isEmpty()) {
				continue;
			}
			
			rows.add(this.readRow(colDefs, line));
		}
		
		return Matrices.wrap(rows.toArray(new double[0][]));
	}
	
	protected double[] readRow(List<Column<?>> colDefs, String row) {
		String[] cols = row.split(",");
		if(colDefs.size() != cols.length) {
			throw new IllegalArgumentException("Number of columns mismatch in "
				+ row + ". Expected " + colDefs.size() + ", found " + cols.length);
		}
		
		double[] num = new double[colDefs.size()];
		for(int i = 0; i < num.length; i++) {
			Column<?> def = colDefs.get(i);
			
			if(def.getItems() instanceof ArrayList) {
				// dynamic column
				@SuppressWarnings("unchecked")
				List<String> items = (List<String>) def.getItems();
				int value = -1;
				for(int j = 0; j < items.size(); j++){
					if(def.getItems().get(j).toString().equalsIgnoreCase(cols[i])) {
						value = j;
						break;
					}
				}
				if(value < 0){
					num[i] = items.size();
					items.add(cols[i].trim().toUpperCase());
				}else {
					num[i] = value;
				}
				
				continue;
			}
			
			if(def.isNumeric()) {
				num[i] = Double.valueOf(cols[i]);
				continue;
			}
			
			Class<?> clazz = def.getItems().get(0).getClass();
			if(clazz == Boolean.class || clazz == boolean.class){
				num[i] = Boolean.parseBoolean(cols[i]) ? 1.0 : 0.0;
				continue;
			}
			
			if(clazz.isEnum()){
				try {
					Object enumVal = clazz.getMethod("valueOf", String.class)
							.invoke(null, cols[i].trim().toUpperCase());
					
					num[i] = ((Integer) clazz.getMethod("ordinal").invoke(enumVal))
							.doubleValue();
				} catch (IllegalAccessException
						| IllegalArgumentException 
						| NoSuchMethodException 
						| SecurityException ex) {
					throw new UnsupportedOperationException(ex);
				} catch(InvocationTargetException ex) {
					throw (RuntimeException) ex.getTargetException();
				}
				continue;
			}
			
			throw new UnsupportedOperationException("Unsupported column type " + clazz);
		}
		return num;
	}
	
	protected List<Column<?>> readDef(BufferedReader reader) throws IOException {
		Map<String, String> defMap = new TreeMap<>();
		
		String line = null;
		while((line = reader.readLine()) != null) {
			if(line.trim().isEmpty()) {
				break;
			}
			
			int pos = line.indexOf(':');
			if(pos < 0) {
				throw new IllegalArgumentException("Illegal column definition " + line);
			}
			
			defMap.put(line.substring(0, pos).trim(), line.substring(pos + 1).trim());
		}
		
		if((line = reader.readLine()) == null) {
			throw new IllegalArgumentException("Unexpected end of file. Data section expected.");
		}	
		
		String[] headers = line.split(",");
		
		List<Column<?>> colDefs = new ArrayList<>();
		for(int i = 0; i < headers.length; i++) {
			String def = defMap.get(headers[i]);
			if(def == null) {
				throw new IllegalArgumentException("Definition for " + headers[i] + " not found.");
			}
			colDefs.add(this.defColumn(i, def));
		}
		return colDefs;
	}
	
	protected Column<?> defColumn(int index, String def) {
		if("?".equals(def)) {
			return new Column<>(index, new ArrayList<String>(), v -> (int) v);
		}
		
		if("#".equals(def) || "double".equalsIgnoreCase(def) || "float".equalsIgnoreCase(def)) {
			return Column.numeric(index);
		}
		
		if("bool".equalsIgnoreCase(def)
		|| "boolean".equalsIgnoreCase(def)) {
			return Column.signed(index);
		}
		
		if(def.startsWith("class ")) {
			String className = def.substring("class ".length()).trim();
			try {
				Class<?> clazz = Class.forName(className);
				if(clazz == String.class) {
					throw new UnsupportedOperationException("For dynamic column, use '?'.");
				}
				
				if(clazz == boolean.class || clazz == Boolean.class) {
					return Column.signed(index);
				}
				
				if(Number.class.isAssignableFrom(clazz)) {
					return Column.numeric(index);
				}
				
				if(clazz.isEnum()) {
					return new Column<>(index, 
						Arrays.asList(clazz.getEnumConstants()), 
						v -> (int) v
					);
				}
				
				throw new UnsupportedOperationException("Unable to create a column of type " 
					+ clazz);
			} catch (ClassNotFoundException e) {
				throw new UnsupportedOperationException(e);
			}
		}
		
		throw new UnsupportedOperationException("Illegal defintion " + def 
				+ ". Use ? for a generic String type.");
	}
	
	public enum Color {
		RED, BLUE, GREEN
	}

}

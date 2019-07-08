package jacobi.core.classifier.cart;

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

import org.junit.Test;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataMatrix;

public class ZeroRTest {
	
	@Test
	public void shouldBeAbleToEncodeGolfCsv() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.csv")){
			Matrix matrix = this.encodeCsv(input, Arrays.asList(
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
		}
	}
	
	protected Matrix encodeCsv(InputStream input, List<Class<?>> colTypes, boolean skipHeader) 
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		String line = null;
		
		int lineNum = 0;
		
		List<double[]> rows = new ArrayList<>();
		
		while((line = reader.readLine()) != null){
			if(lineNum++ == 0 && skipHeader) {
				continue;
			}
			
			String[] tokens = line.split(",");
			if(tokens.length != colTypes.size()) {
				throw new IllegalArgumentException("Mismatch number of tokens in line #" 
					+ (lineNum - 1)
					+ ", expected " + colTypes.size() + " found " + tokens.length);
			}
			
			double[] row = new double[tokens.length];
			for(int i = 0; i < row.length; i++) {
				row[i] = this.encode(colTypes.get(i), tokens[i].trim());
			}
			
			rows.add(row);
		}
		
		return Matrices.of(rows.toArray(new double[0][]));
	}
    
    protected double encode(Class<?> clazz, String token) {
        if(clazz == double.class || clazz == Double.class) {
            return Double.valueOf(token);
        }
        
        if(clazz == boolean.class || clazz == Boolean.class) {
            return Boolean.parseBoolean(token) ? 1.0 : 0.0;
        }
        
        if(clazz.isEnum()) {
            try {
                Object enumVal = clazz.getMethod("valueOf", String.class)
                    .invoke(null, token.toUpperCase());
                
                return ((Integer) clazz.getMethod("ordinal").invoke(enumVal))
                    .doubleValue();
            } catch (IllegalAccessException 
                    | IllegalArgumentException 
                    | InvocationTargetException
                    | NoSuchMethodException 
                    | SecurityException e) {
                throw new UnsupportedOperationException(e);
            }
        }
        
        throw new UnsupportedOperationException("Unsupport type " + clazz.getName());
    }
    
    public enum Outlook {
        SUNNY, OVERCAST, RAIN
    }
    
    public enum YesOrNo {
        YES, NO
    }

}

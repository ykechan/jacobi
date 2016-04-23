/*
 * Copyright (C) 2015 Y.K. Chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jacobi.test.util;

import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Assert;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Utility class for running tests in Jacobi.
 * 
 * Most of the time tests in Jacobi involves accept an input matrix, or input
 * matrices, perform computation and check the result. Or check the intermediate
 * results step-by-step. It would be convenient to automatically load input data
 * and check output data. This class does just that.
 * 
 * 
 * 
 * @author Y.K. Chan
 */
public class JacobiJUnit4ClassRunner extends BlockJUnit4ClassRunner {

    public JacobiJUnit4ClassRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        if(!clazz.isAnnotationPresent(JacobiImport.class)){
            return;
        }
        String path = clazz.getAnnotation(JacobiImport.class).value();
        try(InputStream in = clazz.getResourceAsStream(path)){
            this.dataSource = new JacobiDataSource(WorkbookFactory.create(in));
        } catch(IOException | InvalidFormatException ex) {
            throw new InitializationError(ex);
        } 
        this.injects = new ArrayList<>();
        this.results = new TreeMap<>();
        this.detectInjection(clazz);        
        System.out.println("injected " + this.injects);
    }

    @Override
    protected Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
        Statement stmt = super.withBefores(method, target, statement);
        JacobiImport jImport = method.getAnnotation(JacobiImport.class);
        if(jImport == null){
            return stmt;
        }
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                Map<Integer, Matrix> data = dataSource.get(jImport.value());
                for(Field entry : injects){
                    if(entry.getType() == Map.class){
                        entry.set(target, Collections.unmodifiableMap(data));
                    }else{
                        entry.set(target, data.get(entry.getAnnotation(JacobiInject.class).value()));
                    }                    
                }
                stmt.evaluate();
            }
            
        };
    }

    @Override
    protected Statement withAfters(FrameworkMethod method, Object target, Statement statement) {        
        Statement stmt = super.withAfters(method, target, statement);
        JacobiImport jImport = method.getAnnotation(JacobiImport.class);
        JacobiEquals[] asserts = method.getMethod().getAnnotationsByType(JacobiEquals.class);        
        if(jImport == null || asserts == null || asserts.length == 0){
            return stmt;
        }
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                stmt.evaluate();
                
                Map<Integer, Matrix> data = dataSource.get(jImport.value());
                if(asserts != null){
                    for(JacobiEquals eq : asserts){
                        assertEquals(eq, data, target);
                    }
                }
            }
            
        };
    }
    
    private void assertEquals(JacobiEquals eq, Map<Integer, Matrix> data, Object target) throws Exception {        
        Matrix expects = data.get(eq.expected());
        if(!this.results.containsKey(eq.actual())){
            throw new AssertionError("No actual result #" + eq.actual());
        }
        Matrix actual = (Matrix) this.results.get(eq.actual()).get(target);
        Assert.assertNotNull("No expected result #" + eq.expected(), expects);
        Assert.assertNotNull("No actual result #" + eq.actual(), actual);
        Assert.assertEquals("Row count mismatch.", expects.getRowCount(), actual.getRowCount());
        Assert.assertEquals("Column count mismatch.", expects.getColCount(), actual.getColCount());
        for(int i = 0; i < actual.getRowCount(); i++){
            for(int j = 0; j < actual.getColCount(); j++){
                Assert.assertEquals(
                    " Expected " + eq.expected() + ", Actual " + eq.actual()
                    + " Element (" + i + "," + j + ") mismatch.",
                    expects.getRow(i)[j],
                    actual.getRow(i)[j],
                    eq.epsilon() );
            }
        }
    }
    
    private void detectInjection(Class<?> clazz) {
        Arrays.asList(clazz.getFields())
            .stream()
            .filter( (f) -> f.isAnnotationPresent(JacobiInject.class) 
                         || f.isAnnotationPresent(JacobiResult.class)
            )
            .forEach((f) -> {
                if(f.isAnnotationPresent(JacobiInject.class)){
                    this.injects.add(f);
                }
                if(f.isAnnotationPresent(JacobiResult.class)){
                    this.results.put(
                        f.getAnnotation(JacobiResult.class).value(),
                        f);
                }
            });
    }
    
    private JacobiDataSource dataSource;
    private Map<Integer, Field> results;
    private List<Field> injects;
}

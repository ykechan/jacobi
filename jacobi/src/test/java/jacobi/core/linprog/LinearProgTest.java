/*
 * The MIT License
 *
 * Copyright 2017 Y.K. Chan
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
package jacobi.core.linprog;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Op;
import jacobi.api.ext.Prop;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/LinearProgTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class LinearProgTest {
	
	@JacobiInject(0)
	public Matrix c;
	
	@JacobiInject(1)
	public Matrix a;
	
	@JacobiInject(2)
	public Matrix b;
	
	@JacobiResult(10)
	public Matrix ans;
	
	@JacobiInject(11)
	public Matrix target;

    @Test
    @JacobiImport("4x7")
    @JacobiEquals(expected = 10, actual = 10)
    public void test4x7() {
    	this.ans = new LinearProg().compute(c, a, b).get();
    	double optimal = this.ans
    			.ext(Prop.class).transpose()
    			.ext(Op.class).mul(this.c)
    			.get().get(0, 0);
    	Assert.assertTrue(optimal - this.target.get(0, 0) > -1e-12);
    }
    
    @Test
    @JacobiImport("3x8")
    @JacobiEquals(expected = 10, actual = 10)
    public void test3x8() {
    	this.ans = new LinearProg().compute(c, a, b).get();
    	double optimal = this.ans
    			.ext(Prop.class).transpose()
    			.ext(Op.class).mul(this.c)
    			.get().get(0, 0);
    	Assert.assertTrue(optimal - this.target.get(0, 0) > -1e-12);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullObjective() {
    	new LinearProg().compute(null, Matrices.zeros(0), Matrices.zeros(0));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullConstraint() {
    	new LinearProg().compute(Matrices.zeros(0), null, Matrices.zeros(0));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullBoundary() {
    	new LinearProg().compute(Matrices.zeros(0), Matrices.zeros(0), null);
    }
    
    @Test
    @JacobiImport("3x8")
    public void testEmptyObjective() {
    	Assert.assertFalse(new LinearProg().compute(Matrices.zeros(0), a, b).isPresent());
    }        
    
    @Test
    @JacobiImport("3x8")
    public void testEmptyConstraintAndBoundary() {
    	Assert.assertFalse(new LinearProg().compute(c, Matrices.zeros(0), Matrices.zeros(0)).isPresent());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDimensionMismatchConstraintAndBoundary() {
    	new LinearProg().compute(Matrices.zeros(1, 8), Matrices.zeros(3, 8), Matrices.zeros(4, 1));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDimensionMismatchObjectiveAndConstraint() {
    	new LinearProg().compute(Matrices.zeros(1, 7), Matrices.zeros(3, 8), Matrices.zeros(4, 1));
    }
    
}

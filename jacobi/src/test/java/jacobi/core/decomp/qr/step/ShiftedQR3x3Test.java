/*
 * Copyright (C) 2016 Y.K. Chan
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

package jacobi.core.decomp.qr.step;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/ShiftedQR3x3Test.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class ShiftedQR3x3Test {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiResult(1)
    public Matrix output;
    
    @Test
    @JacobiImport("eig-test-1")
    @JacobiEquals(expected = 1, actual = 1, epsilon = 1e-10)
    public void test1Eig3x3() {        
        double eig = new ShiftedQR3x3(new DefaultQRStep()).eig3x3(input, 0);
        this.output = Matrices.scalar(eig);
    }
    
    @Test
    @JacobiImport("eig-test-2")
    @JacobiEquals(expected = 1, actual = 1, epsilon = 1e-10)
    public void test2Eig3x3() {        
        double eig = new ShiftedQR3x3(new DefaultQRStep()).eig3x3(input, 0);
        this.output = Matrices.scalar(eig);
    }
    
    @Test
    @JacobiImport("eig-test-3")
    @JacobiEquals(expected = 1, actual = 1, epsilon = 1e-10)
    public void test3Eig3x3() {        
        double eig = new ShiftedQR3x3(new DefaultQRStep()).eig3x3(input, 0);
        this.output = Matrices.scalar(eig);
    }
    
    @Test
    @JacobiImport("eig-test-4")
    @JacobiEquals(expected = 1, actual = 1, epsilon = 1e-10)
    public void test4Eig3x3() {        
        double eig = new ShiftedQR3x3(new DefaultQRStep()).eig3x3(input, 0);
        this.output = Matrices.scalar(eig);
    }
    
    @Test
    public void testSolvingDepressed() {
        this.solveDepressedCubicTest(6, -20);
        this.solveDepressedCubicTest(6, 20);
        this.solveDepressCubicTestAllRealRoots(1.0, 2.0);
        this.solveDepressCubicTestAllRealRoots(-3.0, -4.0);
        this.solveDepressCubicTestAllRealRoots(-0.5, 1.0);
        
        this.solveDepressedCubicTest(6, 0);
        this.solveDepressedCubicTest(0, 9);
    }
    
    @Test
    public void testSolvingCubicSingleRealRoot() {
        this.solveCubicSingleRealRootTest(3.0, -1.0, 1.0);
        this.solveCubicSingleRealRootTest(Math.PI, -1.0, 1.0);
        this.solveCubicSingleRealRootTest(-Math.E, -1.0, 1.0);
        this.solveCubicSingleRealRootTest(0.0, -1.0, 1.0);
        
        this.solveCubicSingleRealRootTest(-Math.PI, 2.0, 4.0);
        this.solveCubicSingleRealRootTest(Math.E, 2.0, 4.0);
        this.solveCubicSingleRealRootTest(Math.sqrt(2.0), 2.0, 4.0);
    }
    
    @Test
    public void testSolvingCubicAllRealRoot() {
        this.solveCubicAllRealRootTest(Math.E, Math.PI, Math.sqrt(2.0));
        this.solveCubicAllRealRootTest(Math.E, -Math.PI, Math.sqrt(2.0));
        this.solveCubicAllRealRootTest(-Math.E, Math.PI, -Math.sqrt(2.0));
        this.solveCubicAllRealRootTest(-Math.E, -Math.PI, -Math.sqrt(2.0));
        
        this.solveCubicAllRealRootTest(1.0, 3.0, 7.0);
        this.solveCubicAllRealRootTest(-2.0, -4.0, 6.0);
    }
    
    public void solveCubicSingleRealRootTest(double realRoot, double linearCoeff, double constTerm) {
        double root = new ShiftedQR3x3(new DefaultQRStep()).solveCubic(
                linearCoeff - realRoot, 
                constTerm - realRoot * linearCoeff, 
                - realRoot * constTerm);
        double delta = linearCoeff * linearCoeff - 4.0 * constTerm;
        if(delta < 0.0){
            Assert.assertEquals(realRoot, root, 1e-12);
        }else{
            double root1 = (-linearCoeff + Math.sqrt(delta))/4.0;
            double root2 = (-linearCoeff - Math.sqrt(delta))/4.0;
            Assert.assertTrue(
                Math.abs(root - realRoot) < 1e-12
             || Math.abs(root1 - realRoot) < 1e-12
             || Math.abs(root2 - realRoot) < 1e-12
            );
        }
    }
    
    public void solveCubicAllRealRootTest(double r0, double r1, double r2) {
        double quadCoeff = -(r0 + r1 + r2);
        double linearCoeff = r0 * r1 + r0 * r2 + r1 * r2;
        double constTerm = -(r0 * r1 * r2);
        
        double root = new ShiftedQR3x3(new DefaultQRStep()).solveCubic(quadCoeff, linearCoeff, constTerm);
        System.out.println("root = " + root);
        Assert.assertTrue(
                Math.abs(root - r0) < 1e-12
             || Math.abs(root - r1) < 1e-12
             || Math.abs(root - r2) < 1e-12 );
    }
    
    public void solveDepressedCubicTest(double a, double b){
        ShiftedQR3x3 step = new ShiftedQR3x3(new DefaultQRStep());
        double root = step.solveDepressedCubic(a, b);
        Assert.assertTrue(Math.abs(root * root * root + a*root + b) < 1e-12);
    }

    public void solveDepressCubicTestAllRealRoots(double r0, double r1) {
        double r2 = -r0 - r1;
        double p = r0 * r1 + r0 * r2 + r1 * r2;
        double q = -r0 * r1 * r2;
        
        double root = new ShiftedQR3x3(new DefaultQRStep()).solveDepressedCubic(p, q);
        Assert.assertTrue(
            Math.abs( root - r0 ) < 1e-12 
         || Math.abs( root - r1 ) < 1e-12 
         || Math.abs( root - r2 ) < 1e-12 
        );
    }
}

/* 
 * The MIT License
 *
 * Copyright 2019 Y.K. Chan
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
package jacobi.core.solver.nonlin;

import java.util.Arrays;
import java.util.function.Supplier;

import jacobi.core.impl.ColumnVector;
import jacobi.core.util.Weighted;

/**
 * Simple implementation of an iterative optimizer.
 * 
 * <p>This class iteratives an optimizer step until the magnitude
 * of the delta vector fell below the error tolerance, or number of iteration exhausted.
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public class SimpleIterativeOptimizer implements IterativeOptimizer {
    
    /**
     * Constructor.
     * @param stepFactory
     */
    public SimpleIterativeOptimizer(Supplier<IterativeOptimizerStep> stepFactory) {
        this.stepFactory = stepFactory;
    }

    @Override
    public Weighted<ColumnVector> optimize(VectorFunction func, 
            Supplier<double[]> init, 
            long limit, double epsilon) {
        IterativeOptimizerStep step = this.stepFactory.get();
        
        double[] start = init.get();
        double[] curr = Arrays.copyOf(start, start.length);
        
        double error = 1.0;
        
        double[] ans = start;
        double minFx = func.at(start);
        
        for(long iter = 0; iter < limit; iter++) {
        	double[] dx = step.delta(func, curr);
            curr = this.move(curr, dx);
            
            double fx = func.at(curr);
            if(ans == null || fx < minFx){
            	ans = Arrays.copyOf(curr, curr.length);
            	minFx = fx;
            }
            
            error = this.norm(dx);
            if(error < epsilon) {
            	break;
            }
        }
        
        return ans == null
        	? new Weighted<>(new ColumnVector(curr), func.at(curr))
        	: new Weighted<>(new ColumnVector(ans), minFx);
    }
    
    /**
     * Find the L-1 Norm of a vector
     * @param vector  Input vector
     * @return  Value of the L-1 Norm
     */
    protected double norm(double[] vector) {
        double max = Math.abs(vector[0]);
        for(int i = 1; i < vector.length; i++) {
            if(max < Math.abs(vector[i])) {
                max = Math.abs(vector[i]);
            }
        }
        return max;
    }
    
    /**
     * Move the position vector by a delta vector
     * @param pos  Position vector
     * @param dx  Delta vector
     * @return  Instance of position vector with values updated
     */
    protected double[] move(double[] pos, double[] dx) {
    	for(int i = 0; i < pos.length; i++) {
        	pos[i] += dx[i];
        }
        return pos;
    }

    private Supplier<IterativeOptimizerStep> stepFactory;
}

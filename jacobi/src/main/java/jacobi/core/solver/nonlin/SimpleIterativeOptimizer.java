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
import java.util.Optional;
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
    public Optional<ColumnVector> optimize(VectorFunction func, 
            Supplier<double[]> init, 
            long limit, double epsilon) {
        IterativeOptimizerStep step = this.stepFactory.get();
        
        double[] start = init.get();
        double[] curr = Arrays.copyOf(start, start.length);
        
        for(long iter = 0; iter < limit; iter++) {
            curr = this.move(curr, step.delta(func, curr));
            if(this.norm(func.grad(curr).getVector()) < epsilon) {
                return Optional.of(new ColumnVector(curr));
            }
        }
        
        return Optional.empty();
    }
    
    protected double[] move(double[] position, double[] dx) {
        for(int i = 0; i < position.length; i++) {
            position[i] += dx[i];
        }
        return position;
    }
    
    protected double norm(double[] vector) {
        double max = Math.abs(vector[0]);
        for(int i = 1; i < vector.length; i++) {
            if(max < Math.abs(vector[i])) {
                max = Math.abs(vector[i]);
            }
        }
        return max;
    }

    private Supplier<IterativeOptimizerStep> stepFactory;
}

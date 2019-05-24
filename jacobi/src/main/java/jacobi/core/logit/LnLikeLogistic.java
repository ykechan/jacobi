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
package jacobi.core.logit;

import jacobi.api.Matrix;
import jacobi.core.solver.nonlin.SumLinearArgFunc;
import jacobi.core.util.Throw;

/**
 * Negated Log of Likelihood function of the logistic probabilistic model in a weighted Bernoulli trial. 
 * 
 * <p>
 * Consider the logistic function p(x) = 1/(1 + e<sup>-x</sup>)<br>
 * 
 * p'(x) = -e<sup>-x</sup> -1/(1 + e<sup>-x</sup>)<sup>2</sup><br>
 *       = e<sup>-x</sup>/(1 + e<sup>-x</sup>)<sup>2</sup><br>
 *       = p(x)q(x), where q(x) = 1 - p(x)<br>
 * </p>
 * 
 * <p>
 * Given observations y<sub>k</sub> &isin; {0, 1}, the likelihood of the model is<br>
 * 
 * L(x) = &prod;p<sup>y<sub>k</sub></sup>(x)q<sup>1-y<sub>k</sub></sup>(x)<br>
 * 
 * Let f(x) = lnL(x) = &sum;y<sub>k</sub>ln(p) + (1 - y<sub>k</sub>)ln(q)<br>
 * 
 * f'(x) = &sum;y<sub>k</sub>p'/p + (1 - y<sub>k</sub>)q'/q<br>
 *       = &sum;y<sub>k</sub>q - (1 - y<sub>k</sub>)p<br>
 *       = &sum;y<sub>k</sub>(1 - p) - (1 - y<sub>k</sub>)p<br>
 *       = &sum;y<sub>k</sub> - p<br>
 *       
 * f''(x) = &sum;-p(x)q(x)<br>
 * </p>
 * 
 * <p>
 * The variable x is a linear combination of some variables X and some observations A, i.e.
 * x = &lt;A, X&gt; where A is matrix and X is a column vector. 
 * </p>
 * 
 * <p>This class represents g(x) = -f(x) s.t. a maximum likelihood estimate can be
 * obtained if g(x) is minimized.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class LnLikeLogistic extends SumLinearArgFunc<double[]> {
    
    /**
     * Constructor.
     * @param consts  Constant coefficients for arguments
     * @param weights  Signed weights with signs representing outcomes
     */
    protected LnLikeLogistic(Matrix consts, double[] weights) {
        super(consts);
        this.weights = weights;
    }

    @Override
    protected double valueAt(double[] inter, int index, double x) {        
        return this.weights[index] < 0.0 
            ?  this.weights[index] * Math.log(1.0 - inter[index])
            : -this.weights[index] * Math.log(inter[index]);
    }

    @Override
    protected double slopeAt(double[] inter, int index, double x) {
        return this.weights[index] < 0.0 
                ? -this.weights[index] * inter[index]
                :  this.weights[index] * (inter[index] - 1.0);
    }

    @Override
    protected double convexityAt(double[] inter, int index, double x) {
        return Math.abs(this.weights[index]) * inter[index] * (1.0 - inter[index]);
    }

    @Override
    protected double[] prepare(double[] pos, double[] args) {
        double[] probs = new double[args.length];
        for(int i = 0; i < probs.length; i++) {
            probs[i] = 1.0 / (1.0 + Math.exp(-args[i]));
        }
        return probs;
    }

    private double[] weights;
}

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

import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import java.util.Optional;
import java.util.function.ToIntBiFunction;
import java.util.stream.IntStream;

/**
 * Implementation of Standard Simplex algorithm.
 * 
 * The Linear Programming problem is as follows:
 * Maximize c^t * x s.t. A*x &lt;= b, x &gt;= 0, for some matrix A, and column vector b and c.
 * 
 * The simplex algorithm works as follows:
 * 
 * Add a set of slack variable s s.t. A*x + I*s = b, s &gt;= 0. If b &gt;= 0, the trivial solution
 * [x s] = [0 b] is feasible. Find a k s.t. c[k] &gt; 0, therefore x[k] can be increased. 
 * 
 * To maintain the equality, s[i] needed to be decreased. Since s = b, and s &gt;= 0, i must be chosen s.t. 
 * b[i] / A[i, k] is minimum for all b[i] &gt; 0. If b[i] &lt; 0, s[i] would be increased and poses no constraint.
 * If b[i] &lt; 0 for all i, the problem is unbounded since the value can increase to infinity.
 * 
 * If c[k] &lt;= 0 for all k, the trivial solution [0 b] is already optimal.
 * 
 * The simplex algorithm evolves the problem by linear transformation s.t. the trivial solution in the next stage
 * of the problem is the trivial solution in this stage + the increase in x[k]. Consider the aforementioned step
 * a swap of non-zero-valued variable and zero-valued variable in the trivial solution. 
 * 
 * This would be the case if A^k -&gt; e^i and I^k -&gt; A*^k in [A I] -&gt; [A* J], where A^k and I^k is the k-th column 
 * of A and I respectively, and e^i is the i-th standard basis. [A* J] can be obtained by applying linear transformation
 * T*[A I] so that the problem is invariant under this transformation.
 * 
 * If b[j] &lt; 0 for some j, [0 b] is not feasible. In such cases a auxiliary scalar variable t &gt;= 0 is added, i.e.
 * A*x + I*s - t*1' = b, where 1' = {1, 1, ...}. There is a solution [x s t] = [0 b - |min(b)| |min(b)|], however this
 * unlike normal trivial solution, there is a non-zero-valued variable t with non-standard basis {-1, -1...} 
 * in the constraint. Again, this can be fixed by swapping t with some s[k] s.t. b[k] = min(b).
 * 
 * Sometimes, finding an entering variable k may be expensive. In cases when there are many variables, it would be 
 * beneficial to find a number of entering variables in the order of effectiveness, and expire this set when many of 
 * them are not valid anymore.
 * 
 * @author Y.K. Chan
 */
public class StandardSimplex {
    
    /**
     * Default fraction of number of enter variable pool to number of basic variables.
     */
    public static final double DEFAULT_POOL_FACTOR = 0.1;
    
    /**
     * Default fraction of number of failed enter variables to expire the pool.
     */
    public static final double DEFAULT_EXPIRE_FACTOR = 0.1;
    
    /**
     * Constructor.
     * @param limitFactor  Iteration stopping factor
     * @param pivotingRule   Pivoting rule implementation
     */
    public StandardSimplex(long limitFactor, PivotingRule pivotingRule) {
        this(DEFAULT_POOL_FACTOR, DEFAULT_EXPIRE_FACTOR, limitFactor, pivotingRule);
    }

    /**
     * Constructor.
     * @param poolFactor  Fraction of number of enter variable pool to number of basic variables.
     * @param expireFactor  Fraction of number of failed enter variables to expire the pool.
     * @param limitFactor  Iteration stopping factor
     * @param pivotingRule  Pivoting rule implementation
     */ 
    public StandardSimplex(double poolFactor, double expireFactor, long limitFactor, PivotingRule pivotingRule) {
        this.poolFactor = poolFactor;
        this.expireFactor = expireFactor;
        this.limitFactor = limitFactor;
        this.pivotingRule = pivotingRule;
        this.leavingRule = new LeavingRule();
        this.pivoting = new ElementaryPivoting();
    }
    
    /**
     * Find the optimal solution for LP max c^t * x s.t. A * x &lt;= b, x &gt;= 0.
     * @param c  Objective coefficient
     * @param a  Constraint matrix
     * @param b  Constraint boundary
     * @return  Optimal solution or empty if problem is unbounded/infeasible.
     */
    public Optional<Matrix> compute(Matrix c, Matrix a, Matrix b) { 
        boolean needAux = this.containsNegative(b);        
        int poolMax = Math.max(1, (int) Math.floor(this.poolFactor * a.getRowCount()));         
        long limit = limitFactor * a.getColCount() * a.getRowCount();
        MutableTableau tab = MutableTableau.build(needAux).use(this.pivoting).of(c, a, b);
        return this.simplex(tab, needAux, limit, poolMax)
                .flatMap((t) -> needAux ? t.collapse() : Optional.of(t))
                .flatMap((t) -> needAux 
                        ? this.simplex(t, false, limit, poolMax)
                        : Optional.of(t) )
                .map((t) -> this.getSolution(t)); 
    }
    
    /**
     * Find the solution using Simplex Algorithm.
     * @param tab  Tableau representation of the LP
     * @param isAux  True if this is an auxiliary problem, false otherwise
     * @param limit  Maximum number of iteration 
     * @param poolMax  Maximum number of enter variable pool.
     * @return  Tableau is its stopped state, or empty if problem is unbounded/infeasible
     */
    protected Optional<MutableTableau> simplex(MutableTableau tab, boolean isAux, long limit, int poolMax) {        
        if(isAux){
            // pivot on the auxiliary variable first
            tab = tab.pivot(this.auxLeavingRule(tab), tab.getMatrix().getColCount() - 2);
        }
        for(long k = 0; k < limit; k++){
            int fail = 0;
            int[] pivots = this.pivotingRule.apply(tab, poolMax);
            if(pivots.length == 0){
                return Optional.of(tab);
            }
            int expire = Math.max(0, (int) Math.floor(this.expireFactor * pivots.length));            
            for(int p : pivots){
                if(tab.getCoeff(p) < 0.0){
                    if(++fail > expire){
                        break;
                    }
                    continue;
                }
                int leave = this.leavingRule.applyAsInt(tab, p);
                if(leave < 0){
                    return Optional.empty();
                }
                tab = tab.pivot(leave, p);
            }
        }
        throw new IllegalStateException("Exhaused computational limit (" + limit + ")");
    }
    
    /**
     * Find the trivial solution of a given Tableau.
     * @param tab  LP problem in Tableau form.
     * @return  Trivial solution [0 b] as a column vector
     */
    protected Matrix getSolution(Tableau tab) {
        Matrix mat = tab.getMatrix();
        int last = mat.getColCount() - 1;
        double[] sol = new double[mat.getColCount() - 1];
        int[] vars = tab.getVars();
        for(int i = 0; i < mat.getRowCount(); i++){
            if(vars[sol.length + i] < sol.length){
                sol[vars[sol.length + i]] = mat.get(i, last);
            }
        }
        return new ColumnVector(sol);
    }
    
    /**
     * Find if there is any negative entry in constraint boundary b.
     * @param b  Constraint boundary
     * @return  True if it contains at least 1 negative entry, false otherwise
     */
    protected boolean containsNegative(Matrix b) {
        return IntStream.range(0, b.getRowCount())
                .mapToDouble((i) -> b.get(i, 0))
                .filter((v) -> v < 0.0)
                .findAny()
                .isPresent();
    }
    
    /**
     * Leaving rule for auxiliary problem, which is bounded by the constraint boundary.
     * @param tab  Tableau representation of the LP 
     * @return  Leaving column
     */
    protected int auxLeavingRule(Tableau tab) {
        int leave = -1;
        double min = 0.0;
        Matrix mat = tab.getMatrix();
        int last = mat.getColCount() - 1;
        for(int i = 0; i < mat.getRowCount(); i++){
            double bound = mat.get(i, last);
            if(bound < min){
                leave = i;
                min = bound;
            }
        }
        return leave;
    }

    private double poolFactor, expireFactor;
    private long limitFactor;
    private PivotingRule pivotingRule;
    private ToIntBiFunction<Tableau, Integer> leavingRule;
    private MutableTableau.Pivoting pivoting;
}

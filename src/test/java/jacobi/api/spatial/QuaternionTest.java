package jacobi.api.spatial;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.util.Real;

public class QuaternionTest {
    
    @Test
    public void testMulExampleCase1() {
        // this example comes from MATLAB documentation for quatmultiply 
        this.assertEquals(new Quaternion(0.5, 1.25, 1.5, 0.25), 
            new Quaternion(1, 0, 1, 0).mul(new Quaternion(1, 0.5, 0.5, 0.75)));
    }
    
    @Test
    public void testMulExampleCase2() {
     // this example comes from MATLAB documentation for quatmultiply
        this.assertEquals(new Quaternion(0, 0, 2, 0), 
            new Quaternion(1, 0, 1, 0).mul(new Quaternion(1, 0, 1, 0)));
    }
    
    @Test
    public void testIorJorKSquareIsNegative1() {
        Quaternion[] basis = {
            new Quaternion(1.0, 0.0, 0.0, 0.0),
            new Quaternion(0.0, 1.0, 0.0, 0.0),
            new Quaternion(0.0, 0.0, 1.0, 0.0),
            new Quaternion(0.0, 0.0, 0.0, 1.0)            
        };
        
        for(int i = 1; i < basis.length; i++) {
            this.assertEquals(new Quaternion(-1, 0, 0, 0), basis[i].mul(basis[i]));
        }
    }
    
    @Test
    public void test1IsMultiplicationIdentity() {
        Quaternion[] basis = {
            new Quaternion(1.0, 0.0, 0.0, 0.0),
            new Quaternion(0.0, 1.0, 0.0, 0.0),
            new Quaternion(0.0, 0.0, 1.0, 0.0),
            new Quaternion(0.0, 0.0, 0.0, 1.0)            
        };
        
        for(int i = 1; i < basis.length; i++) {
            this.assertEquals(basis[i], basis[0].mul(basis[i]));
            this.assertEquals(basis[i], basis[i].mul(basis[0]));
        }
    }
    
    @Test
    public void testProductFollowsLeviCivitaPositiveOrder() {
        Quaternion[] basis = {
            new Quaternion(0.0, 1.0, 0.0, 0.0),
            new Quaternion(0.0, 0.0, 1.0, 0.0),
            new Quaternion(0.0, 0.0, 0.0, 1.0)            
        };
           
        for(int i = 0; i < basis.length; i++) {            
            this.assertEquals(basis[(i + 2) % basis.length], basis[i].mul(basis[(i + 1) % basis.length]));
        }
    }
    
    @Test
    public void testProductFollowsLeviCivitaNegativeOrder() {
        Quaternion[] basis = {
            new Quaternion(0.0, 1.0, 0.0, 0.0),
            new Quaternion(0.0, 0.0, 1.0, 0.0),
            new Quaternion(0.0, 0.0, 0.0, 1.0)            
        };
        
        List<int[]> order = Arrays.asList(
            new int[] {2, 1, 0},
            new int[] {1, 0, 2},
            new int[] {0, 2, 1}
        );
        
        
    }
    
    private boolean isEquals(Quaternion p, Quaternion q) {
        return Real.isNegl(p.a - q.a)
            && Real.isNegl(p.i - q.i)
            && Real.isNegl(p.j - q.j)
            && Real.isNegl(p.k - q.k)
            ;
    }
    
    private void assertEquals(Quaternion p, Quaternion q) {
        Assert.assertEquals(p.a, q.a, Real.EPSILON);
        Assert.assertEquals(p.i, q.i, Real.EPSILON);
        Assert.assertEquals(p.j, q.j, Real.EPSILON);
        Assert.assertEquals(p.k, q.k, Real.EPSILON);
    }

}

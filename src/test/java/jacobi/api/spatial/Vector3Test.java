package jacobi.api.spatial;

import java.util.Random;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Op;
import jacobi.core.util.Real;
import jacobi.test.util.Jacobi;

import org.junit.Assert;
import org.junit.Test;

public class Vector3Test {
    
    @Test
    public void shouldCrossProductOf2ElementaryBasisVectorShouldYieldTheOtherBasisVector() {
        this.assertEquals(Vector3.Z, Vector3.X.cross(Vector3.Y));
        this.assertEquals(Vector3.X, Vector3.Y.cross(Vector3.Z));
        this.assertEquals(Vector3.Y, Vector3.Z.cross(Vector3.X));        
    }
    
    @Test
    public void shouldDotProductOfOrthonormalBasisVectorsAreZero() {
        Assert.assertEquals(0.0, Vector3.X.dot(Vector3.Y), Real.EPSILON);
        Assert.assertEquals(0.0, Vector3.Y.dot(Vector3.Z), Real.EPSILON);
        Assert.assertEquals(0.0, Vector3.Z.dot(Vector3.X), Real.EPSILON);
    }
    
    @Test
    public void shouldBeAbleToInvokeDotProductFromFacade() {
        Vector3 a = new Vector3(1.0, 2.0, 3.0);
        Vector3 b = new Vector3(4.0, 5.0, 6.0);
        
        Assert.assertEquals(a.dot(b), a.ext(Op.class)
                .dot(b).get().get(0, 0), Real.EPSILON);
    }
    
    @Test
    public void shouldBeAbleToInvokeDotProductFromFacadeWithMatrixArg() {
    	Vector3 a = new Vector3(1.0, 2.0, 3.0);
    	Assert.assertEquals(a.dot(new Vector3(4.0, 5.0, 6.0)), 
    		a.ext(Op.class)
             .dot(Matrices.wrap(new double[][] { {4.0}, {5.0}, {6.0} })).get().get(0, 0), Real.EPSILON
        );
    }
    
    @Test
    public void shouldCrossProductShouldProduceOrthogonalVectorToOperands() {
        Random rand = new Random(Double.doubleToRawLongBits(-Math.E));
        for(int i = 0; i < 64; i++) {
            Vector3 a = new Vector3(
                1000 * rand.nextDouble(), 
                1000 * rand.nextDouble(), 
                1000 * rand.nextDouble()
            );
            Vector3 b = new Vector3(
               1000 * rand.nextDouble(), 
               1000 * rand.nextDouble(), 
               1000 * rand.nextDouble()
            );
            Vector3 c = a.cross(b);
            
            Assert.assertEquals(0.0, c.dot(a)/c.dot(c), Real.EPSILON);
            Assert.assertEquals(0.0, c.dot(b)/c.dot(c), Real.EPSILON);
        }
                
    }
    
    @Test
    public void shouldVector3EqualsToA3x1MatrixForAllElements() {
    	Jacobi.assertEquals(Matrices.wrap(new double[][] {
    		{Math.PI}, {Math.E}, {Math.sqrt(2.0)}
    	}), new Vector3(Math.PI, Math.E, Math.sqrt(2.0)));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenAccessNonZeroColumn() {
    	new Vector3(Math.PI, Math.E, Math.sqrt(2.0)).get(0, 1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenAccessOutsideRowByElementAccess() {
    	double ans = 0.0;
    	try {	    	
	    	Vector3 v = new Vector3(Math.PI, Math.E, Math.sqrt(2.0));
    	
	    	ans += v.get(0, 0);
	    	ans += v.get(1, 0);
	    	ans += v.get(2, 0);
	    	
	    	ans += v.get(3, 0);
    	} finally {
    		Assert.assertEquals(ans, Math.PI + Math.E + Math.sqrt(2.0), 1e-12);
    	}
    }    
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenAccessOutsideRowByRowAccess() {
    	double ans = 0.0;
    	try {	    	
	    	Vector3 v = new Vector3(Math.PI, Math.E, Math.sqrt(2.0));
    	
	    	ans += v.getRow(0)[0];
	    	ans += v.getRow(1)[0];
	    	ans += v.getRow(2)[0];
	    	
	    	ans += v.getRow(3)[0];
    	} finally {
    		Assert.assertEquals(ans, Math.PI + Math.E + Math.sqrt(2.0), 1e-12);
    	}
    }
    
    @Test
    public void shouldMulShouldYieldANewVector() {
        Vector3 u = new Vector3(1.1, 2.2, 1/3.0);
        Vector3 v = u.mul(3.0);
        
        this.assertEquals(new Vector3(1.1, 2.2, 1/3.0),  u);
        this.assertEquals(new Vector3(3.3, 6.6, 1.0),  v);
    }
    
    public void assertEquals(Vector3 u, Vector3 v) {
        Assert.assertEquals(u.x, v.x, Real.EPSILON);
        Assert.assertEquals(u.y, v.y, Real.EPSILON);
        Assert.assertEquals(u.z, v.z, Real.EPSILON);
    }

}

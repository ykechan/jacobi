package jacobi.api.spatial;

import java.util.Random;

import jacobi.api.ext.Op;
import jacobi.core.util.Real;
import org.junit.Assert;
import org.junit.Test;

public class Vector3Test {
    
    @Test
    public void testCrossProductOf2ElementaryBasisVectorShouldYieldTheOtherBasisVector() {
        this.assertEquals(Vector3.Z, Vector3.X.cross(Vector3.Y));
        this.assertEquals(Vector3.X, Vector3.Y.cross(Vector3.Z));
        this.assertEquals(Vector3.Y, Vector3.Z.cross(Vector3.X));        
    }
    
    @Test
    public void testDotProductOfOrthonormalBasisVectorsAreZero() {
        Assert.assertEquals(0.0, Vector3.X.dot(Vector3.Y), Real.EPSILON);
        Assert.assertEquals(0.0, Vector3.Y.dot(Vector3.Z), Real.EPSILON);
        Assert.assertEquals(0.0, Vector3.Z.dot(Vector3.X), Real.EPSILON);
    }
    
    @Test
    public void testInvokeDotProductFromFacade() {
        Vector3 a = new Vector3(1.0, 2.0, 3.0);
        Vector3 b = new Vector3(4.0, 5.0, 6.0);
        
        Assert.assertEquals(a.dot(b), a.ext(Op.class)
                .dot(b).get().get(0, 0), Real.EPSILON);
    }
    
    @Test
    public void testCrossProductShouldProduceOrthogonalVectorToOperands() {
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
    public void testMulShouldYieldANewVector() {
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

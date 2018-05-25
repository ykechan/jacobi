package jacobi.core.filter.fft;

import org.junit.Assert;
import org.junit.Test;

public class ComplexTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testUnityOfRoot8IsImmutable() {
        Complex.ROOTS_OF_UNITY_8.set(1, Complex.ONE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnityOfRoot16IsImmutable() {
        Complex.ROOTS_OF_UNITY_12.set(2, Complex.ONE);
    }

    @Test
    public void testRootsOfUnity8Value() {
        Assert.assertEquals(8, Complex.ROOTS_OF_UNITY_8.size());
        for(int i = 0; i < 8; i++){
            Assert.assertEquals(Math.cos(2*i*Math.PI / 8), Complex.ROOTS_OF_UNITY_8.get(i).real, 1e-12);
            Assert.assertEquals(Math.sin(2*i*Math.PI / 8), Complex.ROOTS_OF_UNITY_8.get(i).imag, 1e-12);
        }
    }

    @Test
    public void testRootsOfUnity12Value() {
        Assert.assertEquals(12, Complex.ROOTS_OF_UNITY_12.size());
        for(int i = 0; i < 12; i++){
            Assert.assertEquals(Math.cos(2*i*Math.PI / 12), Complex.ROOTS_OF_UNITY_12.get(i).real, 1e-12);
            Assert.assertEquals(Math.sin(2*i*Math.PI / 12), Complex.ROOTS_OF_UNITY_12.get(i).imag, 1e-12);
        }
    }

    @Test
    public void testRootsOfUnity1And2And4And6And8And12() {
        this.assertRootsOfUnity(Complex.rootOfUnity(1));
        this.assertRootsOfUnity(Complex.rootOfUnity(2));
        this.assertRootsOfUnity(Complex.rootOfUnity(4));
        this.assertRootsOfUnity(Complex.rootOfUnity(6));
        this.assertRootsOfUnity(Complex.rootOfUnity(8));
        this.assertRootsOfUnity(Complex.rootOfUnity(12));
    }

    @Test
    public void testRootsOfUnityOfSmallPrimes() {
        this.assertRootsOfUnity(Complex.rootOfUnity(3));
        this.assertRootsOfUnity(Complex.rootOfUnity(5));
        this.assertRootsOfUnity(Complex.rootOfUnity(7));
        this.assertRootsOfUnity(Complex.rootOfUnity(11));
    }

    protected void assertRootsOfUnity(Complex[] roots) {
        for(int i = 0; i < roots.length; i++){
            Assert.assertEquals(Math.cos(2*i*Math.PI / roots.length), roots[i].real, 1e-12);
            Assert.assertEquals(Math.sin(2*i*Math.PI / roots.length), roots[i].imag, 1e-12);
        }
    }

}

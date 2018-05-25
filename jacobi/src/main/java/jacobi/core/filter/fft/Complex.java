package jacobi.core.filter.fft;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class Complex {

    private static final double ROOT2 = Math.sqrt(2.0);

    private static final double ROOT3 = Math.sqrt(3.0);

    public static final Complex ONE = Complex.of(1.0, 0.0);

    public static final List<Complex> ROOTS_OF_UNITY_8 = Collections.unmodifiableList(Arrays.asList(
       ONE,
       Complex.of(1.0/ROOT2, 1.0/ROOT2),
       Complex.of(0.0, 1.0),
       Complex.of(-1.0/ROOT2, 1.0/ROOT2),
       Complex.of(-1.0, 0.0),
       Complex.of(-1.0/ROOT2, -1.0/ROOT2),
       Complex.of(0.0, -1.0),
       Complex.of(1.0/ROOT2, -1.0/ROOT2)
    ));

    public static final List<Complex> ROOTS_OF_UNITY_12 = Collections.unmodifiableList(Arrays.asList(
        ONE,
        Complex.of(ROOT3/2.0, 0.5),
        Complex.of(0.5, ROOT3/2.0),
        ROOTS_OF_UNITY_8.get(2),
        Complex.of(-0.5, ROOT3/2.0),
        Complex.of(-ROOT3/2.0, 0.5),
        ROOTS_OF_UNITY_8.get(4),
        Complex.of(-ROOT3/2.0, -0.5),
        Complex.of(-0.5, -ROOT3/2.0),
        ROOTS_OF_UNITY_8.get(6),
        Complex.of(0.5, -ROOT3/2.0),
        Complex.of(ROOT3/2.0, -0.5)
    ));

    public static Complex[] rootOfUnity(int n) {
        switch(n){
            case 1 :
                return new Complex[]{ONE};
            case 2 :
                return new Complex[]{ROOTS_OF_UNITY_8.get(0), ROOTS_OF_UNITY_8.get(4)};
            case 3 :
                return new Complex[]{ROOTS_OF_UNITY_12.get(0), ROOTS_OF_UNITY_12.get(4), ROOTS_OF_UNITY_12.get(8)};
            case 4 :
                return new Complex[]{
                        ROOTS_OF_UNITY_8.get(0),
                        ROOTS_OF_UNITY_8.get(2),
                        ROOTS_OF_UNITY_8.get(4),
                        ROOTS_OF_UNITY_8.get(6)
                };
            case 6 :
                return new Complex[]{
                        ROOTS_OF_UNITY_12.get(0),
                        ROOTS_OF_UNITY_12.get(2),
                        ROOTS_OF_UNITY_12.get(4),
                        ROOTS_OF_UNITY_12.get(6),
                        ROOTS_OF_UNITY_12.get(8),
                        ROOTS_OF_UNITY_12.get(10)
                };
            case 8 :
                return ROOTS_OF_UNITY_8.toArray(new Complex[8]);
            case 12 :
                return ROOTS_OF_UNITY_12.toArray(new Complex[12]);
            default :
                break;
        }
        return IntStream.range(0, n)
                .mapToObj(i -> Complex.of(Math.cos(2*i*Math.PI / n), Math.sin(2*i*Math.PI / n)))
                .toArray(Complex[]::new);
    }

    public static Complex of(double real, double imag) {
        return new Complex(real, imag);
    }

    public final double real, imag;

    public Complex(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }

}

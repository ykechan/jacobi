package jacobi.api.spatial;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Delegate;
import jacobi.api.annotations.Pure;
import jacobi.api.ext.Op;
import jacobi.core.impl.ImmutableMatrix;

/**
 * A vector in 3-D space.
 * 
 * <p>A vector in 3-D space is a special case of a 3x1 matrix that is so prevalent in 
 * physical / simulation application that warrants it's own implementation.
 * </p>
 * 
 * <p>This class is interoperable with Matrix class but direct usage is recommended 
 * for better performance.</p>
 * 
 * <p>This class is immutable.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class Vector3 extends ImmutableMatrix {
    /**
     * Vector at origin, i.e. 0.
     */
    public static final Vector3 ORIGIN = new Vector3(0.0, 0.0, 0.0);
    
    /**
     * First natural basis, a.k.a. X, e1, [1, 0, 0]^t etc.
     */
    public static final Vector3 X = new Vector3(1.0, 0.0, 0.0);
    
    /**
     * Second natural basis, a.k.a. Y, e2, [0, 1, 0]^t etc.
     */
    public static final Vector3 Y = new Vector3(0.0, 1.0, 0.0);
    
    /**
     * Third natural basis, a.k.a. Z, e3, [0, 0, 1]^t etc.
     */
    public static final Vector3 Z = new Vector3(0.0, 0.0, 1.0);
    
    /**
     * Vector component.
     */
    public final double x, y, z;
    
    /**
     * Constructor.
     * @param x  Component in x-direction
     * @param y  Component in y-direction
     * @param z  Component in z-direction
     */
    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int getRowCount() {
        return 3;
    }

    @Override
    public int getColCount() {
        return 1;
    }
    
    @Override
    public double get(int i, int j) {
        if(j != 0) {
            throw new IllegalArgumentException("Invalid column index " + j);
        }
        switch(i) {
            case 0 :
                return this.x;
            case 1 :
                return this.y;
            case 2 :
                return this.z;
            default :
                break;
        }
        throw new IllegalArgumentException("Invalid row index " + i);
    }

    @Override
    public double[] getRow(int index) {
        switch(index) {
            case 0 :
                return new double[] {this.x};
            case 1 :
                return new double[] {this.y};
            case 2 :
                return new double[] {this.z};
            default :
                break;
        }
        throw new IllegalArgumentException("Invalid row index " + index);
    }
    
    /**
     * Vector addition.
     * @param v  Operand
     * @return  Resultant vector
     */
    @Pure
    @Delegate(facade = Op.class, method = "add")
    public Vector3 add(Vector3 v) {
        return new Vector3(this.x + v.x, this.y + v.y, this.z + v.z);
    }
    
    /**
     * Vector subtraction.
     * @param v  Operand
     * @return  Resultant vector
     */
    @Pure
    @Delegate(facade = Op.class, method = "sub")
    public Vector3 sub(Vector3 v) {
        return new Vector3(this.x - v.x, this.y - v.y, this.z - v.z);
    }
    
    /**
     * Scalar multiplication.
     * @param k  Scalar value
     * @return  Resultant vector
     */
    @Pure
    @Delegate(facade = Op.class, method = "mul")
    public Vector3 mul(double k) {
        return new Vector3(k * this.x, k * this.y, k * this.z);
    }
    
    /**
     * Dot product.
     * @param m  Operand
     * @return  Resultant vector
     */
    @Pure
    @Delegate(facade = Op.class, method = "dot")
    public Matrix dot(Matrix m) {
        if(m instanceof Vector3) {
            return Matrices.scalar(this.dot((Vector3) m));
        }
        if(m.getRowCount() != this.getRowCount()
        || m.getColCount() != this.getColCount()) {
            throw new IllegalArgumentException("Dimension mismatch.");            
        }
        return Matrices.scalar(this.x * m.get(0, 0) + this.y * m.get(1, 0) + this.z * m.get(2, 0));
    }
    
    /**
     * Dot product.
     * @param v  Operand
     * @return  Resultant vector
     */
    public double dot(Vector3 v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }
    
    /**
     * Cross product.
     * @param v  Operand
     * @return  Resultant vector
     */
    public Vector3 cross(Vector3 v) {
        return new Vector3(
            this.y * v.z - this.z * v.y, 
            this.z * v.x - this.x * v.z, 
            this.x * v.y - this.y * v.x
        );
    }        

}

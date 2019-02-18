package jacobi.api.spatial;

public class Quaternion {
    
    public final double a, i, j, k;

    public Quaternion(double a, double i, double j, double k) {
        this.a = a;
        this.i = i;
        this.j = j;
        this.k = k;
    }
    
    public Vector3 imag() {
        return new Vector3(this.i, this.j, this.k);
    }
    
    public Quaternion conj() {
        return new Quaternion(this.a, -this.i, -this.j, -this.k);
    }
    
    public Quaternion mul(Quaternion q) {
        return new Quaternion(
            this.a * q.a - this.i * q.i - this.j * q.j - this.k * q.k,
            this.a * q.i + this.i * q.a + this.j * q.k - this.k * q.j,
            this.a * q.j + this.j * q.a + this.k * q.i - this.i * q.k,
            this.a * q.k + this.k * q.a + this.i * q.j - this.j * q.i
        );
    }   
    
}

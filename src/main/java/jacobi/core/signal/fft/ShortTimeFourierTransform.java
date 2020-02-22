package jacobi.core.signal.fft;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Pure;
import jacobi.core.signal.ComplexVector;
import jacobi.core.util.Pair;
import jacobi.core.util.Throw;

@Pure
public class ShortTimeFourierTransform {
	
	public ShortTimeFourierTransform(DiscreteFourierTransform dft) {
		this.dft = dft;
	}

	public List<Pair> compute(Matrix matRe, Matrix matIm, int width) {
		Throw.when()
			.isNull(() -> matRe, () -> "Missing real part of input signals.")
			.isNull(() -> matIm, () -> "Missing imaginery part of input signals.")
			.isTrue(() -> matRe.getRowCount() != matIm.getRowCount(), () -> "Number of signals mismatch.")
			.isTrue(() -> matRe.getColCount() != matIm.getColCount(), () -> "Dimension mismatch.")
			.isTrue(() -> width > matRe.getColCount(), () -> "Invalid window width " + width);
		
		ComplexVector twiddle = ComplexVector.rootsOfUnity(width);
		UnaryOperator<ComplexVector> func = this.dft.toFunc(width);
		Pair[] results = new Pair[matRe.getRowCount()];
		for(int i = 0; i < results.length; i++) {
			results[i] = this.compute(func, 
				ComplexVector.of(matRe.getRow(i), matIm.getRow(i)), 
				twiddle);
		}
		return Collections.unmodifiableList(Arrays.asList(results));
	}
	
	protected Pair compute(UnaryOperator<ComplexVector> func, ComplexVector sig, ComplexVector twiddle) {
		int width = twiddle.length();
		ComplexVector prev = func.apply(sig.slice(0, width));
		double[][] outRe = new double[sig.length() - width + 1][];
		double[][] outIm = new double[outRe.length][];
		
		outRe[0] = prev.real;
		outIm[0] = prev.imag;
		
		for(int i = 1; i < outRe.length; i++) {
			ComplexVector next = ComplexVector.of(new double[width], new double[width]);
			double deltaRe = sig.real[i + width - 1] - sig.real[i - 1];
			double deltaIm = sig.imag[i + width - 1] - sig.imag[i - 1];
			for(int j = 0; j < width; j++) {
				double re = prev.real[j] + deltaRe;
				double im = prev.imag[j] + deltaIm;
				
				next.real[j] = re * twiddle.real[j] - im * twiddle.imag[j];
				next.imag[j] = re * twiddle.imag[j] + im * twiddle.real[j];
			}
			outRe[i] = next.real;
			outIm[i] = next.imag;
			prev = next;
		}
		
		return Pair.of(Matrices.wrap(outRe), Matrices.wrap(outIm));
	}
	
	private DiscreteFourierTransform dft;
}

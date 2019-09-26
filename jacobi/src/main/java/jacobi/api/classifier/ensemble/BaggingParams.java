package jacobi.api.classifier.ensemble;

public class BaggingParams<P> {
	
	public double samplingRate;
	
	public double dimSpan;
	
	public int stoppingLimit;
	
	public double stoppingDelta;
	
	public P subParams;
	
}

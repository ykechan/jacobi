package jacobi.api.classifier;

import java.util.Arrays;

/**
 * Data class for an instance of data.
 * 
 * This class is immutable.
 * 
 * @author Y.K. Chan
 */
public class Instance {
    
    /**
     * Feature value and outcome value of this instance.
     */
    public final int feature, outcome;
    
    /**
     * Weight of this instance
     */
    public final double weight;

    /**
     * Constructor.
     * @param feature  Feature value
     * @param outcome  Outcome value
     * @param weight  Weight of this instance
     */
    public Instance(int feature, int outcome, double weight) {
        this.feature = feature;
        this.outcome = outcome;
        this.weight = weight;
    }

	@Override
	public int hashCode() {
		return Arrays.hashCode(new Object[] {this.feature, this.outcome, this.weight});
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		
		if(obj instanceof Instance) {
			Instance oth = (Instance) obj;
			return this.feature == oth.feature
				&& this.outcome == oth.outcome
				&& this.weight == oth.weight;
		}
		return false;
	}
    
    
    
}

package jacobi.core.classifier.cart.data;

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
    public int feature, outcome;
    
    /**
     * Weight of this instance
     */
    public double weight;

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
    
}

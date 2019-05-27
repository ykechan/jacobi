package jacobi.core.classifier;

import java.util.function.ToIntFunction;

import jacobi.api.Matrix;

/**
 * Common interface for a supervised learner of classifier.
 * 
 * <p>A classifier in this context is a mapping from a set of attributes in form
 * of a real-valued vector as an array to a discrete category encoded as an integer.</p>
 * 
 * @author Y.K. Chan
 *
 * @param <T> Model class of the classifier
 * 
 */
public interface SupervisedClassifierLearner<T extends ToIntFunction<double[]>> {
    
    /**
     * Learn a classifier from a set of attributes and given classified categories.
     * @param obs  Observations
     * @param outcomes  Outcomes of the observations
     * @return  Classifier model
     */
    public T learn(Matrix obs, int[] outcomes);

}

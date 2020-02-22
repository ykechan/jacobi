package jacobi.core.classifier.nn;

import java.util.Collections;
import java.util.Map;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@JacobiImport("/jacobi/test/data/FFNeuralNetworkTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class FFNeuralNetworkTest {
    
    @JacobiInject(-1)
    public Map<Integer, Matrix> all;
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiResult(100)
    public Matrix output;
    
    
    @Test
    @JacobiImport("test single layer logistic NN")
    @JacobiEquals(expected = 100, actual = 100)
    public void testSingleLayerLogisticNN() {
        new FFNeuralNetwork(Collections.singletonList(
            new FFNeuralNetwork.Layer(all.get(10), this.logistic()))) {

                @Override
                protected double[] flow(Layer layer, double[] input) {
                    double[] result = super.flow(layer, input);
                    output = Matrices.wrap(new double[][] {result});
                    return result;
                }
            
        }.applyAsInt(this.input.getRow(0));
    }
    
    protected Activator logistic() {
        return new Activator() {

            @Override
            public double valueAt(double x) {
                return 1.0/(1.0 + Math.exp(-x));
            }

            @Override
            public double slopeAt(double x) {
                throw new UnsupportedOperationException();
            }
            
        };
    }

}

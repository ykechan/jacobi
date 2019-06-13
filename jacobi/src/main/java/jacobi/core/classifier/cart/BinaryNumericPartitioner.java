package jacobi.core.classifier.cart;

import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.util.Weighted;

public class BinaryNumericPartitioner implements Partitioner<Double> {

    @Override
    public Weighted<Double> partition(
            DataTable table, 
            double[] weights, 
            Column<?> col, 
            Sequence seq) {
        // TODO Auto-generated method stub
        return null;
    }

}

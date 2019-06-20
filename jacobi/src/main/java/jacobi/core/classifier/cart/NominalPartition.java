package jacobi.core.classifier.cart;

import java.util.List;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.classifier.cart.data.Column;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Instance;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.util.Weighted;

public class NominalPartition implements Partition<Void> {

    public NominalPartition(Impurity impurity) {
        this.impurity = impurity;
    }

    @Override
    public Weighted<Void> measure(DataTable table, Column<?> target, Sequence seq) {
        List<Instance> instances = seq.apply(table.getInstances(target));
        return new Weighted<>(null, this.measure(target, table.getOutcomeColumn(), instances));
    }
    
    protected double measure(Column<?> target, Column<?> goal, List<Instance> instances) {
        double[] weights = new double[goal.cardinality()];
        Matrix dist = Matrices.zeros(target.cardinality(), goal.cardinality());
        
        for(Instance inst : instances){
            weights[inst.outcome] += inst.weight;
            double[] row = dist.getRow(inst.feature);
            row[inst.outcome] += inst.weight;
            dist.setRow(inst.feature, row);
        }
        
        return this.measure(dist, weights);
    }
    
    protected double measure(Matrix dist, double[] weights) {
        double value = 0.0;
        for(int i = 0; i < weights.length; i++) {
            value += weights[i] * this.impurity.of(dist.getRow(i));
        }
        return value;
    }
    
    private Impurity impurity;
}

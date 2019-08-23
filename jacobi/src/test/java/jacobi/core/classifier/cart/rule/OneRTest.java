package jacobi.core.classifier.cart.rule;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.classifier.Column;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.core.classifier.cart.data.DataTable;
import jacobi.core.classifier.cart.data.Sequence;
import jacobi.core.classifier.cart.measure.Impurity;
import jacobi.core.classifier.cart.measure.NominalPartition;
import jacobi.core.classifier.cart.node.Decision;
import jacobi.core.classifier.cart.node.NominalSplit;
import jacobi.core.classifier.cart.rule.OneR;
import jacobi.core.classifier.cart.rule.ZeroR;
import jacobi.core.classifier.cart.util.JacobiDefCsvReader;
import jacobi.core.classifier.cart.util.JacobiEnums.YesOrNo;
import jacobi.core.util.Weighted;

public class OneRTest {
    
    /*
     * sunny,85,85,false,no
        sunny,80,90,true,no
        overcast,83,78,false,yes
        rain,70,96,false,yes
        rain,68,80,false,yes
        rain,65,70,true,no
        overcast,64,65,true,yes
        sunny,72,95,false,no
        sunny,69,70,false,yes
        rain,75,80,false,yes
        sunny,75,70,true,yes
        overcast,72,90,true,yes
        overcast,81,75,false,yes
        rain,71,80,true,no
     */
    
    @Test
    public void shouldBeAbleToLearnGolfDataOnOutlook() throws IOException {
        try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
            DataTable<YesOrNo> dataTab = new JacobiDefCsvReader()
                    .read(input, YesOrNo.class);
            
            DecisionNode<YesOrNo> ans = new OneR(
                    new ZeroR(), 
                    new NominalPartition(Impurity.ENTROPY)
                ).make(dataTab, 
                    this.columnSet(
                        dataTab.getColumns().get(0),
                        dataTab.getColumns().get(3)
                    ), 
                    this.defaultSeq(dataTab.size())
                );
                        
            // sunny: yes(2) no(3)
            // overcast: yes(4) no(0)
            // rain: yes(3) no(2)
            
            Assert.assertEquals(dataTab.getColumns().get(0), ans.split());
            Assert.assertTrue(ans instanceof NominalSplit);
            Assert.assertTrue(ans.decide(0.0).get() instanceof Decision);
            Assert.assertEquals(YesOrNo.NO, ans.decide(0.0).get().decide());
            Assert.assertTrue(ans.decide(1.0).get() instanceof Decision);
            Assert.assertEquals(YesOrNo.YES, ans.decide(1.0).get().decide());
            Assert.assertTrue(ans.decide(2.0).get() instanceof Decision);
            Assert.assertEquals(YesOrNo.YES, ans.decide(2.0).get().decide());
        }
    }
    
    @Test
    public void shouldBeAbleToLearnGolfDataOnWindyWhenGivenOnlyWindy() throws IOException {
        try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
            DataTable<YesOrNo> dataTab = new JacobiDefCsvReader()
                    .read(input, YesOrNo.class);
            
            DecisionNode<YesOrNo> ans = new OneR(
                    new ZeroR(), 
                    new NominalPartition(Impurity.ENTROPY)
                ).make(dataTab, 
                    this.columnSet(
                        dataTab.getColumns().get(3)
                    ), 
                    this.defaultSeq(dataTab.size())
                );
                        
            // true: yes(3) no(3)
            // false: yes(6) no(2)
            
            Assert.assertEquals(dataTab.getColumns().get(3), ans.split());
            Assert.assertTrue(ans instanceof NominalSplit);
            Assert.assertTrue(ans.decide(0.0).get() instanceof Decision);
            Assert.assertEquals(YesOrNo.YES, ans.decide(0.0).get().decide());
            Assert.assertTrue(ans.decide(1.0).get() instanceof Decision);
            Assert.assertEquals(YesOrNo.YES, ans.decide(1.0).get().decide());
        }
    }
    
    @Test
    public void shouldBeAbleToMakeDecisionOnPureSet() throws IOException {
    	try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
            DataTable<YesOrNo> dataTab = new JacobiDefCsvReader()
                    .read(input, YesOrNo.class);
            
            DecisionNode<YesOrNo> ans = new OneR(
                    new ZeroR(), 
                    new NominalPartition(Impurity.ENTROPY)
                ).make(dataTab, 
                    this.columnSet(
                        dataTab.getColumns().get(3)
                    ), 
                    new Sequence(new int[] {
                    	0, 2, 3, 4, 6, 1, 5, 7
                    }, 1, 5)
                );            
            
            Assert.assertNull(ans.split());
            Assert.assertTrue(ans instanceof Decision);
            Assert.assertEquals(YesOrNo.YES, ans.decide());
            
        }
    }
    
    @Test
    public void shouldBeAbleToMakeDecisionOnNullSet() throws IOException {
    	try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
            DataTable<YesOrNo> dataTab = new JacobiDefCsvReader()
                    .read(input, YesOrNo.class);
            
            DecisionNode<YesOrNo> ans = new OneR(
                    new ZeroR(), 
                    new NominalPartition(Impurity.ENTROPY)
                ).make(dataTab, 
                    this.columnSet(
                        dataTab.getColumns().get(3)
                    ), 
                    new Sequence(new int[] {
                    	0, 2, 3, 4, 6, 1, 5, 7
                    }, 1, 1)
                );            
            
            Assert.assertNull(ans.split());
            Assert.assertTrue(ans instanceof Decision);
            Assert.assertEquals(YesOrNo.YES, ans.decide());
            
        }
    }
    
    protected Sequence defaultSeq(int len) {
        return new Sequence(IntStream.range(0, len).toArray(), 0, len);
    }
    
    protected Set<Column<?>> columnSet(Column<?>... cols) {
        Set<Column<?>> set = new TreeSet<>();
        for(Column<?> col : cols) {
            set.add(col);
        }
        return set;
    }

}

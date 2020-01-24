package jacobi.core.classifier.cart.rule;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.core.classifier.cart.ArraySequence;
import jacobi.core.classifier.cart.Sequence;
import jacobi.core.classifier.cart.measure.Impurity;
import jacobi.core.classifier.cart.measure.NominalPartition;
import jacobi.core.classifier.cart.measure.Partition;
import jacobi.core.classifier.cart.node.Decision;
import jacobi.core.classifier.cart.node.NominalSplit;
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
                    new ArraySequence(new int[] {
                    	0, 2, 3, 4, 6, 1, 5, 7
                    }, 1, 5)
                );            
            
            Assert.assertNull(ans.split());
            Assert.assertTrue(ans instanceof Decision);
            Assert.assertEquals(YesOrNo.YES, ans.decide());
            
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenGivenBoundariesForNominalColumn() throws IOException {
        try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
            DataTable<YesOrNo> dataTab = new JacobiDefCsvReader()
                    .read(input, YesOrNo.class);
            
            DecisionNode<YesOrNo> ans = new OneR(
                    new ZeroR(), 
                    new Partition() {

						@Override
						public Weighted<double[]> measure(DataTable<?> table, 
								Column<?> target, 
								Sequence seq) {
							return new Weighted<>(IntStream.range(0, seq.length())
									.mapToDouble(i -> i)
									.toArray(), 0.001);
						}
                    	
                    }
                ).make(dataTab, 
                    this.columnSet(
                        dataTab.getColumns().get(0),
                        dataTab.getColumns().get(3)
                    ), 
                    this.defaultSeq(dataTab.size())
                );
        }
    }
        
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenGivenNoBoundaryForNumericColumn() throws IOException {
        try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
            DataTable<YesOrNo> dataTab = new JacobiDefCsvReader()
                    .read(input, YesOrNo.class);
            
            DecisionNode<YesOrNo> ans = new OneR(
                    new ZeroR(), 
                    new Partition() {

						@Override
						public Weighted<double[]> measure(DataTable<?> table, 
								Column<?> target, 
								Sequence seq) {
							return new Weighted<>(new double[0], 0.001);
						}
                    	
                    }
                ).make(dataTab, 
                    this.columnSet(
                        dataTab.getColumns().get(1),
                        dataTab.getColumns().get(3)
                    ), 
                    this.defaultSeq(dataTab.size())
                );
        }
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void shouldFailWhenGivenMoreBoundariesThenSupportedForNumericColumn() throws IOException {
        try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
            DataTable<YesOrNo> dataTab = new JacobiDefCsvReader()
                    .read(input, YesOrNo.class);
            
            DecisionNode<YesOrNo> ans = new OneR(
                    new ZeroR(), 
                    new Partition() {

						@Override
						public Weighted<double[]> measure(DataTable<?> table, 
								Column<?> target, 
								Sequence seq) {
							return new Weighted<>(new double[seq.length()], 
								0.001);
						}
                    	
                    }
                ).make(dataTab, 
                    this.columnSet(
                        dataTab.getColumns().get(1),
                        dataTab.getColumns().get(3)
                    ), 
                    this.defaultSeq(dataTab.size())
                );
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
                    new ArraySequence(new int[] {
                    	0, 2, 3, 4, 6, 1, 5, 7
                    }, 1, 1)
                );            
            
            Assert.assertNull(ans.split());
            Assert.assertTrue(ans instanceof Decision);
            Assert.assertEquals(YesOrNo.YES, ans.decide());
            
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenTryToMergeLeafNode() {
    	new OneR(null, null).mergeFunc(new Decision<>("You can't change my mind."));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void shouldFailWhenTryToMergeMoreBoundariesThenSupported() {
    	new OneR(null, null).mergeFunc(Column.numeric(1), new double[] {1.0, 2.0, 3.0});
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void shouldFailWhenTryToMergeUnknownNode() {
    	new OneR(null, null).mergeFunc(new DecisionNode<String>() {

			@Override
			public Column<?> split() {
				return Column.numeric(3);
			}

			@Override
			public String decide() {
				return null;
			}

			@Override
			public Optional<DecisionNode<String>> decide(double value) {
				return Optional.empty();
			}
    		
    	});
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void shouldFailWhenTryToSplitUnknownNode() {
    	new OneR(null, null).splitFunc(null, new DecisionNode<String>() {

			@Override
			public Column<?> split() {
				return Column.numeric(3);
			}

			@Override
			public String decide() {
				return null;
			}

			@Override
			public Optional<DecisionNode<String>> decide(double value) {
				return Optional.empty();
			}
    		
    	});
    }    
    
    protected ArraySequence defaultSeq(int len) {
        return new ArraySequence(IntStream.range(0, len).toArray(), 0, len);
    }
    
    protected Set<Column<?>> columnSet(Column<?>... cols) {
        Set<Column<?>> set = new TreeSet<>();
        for(Column<?> col : cols) {
            set.add(col);
        }
        return set;
    }

}

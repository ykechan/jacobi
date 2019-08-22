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
import jacobi.core.classifier.cart.rule.Id3;
import jacobi.core.classifier.cart.rule.OneR;
import jacobi.core.classifier.cart.rule.ZeroR;
import jacobi.core.classifier.cart.util.JacobiDefCsvReader;
import jacobi.core.classifier.cart.util.JacobiEnums.YesOrNo;
import jacobi.core.util.Weighted;

public class Id3Test {
	
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
	public void testShouldBeAbleToBuildFromGolf() throws IOException {
		
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/golf.def.csv")){
			DataTable<YesOrNo> dataTab = new JacobiDefCsvReader()
					.read(input, YesOrNo.class);
			
			Weighted<DecisionNode<YesOrNo>> ans = new Id3(
					new ZeroR(Impurity.ENTROPY), 
					new OneR(	
						new ZeroR(Impurity.ENTROPY), 
						new NominalPartition(Impurity.ENTROPY)
					), (seq, fn) -> {})
				.make(dataTab, 
					this.columnSet(
						dataTab.getColumns().get(0),
						dataTab.getColumns().get(3)
					), 
					this.defaultSeq(dataTab.size()));
			
			Assert.assertEquals(0, ans.item.split().getIndex());
			Assert.assertTrue(ans.item instanceof NominalSplit);
			
			NominalSplit<YesOrNo> root = (NominalSplit<YesOrNo>) ans.item;
			Assert.assertTrue(root.decide(0.0).get() instanceof NominalSplit); // sunny
			Assert.assertTrue(root.decide(1.0).get() instanceof Decision); // overcast
			Assert.assertTrue(root.decide(2.0).get() instanceof NominalSplit); // rain
			
			Assert.assertEquals(YesOrNo.YES, root.decide(1.0).get().decide());
			
			NominalSplit<YesOrNo> ifsunny = (NominalSplit<YesOrNo>) root.decide(0.0).get();
			NominalSplit<YesOrNo> ifrain = (NominalSplit<YesOrNo>) root.decide(2.0).get();
			
			Assert.assertEquals(3, ifsunny.split().getIndex());
			Assert.assertEquals(3, ifrain.split().getIndex());

			Assert.assertTrue(ifsunny.decide(0.0).get() instanceof Decision);
			Assert.assertTrue(ifsunny.decide(1.0).get() instanceof Decision);
			
			Assert.assertTrue(ifrain.decide(0.0).get() instanceof Decision);
			Assert.assertTrue(ifrain.decide(1.0).get() instanceof Decision);
			
			Assert.assertEquals(YesOrNo.NO, ifsunny.decide(0.0).get().decide()); // windy: false
			Assert.assertEquals(YesOrNo.YES, ifsunny.decide(1.0).get().decide()); // windy: true
			
			Assert.assertEquals(YesOrNo.YES, ifrain.decide(0.0).get().decide()); // windy: false
			Assert.assertEquals(YesOrNo.NO, ifrain.decide(1.0).get().decide()); // windy: true
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

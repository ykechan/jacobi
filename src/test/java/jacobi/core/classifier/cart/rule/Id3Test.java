package jacobi.core.classifier.cart.rule;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.core.classifier.cart.ArraySequence;
import jacobi.core.classifier.cart.measure.Impurity;
import jacobi.core.classifier.cart.measure.NominalPartition;
import jacobi.core.classifier.cart.node.Decision;
import jacobi.core.classifier.cart.node.NominalSplit;
import jacobi.core.classifier.cart.util.JacobiDefCsvReader;
import jacobi.core.classifier.cart.util.JacobiEnums.Lens;
import jacobi.core.classifier.cart.util.JacobiEnums.YesOrNo;

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
			
			DecisionNode<YesOrNo> ans = new Id3(new ZeroR(), 
					new OneR(new ZeroR(), new NominalPartition(Impurity.ENTROPY)))
				.make(dataTab, 
					this.columnSet(
						dataTab.getColumns().get(0),
						dataTab.getColumns().get(3)
					), 
					this.defaultSeq(dataTab.size()));
			
			Assert.assertEquals(0, ans.split().getIndex());
			Assert.assertTrue(ans instanceof NominalSplit);
			
			NominalSplit<YesOrNo> root = (NominalSplit<YesOrNo>) ans;
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
			
			System.out.println(this.toJson(root));
		}
	}

	@Test
	public void shouldBeAbleToBuildFromContactLensesDefCsv() throws IOException {
		try(InputStream input = this.getClass().getResourceAsStream("/jacobi/test/data/contact-lenses.def.csv")){
			DataTable<Lens> dataTab = new JacobiDefCsvReader()
					.read(input, Lens.class);
			
			DecisionNode<Lens> ans = new Id3(new ZeroR(), 
					new OneR(new ZeroR(), new NominalPartition(Impurity.ENTROPY)))
				.make(dataTab, 
					new TreeSet<>(dataTab.getColumns()), 
					this.defaultSeq(dataTab.size()));
			
			Assert.assertEquals(dataTab.getColumns().get(3), ans.split());
			Assert.assertTrue(ans instanceof NominalSplit);
			NominalSplit<Lens> root = (NominalSplit<Lens>) ans;
			
			Assert.assertTrue(root.decide(0.0).get() instanceof Decision); // reduced			
			Assert.assertTrue(root.decide(1.0).get() instanceof NominalSplit); // normal
			
			Assert.assertEquals(Lens.NONE, root.decide(0.0).get().decide());
			
			NominalSplit<Lens> ifNormal = (NominalSplit<Lens>) root.decide(1.0).get();
			
			//
			Assert.assertEquals(dataTab.getColumns().get(2), ifNormal.split());			
			Assert.assertTrue(ifNormal.decide(0.0).get() instanceof NominalSplit); // Yes			
			Assert.assertTrue(ifNormal.decide(1.0).get() instanceof NominalSplit); // No
			
			Assert.assertTrue(ifNormal
				.decide(0.0).get() // astigmatism: yes
				.decide(0.0).get() // spectacle: myope 
				instanceof Decision);
			
			Assert.assertEquals(Lens.HARD, ifNormal
				.decide(0.0).get() 			// astigmatism: yes
				.decide(0.0).get().decide() // spectacle: myope
			);
			
			Assert.assertEquals(dataTab.getColumns().get(0),
				ifNormal
					.decide(0.0).get() // astigmatism: yes
					.decide(1.0).get() // spectacle: hypermetrope
					.split()
			);
			
			Assert.assertEquals(null,
				ifNormal
					.decide(0.0).get() // astigmatism: yes
					.decide(1.0).get() // spectacle: hypermetrope
					.decide(0.0).get() // age: young
					.split()
			);
			
			Assert.assertEquals(Lens.HARD,
				ifNormal
					.decide(0.0).get() // astigmatism: yes
					.decide(1.0).get() // spectacle: hypermetrope
					.decide(0.0).get() // age: young
					.decide()
			);
			
			Assert.assertEquals(null, ifNormal
				.decide(0.0).get() // astigmatism: yes
				.decide(1.0).get() // spectacle: hypermetrope
				.decide(0.0).get() // age: young
				.split()
			);
				
			Assert.assertEquals(Lens.HARD, ifNormal
				.decide(0.0).get() // astigmatism: yes
				.decide(1.0).get() // spectacle: hypermetrope
				.decide(0.0).get() // age: young
				.decide()
			);
			
			Assert.assertEquals(null, ifNormal
				.decide(0.0).get() // astigmatism: yes
				.decide(1.0).get() // spectacle: hypermetrope
				.decide(1.0).get() // age: prepresbyopic
				.split()
			);
					
			Assert.assertEquals(Lens.NONE, ifNormal
				.decide(0.0).get() // astigmatism: yes
				.decide(1.0).get() // spectacle: hypermetrope
				.decide(1.0).get() // age: prepresbyopic
				.decide()
			);
			
			Assert.assertEquals(null, ifNormal
				.decide(0.0).get() // astigmatism: yes
				.decide(1.0).get() // spectacle: hypermetrope
				.decide(2.0).get() // age: presbyopic
				.split()
			);
						
			Assert.assertEquals(Lens.NONE, ifNormal
				.decide(0.0).get() // astigmatism: yes
				.decide(1.0).get() // spectacle: hypermetrope
				.decide(2.0).get() // age: presbyopic
				.decide()
			);
			
			Assert.assertEquals(dataTab.getColumns().get(0),
				ifNormal
					.decide(1.0).get() // astigmatism: no
					.split()
			);
			
			Assert.assertEquals(null, 
				ifNormal
					.decide(1.0).get() // astigmatism: no
					.decide(0.0).get() // age: young
					.split()
			);
			
			Assert.assertEquals(Lens.SOFT, 
				ifNormal
					.decide(1.0).get() // astigmatism: no
					.decide(0.0).get() // age: young
					.decide()
			);
			
			Assert.assertEquals(null, 
				ifNormal
					.decide(1.0).get() // astigmatism: no
					.decide(1.0).get() // age: prepresbyopic
					.split()
			);
				
			Assert.assertEquals(Lens.SOFT, 
				ifNormal
					.decide(1.0).get() // astigmatism: no
					.decide(1.0).get() // age: prepresbyopic
					.decide()
			);
			
			Assert.assertEquals(dataTab.getColumns().get(1), 
				ifNormal
					.decide(1.0).get() // astigmatism: no
					.decide(2.0).get() // age: presbyopic
					.split()
			);
					
			Assert.assertEquals(Lens.NONE, 
				ifNormal
					.decide(1.0).get() // astigmatism: no
					.decide(2.0).get() // age: presbyopic
					.decide(0.0).get() // spectacle: myope
					.decide()
			);
			
			Assert.assertEquals(Lens.SOFT, 
				ifNormal
					.decide(1.0).get() // astigmatism: no
					.decide(2.0).get() // age: presbyopic
					.decide(1.0).get() // spectacle: hypermetrope
					.decide()
			);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected String toJson(DecisionNode<?> node) {
		if(node.split() == null){
			return "\"" + node.decide() + "\"";
		}
		
		StringBuilder buf = new StringBuilder().append('{');
		if(node instanceof NominalSplit){
			List<DecisionNode<?>> children = ((NominalSplit) node).getChildren();
			for(int i = 0; i < node.split().cardinality(); i++) {
				if(i > 0) {
					buf.append(',');
				}
				buf.append('\"').append('#').append(node.split().getIndex()).append('=')				
					.append(node.split().valueOf(i))
					.append('\"')
					.append(':')
					.append(this.toJson(children.get(i)));				
			}
		}
		
		return buf.append('}').toString();
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

package jacobi.core.classifier.cart.node;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.classifier.Column;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.core.classifier.cart.util.JacobiEnums.Outlook;
import jacobi.core.classifier.cart.util.JacobiEnums.YesOrNo;

public class DecisionNodeSerializer {
	
	@Test
	public void shouldBeAbleToSerializeOneROnGolfSplitByOutlook() {
		Assert.assertEquals("{\"#0 = SUNNY\": \"NO\",\"#0 = OVERCAST\": \"YES\",\"#0 = RAIN\": \"YES\"}", 
			toJson(new NominalSplit<>(Column.of(0, Outlook.class), null, Arrays.asList(
				new Decision<>(YesOrNo.NO),
				new Decision<>(YesOrNo.YES),
				new Decision<>(YesOrNo.YES)
			))));
	}
	
	public String toJson(DecisionNode<?> node) {
		if(node instanceof Decision){
			return this.toJson((Decision<?>) node);
		}
		
		if(node instanceof NominalSplit){
			return this.toJson((NominalSplit<?>) node);
		}
		
		if(node instanceof BinaryNumericSplit){
			return this.toJson((BinaryNumericSplit<?>) node);
		}
		
		throw new UnsupportedOperationException("Unknown node type " + node);
	}
	
	protected String toJson(Decision<?> leaf) {
		return "\"" + (leaf.decide() == null ? "" : leaf.decide().toString()) + "\"";
	}
	
	protected String toJson(NominalSplit<?> node) {
		Column<?> column = node.split();
		String[] subtree = new String[column.cardinality()];
		
		for(int i = 0; i < subtree.length; i++){
			String key = "#" + column.getIndex() + " = " + column.valueOf(i);
			subtree[i] = "\"" + key + "\": " 
				+ this.toJson(node.getChildren().get(i));
		}
		
		return Arrays.stream(subtree).collect(Collectors.joining(",", "{", "}"));
	}
	
	protected String toJson(BinaryNumericSplit<?> node) {
		Column<?> column = node.split();
		
		return "\"#" + column.getIndex() + " < " + node.getThreshold() 
				+ "\": " + this.toJson(node.getLeft())
			+ ","
			+ "\"#" + column.getIndex() + " > " + node.getThreshold() 
				+ "\": " + this.toJson(node.getRight());
	}

}

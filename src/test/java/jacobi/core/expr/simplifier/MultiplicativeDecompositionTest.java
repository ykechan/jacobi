package jacobi.core.expr.simplifier;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.expr.Expression;
import jacobi.core.expr.Formula;
import jacobi.core.expr.Inv;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Var;
import jacobi.core.expr.Visitor;

public class MultiplicativeDecompositionTest {
	
	@Test
	public void shouldBeAbleToCancelWithReciprocal() {
		Expression one = new Mul(new Var("x"), new Inv(new Var("x")));
		Map<String, Component> components = one.accept(this.mock());
		
		Assert.assertTrue(components.isEmpty());
	}
	
	@Test
	public void shouldBeAbleToGroupMultiplicativeTerms() {
		Expression xSqY = new Mul(new Mul(new Var("x"), new Var("y")), new Var("x"));
		
		Map<String, Component> components = xSqY.accept(this.mock());
		Assert.assertEquals(2, components.size());
		Assert.assertEquals(2.0, components.get("x").lambda.doubleValue(), 1e-12);
		Assert.assertEquals(1.0, components.get("y").lambda.doubleValue(), 1e-12);
	}
	
	protected Visitor<Map<String, Component>> mock() {
		return new MultiplicativeDecomposition(Formula.INST);
	}

}

package jacobi.core.expr.simplifier;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.expr.Add;
import jacobi.core.expr.Const;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Formula;
import jacobi.core.expr.Inv;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Neg;
import jacobi.core.expr.Pow;
import jacobi.core.expr.Var;

public class AdditiveDecompositionTest {
	
	@Test
	public void shouldBeAbleToDecomposeQuadraticEqt() {
		Expression c = new Const<>(4);
		Expression bx = new Neg(new Mul(new Const<>(4), new Var("x")));
		Expression sqx = new Pow(new Var("x"), new Const<>(2));
		
		Expression px = new Add(new Add(sqx, bx), c);
		Map<String, Component> components = px.accept(this.mock());
		
		Assert.assertTrue(components.containsKey("1"));
		Assert.assertTrue(components.containsKey("x"));
		Assert.assertTrue(components.containsKey("x^2"));
		
		Assert.assertEquals(4, components.get("1").lambda.intValue());
		Assert.assertEquals(-4, components.get("x").lambda.intValue());
		Assert.assertEquals(1, components.get("x^2").lambda.intValue());
	}
	
	@Test
	public void shouldBeAbleToDecompositeProductAsSingleton() {
		Expression xPlusY = new Add(new Var("x"), new Var("y"));
		Expression xMinusY = new Add(new Var("x"), new Neg(new Var("y")));
		Expression px = new Mul(xPlusY, xMinusY);
		
		Map<String, Component> components = px.accept(this.mock());
		Assert.assertEquals(1, components.size());
		
		Component component = components.get("(x - y) * (x + y)");
		Assert.assertNotNull(component);
		Assert.assertEquals(1, component.lambda);
	}
	
	@Test
	public void shouldBeAbleToGroupAndCancelSameAdditiveTerms() {
		Expression xPlusY = new Add(new Var("x"), new Var("y"));
		Expression xMinusY = new Add(new Var("x"), new Neg(new Var("y")));
		Expression px = new Add(xPlusY, xMinusY);
		Map<String, Component> components = px.accept(this.mock());
		Assert.assertEquals(1, components.size());
		
		Component component = components.get("x");
		Assert.assertNotNull(component);
		Assert.assertEquals(2, component.lambda);
	}
	
	@Test
	public void shouldBeAbleToDetectReciprocalCoefficient() {
		Expression c = new Mul(new Const<>(2), new Inv(new Const<>(4)));
		Expression bx = new Neg(new Mul(new Inv(new Const<>(4)), new Var("x")));
		Expression sqx = new Pow(new Var("x"), new Const<>(2));
		
		Expression px = new Add(new Add(sqx, bx), c);
		Map<String, Component> components = px.accept(this.mock());
		
		Assert.assertTrue(components.containsKey("1"));
		Assert.assertTrue(components.containsKey("x"));
		Assert.assertTrue(components.containsKey("x^2"));
		
		Assert.assertEquals(0.5, components.get("1").lambda.doubleValue(), 1e-12);
		Assert.assertEquals(-0.25, components.get("x").lambda.doubleValue(), 1e-12);
		Assert.assertEquals(1, components.get("x^2").lambda.intValue());
	}
	
	protected AdditiveDecomposition mock() {
		return new AdditiveDecomposition(Formula.INST);
	}

}

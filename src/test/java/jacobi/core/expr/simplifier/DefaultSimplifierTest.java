package jacobi.core.expr.simplifier;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.expr.Add;
import jacobi.core.expr.Const;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Formula;
import jacobi.core.expr.Mul;
import jacobi.core.expr.parser.Parser;

public class DefaultSimplifierTest {
	
	@Test
	public void shouldBeAbleToSimplifyExpOfZeroEquivalentToOne() throws ParseException {
		Expression expr = Parser.parse("exp(a * b - b * a)");
		
		Expression result = DefaultSimplifier.getInstance().apply(expr);
		Assert.assertTrue(result == Const.ONE);
	}
	
	@Test
	public void shouldBeAbleToGroupingOfAdditiveTerms() throws ParseException {
		Expression expr = Parser.parse(
			//"(x - y + z)^2 + (-y + z + x) * (z + x - y)"
			"(x - y + z) + (z + x - y)"
		);
		
		System.out.println(expr.accept(Formula.INST));
		
		Expression result = DefaultSimplifier.getInstance().apply(expr);
		System.out.println(result.accept(Formula.INST));
		
		Assert.assertTrue(result instanceof Mul);
		Assert.assertTrue(result.getArgs().get(0) instanceof Const);
		Assert.assertEquals(2, ((Const<?>) result.getArgs().get(0)).get() );
		
		Assert.assertTrue(result.getArgs().get(1) instanceof Add);
	}
	
}

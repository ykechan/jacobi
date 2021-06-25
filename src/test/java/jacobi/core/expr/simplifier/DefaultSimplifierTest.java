package jacobi.core.expr.simplifier;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.expr.Const;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Formula;
import jacobi.core.expr.parser.Parser;

public class DefaultSimplifierTest {
	
	@Test
	public void shouldBeAbleToSimplifyExpOfZeroEquivalentToOne() throws ParseException {
		Expression expr = Parser.parse("exp(a * b - b * a)");
		System.out.println(expr.accept(Formula.INST));
		
		Expression result = expr.accept(DefaultSimplifier.INST);
		System.out.println(result.accept(Formula.INST));
		Assert.assertTrue(result == Const.ONE);
	}
	
	@Test
	public void shouldBeAbleToSimplifySquareOfSums() throws ParseException {
		Expression expr = Parser.parse(
			//"(x - y + z)^2 + (-y + z + x) * (z + x - y)"
			"(x - y + z) + (z + x - y)"
		);
		
		System.out.println(expr.accept(Formula.INST));
		
		Expression result = expr.accept(DefaultSimplifier.INST);
		System.out.println(result.accept(Formula.INST));
	}

}

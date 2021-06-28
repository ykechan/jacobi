package jacobi.core.expr.simplifier;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.expr.Add;
import jacobi.core.expr.Const;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Formula;
import jacobi.core.expr.Inv;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Neg;
import jacobi.core.expr.Var;

public class InverseReducerTest {
	
	@Test
	public void shouldBeAbleToReduceAdditiveInverse() {
		Expression expr = new Add(
			new Mul(new Var("a"), new Var("b")),
			new Neg(new Mul(new Var("a"), new Var("b")))
		);
		
		Expression zero = expr.accept(this.mock());
		Assert.assertTrue(zero == Const.ZERO);
	}
	
	@Test
	public void shouldBeAbleToReduceMultiplicativeInverse() {
		Expression expr = new Mul(
			new Add(new Var("a"), new Var("b")),
			new Inv(new Add(new Var("a"), new Var("b")))
		);
		
		Expression one = expr.accept(this.mock());
		Assert.assertTrue(one == Const.ONE);
	}
	
	protected InverseReducer mock() {
		return new InverseReducer(Formula.INST, 
			new AdditiveDecomposition(Formula.INST), 
			new MultiplicativeDecomposition(Formula.INST)
		);
	}
	
}

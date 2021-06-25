package jacobi.core.expr.simplifier;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.expr.Add;
import jacobi.core.expr.Const;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Formula;
import jacobi.core.expr.Func;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Pow;

public class IdentityReducerTest {
	
	@Test
	public void shouldBeAbleToReduceAdditiveIdentity() {
		Expression expr = new Add(Const.ZERO, Func.of("sin", Collections.singletonList(Const.ONE)));
		
		Assert.assertEquals("sin(1)", 
			expr.accept(new IdentityReducer()).accept(Formula.INST));
		
		Expression expr1 = new Add(Func.of("sin", Collections.singletonList(Const.ONE)), Const.ZERO);
		
		Assert.assertEquals("sin(1)", 
			expr1.accept(new IdentityReducer()).accept(Formula.INST));
	}
	
	@Test
	public void shouldBeAbleToLeakThroughWithoutAdditiveIdentity() {
		Expression expr = new Add(Const.ONE, Const.NEG_ONE);
		Assert.assertEquals("1 + -1", 
				expr.accept(new IdentityReducer()).accept(Formula.INST));
	}
	
	@Test
	public void shouldBeAbleToReduceMultiplicativeIdentity() {
		Expression expr = new Mul(Const.ONE, Func.of("sin", Collections.singletonList(Const.ONE)));
		
		Assert.assertEquals("sin(1)", 
			expr.accept(new IdentityReducer()).accept(Formula.INST));
		
		Expression expr1 = new Mul(Func.of("sin", Collections.singletonList(Const.ONE)), Const.ONE);
		
		Assert.assertEquals("sin(1)", 
			expr1.accept(new IdentityReducer()).accept(Formula.INST));
	}
	
	@Test
	public void shouldBeAbleToReducePowerOfZeroAndOne() {
		Expression expr1 = new Pow(Func.of("sin", Collections.singletonList(Const.ONE)), Const.ONE);
		
		Assert.assertEquals("sin(1)", 
			expr1.accept(new IdentityReducer()).accept(Formula.INST));
		
		Expression expr2 = new Pow(Func.of("sin", Collections.singletonList(Const.ONE)), Const.ZERO);
		
		Assert.assertEquals("1", 
			expr2.accept(new IdentityReducer()).accept(Formula.INST));
	}
	
	@Test
	public void shouldBeAbleToReduceToDefaultInstancesForIdentity() {
		Assert.assertTrue(Const.ZERO == new Const<>(0).accept(new IdentityReducer()));
		Assert.assertTrue(Const.ZERO == new Const<>(0L).accept(new IdentityReducer()));
		Assert.assertTrue(Const.ZERO == new Const<>(0.0).accept(new IdentityReducer()));
		
		Assert.assertTrue(Const.ONE == new Const<>(1).accept(new IdentityReducer()));
		Assert.assertTrue(Const.ONE == new Const<>(1L).accept(new IdentityReducer()));
		Assert.assertTrue(Const.ONE == new Const<>(1.0).accept(new IdentityReducer()));
		
		Assert.assertTrue(Const.NEG_ONE == new Const<>(-1).accept(new IdentityReducer()));
		Assert.assertTrue(Const.NEG_ONE == new Const<>(-1L).accept(new IdentityReducer()));
		Assert.assertTrue(Const.NEG_ONE == new Const<>(-1.0).accept(new IdentityReducer()));
	}

}

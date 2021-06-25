package jacobi.core.expr.simplifier;

import java.util.IdentityHashMap;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.expr.Add;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Neg;
import jacobi.core.expr.Pow;
import jacobi.core.expr.Var;

public class HashVisitorTest {
	
	@Test
	public void shouldBeAbleToHashAdditiveComponentsWithSorting() {
		Expression expr = new Add(
			new Add(new Var("z"), new Var("y")), 
			new Add(new Var("b"), new Var("a")));
		
		String hash = expr.accept(new HashVisitor(new IdentityHashMap<>()));
		Assert.assertEquals("{+:a,b,y,z}", hash);
	}
	
	
	@Test
	public void shouldBeAbleToHashNegativeComponentsWithSorting() {
		Expression expr = new Add(
			new Add(new Var("z"), new Var("y")), 
			new Neg(new Add(new Var("b"), new Var("a"))));
		
		String hash = expr.accept(new HashVisitor(new IdentityHashMap<>()));
		Assert.assertEquals("{+:!a,!b,y,z}", hash);
		
		Expression expr1 = new Add(
				new Add(new Var("z"), new Var("y")), 
				new Neg(new Pow(new Var("b"), new Var("a"))));
			
		String hash1 = expr1.accept(new HashVisitor(new IdentityHashMap<>()));
		System.out.println(hash1);
	}
	
	@Test
	public void shouldBeAbleToHashMultiplicativeComponentsWithSorting() {
		Expression expr = new Mul(
			new Mul(new Var("z"), new Var("y")), 
			new Mul(new Var("b"), new Var("a")));
		
		String hash = expr.accept(new HashVisitor(new IdentityHashMap<>()));
		Assert.assertEquals("{*:a,b,y,z}", hash);
	}

}

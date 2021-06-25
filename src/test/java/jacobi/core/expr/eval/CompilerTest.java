package jacobi.core.expr.eval;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.expr.Add;
import jacobi.core.expr.Const;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Func;
import jacobi.core.expr.Inv;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Neg;
import jacobi.core.expr.Pow;
import jacobi.core.expr.Var;

public class CompilerTest {
	
	@Test
	public void shouldBeAbleToCompileSimpleAdditionOfConsts() {
		Expression expr = new Add(new Const<>("str"), new Const<>("ing"));
		Eval eval = Compiler.INST.compile(expr);
		
		Assert.assertTrue(eval.getVars().isEmpty());
		Assert.assertEquals(3, eval.getProcedure().size());
		Assert.assertEquals("move str $0", eval.getProcedure().get(0).toString());
		Assert.assertEquals("move ing $1", eval.getProcedure().get(1).toString());
		Assert.assertEquals("+    $0 $1", eval.getProcedure().get(2).toString());
	}
	
	@Test
	public void shouldBeAbleToCompileSimpleSubtractionOfConsts() {
		Expression expr = new Add(new Const<>("abc"), new Neg(new Const<>("def")));
		Eval eval = Compiler.INST.compile(expr);
		
		Assert.assertTrue(eval.getVars().isEmpty());
		Assert.assertEquals(3, eval.getProcedure().size());
		Assert.assertEquals("move abc $0", eval.getProcedure().get(0).toString());
		Assert.assertEquals("move def $1", eval.getProcedure().get(1).toString());
		Assert.assertEquals("-    $0 $1", eval.getProcedure().get(2).toString());
	}
	
	@Test
	public void shouldBeAbleToCompileSimpleMultOfConsts() {
		Expression expr = new Mul(new Const<>("str"), new Const<>("ing"));
		Eval eval = Compiler.INST.compile(expr);
		
		Assert.assertTrue(eval.getVars().isEmpty());
		Assert.assertEquals(3, eval.getProcedure().size());
		Assert.assertEquals("move str $0", eval.getProcedure().get(0).toString());
		Assert.assertEquals("move ing $1", eval.getProcedure().get(1).toString());
		Assert.assertEquals("*    $0 $1", eval.getProcedure().get(2).toString());
	}
	
	@Test
	public void shouldBeAbleToCompileLineEqt() {
		Expression expr = new Add(new Mul(new Const<>("m"), new Var("x")), new Const<>("c"));
		Eval eval = Compiler.INST.compile(expr);
		
		Assert.assertEquals(1, eval.getVars().size());
		Assert.assertEquals("x", eval.getVars().get(0));
		
		Assert.assertEquals(5, eval.getProcedure().size());
		Assert.assertEquals("move m $0", eval.getProcedure().get(0).toString());
		Assert.assertEquals("load #0 $1", eval.getProcedure().get(1).toString());
		Assert.assertEquals("*    $0 $1", eval.getProcedure().get(2).toString());
		Assert.assertEquals("move c $1", eval.getProcedure().get(3).toString());
		Assert.assertEquals("+    $0 $1", eval.getProcedure().get(4).toString());
	}
	
	@Test
	public void shouldBeAbleToCompileQuadraticEqt() {
		Expression linear = new Add(new Mul(new Const<>("b"), new Var("x")), new Const<>("c"));
		Expression expr = new Add(new Mul(new Const<>("a"), new Var("x")), linear);
		Eval eval = Compiler.INST.compile(expr);
		
		Assert.assertEquals(1, eval.getVars().size());
		Assert.assertEquals("x", eval.getVars().get(0));
		
		Assert.assertEquals(9, eval.getProcedure().size());
		Assert.assertEquals("move a $0", eval.getProcedure().get(0).toString());
		Assert.assertEquals("load #0 $1", eval.getProcedure().get(1).toString());
		Assert.assertEquals("*    $0 $1", eval.getProcedure().get(2).toString());
		Assert.assertEquals("move b $1", eval.getProcedure().get(3).toString());
		Assert.assertEquals("load #0 $2", eval.getProcedure().get(4).toString());
		Assert.assertEquals("*    $1 $2", eval.getProcedure().get(5).toString());
		Assert.assertEquals("move c $2", eval.getProcedure().get(6).toString());
		Assert.assertEquals("+    $1 $2", eval.getProcedure().get(7).toString());
		Assert.assertEquals("+    $0 $1", eval.getProcedure().get(8).toString());
	}
	
	@Test
	public void shouldBeAbleToCompileStdGaussPdf() {
		Expression pdf = Func.of("exp", Arrays.asList(new Neg(new Pow(new Var("x"), new Const<>(2)))));
		Eval eval = Compiler.getInstance().compile(pdf);
		
		Assert.assertEquals(1, eval.getVars().size());
		Assert.assertEquals("x", eval.getVars().get(0));
		
		Assert.assertEquals(5, eval.getProcedure().size());
		Assert.assertEquals("load #0 $0", eval.getProcedure().get(0).toString());
		Assert.assertEquals("move 2 $1", eval.getProcedure().get(1).toString());
		Assert.assertEquals("^    $0 $1", eval.getProcedure().get(2).toString());
		Assert.assertEquals("!    $0", eval.getProcedure().get(3).toString());
		Assert.assertEquals("exp  $0", eval.getProcedure().get(4).toString());
	}

}

package jacobi.core.sym.eval;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.sym.Expr;
import jacobi.core.sym.Var;

public class TranspilerTest {
	
	@Test
	public void shouldBeAbleToCompileAnIdentityFunc() {
		Expr expr = Var.of("x"); // single swap in 
		Procedure proc = Transpiler.getInstance().compile(expr);
		Assert.assertEquals(1, proc.getSteps().size());
		
		Instruction swap = proc.getSteps().get(0);
		Assert.assertTrue(swap instanceof Swap);
		Assert.assertEquals(0, swap.offset);
		Assert.assertEquals("swap #0 $0", swap.toString());
	}

}

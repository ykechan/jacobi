package jacobi.core.sym;

import java.util.function.BinaryOperator;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.sym.eval.Arithmetic;

public class FormulaTest {
	
	@Test
	public void test(){
		BinaryOperator<Number> oper = Arithmetic::add;
		System.out.println(oper);
	}

}

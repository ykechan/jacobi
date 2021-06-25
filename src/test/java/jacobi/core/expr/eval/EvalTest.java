package jacobi.core.expr.eval;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import org.junit.Assert;
import org.junit.Test;

public class EvalTest {
	
	@Test
	public void shouldBeAbleToLoadInputAndMoveConst() {
		Eval eval = new Eval(Collections.singletonList("x"), Arrays.asList(
			new Load(0, 0),
			new Move<>(1, "suffix"),
			this.mock(0, (in, mem) -> mem.get(0).toString() + " " + mem.get(1).toString())
		), 32);
		
		Object result = eval.apply(Collections.singletonList("prefix"));
		Assert.assertEquals("prefix suffix", result.toString());
	}
	
	protected <T> Instruction<T> mock(int offset, BiFunction<List<?>, List<?>, T> f) {
		return new Instruction<T>(offset){

			@Override
			public T run(List<?> input, List<?> mem) {
				return f.apply(input, mem);
			}
			
		};
	}

}

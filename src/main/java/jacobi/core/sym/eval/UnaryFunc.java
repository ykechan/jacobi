package jacobi.core.sym.eval;

import java.util.List;
import java.util.function.UnaryOperator;

public class UnaryFunc extends Instruction {

	public UnaryFunc(int offset, UnaryOperator<Number> func) {
		super(offset);
		this.func = func;
	}

	@Override
	public Object run(List<?> input, List<?> mem) {
		Object arg = mem.get(this.offset);
		if(arg instanceof Number){
			return this.func.apply((Number) arg);
		}
		
		throw new UnsupportedOperationException("Expected number, found " + arg);
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append(this.func.toString().toLowerCase())
			.append(' ').append('#').append(this.offset)
			.toString();
	}

	private UnaryOperator<Number> func;
}

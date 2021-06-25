package jacobi.core.expr.eval;

import java.util.List;

public class Negate extends Instruction<Number> {

	public Negate(int offset) {
		super(offset);
	}

	@Override
	public Number run(List<?> input, List<?> mem) {
		try {
			Number num = (Number) mem.get(this.offset);
			if(num instanceof Integer){
				return -num.intValue();
			}
			
			if(num instanceof Long){
				return -num.longValue();
			}
			
			return -num.doubleValue();
		}catch(ClassCastException ex){
			return Double.NaN;
		}
	}

	@Override
	public String toString() {
		return new StringBuilder()
			.append('!').append("   ")
			.append(' ').append('$').append(this.offset)
			.toString();
	}

}

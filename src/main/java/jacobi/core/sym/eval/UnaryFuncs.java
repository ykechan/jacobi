package jacobi.core.sym.eval;

import java.util.function.UnaryOperator;

public enum UnaryFuncs implements UnaryOperator<Number> {
	ID {

		@Override
		public Number apply(Number t) {
			return t;
		}
		
	},
	
	NEG {

		@Override
		public Number apply(Number t) {
			if(t instanceof Double){
				return -t.doubleValue();
			}
			
			if(t instanceof Long){
				return -t.longValue();
			}
			
			return -t.intValue();
		}
		
	};
	
	public Instruction toInst(int offset) {
		return new UnaryFunc(offset, this);
	}

}

package jacobi.core.expr;

public interface Visitor<T> {
	
	public T visit(Add expr);
	
	public T visit(Mul expr);
	
	public T visit(Pow expr);
	
	public T visit(Func expr);
	
	public T visit(Var expr);
	
	public <V> T visit(Const<V> expr);

}

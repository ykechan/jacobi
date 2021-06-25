package jacobi.core.expr.simplifier;

import java.util.Map;

import jacobi.core.expr.Add;
import jacobi.core.expr.Const;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Func;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Pow;
import jacobi.core.expr.Var;
import jacobi.core.expr.Visitor;

public class CachedVisitor<T> implements Visitor<T> {
	
	
	public CachedVisitor(Visitor<T> visitor, Map<Expression, T> cache) {
		this.visitor = visitor;
		this.cache = cache;
	}

	@Override
	public T visit(Add expr) {
		return this.cache.computeIfAbsent(expr, f -> f.accept(this.visitor));
	}
	
	@Override
	public T visit(Mul expr) {
		return this.cache.computeIfAbsent(expr, f -> f.accept(this.visitor));
	}
	
	@Override
	public T visit(Pow expr) {
		return this.cache.computeIfAbsent(expr, f -> f.accept(this.visitor));
	}
	
	@Override
	public T visit(Func expr) {
		return this.cache.computeIfAbsent(expr, f -> f.accept(this.visitor));
	}
	
	@Override
	public T visit(Var expr) {
		return expr.accept(this.visitor);
	}
	
	@Override
	public <V> T visit(Const<V> expr) {
		return expr.accept(this.visitor);
	}
	
	private Visitor<T> visitor;
	private Map<Expression, T> cache;
}

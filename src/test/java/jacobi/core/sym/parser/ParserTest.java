package jacobi.core.sym.parser;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.lexer.ItemLexer.Action;
import jacobi.core.sym.Const;
import jacobi.core.sym.Expr;
import jacobi.core.sym.Formula;
import jacobi.core.sym.Neg;
import jacobi.core.sym.Pow;
import jacobi.core.sym.Var;

public class ParserTest {
	
	@Test
	public void shouldBeAbleToParseThroughSingleConstInteger() {
		Parser parser = new Parser(Tokenizer::new);
		Assert.assertEquals(Parser.State.INIT, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.CONST.of("123")));
		Assert.assertEquals(Parser.State.END_ELEMENT, parser.getState());
		Assert.assertEquals(Action.ACCEPT, parser.push(Token.END));
		Assert.assertEquals(Parser.State.END, parser.getState());
		
		Expr expr = parser.get().orElse(null);
		Assert.assertTrue(expr instanceof Const);
		Const<?> c = (Const<?>) expr;
		Assert.assertEquals(123, c.as(Integer.class).orElse(0).intValue());
 	}
	
	@Test
	public void shouldBeAbleToParseThroughSingleConstNegativeNumber() {
		Parser parser = new Parser(Tokenizer::new);
		Assert.assertEquals(Parser.State.INIT, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.OPRT.of("-")));
		Assert.assertEquals(Parser.State.BEGIN_ELEMENT, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.CONST.of("123")));
		Assert.assertEquals(Parser.State.END_ELEMENT, parser.getState());
		Assert.assertEquals(Action.ACCEPT, parser.push(Token.END));
		Assert.assertEquals(Parser.State.END, parser.getState());
		
		Expr expr = parser.get().orElse(null);
		Assert.assertTrue(expr instanceof Neg);
		Expr arg = expr.getArgs().get(1);
		Assert.assertTrue(arg instanceof Const);
		Const<?> c = (Const<?>) arg;
		Assert.assertEquals(123, c.as(Integer.class).orElse(0).intValue());
	}
	
	@Test
	public void shouldBeAbleToParseThroughXSquare() {
		Parser parser = new Parser(Tokenizer::new);
		Assert.assertEquals(Parser.State.INIT, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.IDFR.of("x")));
		Assert.assertEquals(Parser.State.INVOKE_FUNC, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.OPRT.of("^")));
		Assert.assertEquals(Parser.State.BEGIN_ELEMENT, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.CONST.of("2")));
		Assert.assertEquals(Parser.State.END_ELEMENT, parser.getState());
		Assert.assertEquals(Action.ACCEPT, parser.push(Token.END));
		Assert.assertEquals(Parser.State.END, parser.getState());
		
		Expr expr = parser.get().orElse(null);
		Assert.assertTrue(expr instanceof Pow);
		
		Expr base = expr.getArgs().get(0);
		Expr index = expr.getArgs().get(1);
		
		Assert.assertTrue(base instanceof Var);
		Assert.assertEquals("x", base.toString());
		
		Assert.assertTrue(index instanceof Const);
		Const<?> c = (Const<?>) index;
		Assert.assertEquals(2, c.as(Integer.class).orElse(2).intValue());
	}
	
	@Test
	public void shouldBeAbleToParseThroughNegativeXSquare() {
		Parser parser = new Parser(Tokenizer::new);
		Assert.assertEquals(Parser.State.INIT, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.OPRT.of("-")));
		Assert.assertEquals(Parser.State.BEGIN_ELEMENT, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.IDFR.of("x")));
		Assert.assertEquals(Parser.State.INVOKE_FUNC, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.OPRT.of("^")));
		Assert.assertEquals(Parser.State.BEGIN_ELEMENT, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.CONST.of("2")));
		Assert.assertEquals(Parser.State.END_ELEMENT, parser.getState());
		Assert.assertEquals(Action.ACCEPT, parser.push(Token.END));
		Assert.assertEquals(Parser.State.END, parser.getState());
		
		Expr expr = parser.get().orElse(null);
		Assert.assertTrue(expr instanceof Neg);
		
		Expr pow = expr.getArgs().get(1);
		Assert.assertTrue(pow instanceof Pow);
		
		Expr base = pow.getArgs().get(0);
		Expr index = pow.getArgs().get(1);
		
		Assert.assertTrue(base instanceof Var);
		Assert.assertEquals("x", base.toString());
		
		Assert.assertTrue(index instanceof Const);
		Const<?> c = (Const<?>) index;
		Assert.assertEquals(2, c.as(Integer.class).orElse(2).intValue());
	}
	
	@Test
	public void shouldBeAbleToParseThroughQuadraticEqt() {
		Parser parser = new Parser(Tokenizer::new);
		Assert.assertEquals(Parser.State.INIT, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.IDFR.of("x")));
		Assert.assertEquals(Parser.State.INVOKE_FUNC, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.OPRT.of("^")));
		Assert.assertEquals(Parser.State.BEGIN_ELEMENT, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.CONST.of("2")));
		Assert.assertEquals(Parser.State.END_ELEMENT, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.CONST.of("-")));
		Assert.assertEquals(Parser.State.BEGIN_PRODUCT, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.CONST.of("2")));
		Assert.assertEquals(Parser.State.END_ELEMENT, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.CONST.of("*")));
		Assert.assertEquals(Parser.State.BEGIN_EXPONENTIALS, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.IDFR.of("x")));
		Assert.assertEquals(Parser.State.INVOKE_FUNC, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.CONST.of("+")));
		Assert.assertEquals(Parser.State.BEGIN_PRODUCT, parser.getState());
		Assert.assertEquals(Action.MOVE, parser.push(Token.Type.CONST.of("4")));
		Assert.assertEquals(Parser.State.END_ELEMENT, parser.getState());
		Assert.assertEquals(Action.ACCEPT, parser.push(Token.END));
		Assert.assertEquals(Parser.State.END, parser.getState());
		
		Expr eqt = parser.get().orElse(null);
		Assert.assertEquals("((x^2) - (2 * x)) + 4", eqt.accept(Formula.getInstance()));
	}

}

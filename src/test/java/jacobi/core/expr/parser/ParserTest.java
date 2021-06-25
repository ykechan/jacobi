package jacobi.core.expr.parser;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.expr.Expression;
import jacobi.core.expr.Formula;
import jacobi.core.expr.Neg;
import jacobi.core.expr.Pow;
import jacobi.core.expr.parser.Parser;
import jacobi.core.expr.parser.Parser.Element;

public class ParserTest {
	
	@Test
	public void shouldBeAbleToParseQuadraticDet() throws ParseException {
		Expression det = Parser.parse("b^2 - 4 * a * c");
		Assert.assertEquals("(b^2) - ((4 * a) * c)", det.accept(Formula.INST));
	}
	
	@Test
	public void shouldBeAbleToParseBracketWithPriority() throws ParseException {
		Expression expr0 = Parser.parse("3 + 4 * 5 - 6");
		Assert.assertEquals("(3 + (4 * 5)) - 6", expr0.accept(Formula.INST));
		
		Expression expr1 = Parser.parse("(3 + 4) * (5 - 6)");
		Assert.assertEquals("(3 + 4) * (5 - 6)", expr1.accept(Formula.INST));
	}
	
	@Test
	public void shouldBeAbleToParseHingeLossFunction() throws ParseException {
		Expression hx = Parser.parse("w^2 + sum(max(0, 1 - w * x))");
		Assert.assertEquals("(w^2) + sum(max(0, 1 - (w * x)))", hx.accept(Formula.INST));
	}
	
	@Test
	public void shouldBeAbleToJumpParserStateOnConst() throws ParseException {
		Parser parser = new Parser(Tokenizer::new);
		Assert.assertTrue(Parser.State.END_ELEMENT == Parser.State.INIT.jump(parser, Token.Type.CONST.of("1")));
		Assert.assertTrue(Parser.State.END_ELEMENT == Parser.State.INIT.jump(parser, Token.Type.CONST.of("0")));
		Assert.assertTrue(Parser.State.END_ELEMENT == Parser.State.INIT.jump(parser, Token.Type.CONST.of("3.14")));
		Assert.assertTrue(Parser.State.END_ELEMENT == Parser.State.INIT.jump(parser, Token.Type.CONST.of("2.71828")));
	}
	
	@Test
	public void shouldBeAbleToJumpParserStateOnVar() throws ParseException {
		Parser parser = new Parser(Tokenizer::new);
		Assert.assertTrue(Parser.State.END_ELEMENT == Parser.State.INIT.jump(parser, Token.Type.IDFR.of("x")));
		Assert.assertTrue(Parser.State.END_ELEMENT == Parser.State.INIT.jump(parser, Token.Type.IDFR.of("y")));
		Assert.assertTrue(Parser.State.END_ELEMENT == Parser.State.INIT.jump(parser, Token.Type.IDFR.of("func")));
		Assert.assertTrue(Parser.State.END_ELEMENT == Parser.State.INIT.jump(parser, Token.Type.IDFR.of("?")));
	}
	
	@Test
	public void shouldBeAbleToJumpParserStateOnNegVar() throws ParseException {
		Parser parser = new Parser(Tokenizer::new);
		Assert.assertTrue(Parser.State.BEGIN_ELEMENT == Parser.State.INIT.jump(parser, Token.Type.OPRT.of("-")));
		Assert.assertTrue(Parser.State.END_ELEMENT == Parser.State.BEGIN_ELEMENT.jump(parser, Token.Type.IDFR.of("x")));
		Assert.assertTrue(Parser.State.BEGIN_EXPONENTIALS == Parser.State.END_ELEMENT.jump(parser, Token.Type.IDFR.of("*")));
		
		Element elem = parser.peek();
		Assert.assertEquals('*', elem.suffix);
		Assert.assertTrue(elem.expr instanceof Neg);
		Assert.assertEquals("x", elem.expr.getArgs().get(1).toString());
	}
	
	@Test
	public void shouldBeAbleToJumpParserStateOnPowerTower() throws ParseException {
		Parser parser = new Parser(Tokenizer::new);
		Assert.assertTrue(Parser.State.END_ELEMENT == Parser.State.INIT.jump(parser, Token.Type.IDFR.of("x")));
		Assert.assertTrue(Parser.State.BEGIN_ELEMENT == Parser.State.END_ELEMENT.jump(parser, Token.Type.OPRT.of("^")));
		Assert.assertTrue(Parser.State.END_ELEMENT == Parser.State.BEGIN_ELEMENT.jump(parser, Token.Type.IDFR.of("y")));
		Assert.assertTrue(Parser.State.BEGIN_ELEMENT == Parser.State.END_ELEMENT.jump(parser, Token.Type.OPRT.of("^")));
		Assert.assertTrue(Parser.State.END_ELEMENT == Parser.State.BEGIN_ELEMENT.jump(parser, Token.Type.IDFR.of("z")));
		Assert.assertTrue(Parser.State.BEGIN_EXPONENTIALS == Parser.State.END_ELEMENT.jump(parser, Token.Type.OPRT.of("*")));
		
		Element elem = parser.peek();
		Assert.assertEquals('*', elem.suffix);
		Assert.assertTrue(elem.expr instanceof Pow);
		Assert.assertEquals("x", elem.expr.getArgs().get(0).toString());
		Assert.assertTrue(elem.expr.getArgs().get(1) instanceof Pow);
		Assert.assertEquals("y", elem.expr.getArgs().get(1).getArgs().get(0).toString());
		Assert.assertEquals("z", elem.expr.getArgs().get(1).getArgs().get(1).toString());
	}
	
	@Test
	public void shouldBeAbleToJumpParserStateOnProduct() throws ParseException {
		Parser parser = new Parser(Tokenizer::new);
		Assert.assertTrue(Parser.State.END_ELEMENT == Parser.State.INIT.jump(parser, Token.Type.CONST.of("3.14")));
		Assert.assertTrue(Parser.State.BEGIN_EXPONENTIALS == Parser.State.END_ELEMENT.jump(parser, Token.Type.OPRT.of("*")));
		Assert.assertTrue(Parser.State.END_ELEMENT == Parser.State.BEGIN_EXPONENTIALS.jump(parser, Token.Type.IDFR.of("fx")));
		Assert.assertTrue(Parser.State.BEGIN_PRODUCT == Parser.State.END_ELEMENT.jump(parser, Token.Type.OPRT.of("+")));
	}

}

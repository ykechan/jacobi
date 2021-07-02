package jacobi.core.sym.parser;

import org.junit.Assert;
import org.junit.Test;

import jacobi.core.lexer.ItemLexer.Action;

public class TokenizerTest {
	
	@Test
	public void shouldBeAbleToTokenizeAnIntDelimitedByDollarSign() {
		Tokenizer tokenizer = new Tokenizer();
		Assert.assertEquals(Action.MOVE, tokenizer.push(' '));
		Assert.assertEquals(Action.MOVE, tokenizer.push('1'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('2'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('3'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('4'));
		Assert.assertEquals(Action.ACCEPT, tokenizer.push('$'));
		
		Token token = tokenizer.get().orElseThrow(IllegalStateException::new);
		Assert.assertEquals(Token.Type.CONST, token.type);
		Assert.assertEquals("1234", token.value);
	}
	
	@Test
	public void shouldBeAbleToTokenizeAnIntDelimitedBySpace() {
		Tokenizer tokenizer = new Tokenizer();
		Assert.assertEquals(Action.MOVE, tokenizer.push(' '));
		Assert.assertEquals(Action.MOVE, tokenizer.push('1'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('2'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('3'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('4'));
		Assert.assertEquals(Action.ACCEPT, tokenizer.push('\n'));
		
		Token token = tokenizer.get().orElseThrow(IllegalStateException::new);
		Assert.assertEquals(Token.Type.CONST, token.type);
		Assert.assertEquals("1234", token.value);
	}
	
	@Test
	public void shouldBeAbleToTokenizeAnIntDelimitedByComma() {
		Tokenizer tokenizer = new Tokenizer();
		Assert.assertEquals(Action.MOVE, tokenizer.push(' '));
		Assert.assertEquals(Action.MOVE, tokenizer.push('1'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('2'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('3'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('4'));
		Assert.assertEquals(Action.ACCEPT, tokenizer.push(','));
		
		Token token = tokenizer.get().orElseThrow(IllegalStateException::new);
		Assert.assertEquals(Token.Type.CONST, token.type);
		Assert.assertEquals("1234", token.value);
	}
	
	@Test
	public void shouldBeAbleToTokenizeAnFloatDelimitedByComma() {
		Tokenizer tokenizer = new Tokenizer();
		Assert.assertEquals(Action.MOVE, tokenizer.push(' '));
		Assert.assertEquals(Action.MOVE, tokenizer.push('1'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('2'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('.'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('3'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('4'));
		Assert.assertEquals(Action.ACCEPT, tokenizer.push(','));
		
		Token token = tokenizer.get().orElseThrow(IllegalStateException::new);
		Assert.assertEquals(Token.Type.CONST, token.type);
		Assert.assertEquals("12.34", token.value);
	}
	
	@Test
	public void shouldFailWhenFloatEndedWithoutFloatingPoint() {
		Tokenizer tokenizer = new Tokenizer();
		Assert.assertEquals(Action.MOVE, tokenizer.push(' '));
		Assert.assertEquals(Action.MOVE, tokenizer.push('1'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('2'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('.'));
		Assert.assertEquals(Action.FAIL, tokenizer.push(','));
		
		Token token = tokenizer.get().orElse(null);
		Assert.assertNull(token);
	}
	
	@Test
	public void shouldFailWhenFloatWithDoubleFloatingPoint() {
		Tokenizer tokenizer = new Tokenizer();
		Assert.assertEquals(Action.MOVE, tokenizer.push(' '));
		Assert.assertEquals(Action.MOVE, tokenizer.push('1'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('2'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('.'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('3'));
		Assert.assertEquals(Action.FAIL, tokenizer.push('.'));
		
		Token token = tokenizer.get().orElse(null);
		Assert.assertNull(token);
	}
	
	@Test
	public void shouldFailWhenNumberNotFollowedByDelimiter() {
		Tokenizer tokenizer = new Tokenizer();
		Assert.assertEquals(Action.MOVE, tokenizer.push(' '));
		Assert.assertEquals(Action.MOVE, tokenizer.push('1'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('2'));
		Assert.assertEquals(Action.FAIL, tokenizer.push('a'));
		
		Token token = tokenizer.get().orElse(null);
		Assert.assertNull(token);
		
		tokenizer = new Tokenizer();
		Assert.assertEquals(Action.MOVE, tokenizer.push(' '));
		Assert.assertEquals(Action.MOVE, tokenizer.push('1'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('2'));
		Assert.assertEquals(Action.FAIL, tokenizer.push('_'));
		
		token = tokenizer.get().orElse(null);
		Assert.assertNull(token);
		
		tokenizer = new Tokenizer();
		Assert.assertEquals(Action.MOVE, tokenizer.push(' '));
		Assert.assertEquals(Action.MOVE, tokenizer.push('1'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('2'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('.'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('5'));
		Assert.assertEquals(Action.FAIL, tokenizer.push('d'));
		
		token = tokenizer.get().orElse(null);
		Assert.assertNull(token);
	}
	
	@Test
	public void shouldBeAbleToTokenizeIdentifier() {
		Tokenizer tokenizer = new Tokenizer();
		Assert.assertEquals(Action.MOVE, tokenizer.push('\n'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('_'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('a'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('b'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('c'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('4'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('5'));
		Assert.assertEquals(Action.MOVE, tokenizer.push('6'));
		Assert.assertEquals(Action.ACCEPT, tokenizer.push('$'));
		
		Token token = tokenizer.get().orElse(null);
		Assert.assertNotNull(token);
		Assert.assertEquals(Token.Type.IDFR, token.type);
		Assert.assertEquals("_abc456", token.value);
	}
	
	@Test
	public void shouldBeAbleToTokenizeBrackets() {
		Tokenizer tokenizer = new Tokenizer();
		Assert.assertEquals(Action.MOVE, tokenizer.push('('));
		Assert.assertEquals(Action.ACCEPT, tokenizer.push(' '));
		Assert.assertEquals(Token.Type.DLMR, tokenizer.get().get().type);
		Assert.assertEquals("(", tokenizer.get().get().value);
		
		tokenizer = new Tokenizer();
		Assert.assertEquals(Action.MOVE, tokenizer.push(')'));
		Assert.assertEquals(Action.ACCEPT, tokenizer.push('1'));
		Assert.assertEquals(Token.Type.DLMR, tokenizer.get().get().type);
		Assert.assertEquals(")", tokenizer.get().get().value);
	}
	
	@Test
	public void shouldFailWhenTokenizeCurlyOrSquareBrackets() {
		Tokenizer tokenizer = new Tokenizer();
		Assert.assertEquals(Action.FAIL, tokenizer.push('['));
		Assert.assertFalse(tokenizer.get().isPresent());
		
		tokenizer = new Tokenizer();
		Assert.assertEquals(Action.FAIL, tokenizer.push(']'));
		Assert.assertFalse(tokenizer.get().isPresent());
		
		tokenizer = new Tokenizer();
		Assert.assertEquals(Action.FAIL, tokenizer.push('{'));
		Assert.assertFalse(tokenizer.get().isPresent());
		
		tokenizer = new Tokenizer();
		Assert.assertEquals(Action.FAIL, tokenizer.push('}'));
		Assert.assertFalse(tokenizer.get().isPresent());
	}

}

/*
 * The MIT License
 *
 * Copyright 2017 Y.K. Chan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jacobi.core.lexer;

import jacobi.core.lexer.ItemLexer.Action;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class BracketedLexerTest {
    
    @Test
    public void testRoundBraces() {
        ItemLexer<Integer> lexer = new BracketedLexer<>(new DigitLexer(), '(', ')');
        Assert.assertEquals(Action.MOVE, lexer.push('('));
        Assert.assertEquals(Action.MOVE, lexer.push('3'));
        Assert.assertEquals(Action.MOVE, lexer.push(')'));
        Assert.assertEquals(Action.ACCEPT, lexer.push(')'));
        Assert.assertEquals(3, lexer.get().get().intValue());
    }
    
    @Test
    public void testCurlyBraces() {
        ItemLexer<Integer> lexer = new BracketedLexer<>(new DigitLexer(), '{', '}');
        Assert.assertEquals(Action.MOVE, lexer.push('{'));
        Assert.assertEquals(Action.MOVE, lexer.push('7'));
        Assert.assertEquals(Action.MOVE, lexer.push('}'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('?'));
        Assert.assertEquals(7, lexer.get().get().intValue());
    }
    
    @Test
    public void testQuoted() {
        ItemLexer<Integer> lexer = new BracketedLexer<>(new DigitLexer(), '\"', '\"');
        Assert.assertEquals(Action.MOVE, lexer.push('\"'));
        Assert.assertEquals(Action.MOVE, lexer.push('0'));
        Assert.assertEquals(Action.MOVE, lexer.push('\"'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('$'));
        Assert.assertEquals(0, lexer.get().get().intValue());
    }
    
    @Test
    public void testStackedBracket() {
        ItemLexer<Integer> lexer = Optional.of(new DigitLexer())
                .map(BracketedLexer.by('[', ']'))
                .map(BracketedLexer.by('[', ']'))
                .get();
        Assert.assertEquals(Action.MOVE, lexer.push('['));
        Assert.assertEquals(Action.MOVE, lexer.push('['));
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.MOVE, lexer.push(']'));
        Assert.assertEquals(Action.MOVE, lexer.push(']'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('$'));
        Assert.assertEquals(2, lexer.get().get().intValue());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testEndedByAccept() {
        ItemLexer<Integer> lexer = new BracketedLexer<>(new DigitLexer(), '\"', '\"');
        Assert.assertEquals(Action.MOVE, lexer.push('\"'));
        Assert.assertEquals(Action.MOVE, lexer.push('0'));
        Assert.assertEquals(Action.MOVE, lexer.push('\"'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('$'));
        Assert.assertEquals(0, lexer.get().get().intValue());
        lexer.push('.');
    }
    
    @Test(expected = IllegalStateException.class)
    public void testIncorrectOpeningBracket() {
        ItemLexer<Integer> lexer = new BracketedLexer<>(new DigitLexer(), '\"', '\"');
        Assert.assertEquals(Action.REJECT, lexer.push('\''));
        lexer.push(':');
    }
    
    @Test(expected = IllegalStateException.class)
    public void testIncorrectEndingBracket() {
        ItemLexer<Integer> lexer = new BracketedLexer<>(new DigitLexer(), '(', ')');
        Assert.assertEquals(Action.MOVE, lexer.push('('));
        Assert.assertEquals(Action.MOVE, lexer.push('0'));
        Assert.assertEquals(Action.FAIL, lexer.push('('));
        lexer.push(':');
    }
    
    @Test
    public void testRejectedByBaseLexer() {
        ItemLexer<Integer> lexer = new BracketedLexer<>(new DigitLexer(), '(', ')');
        Assert.assertEquals(Action.MOVE, lexer.push('('));
        Assert.assertEquals(Action.FAIL, lexer.push('.'));
    }
    
    @Test
    public void testFailedByBaseLexer() {
        ItemLexer<Integer> lexer = new BracketedLexer<>(new DigitLexer(), '(', ')');
        Assert.assertEquals(Action.MOVE, lexer.push('('));
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.FAIL, lexer.push('2'));
    }
    
}

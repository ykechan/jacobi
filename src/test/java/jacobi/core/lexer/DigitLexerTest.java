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
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class DigitLexerTest {
    
    public DigitLexerTest() {
    }

    @Test
    public void testMoveAndAccept() {
        ItemLexer<Integer> lexer = new DigitLexer();
        Assert.assertEquals(Action.MOVE, lexer.push('0'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('+'));
        Assert.assertEquals(0, lexer.get().get().intValue());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testMoveAndAcceptAndEnded() {
        ItemLexer<Integer> lexer = new DigitLexer();
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('-'));
        Assert.assertEquals(1, lexer.get().get().intValue());
        lexer.push('!');
    }
    
    @Test(expected = IllegalStateException.class)
    public void testReject() {
        ItemLexer<Integer> lexer = new DigitLexer();
        Assert.assertEquals(Action.REJECT, lexer.push('?'));
        lexer.push('0');
    }
    
    @Test(expected = IllegalStateException.class)
    public void testMoveAndFailed() {
        ItemLexer<Integer> lexer = new DigitLexer();
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.FAIL, lexer.push('3'));
        lexer.push('?');
    }
    
}

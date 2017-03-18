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
public class FilteredLexerTest {
    
    @Test
    public void testPassingFilter() {
        ItemLexer<Integer> lexer = Optional.of(new DigitLexer())
                .map(FilteredLexer.by((i) -> i % 2 == 0))
                .get();
        Assert.assertEquals(Action.MOVE, lexer.push('4'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('\0'));
        Assert.assertEquals(4, lexer.get().get().intValue());
    }
    
    @Test
    public void testFailingFilter() {
        ItemLexer<Integer> lexer = Optional.of(new DigitLexer())
                .map(FilteredLexer.by((i) -> i % 2 == 0))
                .get();
        Assert.assertEquals(Action.MOVE, lexer.push('5'));
        Assert.assertEquals(Action.FAIL, lexer.push('\0'));
    }

    @Test
    public void testFailedByBase() {
        ItemLexer<Integer> lexer = Optional.of(new DigitLexer())
                .map(FilteredLexer.by((i) -> i % 2 == 0))
                .get();
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.FAIL, lexer.push('4'));
    }
    
    @Test
    public void testRejectedByBase() {
        ItemLexer<Integer> lexer = Optional.of(new DigitLexer())
                .map(FilteredLexer.by((i) -> i % 2 == 0))
                .get();
        Assert.assertEquals(Action.REJECT, lexer.push('$'));
    }
    
    @Test(expected = IllegalStateException.class)
    public void testEndedAfterPassingFilter() {
        ItemLexer<Integer> lexer = Optional.of(new DigitLexer())
                .map(FilteredLexer.by((i) -> i % 2 == 0))
                .get();
        Assert.assertEquals(Action.MOVE, lexer.push('4'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('\0'));
        Assert.assertEquals(4, lexer.get().get().intValue());
        lexer.push('2');
    }
    
    @Test(expected = IllegalStateException.class)
    public void testEndedAfterFailingFilter() {
        ItemLexer<Integer> lexer = Optional.of(new DigitLexer())
                .map(FilteredLexer.by((i) -> i % 2 == 0))
                .get();
        Assert.assertEquals(Action.MOVE, lexer.push('5'));
        Assert.assertEquals(Action.FAIL, lexer.push('\0'));
        lexer.push('4');
    }

    @Test(expected = IllegalStateException.class)
    public void testEndedAfterFailedByBase() {
        ItemLexer<Integer> lexer = Optional.of(new DigitLexer())
                .map(FilteredLexer.by((i) -> i % 2 == 0))
                .get();
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.FAIL, lexer.push('4'));
        lexer.push('?');
    }
    
    @Test(expected = IllegalStateException.class)
    public void testEndedAfterRejectedByBase() {
        ItemLexer<Integer> lexer = Optional.of(new DigitLexer())
                .map(FilteredLexer.by((i) -> i % 2 == 0))
                .get();
        Assert.assertEquals(Action.REJECT, lexer.push('$'));
        lexer.push('0');
    }
    
}

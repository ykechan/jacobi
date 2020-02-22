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
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class ListLexerTest {
    
    @Test
    public void testCsv() {
        ItemLexer<List<Integer>> lexer = new ListLexer<>(() -> new DigitLexer(), ',', true);
        Assert.assertEquals(Action.MOVE, lexer.push('3'));
        Assert.assertEquals(Action.MOVE, lexer.push(','));
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.MOVE, lexer.push(','));
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('$'));
        Assert.assertArrayEquals(new int[]{3, 2, 1}, lexer.get().get().stream().mapToInt((i) -> i).toArray());
    }
    
    @Test
    public void testEmptyList() {
        ItemLexer<List<Integer>> lexer = new ListLexer<>(() -> new DigitLexer(), ',', true);
        Assert.assertEquals(Action.ACCEPT, lexer.push('$'));
        Assert.assertTrue(lexer.get().get().isEmpty());
    }
    
    @Test
    public void testRejectedByBaseLexer() {
        ItemLexer<List<Integer>> lexer = new ListLexer<>(() -> new DigitLexer(), ',', true);
        Assert.assertEquals(Action.MOVE, lexer.push('3'));
        Assert.assertEquals(Action.MOVE, lexer.push(','));
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.MOVE, lexer.push(','));
        Assert.assertEquals(Action.FAIL, lexer.push('$'));
    }
    
    @Test
    public void testFailedByBaseLexer() {
        ItemLexer<List<Integer>> lexer = new ListLexer<>(() -> new DigitLexer(), ',', true);
        Assert.assertEquals(Action.MOVE, lexer.push('3'));
        Assert.assertEquals(Action.MOVE, lexer.push(','));
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.FAIL, lexer.push('1'));
    }
    
    @Test
    public void testLenientLexer() {
        ItemLexer<List<Integer>> lexer = new ListLexer<>(() -> new DigitLexer(), ',', false);
        Assert.assertEquals(Action.MOVE, lexer.push('3'));
        Assert.assertEquals(Action.MOVE, lexer.push(','));
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.MOVE, lexer.push(','));
        Assert.assertEquals(Action.ACCEPT, lexer.push('$'));
        Assert.assertArrayEquals(new int[]{3, 2}, lexer.get().get().stream().mapToInt((i) -> i).toArray());
    }
    
    @Test
    public void testNoDelimiter() {
        ItemLexer<List<Integer>> lexer = new ListLexer<>(() -> new IgnorableWhitespaceLexer<>(new DigitLexer()), '\0', true);
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.MOVE, lexer.push(' '));
        Assert.assertEquals(Action.MOVE, lexer.push('4'));
        Assert.assertEquals(Action.MOVE, lexer.push('\t'));
        Assert.assertEquals(Action.MOVE, lexer.push('8'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('$'));
        Assert.assertArrayEquals(new int[]{2, 4, 8}, lexer.get().get().stream().mapToInt((i) -> i).toArray());
    }

}

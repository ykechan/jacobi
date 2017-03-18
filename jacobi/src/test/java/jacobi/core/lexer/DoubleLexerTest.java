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
public class DoubleLexerTest {
    
    @Test
    public void testNormal() {
        ItemLexer<Double> lexer = new DoubleLexer();
        Assert.assertEquals(Action.MOVE, lexer.push('3'));
        Assert.assertEquals(Action.MOVE, lexer.push('.'));
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.MOVE, lexer.push('4'));
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.MOVE, lexer.push('5'));
        Assert.assertEquals(Action.MOVE, lexer.push('9'));
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.MOVE, lexer.push('6'));
        Assert.assertEquals(Action.MOVE, lexer.push('5'));
        Assert.assertEquals(Action.MOVE, lexer.push('3'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('$'));
        Assert.assertEquals(3.141592653, lexer.get().get(), 1e-16);
    }
    
    @Test
    public void testInteger() {
        ItemLexer<Double> lexer = new DoubleLexer();
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.MOVE, lexer.push('9'));
        Assert.assertEquals(Action.MOVE, lexer.push('9'));
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.ACCEPT, lexer.push(' '));
        Assert.assertEquals(1991, lexer.get().get(), 1e-16);
    }
    
    @Test
    public void testNegativeInteger() {
        ItemLexer<Double> lexer = new DoubleLexer();
        Assert.assertEquals(Action.MOVE, lexer.push('-'));
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.MOVE, lexer.push('9'));
        Assert.assertEquals(Action.MOVE, lexer.push('9'));
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.ACCEPT, lexer.push(' '));
        Assert.assertEquals(-1991, lexer.get().get(), 1e-16);
    }
    
    @Test
    public void testNegativeFractional() {
        ItemLexer<Double> lexer = new DoubleLexer();
        Assert.assertEquals(Action.MOVE, lexer.push('-'));
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.MOVE, lexer.push('.'));
        Assert.assertEquals(Action.MOVE, lexer.push('7'));
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.MOVE, lexer.push('8'));
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.MOVE, lexer.push('8'));
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.MOVE, lexer.push('8'));
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.MOVE, lexer.push('8'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('?'));
        Assert.assertEquals(-2.718281828, lexer.get().get(), 1e-16);
    }
    
    @Test
    public void testNoIntegralPart() {
        ItemLexer<Double> lexer = new DoubleLexer();
        Assert.assertEquals(Action.MOVE, lexer.push('.'));
        Assert.assertEquals(Action.MOVE, lexer.push('6'));
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.MOVE, lexer.push('8'));
        Assert.assertEquals(Action.MOVE, lexer.push('0'));
        Assert.assertEquals(Action.MOVE, lexer.push('3'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('='));
        Assert.assertEquals(0.61803, lexer.get().get(), 1e-16);
    }
    
    @Test
    public void testNegativeNoIntegralPart() {
        ItemLexer<Double> lexer = new DoubleLexer();
        Assert.assertEquals(Action.MOVE, lexer.push('-'));
        Assert.assertEquals(Action.MOVE, lexer.push('.'));
        Assert.assertEquals(Action.MOVE, lexer.push('6'));
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.MOVE, lexer.push('8'));
        Assert.assertEquals(Action.MOVE, lexer.push('0'));
        Assert.assertEquals(Action.MOVE, lexer.push('3'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('='));
        Assert.assertEquals(-0.61803, lexer.get().get(), 1e-16);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testNotANumberAtTheStart() {
        ItemLexer<Double> lexer = new DoubleLexer();
        Assert.assertEquals(Action.REJECT, lexer.push('?'));
        lexer.push('2');
    }
    
    @Test(expected = IllegalStateException.class)
    public void testNotANumberDoubleDotted() {
        ItemLexer<Double> lexer = new DoubleLexer();
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.MOVE, lexer.push('3'));
        Assert.assertEquals(Action.MOVE, lexer.push('.'));
        Assert.assertEquals(Action.FAIL, lexer.push('.'));
        lexer.push('2');
    }    
    
    @Test(expected = IllegalStateException.class)
    public void testNotANumberDottedNoFractional() {
        ItemLexer<Double> lexer = new DoubleLexer();
        Assert.assertEquals(Action.MOVE, lexer.push('3'));
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.MOVE, lexer.push('.'));
        Assert.assertEquals(Action.FAIL, lexer.push('$'));
        lexer.push('2');
    }
    
    @Test(expected = IllegalStateException.class)
    public void testNotANumberDoubleLater() {
        ItemLexer<Double> lexer = new DoubleLexer();
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.MOVE, lexer.push('3'));
        Assert.assertEquals(Action.MOVE, lexer.push('.'));
        Assert.assertEquals(Action.MOVE, lexer.push('3'));
        Assert.assertEquals(Action.MOVE, lexer.push('2'));
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.FAIL, lexer.push('.'));
        lexer.push('2');
    }
    
    @Test(expected = IllegalStateException.class)
    public void testDoubleSigned() {
        ItemLexer<Double> lexer = new DoubleLexer();
        Assert.assertEquals(Action.MOVE, lexer.push('+'));
        Assert.assertEquals(Action.FAIL, lexer.push('-'));
        lexer.push('2');
    }
    
    @Test(expected = IllegalStateException.class)
    public void testDoubleNegative() {
        ItemLexer<Double> lexer = new DoubleLexer();
        Assert.assertEquals(Action.MOVE, lexer.push('-'));
        Assert.assertEquals(Action.FAIL, lexer.push('-'));
        lexer.push('2');
    }
    
    @Test(expected = IllegalStateException.class)
    public void testDoublePositive() {
        ItemLexer<Double> lexer = new DoubleLexer();
        Assert.assertEquals(Action.MOVE, lexer.push('+'));
        Assert.assertEquals(Action.FAIL, lexer.push('+'));
        lexer.push('2');
    }
}

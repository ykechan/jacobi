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
public class IgnorableWhitespaceLexerTest {
    
    @Test
    public void testDigitLexer() {
        ItemLexer<Integer> lexer0 = this.digitLexer();
        Assert.assertEquals(Action.MOVE, lexer0.push('3'));
        Assert.assertEquals(Action.ACCEPT, lexer0.push('?'));
        Assert.assertEquals(3, lexer0.get().orElse(-1).intValue());
        
        ItemLexer<Integer> lexer1 = this.digitLexer();
        Assert.assertEquals(Action.REJECT, lexer1.push(' '));
        Assert.assertFalse(lexer1.get().isPresent());
        
        ItemLexer<Integer> lexer2 = this.digitLexer();
        Assert.assertEquals(Action.MOVE, lexer2.push('1'));
        Assert.assertEquals(Action.FAIL, lexer2.push('2'));
        Assert.assertFalse(lexer1.get().isPresent());
    }
    
    @Test
    public void testWithDoubleLexer() {
        ItemLexer<Double> lexer = IgnorableWhitespaceLexer.upon(new DoubleLexer());
        Assert.assertEquals(Action.MOVE, lexer.push(' '));
        Assert.assertEquals(Action.MOVE, lexer.push('\r'));
        Assert.assertEquals(Action.MOVE, lexer.push('\n'));
        Assert.assertEquals(Action.MOVE, lexer.push('3'));
        Assert.assertEquals(Action.MOVE, lexer.push('.'));
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.MOVE, lexer.push('4'));
        Assert.assertEquals(Action.MOVE, lexer.push(' '));
        Assert.assertEquals(Action.ACCEPT, lexer.push('?'));
        Assert.assertEquals(3.14, lexer.get().orElse(-1.0), 1e-16);
    }
    
    @Test
    public void testLeadingWhitespace() {
        ItemLexer<Integer> lexer = new IgnorableWhitespaceLexer<>(this.digitLexer());
        Assert.assertEquals(Action.MOVE, lexer.push(' '));
        Assert.assertEquals(Action.MOVE, lexer.push('\r'));
        Assert.assertEquals(Action.MOVE, lexer.push('\n'));
        Assert.assertEquals(Action.MOVE, lexer.push('3'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('?'));
        Assert.assertEquals(3, lexer.get().orElse(-1).intValue());
    }
    
    @Test
    public void testTrailingWhitespace() {
        ItemLexer<Integer> lexer = new IgnorableWhitespaceLexer<>(this.digitLexer());
        Assert.assertEquals(Action.MOVE, lexer.push('4'));
        Assert.assertEquals(Action.MOVE, lexer.push('\r'));
        Assert.assertEquals(Action.MOVE, lexer.push('\n'));
        Assert.assertEquals(Action.MOVE, lexer.push('\t'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('#'));
        Assert.assertEquals(4, lexer.get().orElse(-1).intValue());
    }
    
    @Test
    public void testLeadingAndTrailingWhitespace() {
        ItemLexer<Integer> lexer = new IgnorableWhitespaceLexer<>(this.digitLexer());
        Assert.assertEquals(Action.MOVE, lexer.push(' '));
        Assert.assertEquals(Action.MOVE, lexer.push('\t'));
        Assert.assertEquals(Action.MOVE, lexer.push(' '));
        Assert.assertEquals(Action.MOVE, lexer.push('5'));
        Assert.assertEquals(Action.MOVE, lexer.push('\r'));
        Assert.assertEquals(Action.MOVE, lexer.push('\n'));
        Assert.assertEquals(Action.MOVE, lexer.push('\t'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('#'));
        Assert.assertEquals(5, lexer.get().orElse(-1).intValue());
    }
    
    @Test
    public void testNullTerminatorIsNotWhitespace() {
        ItemLexer<Integer> lexer = new IgnorableWhitespaceLexer<>(this.digitLexer());
        Assert.assertEquals(Action.MOVE, lexer.push(' '));
        Assert.assertEquals(Action.MOVE, lexer.push('\t'));
        Assert.assertEquals(Action.MOVE, lexer.push(' '));
        Assert.assertEquals(Action.MOVE, lexer.push('6'));
        Assert.assertEquals(Action.MOVE, lexer.push('\r'));
        Assert.assertEquals(Action.MOVE, lexer.push('\n'));
        Assert.assertEquals(Action.MOVE, lexer.push('\t'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('\0'));
        Assert.assertEquals(6, lexer.get().orElse(-1).intValue());
    }
    
    @Test
    public void testRejectAfterLeadingSpace() {
        ItemLexer<Integer> lexer = new IgnorableWhitespaceLexer<>(this.digitLexer());
        Assert.assertEquals(Action.MOVE, lexer.push(' '));
        Assert.assertEquals(Action.MOVE, lexer.push('\t'));
        Assert.assertEquals(Action.MOVE, lexer.push(' '));
        Assert.assertEquals(Action.REJECT, lexer.push('?'));
        Assert.assertFalse(lexer.get().isPresent());
    }
    
    @Test
    public void testFailWhenParsing() {
        ItemLexer<Integer> lexer = new IgnorableWhitespaceLexer<>(this.digitLexer());
        Assert.assertEquals(Action.MOVE, lexer.push(' '));
        Assert.assertEquals(Action.MOVE, lexer.push('\t'));
        Assert.assertEquals(Action.MOVE, lexer.push('1'));
        Assert.assertEquals(Action.FAIL, lexer.push('7'));
        Assert.assertFalse(lexer.get().isPresent());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testFailOnStart() {
        ItemLexer<Integer> lexer = new IgnorableWhitespaceLexer<>(new ItemLexer<Integer>(){

            @Override
            public Action push(char ch) {
                return Action.FAIL;
            }

            @Override
            public Optional<Integer> get() {
                throw new UnsupportedOperationException();
            }
            
        });
        lexer.push('?');
    }
    
    @Test(expected = IllegalStateException.class)
    public void testAcceptOn1stChar() {
        ItemLexer<Integer> lexer = new IgnorableWhitespaceLexer<>(new ItemLexer<Integer>(){

            @Override
            public Action push(char ch) {
                return Action.ACCEPT;
            }

            @Override
            public Optional<Integer> get() {
                throw new UnsupportedOperationException();
            }
            
        });
        Assert.assertEquals(Action.MOVE, lexer.push('\t'));
        Assert.assertEquals(Action.MOVE, lexer.push('\n'));
        lexer.push('?');
    }
    
    @Test(expected = IllegalStateException.class)
    public void testFailOn1stChar() {
        ItemLexer<Integer> lexer = new IgnorableWhitespaceLexer<>(new ItemLexer<Integer>(){

            @Override
            public Action push(char ch) {
                return Action.FAIL;
            }

            @Override
            public Optional<Integer> get() {
                throw new UnsupportedOperationException();
            }
            
        });
        Assert.assertEquals(Action.MOVE, lexer.push('\t'));
        Assert.assertEquals(Action.MOVE, lexer.push('\n'));
        lexer.push('?');
    }
    
    @Test(expected = IllegalStateException.class)
    public void testRejectNotOn1stChar() {
        ItemLexer<Integer> lexer = new IgnorableWhitespaceLexer<>(new ItemLexer<Integer>(){

            @Override
            public Action push(char ch) {
                return numChar++ == 0 ? Action.MOVE : Action.REJECT;
            }

            @Override
            public Optional<Integer> get() {
                throw new UnsupportedOperationException();
            }
            
            private int numChar = 0;
        });
        Assert.assertEquals(Action.MOVE, lexer.push('\t'));
        Assert.assertEquals(Action.MOVE, lexer.push('\n'));
        Assert.assertEquals(Action.MOVE, lexer.push('?'));
        lexer.push('!');
    }
    
    @Test(expected = IllegalStateException.class)
    public void testLexerEnded() {
        ItemLexer<Integer> lexer = new IgnorableWhitespaceLexer<>(this.digitLexer());
        Assert.assertEquals(Action.MOVE, lexer.push(' '));
        Assert.assertEquals(Action.MOVE, lexer.push('\r'));
        Assert.assertEquals(Action.MOVE, lexer.push('\n'));
        Assert.assertEquals(Action.MOVE, lexer.push('3'));
        Assert.assertEquals(Action.ACCEPT, lexer.push('?'));
        Assert.assertEquals(3, lexer.get().orElse(-1).intValue());
        lexer.push('!');
    }
    
    @Test
    public void testRejectOnStart() {
        ItemLexer<Integer> lexer = new IgnorableWhitespaceLexer<>(this.digitLexer());
        Assert.assertEquals(Action.REJECT, lexer.push('?'));
    }
    
    protected ItemLexer<Integer> digitLexer() {
        return new DigitLexer();
    }
}

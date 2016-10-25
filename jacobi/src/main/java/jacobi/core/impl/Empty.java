/* 
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan
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
package jacobi.core.impl;

import jacobi.api.Matrix;

/**
 *
 * An empty matrix, i.e. a matrix with no element.
 * 
 * This serves as a NULL object for matrix.
 * 
 * Instead of simple null value of which Java screams annoyingly every time 
 * developers ever touches it no matter how slightly, an Empty matrix serves 
 * to cause as little error as possible. All operations are encouraged to 
 * define a default behaviour whenever an Empty matrix is encountered.
 * 
 * Nonetheless, computation result with Empty should not be relied upon.
 * 
 * @author Y.K. Chan
 */
public final class Empty extends ImmutableMatrix {
    
    /**
     * Get singleton instance.
     * @return  Instance of an Empty matrix
     */
    public static final Matrix getInstance() {
        return INST;
    }
    
    private Empty() {        
    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public int getColCount() {
        return 0;
    }

    @Override
    public double[] getRow(int index) {
        throw new ArrayIndexOutOfBoundsException(index);
    }

    @Override
    public Matrix copy() {
        return INST;
    }
    
    private static final Matrix INST = new Empty();
}

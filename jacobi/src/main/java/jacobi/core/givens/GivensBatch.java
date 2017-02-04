/*
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan.
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

package jacobi.core.givens;

import java.util.Collections;
import java.util.List;

/**
 * Data object for a batch of Givens rotation done by bulge-chasing.
 * 
 * In bulge-chasing, a series of pair of Givens rotation is done along in chasing a 4x4 bulge down to the bottom,
 * and a final single Givens rotation to reduce the matrix into Hessenberg form.
 * 
 * This class is immutable and contains only immutable objects, thus getter deemed un-necessary.
 * 
 * @author Y.K. Chan
 */
public final class GivensBatch {
    
    /**
     * A pair of Givens rotation to create bulge.
     */
    public final GivensPair implicitG;
    
    /**
     * List of Givens rotation for chasing the bulge down to the bottom.
     */
    public final List<GivensPair> rotList;
    
    /**
     * Givens rotation for finally reducing the matrix into Hessenberg form.
     */
    public final Givens last;
    
    /**
     * Bottom off-diagonal element of the Hessenberg form.
     */
    public final double bottom; 
    
    /**
     * Constructor.
     * @param implicitG  A pair of Givens rotation used to create bulge.
     * @param rotList  List of pairs of Givens rotation
     * @param last  Last Givens rotation to reduce the matrix into Hessenberg form.
     * @param bottom  Bottom off-diagonal element of the Hessenberg form.
     */
    public GivensBatch(GivensPair implicitG, List<GivensPair> rotList, Givens last, double bottom) {
        this.implicitG = implicitG;
        this.rotList = Collections.unmodifiableList(rotList);
        this.last = last;
        this.bottom = bottom;
    }

}

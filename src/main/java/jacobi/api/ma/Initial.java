/*
 * The MIT License
 *
 * Copyright (c) 2018 Y.K. Chan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jacobi.api.ma;

/**
 * Modes for moving average algorithms to compute the initial points which not all data is available.
 *
 * @author Y.K. Chan
 */
public enum Initial {
    /**
     * Ignore the points that the window not fully cover resulting in a shrank moving average signal
     */
    SHRINK,
    /**
     * Trim the window to cover available points only, i.e. adjust the denominator
     */
    ADAPT,
    /**
     * Logically pad the signal with the nearest data point, i.e. the first and the last element.
     */
    PAD,
    /**
     * Logically pad the signal with 0.
     */
    ZERO;
}

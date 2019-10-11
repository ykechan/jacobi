/* 
 * The MIT License
 *
 * Copyright 2019 Y.K. Chan
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
package jacobi.api.graph;

import java.util.Objects;

/**
 * Data class representing an edge in a graph.
 * 
 * @author Y.K. Chan
 */
public class Edge {
	
	/**
	 * Vertex of the beginning and end of this edge.
	 */
	public final int from, to;
	
	/**
	 * Weight of this edge
	 */
	public final double weight;

	/**
	 * Constructor.
	 * @param from  Beginning of this edge
	 * @param to  End of this edge
	 * @param weight  weight of this edge
	 */
	public Edge(int from, int to, double weight) {
		this.from = from;
		this.to = to;
		this.weight = weight;
	}

	@Override
	public boolean equals(Object obj) {
		
		if(obj instanceof Edge){
			Edge that = (Edge) obj;
			return this.from == that.from
				&& this.to == that.to
				&& this.weight == that.weight;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.from, this.to, this.weight);
	}
	
	@Override
	public String toString() {
		return this.from + " -> " + this.to + " (" + this.weight + ")";
	}
}

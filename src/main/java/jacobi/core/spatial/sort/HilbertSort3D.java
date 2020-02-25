/* 
 * The MIT License
 *
 * Copyright 2020 Y.K. Chan
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
package jacobi.core.spatial.sort;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.stream.IntStream;

import jacobi.core.util.IntStack;

/**
 * Implementation of sorting of Hilbert curve order in 3-D.
 * 
 * <p>Spatial sort does not scale well into higher dimensions since the number of partitions
 * and possible transitions grows exponentially with the number of dimensions. Sorting in 3-D
 * is on the verge of being manageable and mentally visualizable. To justify the complexity,
 * this class is dedicated to sorting in Hilbert curve which preserves locality the best.</p>
 * 
 * <p>A region of 3-D space can be partitioned into 8 octrants. The octrants are traversed
 * 2 different ways for each starting position which makes 16 basis curve. These are pre-defined
 * with each curve represented in an octal integer with least significant coefficient being
 * the code of the octrant to travel first. For each basis curve, the octrants can be further
 * enhanced by the basis curves themselves, and the resolutaion is encoded in 16-based long
 * with least significant coefficient being the index of the basis curves to enhance the first
 * octrant that was travelled first.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class HilbertSort3D implements SpatialSort {

	/**
	 * Constructor.
	 * @param xIdx  Index of x-dimension
	 * @param yIdx  Index of y-dimension
	 * @param zIdx  Index of z-dimension
	 */
	public HilbertSort3D(int xIdx, int yIdx, int zIdx) {
		this.xIdx = xIdx;
		this.yIdx = yIdx;
		this.zIdx = zIdx;
	}
	
	@Override
	public int[] sort(List<double[]> vectors) {
		
		return this.sort(
			this.init(vectors), 
			BASIS[0], 
			IntStream.range(0, vectors.size()).toArray()
		);
	}

	protected int[] sort(double[] comps, int basis, int[] order) {
		Deque<Category> stack = new ArrayDeque<>();
		stack.push(new Category(0, order.length, basis, 0));
		
		while(!stack.isEmpty()){
			Category cat = stack.pop();
			if(cat.end - cat.begin < 2) {
				continue;
			}
			
			int[] counts = this.sort(comps, cat, order);
			long enhance = this.enhanceResolution(cat.parity);
			
			System.out.println("begin=" + cat.begin + ", end = " + cat.end 
					+ ", depth = " + cat.depth
					+ ", parity = " + cat.parity
					+ ", enhance = " + enhance);
			System.out.println("Groups: " + Arrays.toString(counts));
			for(int i = cat.begin; i < cat.end; i++) {
				int k = order[i];
				System.out.println(comps[3*k] + ", " + comps[3*k + 1] + ", " + comps[3*k + 2]);
			}
			
			int start = cat.begin;
						
			for(int count : counts){
				if(count == cat.end - cat.begin){
					// all are degenerated
					break;
				}
				
				if(count > 0){
					stack.push(new Category(
						start, start + count, 
						BASIS[(int) (enhance % BASIS.length)], 
						cat.depth + 1
					));
				}
				start += count;
				enhance >>= 4;
			}
		}
		return order;
	}
	
	protected int[] sort(double[] comps, Category octrant, int[] order) {
		IntStack[] octs = this.octGroups(comps, octrant.begin, octrant.end, order);
		int[] counts = new int[octs.length];
		
		int start = octrant.begin;
		for(int i = 0; i < octs.length; i++){
			IntStack oct = octs[(octrant.parity >> 3 * i) % octs.length];
			if(oct == null){
				continue;
			}
			oct.toArray(order, start);
			
			start += oct.size();
			counts[i] = oct.size();
		}
		
		if(start != octrant.end) {
			throw new IllegalStateException();
		}
		return counts;
	}
	
	protected IntStack[] octGroups(double[] comps, int begin, int end, int[] order) {
		IntStack[] groups = new IntStack[8];
		double[] u = this.mean(comps, begin, end, order);
		
		for(int i = begin; i < end; i++){
			int k = order[i];
			int parity = (comps[3*k]     < u[0] ? 0 : 2)
					   + (comps[3*k + 1] < u[1] ? 0 : 1)
					   + (comps[3*k + 2] < u[2] ? 0 : 4);
			
			if(groups[parity] == null) {
				groups[parity] = new IntStack(4);
			}
			
			groups[parity].push(k);
		}
		return groups;
	}
	
	protected double[] init(List<double[]> vectors) {
		double[] comps = new double[3 * vectors.size()];
		int k = 0;
		for(double[] v : vectors){
			comps[k++] = v[this.xIdx];
			comps[k++] = v[this.yIdx];
			comps[k++] = v[this.zIdx];
		}
		return comps;
	}
	
	protected double[] mean(double[] comps, int begin, int end, int[] order) {
		double[] ans = new double[3];
		for(int i = begin; i < end; i++) {
			int k = 3 * order[i];
			ans[0] += comps[k++];
			ans[1] += comps[k++];
			ans[2] += comps[k++];
		}
		
		for(int i = 0; i < ans.length; i++) {
			ans[i] /= end - begin;
		}
		return ans;
	}
	
	protected long enhanceResolution(int parity) {
		int startOct = parity % 8;
		
		if(parity == BASIS[2*startOct]) {
			return ENHANCE[2*startOct];
		}
		
		if(parity == BASIS[2*startOct + 1]) {
			return ENHANCE[2*startOct + 1];
		}
		
		throw new IllegalArgumentException("Parity " + parity + " is not a basis.");
	}

	private int xIdx, yIdx, zIdx;
	
	protected static final int[] BASIS = {
		10212048, 16405200, 
		11761281, 13825665, 
		14598234, 12533850, 
		16147467, 9954315, 
		629748, 6822900, 
		2178981, 4243365, 
		5015934, 2951550, 
		6565167, 372015
	};
	
	protected static final long[] ENHANCE = {
		4068694057L, 2464372777L, 
		3601275499L, 3066501739L, 
		2965470221L, 3500243981L, 
		2498051663L, 4102372943L, 
		2047594657L, 443273377L, 
		1580176099L, 1045402339L, 
		944370821L, 1479144581L, 
		476952263L, 2081273543L
	};
}

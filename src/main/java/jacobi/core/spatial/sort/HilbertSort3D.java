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
			int parity = (comps[3*k]     < u[0] ? 0 : 1)
					   + (comps[3*k + 1] < u[1] ? 0 : 2)
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
			ans[2] += comps[k];
		}
		
		for(int i = 0; i < ans.length; i++) {
			ans[i] /= end - begin;
		}
		return ans;
	}
	
	protected long enhanceResolution(int parity) {
		int index = BasisType.values().length * (parity % 8);
		
		for(BasisType type : BasisType.values()) {
			if(parity == BASIS[index + type.ordinal()]) {
				return ENHANCE[index + type.ordinal()];
			}
		}
		
		throw new IllegalArgumentException("Parity " + parity + " is not a basis.");
	}

	private int xIdx, yIdx, zIdx;
	
	protected static final int[] BASIS = {
		9954504, 16147656, 3135888, 5237064, 
		12533913, 14598297, 740313, 7043841, 
		13825602, 11761218, 6749442, 445914, 
		16405011, 10211859, 4353867, 2252691, 
		372204, 6565356, 12423348, 14524524, 
		2951613, 5015997, 10027773, 16331301, 
		4243302, 2178918, 16036902, 9733374, 
		6822711, 629559, 13641327, 11540151
	};
	
	protected static final long[] ENHANCE = {
		472462885633L , 60548678401L  , 882708449985L , 750667566945L , 
		326563205637L , 189258469893L , 1015990721093L, 617653477349L , 
		202138788745L , 339443524489L , 616680270793L , 1017232109161L, 
		56241271437L  , 468155478669L , 749962541901L , 884218019565L , 
		1005591970065L, 593677762833L , 316293400785L , 184252517745L , 
		859692290069L , 722387554325L , 449575671893L , 51238428149L  , 
		735267873177L , 872572608921L , 50265221593L  , 450817059961L , 
		589370355869L , 1001284563101L, 183547492701L , 317802970365L
	};
	
	protected enum BasisType {
		
		STAY, DIAG, RIGHT, FORWARD
	}
	
}

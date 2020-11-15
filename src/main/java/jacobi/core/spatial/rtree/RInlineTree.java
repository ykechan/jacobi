package jacobi.core.spatial.rtree;

import java.util.Iterator;
import java.util.List;

import jacobi.api.Matrix;
import jacobi.api.spatial.SpatialIndex;
import jacobi.core.util.IntStack;

public class RInlineTree implements SpatialIndex<Integer> {
	
	public RInlineTree(List<RLayer> index, RLayer leaves, Matrix vectors) {
		this.index = index;
		this.leaves = leaves;
		this.vectors = vectors;
	}
	
	@Override
	public List<Integer> queryKNN(double[] query, int kMax) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Iterator<Integer> queryRange(double[] query, double dist) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected int[] queryFilter(RLayer rLayer, double[] query, double qDist, int[] filter) {
		IntStack plan = IntStack.newInstance();
		
		int[] cuts = rLayer.cuts;
		double[] bounds = rLayer.bounds;
		
		int mbbLen = 2 * rLayer.dim();
		int begin = 0;
		for(int i = 0; i < filter.length; i++){
			int span = filter[i];
			if(span < 0){
				int end = begin + span;
				
				int len = cuts[end - 1] - (begin == 0 ? 0 : cuts[begin - 1]);
				this.push(plan, -1, len);
				begin = end;
				continue;
			}
			
			for(int j = 0; j < span; j++){
				int index = begin + j;
				int base = index * mbbLen;
				
				double dist = 0.0;
				for(int k = 0; k < mbbLen; k += 2){
					double q = query[k / 2];
					double dx = Math.max(bounds[base + k] - q, q - bounds[base + k + 1]);
					
					if(dx < 0.0){
						continue;
					}
					
					dist += dx * dx;
					if(dist > qDist){
						break;
					}
				}
				
				int len = cuts[index] - (index == 0 ? 0 : cuts[index - 1]);
				this.push(plan, dist < qDist ? 1 : -1, len);
			}
			
			begin += span;
		}
		
		while(!plan.isEmpty()){
			if(plan.peek() > 0){
				break;
			}
			
			plan.pop();
		}
		return plan.toArray();
	}
	
	protected void push(IntStack stack, int sgn, int span) {
		int val = sgn * span;
		if(stack.isEmpty()){
			stack.push(val);
			return;
		}
		
		if(stack.peek() * sgn > 0){
			int temp = stack.pop();
			temp += val;
			stack.push(temp);
			return;
		}
		
		stack.push(val);
	}
	
	private List<RLayer> index;
	private RLayer leaves;
	private Matrix vectors;	
}

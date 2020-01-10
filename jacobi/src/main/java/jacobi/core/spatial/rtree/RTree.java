package jacobi.core.spatial.rtree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import jacobi.core.util.Weighted;

public class RTree<T> {
	
	public RTree(RNode<T> root) {
		this.root = root;
	}

	public List<T> queryRange(double[] q, double dist) {
		List<T> result = new ArrayList<>();
		
		Deque<RObject<T>> stack = new ArrayDeque<>();
		stack.push(this.root);
		
		while(!stack.isEmpty()){
			RObject<T> obj = stack.pop();
			
			if(!obj.nodes().isEmpty()) {
				obj.nodes().stream().sequential()
					.filter(n -> this.bDist.isCloser(n.minBoundBox(), q, dist))
					.forEach(stack::push);
				continue;
			}	
			
			double[] p = this.toArray(obj.minBoundBox());
			if(this.pDist.isCloser(p, q, dist)){
				result.add(obj.get()
					.orElseThrow(() -> new UnsupportedOperationException(
						"R-Object " + obj + " contains no item or child node."
					))
				);
			}
		}
		return result;
	}
	
	public List<T> queryKNN(double[] q, int k) {
		PriorityQueue<Weighted<T>> result = new PriorityQueue<>();
		PriorityQueue<Weighted<RObject<T>>> queue = new PriorityQueue<>();		
		queue.add(new Weighted<>(this.root, 0.0));
		
		while(!queue.isEmpty()){ 
			Weighted<RObject<T>> head = queue.remove();
			if(result.size() >= k 
			|| !this.bDist.isCloser(head.item.minBoundBox(), q, result.peek().weight)){
				continue;
			}
			
			
		}
		return result.stream().map(w -> w.item).collect(Collectors.toList());
	}	
	
	protected double[] toArray(Aabb aabb) {
		double[] array = new double[aabb.dim()];
		for(int i = 0; i < array.length; i++){
			array[i] = aabb.min(i);
			if(aabb.max(i) > array[i]) {
				throw new UnsupportedOperationException(
					"Unable to convert non-degenerate AABB to an array."
				);
			}
		}
		return array;
	}

	private RNode<T> root;
	private RDistance<Aabb> bDist;
	private RDistance<double[]> pDist;
}

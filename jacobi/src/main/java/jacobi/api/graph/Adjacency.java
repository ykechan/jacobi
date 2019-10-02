package jacobi.api.graph;

import jacobi.api.annotations.Facade;

@Facade(AdjList.class)
public interface Adjacency {
	
	public int[] topsort();
	
	public AdjList minSpan();

}

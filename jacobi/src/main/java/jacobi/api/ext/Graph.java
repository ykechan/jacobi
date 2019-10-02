package jacobi.api.ext;

import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Pure;
import jacobi.api.graph.Adjacency;

@Facade
public interface Graph {
	
	@Pure
	public Adjacency init();

}

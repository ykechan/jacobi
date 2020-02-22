package jacobi.core.graph.util;

import java.util.Arrays;

/**
 * Data object for route information from a single source.
 * 
 * <p>Route information include distances with the vertex passing through immediately
 * before the destination. A negative value indicates the vertex is not reachable. Distance
 * value for such vertex is non-determined.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class Routes {
	
	/**
	 * Factory method for a route with all vertices un-reachable.
	 * @param size  Number of vertices
	 * @return  Routes
	 */
	public static Routes init(int size) {
		Routes routes = new Routes(new double[size], new int[size]);
		Arrays.fill(routes.via, -1);
		return routes;
	}
	
	/**
	 * Distances
	 */
	public final double[] dist;
	
	/**
	 * Intermediate vertices
	 */
	public final int[] via;

	/**
	 * Constructor.
	 * @param dist   Distances
	 * @param via  Intermediate vertices
	 */
	public Routes(double[] dist, int[] via) {
		this.dist = dist;
		this.via = via;
	}

}

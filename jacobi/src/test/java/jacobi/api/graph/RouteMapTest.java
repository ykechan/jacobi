package jacobi.api.graph;

import org.junit.Assert;
import org.junit.Test;

public class RouteMapTest {
	
	@Test
	public void shouldBeAbleToTraceAStraightPath() {
		RouteMap routeMap = RouteMap.wrap(
			new double[] {0.0, 1.0, 2.0, 3.0, 4.0, 5.0}, 
			new int[] {0, 0, 1, 2, 3, 4}
		);
		
		//(routeMap.trace(5));
		Assert.assertArrayEquals(
			new Edge[] {
				new Edge(0, 1, 1.0),
				new Edge(1, 2, 1.0),
				new Edge(2, 3, 1.0),
				new Edge(3, 4, 1.0),
				new Edge(4, 5, 1.0)
			}, 
			routeMap.trace(5).toArray(new Edge[0])
		);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToConstructWithoutFixedPoint() {
		RouteMap.wrap(
			new double[] {0.0, 1.0, 2.0, 3.0, 4.0, 5.0}, 
			new int[] {4, 0, 1, 2, 3, 4}
		);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToTraceCyclicRoutePoint() {
		RouteMap routeMap = RouteMap.wrap(
			new double[] {0.0, 1.0, 2.0, 3.0, 4.0, 5.0}, 
			new int[] {0, 0, 1, 2, 5, 4}
		);
			
		routeMap.trace(5);			
	}

}

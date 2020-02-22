package jacobi.core.graph;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.junit.Test;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;
import jacobi.test.util.Jacobi;

public class FloydWarshallTest {
	
	@Test
	public void shouldBeAbleToComputeDistMatOn4Vertices() {
		AdjList adjList = new AdjList() {

			@Override
			public int order() {
				return 4;
			}

			@Override
			public Stream<Edge> edges(int from) {
				return Arrays.asList(
					new Edge(0, 2, -2.0),
					new Edge(2, 3,  2.0),
					new Edge(3, 1, -1.0),
					new Edge(1, 2,  3.0),
					new Edge(1, 0,  4.0)
				).stream().filter(e -> e.from == from);
			}
			
			//  4
			//   +-->*--+ -2 
			//   |      v
			//   *--3--->* 
			//   ^      |
			//   +--*<--+
			// -1         2
			
		};
		
		Matrix matrix = new FloydWarshall().computeDist(adjList);
		for(int i = 0; i < matrix.getRowCount(); i++) {
			System.out.println(Arrays.toString(matrix.getRow(i)));
		}
		Jacobi.assertEquals(Matrices.of(new double[][] {
			{0.0, -1.0, -2.0, 0.0},
			{4.0,  0.0,  2.0, 4.0},
			{5.0,  1.0,  0.0, 2.0},
			{3.0, -1.0,  1.0, 0.0},
		}), matrix);
	}

}

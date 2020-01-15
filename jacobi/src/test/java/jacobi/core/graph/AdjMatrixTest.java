package jacobi.core.graph;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;
import jacobi.core.impl.ImmutableMatrix;

public class AdjMatrixTest {
	
	@Test
	public void shouldIdentityMatrixBeConsistingOnlySelfLoop() {
		AdjList adjList = AdjMatrix.of(Matrices.identity(7));
		Assert.assertEquals(7, adjList.order());
		
		for(int i = 0; i < adjList.order(); i++) {
			Assert.assertArrayEquals(
				new Edge[] { new Edge(i, i, 1.0) }, 
				adjList.edges(i).toArray(n -> new Edge[n]));
		}
	}
	
	@Test
	public void shouldOnlyOneRowHavingNonZeroEntryBeRadiationNetwork() {
		AdjList adjList = AdjMatrix.of(Matrices.zeros(5).getAndSet(2, r -> {
			Arrays.fill(r, 1.0);
			r[2] = 0.0;
		}));
		
		Assert.assertEquals(5, adjList.order());
		for(int i = 0; i < adjList.order(); i++) {
			if(i == 2) {
				continue;
			}
			
			Assert.assertArrayEquals(
				new Edge[0], 
				adjList.edges(i).toArray(n -> new Edge[n])
			);
		}
		
		Assert.assertArrayEquals(
			new Edge[] {
				new Edge(2, 0, 1.0),
				new Edge(2, 1, 1.0),
				//new Edge(2, 2, 1.0),
				new Edge(2, 3, 1.0),
				new Edge(2, 4, 1.0)
				
			}, 
			adjList.edges(2).toArray(n -> new Edge[n])
		);
	}
	
	@Test
	public void shouldBeAbleToRepresentBipartiteGraph() {
		AdjMatrix adjMat = AdjMatrix.of(Matrices.wrap(new double[][] {
			{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 6.0, 11.0, 16.0, 21.0},
			{0.0, 0.0, 0.0, 0.0, 0.0, 2.0, 7.0, 12.0, 17.0, 22.0},
			{0.0, 0.0, 0.0, 0.0, 0.0, 3.0, 8.0, 13.0, 18.0, 23.0},
			{0.0, 0.0, 0.0, 0.0, 0.0, 4.0, 9.0, 14.0, 19.0, 24.0},
			{0.0, 0.0, 0.0, 0.0, 0.0, 5.0,10.0, 15.0, 20.0, 25.0},
			
			{ 1.0, 2.0, 3.0, 4.0, 5.0, 0.0, 0.0, 0.0, 0.0, 0.0},
			{ 6.0, 7.0, 8.0, 9.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0},
			{11.0, 12.0, 13.0, 14.0, 15.0, 0.0, 0.0, 0.0, 0.0, 0.0},
			{16.0, 17.0, 18.0, 19.0, 20.0, 0.0, 0.0, 0.0, 0.0, 0.0},
			{21.0, 22.0, 23.0, 24.0, 25.0, 0.0, 0.0, 0.0, 0.0, 0.0}						
			
		}));
		
		for(int i = 0; i < 5; i++){
			int base = i + 1;
			Assert.assertArrayEquals(new Edge[] {
				new Edge(i, 5, base + 0),
				new Edge(i, 6, base + 5),
				new Edge(i, 7, base + 10),
				new Edge(i, 8, base + 15),
				new Edge(i, 9, base + 20),
			}, adjMat.edges(i).toArray(n -> new Edge[n]));
		}
		
		for(int i = 6; i < 10; i++){
			int base = 5 * (i - 5);
			Assert.assertArrayEquals(new Edge[] {
					new Edge(i, 0, base + 1),
					new Edge(i, 1, base + 2),
					new Edge(i, 2, base + 3),
					new Edge(i, 3, base + 4),
					new Edge(i, 4, base + 5),
				}, adjMat.edges(i).toArray(n -> new Edge[n]));
		}
	}
	
	@Test
	public void shouldBeAbleToRepresentGridWith4NeighborConnectivity() {
		int row = 50;
		int col = 50;
		
		AdjMatrix adjMat = AdjMatrix.of(new ImmutableMatrix() {

			@Override
			public int getRowCount() {
				return row * col;
			}

			@Override
			public int getColCount() {
				return this.getRowCount();
			}

			@Override
			public double[] getRow(int index) {
				double[] rowInst = new double[this.getColCount()];
				if(index >= col){
					rowInst[index - col] = 1.0;
				}
				
				if(index / col < row - 1){
					rowInst[index + col] = 1.0;
				}
				
				if(index % col > 0) {
					rowInst[index - 1] = 1.0;
				}
				
				if(index % col < col - 1) {
					rowInst[index + 1] = 1.0;
				}
				return rowInst;
			}
			
		});
		
		Assert.assertEquals(row * col, adjMat.order());
		for(int i = 0; i < adjMat.order(); i++){
			Assert.assertEquals("i = " + i, 4 
					- (i % col == 0 || i % col == col - 1 ? 1 : 0)
					- (i / col == 0 || i / col == row - 1 ? 1 : 0), 
				adjMat.edges(i)
					.filter(e -> {
						
						Assert.assertTrue("Unexpected edge " + e.from + " -> " + e.to,
   							   e.to - e.from == -1   || e.to - e.from == 1
							|| e.to - e.from == -row || e.to - e.from == row);
						return true;
					})
					.count());
		}
	}
	
	@Test
	public void shouldBeAbleToRepresentRandomGraph() {
		Matrix matrix = Matrices.zeros(16);
		Random rand = new Random(Double.doubleToLongBits(-Math.E * Math.E * Math.PI));
		for(int i = 0; i < matrix.getRowCount(); i++) {
			matrix.getAndSet(i, r -> {
				for(int j = 0; j < r.length; j++) {
					r[j] = rand.nextBoolean() ? rand.nextDouble() : 0.0;
				}
			});
		}
		
		AdjMatrix adjMat = AdjMatrix.of(matrix);
		for(int i = 0; i < adjMat.order(); i++){
			Set<Integer> conn = adjMat.edges(i)
				.filter(e -> {
					Assert.assertEquals(matrix.get(e.from, e.to), e.weight, 1e-12);
					return e.weight != 0.0;
				})
				.map(e -> e.to).collect(Collectors.toSet());
			
			int from = i;
			Assert.assertTrue(IntStream.range(0, adjMat.order())
				.filter(k -> !conn.contains(k))
				.allMatch( j -> matrix.get(from, j) == 0.0 ));
		}
	}
	
	@Test
	public void shouldBeAbleToEncodeSparseList() {
		
		Assert.assertArrayEquals(new int[] {0, 2, 4}, AdjMatrix.scatters(new double[] {
			1.0, 0.0, 2.0, 0.0, 3.0
		}, new int[100]));
		
		Assert.assertArrayEquals(new int[] {2, 4, 9}, AdjMatrix.scatters(new double[] {
			0.0, 0.0, 1.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 9.0, 0.0
		}, new int[100]));
	}
	
	@Test
	public void shouldBeAbleToReturnNullIfAllAreNonZero() {
		double[] weights = new double[100];
		Arrays.fill(weights, Math.PI);
		
		Assert.assertNull(AdjMatrix.scatters(weights, new int[weights.length]));
	}
	
	@Test
	public void shouldBeAbleToReturnSparseIndexIfEntriesAreFew() {		
		int[] map = AdjMatrix.scatters(new double[]{
			0.0, 0.0, 0.0, 
			1.0, 0.0, 3.0, 
			0.0, 0.0, 0.0, 
			0.0}, new int[100]);
		Assert.assertArrayEquals(new int[] {3, 5}, map);
	}
	
	@Test
	public void shouldBeAbleToReturnSparseIndexIfEntriesAreNotContinuous() {	
		int[] map = AdjMatrix.scatters(IntStream.range(0, 30)
				.mapToDouble(i -> i % 2 == 0 ? Math.PI : 0)
				.toArray(),  
				new int[100]);
		
		Assert.assertArrayEquals(
			IntStream.range(0, 30).filter(i -> i % 2 == 0).toArray(), 
			map
		);
	}
	
	@Test
	public void shouldBeAbleToReturnSparseIndexIfContinuousRangeBreaksTooOften() {	
		int[] map = AdjMatrix.scatters(IntStream.range(0, 30)
				.mapToDouble(i -> i % 4 == 0 ? 0 : Math.PI)
				.toArray(),  
				new int[100]);
		
		Assert.assertArrayEquals(
			IntStream.range(0, 30).filter(i -> i % 4 > 0).toArray(), 
			map
		);
	}
	
	@Test
	public void shouldBeAbleToReturnRangePairsWhenOnly1ContinousRange() {
		double[] weights = new double[30];
		Arrays.fill(weights, 3, 23, Math.E);
		
		Assert.assertArrayEquals(new int[] {-1, 3, 23}, AdjMatrix.scatters(weights, new int[100]));
	}
	
	@Test
	public void shouldBeAbleToReturnRangePairsWhenAreMultipleRanges() {
		double[] weights = IntStream.range(0, 100)
				.mapToDouble(i -> i % 10 == 0 ? 0 : Math.PI)
				.toArray();
		
		//Assert.assertArrayEquals(new int[] {-1, 3, 23}, AdjMatrix.scatters(weights, new int[100]));
		int[] map = AdjMatrix.scatters(weights, new int[100]);
		Assert.assertEquals(-1, map[0]);
		for(int i = 1; i < map.length; i += 2) {
			int off = 10 * ((i - 1) / 2);
			Assert.assertEquals(off + 1, map[i]);
			Assert.assertEquals(off + 10, map[i + 1]);
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldFailWhenInputMatrixIsNotSquare() {
		AdjMatrix.of(Matrices.zeros(3, 2));
	}
	

}

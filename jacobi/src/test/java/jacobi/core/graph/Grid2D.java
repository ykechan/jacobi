package jacobi.core.graph;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;

public class Grid2D implements AdjList {
	
	public Grid2D(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public int order() {
		return this.width * this.height;
	}

	@Override
	public Stream<Edge> edges(int from) {
		int x = from % this.width;
		int y = from / this.height;
		return IntStream.of(
			this.encode(x + 1, y),
			this.encode(x, y + 1)
		).filter(v -> v >= 0).mapToObj(n -> new Edge(from, n, 1.0));
	}
	
	protected int encode(int x, int y) {
		return x < 0 || y < 0 || x >= this.width || y >= this.height
			? -1
			: this.width * y + x;
	}

	private int width, height;
}

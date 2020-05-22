package jacobi.core.hmm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.hmm.MarkovModel;
import jacobi.core.hmm.Viterbi.Track;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;

@JacobiImport("/jacobi/test/data/ViterbiTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class ViterbiTest {
	
	@JacobiInject(0)
	public Matrix transit;
	
	@JacobiInject(1)
	public Matrix emits;
	
	@JacobiInject(2)
	public Matrix init;
	
	@JacobiInject(3)
	public Matrix input;
	
	@JacobiResult(1000)
	public Matrix lnProbs;
	
	@Test
	@JacobiImport("test DNA GC Content #1")
	@JacobiEquals(expected = 1000, actual = 1000)
	public void shouldBeAbleToComputeLnProbInTestDNAGCContent1() {
		MarkovModel mm = MarkovModel.of(transit, emits, this.init.getRow(0)).validate();
		List<double[]> rows = new ArrayList<>();
		new Viterbi() {

			@Override
			protected Track forward(Matrix txFrom, double[] by, double[] curr) {
				if(rows.isEmpty()) {
					rows.add(curr);
				}
				Track track = super.forward(txFrom, by, curr);
				rows.add(track.values);
				return track;
			}
			
		}.compute(mm, IntStream.range(0, this.input.getRowCount())
			.mapToDouble(i -> this.input.get(i, 0))
			.mapToInt( v -> (int) v )
			.toArray());
		
		this.lnProbs = Matrices.wrap(rows.toArray(new double[0][]));
	}
	
	@Test
	public void shouldBeAbleToBacktrackAltRoutes() {
		List<short[]> tracks = Arrays.asList(
			new short[] {0, 1, 2, 3, 4},
			new short[] {4, 3, 2, 1, 0},
			new short[] {0, 1, 2, 3, 4},
			new short[] {4, 3, 2, 1, 0}
		);
		
		Assert.assertArrayEquals(new int[] {4, 0, 0, 4}, new Viterbi().backtrack(tracks, 4));
		Assert.assertArrayEquals(new int[] {1, 3, 3, 1}, new Viterbi().backtrack(tracks, 1));
		Assert.assertArrayEquals(new int[] {2, 2, 2, 2}, new Viterbi().backtrack(tracks, 2));
	}
	
	@Test
	public void shouldBeAbleToBacktrackAltRoutesWithFirstNullEntry() {
		List<short[]> tracks = Arrays.asList(
			null,
			new short[] {4, 3, 2, 1, 0},
			new short[] {0, 1, 2, 3, 4},
			new short[] {4, 3, 2, 1, 0}
		);
		
		Assert.assertArrayEquals(new int[] {4, 0, 0, 4}, new Viterbi().backtrack(tracks, 4));
		Assert.assertArrayEquals(new int[] {1, 3, 3, 1}, new Viterbi().backtrack(tracks, 1));
		Assert.assertArrayEquals(new int[] {2, 2, 2, 2}, new Viterbi().backtrack(tracks, 2));
	}

}

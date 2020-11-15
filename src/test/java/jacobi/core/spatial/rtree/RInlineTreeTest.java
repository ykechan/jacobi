package jacobi.core.spatial.rtree;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

public class RInlineTreeTest {
	
	@Test
	public void shouldBeAbleToFilter2DMbbsByRLayer() {
		int[] cuts = {
			//3, 5, 7, 11, 13
			3, 8, 15, 26, 39
		};
		
		double[] rects = {
		//  min-x, min-y, max-x, max-y
			110.0, 77.0, 241.0, 202.0,
			196.0, 156.0, 434.0, 286.0,
			395, 198, 865, 436,
			569, 54, 678, 121,
			855, 90, 1024, 135
		};
		
		double[] bounds = new double[rects.length];
		for(int i = 0; i < bounds.length; i +=4){
			bounds[i] = rects[i];
			bounds[i + 1] = rects[i + 2];
			bounds[i + 2] = rects[i + 1];
			bounds[i + 3] = rects[i + 3];
		}
		
		
		RLayer rLayer = new RLayer(cuts, bounds);
		double[] query = {557, 269};
		double dist = 557 - 422;
		
		RInlineTree rTree = new RInlineTree(
			Collections.singletonList(rLayer), 
			null, 
			null
		);
		
		int[] filter = rTree.queryFilter(rLayer, query, dist * dist, new int[]{5});
		Assert.assertArrayEquals(new int[]{-3, 12}, filter);
	}

}

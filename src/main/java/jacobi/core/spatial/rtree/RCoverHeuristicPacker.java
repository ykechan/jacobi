/* 
 * The MIT License
 *
 * Copyright 2020 Y.K. Chan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jacobi.core.spatial.rtree;

import java.util.Arrays;
import java.util.function.Function;

import jacobi.core.util.IntStack;
import jacobi.core.util.Weighted;

/**
 * Implementation of packing function of AABBs using greedy algorithm with cover heuristics.
 * 
 * <p>A collection of axis-aligned bounded boxes is a good fit if the resultant AABB
 * contains as little empty space as possible and the members overlaps each other 
 * as little as possible. However computing the exact volume of overlaps is inefficient.</p>
 * 
 * <p>Consider the coverage function f(V, X[i]) = Sum{Vol(X[i])} / Vol(V) for minimum bounding
 * box V with a series of AABBs X[i] inside. If X[i] is a tiling of V, f = 1. When f &lt; 1,
 * f is a measure of the ratio of empty spaces of V that is not covered by X[i]. When f &gt; 1
 * it indicates there are overlaps between the X[i]. This heuristics fails when amount of
 * empty spaces and overlaps cancels each others out, but since V is minimum bounding this
 * case is considered rare.</p>
 * 
 * <p>The algorithm thus greedy find the best fit based on the coverage function for the
 * number of nodes to pack within a parent node.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class RCoverHeuristicPacker implements Function<RLayer, RLayer> {
	
	/**
	 * Constructor.
	 * @param min  Minimum number of children to be packed in a node
	 * @param max  Maximum number of children to be packed in a node
	 */
	public RCoverHeuristicPacker(int min, int max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public RLayer apply(RLayer rLayer) {
		if(this.min >= this.max){
			int tail = rLayer.length() % this.min;
			int num = (rLayer.length() / this.min) + (tail == 0 ? 0 : 1);
			int[] packing = new int[num];
			Arrays.fill(packing, this.min);
			if(rLayer.length() % this.min > 0){
				packing[num - 1] = tail;
			}
			
			return RLayer.coverOf(packing, rLayer);
		}
		
		IntStack array = IntStack.newInstance();
		int begin = 0;
		while(begin < rLayer.length()){
			int span = this.packFront(rLayer, begin);
			array.push(span);
		}
		return RLayer.coverOf(array.toArray(), rLayer);
	}
	
	protected int packFront(RLayer rLayer, int begin) {
		int end = Math.min(begin + this.max, rLayer.length());
		if(end - begin < this.min){
			return end - begin;
		}
		
		double[] mbb = rLayer.mbbOf(begin, end - begin);
		double denom = this.volume(mbb);
		
		if(denom < 0.0){
			//return this.packLarge(rLayer, begin, mbb);
		}
		
		double heuristics = 1.0;
		int at = -1;
		
		mbb = rLayer.mbbOf(begin, 1);
		double mbbVol = this.volume(mbb);
		
		double cov = mbbVol;
		for(int i = begin + 1; i < end; i++){
			Weighted<Boolean> vol = this.updateMBB(rLayer, i, mbb);
			cov += vol.weight;
			
			if(i + 1 - begin < this.min){
				continue;
			}
			
			if(!vol.item && at == i){
				at = i + 1;
				continue;
			}
			
			if(vol.item){
				mbbVol = this.volume(mbb);
			}
				
			double score = Math.abs(1.0 - cov / mbbVol);
			if(score < heuristics){
				heuristics = score;
				at = i + 1;
			}
		}
		return at;
	}
	
	
	protected Weighted<Boolean> updateMBB(RLayer rLayer, int index, double[] mbb) {
		double vol = 1.0;
		boolean extended = false;
		
		int base = index * mbb.length;
		for(int j = 0; j < mbb.length; j+=2){
			double min = rLayer.bounds[base + j];
			double max = rLayer.bounds[base + j + 1];
			vol *= max - min;
			
			if(mbb[j] > min){
				mbb[j] = min;
				extended = true;
			}
			
			if(mbb[j + 1] < max){
				mbb[j + 1] = max;
				extended = true;
			}
		}
		
		return new Weighted<>(extended, vol);
	}
	
	protected double volume(double[] aabb) {
		double vol = 1.0;
		for(int i = 0; i < aabb.length; i+=2){
			vol *= aabb[i + 1] - aabb[i];
			if(vol > MAG_THRES){
				return -1.0;
			}
		}
		return vol;
	}
	
	protected double volume(double[] aabb, double[] range) {
		double vol = 1.0;
		for(int i = 0; i < aabb.length; i+=2){
			vol *= (aabb[i + 1] - aabb[i]) / range[i / 2];
		}
		return vol;
	}

	private int min, max;
	
	protected static final double MAG_THRES = Integer.MAX_VALUE;
	
	protected static final double SPAN = 16.0;
}
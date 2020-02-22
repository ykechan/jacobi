package jacobi.test.util;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.graph.AdjList;
import jacobi.api.graph.Edge;
import jacobi.core.util.Real;

public class JacobiGraphSvg {
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static JacobiGraphSvg readFrom(InputStream input) throws IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		GraphSvgHandler handler = new GraphSvgHandler();
		try {
			factory.newSAXParser().parse(input, handler);
			return handler.get();
		} catch (SAXException | ParserConfigurationException ex) {
			throw new UnsupportedOperationException(ex);
		}
	}
	
	protected JacobiGraphSvg(AdjList graph, Matrix placement) {
		this.graph = graph;
		this.placement = placement;
	}
	
	public AdjList getGraph() {
		return this.graph;
	}
	
	public Matrix getPlacement() {
		return this.placement;
	}

	public JacobiSvg render() {
		JacobiSvg svg = new JacobiSvg();
		
		for(int i = 0; i < this.graph.order(); i++) {
			this.graph.edges(i).sequential().forEach(e -> {
				double[] uPos = placement.getRow(e.from);
				double[] vPos = placement.getRow(e.to);
				
				svg.arrow(uPos[0], uPos[1], vPos[0], vPos[1], Color.LIGHT_GRAY).text(
					String.format("%.2f", e.weight), 
					(uPos[0] + vPos[0]) / 2.0, 
					(uPos[1] + vPos[1]) / 2.0, 
					Color.BLUE);
			});
		}
		
		for(int i = 0; i < this.graph.order(); i++){
			double[] pos = this.placement.getRow(i);
			svg.dot(pos[0], pos[1], Color.RED)
				.text("#" + i, pos[0], pos[1], Color.BLUE);
		}
		
		return svg;
	}
	
	private AdjList graph;
	private Matrix placement;
	
	public static class Builder implements Supplier<JacobiGraphSvg> {
		
		protected Builder() {
			this.places = new ArrayList<>();
			this.edgeMap = new TreeMap<>();
		}
		
		public Builder place(int vertex, double x, double y) {
			double[] pos = this.touch(vertex);
			pos[0] = x;
			pos[1] = y;
			return this;
		}
		
		public Builder add(double x, double y) {
			return this.place(this.places.size(), x, y);
		}
		
		public Builder fitCircle(double centerX, double centerY, double radius, int deg) {
			double x = radius;
			double y = 0.0;
			
			this.add(x, y);
			
			for(int i = 1; i < deg; i++) {
				// cos -sin
				// sin  cos
				double rad = i * (2 * Math.PI) / deg;
				double c = Math.cos(rad);
				double s = Math.sin(rad);
				this.add(c * x - s * y, s * x + c * y);
			}
			return this;
		}
		
		public Builder fitUnitCircle(int deg) {
			return this.fitCircle(0.0, 0.0, 1.0, deg);
		}
		
		public Builder connect(int u, int v, double w) {
			
			this.touch(u);
			this.touch(v);
			
			this.edgeMap
				.computeIfAbsent(u, ArrayList::new)
				.add(new Edge(u, v, w));
			return this;
		}
		
		public Builder connect(
				double fromX, double fromY, 
				double toX, double toY, 
				double w) {
			int u = this.vertexAt(new double[] {fromX, fromY});
			int v = this.vertexAt(new double[] {toX, toY});
			
			if(u < 0 || v < 0){
				throw new IllegalArgumentException("No vertex found at "
						+ (u < 0 ? fromX + "," + fromY : toX + "," + toY ));
			}
			
			return this.connect(u, v, w);
		}

		@Override
		public JacobiGraphSvg get() {
			return new JacobiGraphSvg(
				this.toGraph(), 
				Matrices.wrap(places.toArray(new double[0][]))
			);
		}
		
		protected double[] touch(int vertex) {
			if(vertex < 0){
				throw new IllegalArgumentException("Invalid vertex " + vertex);
			}
			
			while(this.places.size() <= vertex){
				this.places.add(new double[2]);
			}
			
			return this.places.get(vertex);
		}
		
		protected AdjList toGraph() {
			return new AdjList() {

				@Override
				public int order() {
					return places.size();
				}

				@Override
				public Stream<Edge> edges(int from) {
					List<Edge> e = edgeMap.get(from);
					return e == null ? Stream.empty() : e.stream();
				}
				
			};
		}
		
		protected int vertexAt(double[] pos) {
			for(int i = 0; i < this.places.size(); i++){
				double[] q = this.places.get(i);
				if(Real.isNegl(pos[0] - q[0])
				&& Real.isNegl(pos[1] - q[1])) {
					return i;
				}
			}
			return -1;
		}
		
		private Map<Integer, List<Edge>> edgeMap;
		private List<double[]> places;
	}
	
	protected static class GraphSvgHandler 
		extends DefaultHandler 
		implements Supplier<JacobiGraphSvg> {

		@Override
		public JacobiGraphSvg get() {
			return this.builder.get();
		}
		
		@Override
		public void startDocument() throws SAXException {
			this.builder = JacobiGraphSvg.builder();
			this.elements = new ArrayDeque<>();
		}
		
		@Override
		public void endDocument() throws SAXException {
			this.elements.stream().filter(e -> "line".equals(e.tag))
				.sequential()
				.forEach(e -> this.builder.connect(
					e.prop[0], e.prop[1], 
					e.prop[2], e.prop[3], 
					e.prop[4]
				));
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if(this.buf != null){
				this.buf.append(ch, start, length);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) 
				throws SAXException {									
			
			Element prev = this.elements.isEmpty() ? null : this.elements.peek();
			
			if("text".equals(localName) && prev != null && "line".equals(prev.tag)) {
				// assign weight
				String text = this.buf.toString().trim();
				prev.prop[4] = Double.parseDouble(text);
			}
			
			if("text".equals(localName) && prev != null && "circle".equals(prev.tag)) {
				// assign vertex key
				String text = this.buf.toString().trim();
				if(!text.startsWith("#")) {
					throw new IllegalArgumentException("Un-recognized text " + text + ", not a vertex key.");
				}
				
				this.builder.place(Integer.parseInt(text.substring(1)), 
					prev.prop[0], 
					prev.prop[1]
				);
			}
			
			if("text".equals(localName)){				
				this.buf = null;
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attrs) 
				throws SAXException {
			
			if("text".equals(localName)){
				this.buf = new StringBuilder();	
				return;
			}
				
			if("line".equals(localName)){
				this.elements.push(new Element(localName, new double[]{
					Double.parseDouble(attrs.getValue("x1")),
					Double.parseDouble(attrs.getValue("y1")),
					Double.parseDouble(attrs.getValue("x2")),
					Double.parseDouble(attrs.getValue("y2")),
					0.0
				}));
				return;
			}
			
			if("circle".equals(localName)){
				this.elements.push(new Element(localName, new double[]{
					Double.parseDouble(attrs.getValue("cx")),
					Double.parseDouble(attrs.getValue("cy")),
				}));
				
				return;
			}
		}
		
		private StringBuilder buf;
		private JacobiGraphSvg.Builder builder;
		private Deque<Element> elements;
	}
	
	protected static class Element {
		
		public final String tag;
		
		public final double[] prop;

		public Element(String tag, double[] prop) {
			this.tag = tag;
			this.prop = prop;
		}
		
	}
	
}

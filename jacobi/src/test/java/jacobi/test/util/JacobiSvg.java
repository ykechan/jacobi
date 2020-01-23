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
package jacobi.test.util;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * Basic drawing utility for visualizing spatial data using SVG.
 * 
 * <p>Asserting the result of testing on spatial data can be hard for non-trivial cases.
 * This basic utility provides a visualization of the given result using simple representation
 * of dots, lines, rectangles and text to provide a representation of various data structures.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class JacobiSvg {
	
	public JacobiSvg() {
		this.elements = new ArrayList<>();
	}

	public JacobiSvg touch(double x, double y) {
		this.min = this.min == null ? new Vector2D(x, y) : new Vector2D(
			Math.min(x, this.min.x),
			Math.min(y, this.min.y)
		);
		
		this.max = this.max == null ? new Vector2D(x, y) : new Vector2D(
			Math.max(x, this.max.x),
			Math.max(y, this.max.y)
		);
		return this;
	}
	
	public JacobiSvg dot(double x, double y, Color color) {
		return this.touch(x, y).push(new StringBuilder()
			.append("<circle")
			.append(" cx=").append(this.quote(x))
			.append(" cy=").append(this.quote(y))
			.append(" fill=").append(this.quote(color))
			.append(" />")
			.toString());
	}
	
	public JacobiSvg rect(double x, double y, double w, double h, Color color) {
		return this.touch(x, y).touch(x + w, y + h).push(new StringBuilder()
			.append("<rect")
			.append(" x=").append(this.quote(x)).append(" y=").append(this.quote(y))
			.append(" width=").append(this.quote(w)).append(" height=").append(this.quote(h))
			.append(" stroke=").append(this.quote(color))
			.append(" fill=").append(this.quote("none"))
			.append(" />")
			.toString()
		);
	}
	
	public JacobiSvg line(double x0, double y0, double x1, double y1, Color color) {
		return this.touch(x0, y0).touch(x1, y1)
			.push(this.elementLine(x0, y0, x1, y1, color));
	}
	
	public JacobiSvg arrow(double x0, double y0, double x1, double y1, Color color) {
		return this.touch(x0, y0).touch(x1, y1)
			.push(this.elementLine(x0, y0, x1, y1, color)
				.replace(" />", " marker-end="+this.quote("url(#arrow)") + "/>")
			);
	}
	
	public JacobiSvg text(String message, double x, double y, Color color) {		
		return this.touch(x, y).push(new StringBuilder()
			.append("<text")
			.append(" x=").append(this.quote(x))
			.append(" y=").append("{^").append(String.valueOf(y)).append('}')
			.append(" fill=").append(this.quote(color))
			.append(" font-size=?")
			.append('>')
			.append(message == null ? "" : message.replace("<", "&lt;").replace(">", "&gt;"))
			.append("</text>")
			.toString()
		);
	}	
	
	public JacobiSvg toSVG(OutputStream output) {
		double width = this.min == null ? 100.0 : this.max.x - this.min.x;
		double height = this.min == null ? 100.0 : this.max.y - this.min.y;
		
		PrintStream out = new PrintStream(output);
		out.println("<svg viewBox=\"{0}\" xmlns=\"{1}\">"
			.replace("{0}", DoubleStream
				.of(
					(this.min == null ? 0.0 : this.min.x) - 0.1 * width,
					(this.min == null ? 0.0 : this.min.y) - 0.1 * height,
					1.2 * width,
					1.2 * height
				)
				.mapToObj(String::valueOf)
				.collect(Collectors.joining(" ")))
			.replace("{1}", XMLNS)
		);
		
		double stroke = Math.max(width, height) / 160.0;
		double fontSize = 3 * stroke;
				
		this.defaultSection(out, stroke).elements
			.stream()
			.map(s -> s.replace("?", this.quote(fontSize)))
			.map(s -> {
				int pos = s.indexOf("{^");
				if(pos < 0) {
				    return s;
				}
				
				int end = s.indexOf("}", pos);
				double y = Double.parseDouble(s.substring(pos + 2, end));
				return s.substring(0, pos) 
				     + this.quote(String.valueOf(y - 1.2 * stroke)) 
				     + s.substring(end + 1);
			})
			.map(s -> "    " + s).forEach(out::println);
		
		out.println("</svg>");
		return this;
	}
	
	public File exportTo(File outFile) throws IOException {
	    if(outFile == null) {
	        File tempFile = File.createTempFile("tmp", ".svg");
	        try {
	            return this.exportTo(tempFile);
	        } finally {
	            StackTraceElement caller = new Exception().getStackTrace()[1];
	            System.out.println(caller.getClassName() + "::" + caller.getMethodName()
	                + " export " + tempFile.getAbsolutePath());
	        }
	    }
	    
	    try(OutputStream output = new FileOutputStream(outFile)){
	        this.toSVG(output);
	    }
	    return outFile;
	}
	
	protected String elementLine(double x0, double y0, double x1, double y1, Color color) {
		return new StringBuilder()
			.append("<line")
			.append(" x1=").append(this.quote(x0)).append(" y1=").append(this.quote(y0))
			.append(" x2=").append(this.quote(x1)).append(" y2=").append(this.quote(y1))
			.append(" stroke=").append(this.quote(color))
			.append(" />").toString();
	}
	
	protected JacobiSvg defaultSection(PrintStream out, double thickness) {
		out.println("<defs>");
		out.println(new StringBuilder("<marker")
			.append(" id=").append(this.quote("arrow"))
			.append(" viewBox=").append(this.quote("0 0 10 10"))
			.append(" refX=").append(this.quote("5"))
			.append(" refY=").append(this.quote("5"))
			.append(" markerWidth=").append(this.quote("3"))
			.append(" markerHeight=").append(this.quote("3"))
			.append(" orient=").append(this.quote("auto-start-reverse"))
			.append('>')
		);
		out.println("    <path d=" + this.quote("M 0 0 L 10 5 L 0 10 z") + " />");
		out.println("</marker>");
		out.println("</defs>");
		
		out.println("<style>");
		out.println(" circle { r: " + String.valueOf(thickness) +  " } ");
		out.println(" rect { fill: none; stroke-width: " + String.valueOf(thickness) +  " } ");
		out.println(" line { stroke-width: " + String.valueOf(thickness) +  " } ");
		
		out.println("</style>");
		return this;
	}
	
	protected JacobiSvg push(String elem) {
		if(elem == null || elem.trim().isEmpty()) {
			return this;
		}
		
		this.elements.add(elem);
		return this;
	}		
	
	protected <T> String quote(T item) {		
		String str = item == null 
			? ""
			: item instanceof Color
				? "rgb({0},{1},{2})"
					.replace("{0}", String.valueOf(((Color) item).getRed()))
					.replace("{1}", String.valueOf(((Color) item).getGreen()))
					.replace("{2}", String.valueOf(((Color) item).getBlue()))
				: item.toString();
		return "\"" + str.replace("\"", "\\\"") + "\"";
	}	

	private List<String> elements;
	private Vector2D min, max;
	
	protected static class Vector2D {
		
		public final double x, y;

		public Vector2D(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
	}
	
	private static final String XMLNS = "http://www.w3.org/2000/svg";
	
}

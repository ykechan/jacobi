package jacobi.test.util;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.SAXParserFactory;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class JacobiSvgTest {
	
	@Test
	public void shouldBeAbleToGenerateEmptySvg() throws IOException {
		this.assertValidXML(new JacobiSvg().exportTo(null), null);
	}
	
	@Test
	public void shouldBeAbleToGenerateDots() throws IOException {
		AtomicInteger count = new AtomicInteger(0);
		this.assertValidXML(new JacobiSvg()
				.dot(1.0, 1.0, Color.RED)
				.dot(2.0, 2.0, Color.RED)
				.dot(3.0, 3.0, Color.RED)
				.exportTo(null)
			, new DefaultHandler() {

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attrs) 
						throws SAXException {					
					if("circle".equals(localName)) {
						Assert.assertTrue(attrs.getValue("cx").equals(attrs.getValue("cy")));
						Assert.assertEquals("rgb(255,0,0)", attrs.getValue("fill"));
						count.incrementAndGet();
					}					
				}
				
			});
		
		Assert.assertEquals(3, count.get());
	}
	
	@Test
	public void shouldBeAbleToGenerateRect() throws IOException {
		AtomicInteger count = new AtomicInteger(0);
		AtomicInteger verify = new AtomicInteger(0);
		this.assertValidXML(new JacobiSvg()
				.rect(0.0, 0.0, 1.0, 1.0, Color.BLUE)
				.rect(0.0, 1.0, 1.0, 1.0, Color.BLUE)
				.exportTo(null)
			, new DefaultHandler() {

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attrs) 
						throws SAXException {					
					if("rect".equals(localName)) {
						//Assert.assertTrue(attrs.getValue("x1").equals(attrs.getValue("cy")));
						
						Assert.assertEquals("rgb(0,0,255)", attrs.getValue("stroke"));
						count.incrementAndGet();
					}
					
					if("svg".equals(localName)) {
						double[] viewbox = Arrays.stream(attrs.getValue("viewBox").split(" "))
								.mapToDouble(Double::parseDouble)
								.toArray();
						
						Assert.assertTrue(viewbox[0] <= 0.0);
						Assert.assertTrue(viewbox[1] <= 0.0);
						
						Assert.assertTrue(viewbox[2] >= 1.0);
						Assert.assertTrue(viewbox[3] >= 1.0);
						verify.incrementAndGet();
					}
				}
				
			});
		
		Assert.assertEquals(2, count.get());
		Assert.assertEquals(1, verify.get());
	}
	
	@Test
	public void shouldBeAbleToGenerateLineAndText() throws IOException {
		this.assertValidXML(new JacobiSvg()
			.rect(-1.0, -1.0, 11.0, 11.0, Color.LIGHT_GRAY)
			.line(0.0, 0.0, 10.0, 0.0, Color.BLACK)
			.text("This should stay on top", 0.0, 0.0, Color.GRAY)
			.exportTo(null), null);
	}	
	
	@Test
	public void shouldBeAbleToGenerateScatteredPointsWithLabels() throws IOException {
	    JacobiSvg svg = new JacobiSvg();
	    Random rand = new Random(Double.doubleToLongBits(Math.PI));
	    for(int i = 0; i < 100; i++) {
	        double x = rand.nextDouble() * 100.0;
	        double y = rand.nextDouble() * 100.0;
	        
	        svg.dot(x, y, Color.RED).text(String.format("%.2f, %.2f", x, y), x, y, Color.GRAY);
	    }
	    this.assertValidXML(svg.exportTo(null), null);
	}
	
	protected void assertValidXML(File inFile, DefaultHandler handler) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.newSAXParser()
				.parse(inFile, handler == null ? new DefaultHandler() : handler);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}

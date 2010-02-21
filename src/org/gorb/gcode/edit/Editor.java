/**
 * 
 */
package org.gorb.gcode.edit;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

public class Editor
{
	public double	x = 0;
	public double	y = 0;

	public boolean inBoundingBox(String gcode, Rectangle2D r) {
		try {parse(gcode);} catch (Exception e) {return false;}
		return r.contains(x, y);
	}

	public void parse(String gcode) {
		Pattern p = Pattern.compile(".*X([0-9.]+)\\s*Y([0-9.]+).*");
		Matcher m = p.matcher(gcode);
		m.matches();
		x = Double.parseDouble(m.group(1));
		y = Double.parseDouble(m.group(2));
	}
	public List<String> gcodesWithinRect(List<String> lines, Rectangle rect) {
		List<String> filteredLines = new ArrayList<String>();
		
		for (String line : lines) {
			if (!inBoundingBox(line, rect)) {
				filteredLines.add(line);
			}
		}
		return filteredLines;
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		List<String> lines = IOUtils.readLines(new ClassPathResource("emmy.nc").getInputStream());

		Rectangle rect = new Rectangle(0,0,1,1);

		Editor editor = new Editor();
		List<String> filteredLines = editor.gcodesWithinRect(lines, rect);
		for (String line : filteredLines) {
			System.out.println(line);
		}
	}

}
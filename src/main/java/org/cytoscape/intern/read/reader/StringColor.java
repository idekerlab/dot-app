/**************************
 * Copyright © 2015-2017 Braxton Fitts, Ziran Zhang, Massoud Maher
 * 
 * This file is part of dot-app.
 * dot-app is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * dot-app is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with dot-app.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cytoscape.intern.read.reader;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Handles mapping of String colors to Java Color objects
 * 
 * @author Braxton Fitts
 * @author Ziran Zhang
 * @author Massoud Maher
 */
public class StringColor {

	private Map<String, Map<String, Color>> colorMap = new HashMap<String, Map<String, Color>>();

	/**
	 * Initializes colorMap with all passed in colors by reading in files
	 * 
	 * @param files Text files mapping RGB vals to a color string name in this format
	 * colorscheme
	 * R	G	B	name
	 * separated by whitespace
	 * Files are expected to be stored in resources folder
	 */
	public StringColor(String... files) {
		
		for (String fileName : files) {
			try {
				URL fileURL = StringColor.class.getClassLoader().getResource(fileName);
				Scanner scanner = new Scanner( new InputStreamReader(fileURL.openStream()) );
				Map<String, Color> colors = new HashMap<String, Color>();

				// First line of file is colorscheme
				String colorscheme = scanner.next();
				while(scanner.hasNext()) {
					// Create map of colors to color strings
					int red = scanner.nextInt();
					int green = scanner.nextInt();
					int blue = scanner.nextInt();
				
					String name = "";
					// While there are more tokens in name and not at EOF
					while(scanner.hasNextInt() == false && scanner.hasNext()) {
						// Add all tokens of name (some x11 names have spaces)
						name += scanner.next() + " ";
					}
					name = name.trim();
					name = name.toLowerCase();
			
					colors.put(name, new Color(red, green, blue));
				}
				scanner.close();
			
				// Add created map with its corresponding scheme
				colorMap.put(colorscheme, colors);
			}
			catch(FileNotFoundException fileEx) {
				System.out.println(fileEx.toString());
			}
			catch(IOException IOEx) {
				System.out.println(IOEx.toString());
			}
			
		}
		
	}
	
	/**
	 * Gets the Java color associated with a color name String
	 *
	 *  @param colorScheme colorscheme desired. Either "x11" or "svg"
	 *  @param name Name of color
	 */
	public Color getColor(String colorScheme, String name) {
		// Default to x11
		if(colorScheme == null) {
			colorScheme = "x11";
		}
		
		Map<String, Color> map = colorMap.get(colorScheme);
		return map.get(name.toLowerCase());
	}
	
}
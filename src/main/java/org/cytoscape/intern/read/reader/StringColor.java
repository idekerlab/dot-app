package org.cytoscape.intern.read.reader;

import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.awt.Color;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * Handles mapping of String colors to Java Color objects
 * 
 * @author Braxton Fitts
 * @author Ziran Zhang
 * @author Massoud Maher
 */
public class StringColor {

	private Map<String, Color> colorMap = new HashMap<String, Color>();

	/**
	 * Initializes colorMap with all passed in colors by reading in files
	 * 
	 * @param files Text files mapping RGB vals to a color string name in this format
	 * R	G	B	name
	 * separated by tabs/whitespace
	 */
	public StringColor(String... files) {
		
		for (String fileName : files) {
			try {
				URL fileURL = StringColor.class.getClassLoader().getResource(fileName);
				Scanner scanner = new Scanner( new InputStreamReader(fileURL.openStream()) );

				while(scanner.hasNext()) {
					System.out.println(scanner.nextLine());
					int red = scanner.nextInt();
					int green = scanner.nextInt();
					int blue = scanner.nextInt();
					String name = scanner.next();
				
					colorMap.put(name, new Color(red, green, blue));
					System.out.print(name);
				}
				scanner.close();
			}
			catch(FileNotFoundException fileEx) {
				System.out.println("File " + fileName + " not found");
			}
			catch(IOException IOEx) {
				System.out.println(IOEx.toString());
			}
		}
		
	}
	
	/**
	 * Gets the Java color associated with a color name String
	 * 
	 *  @param name Name of color
	 */
	public Color getColor(String name) {
		return colorMap.get(name);
	}
	
	/*public static void main(String[] args) {
		StringColor strColor = new StringColor("svg_colors.txt");
		System.out.println( strColor.getColor("peachpuff").toString() );
	}*/
	
}




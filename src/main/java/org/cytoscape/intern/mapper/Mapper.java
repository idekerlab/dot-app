package org.cytoscape.intern.mapper;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Handles mapping of Cytoscape properties to .dot attributes in the form of a String.
 * Contains implementation for properties that are shared by nodes and edges and declarations
 * for unshared properties. 
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public abstract class Mapper {

	// maps Cytoscape properties  by their ID Strings to their .dot equivalents if relationship is simple equivalency
	protected ArrayList<String> simpleVisPropsToDot; 
	
	// maps Cytoscape line types to the equivalent string used in .dot
	private HashMap<LineType, String> lineTypeMap;
	
	// view that this mapper object is mapping
	protected View<? extends CyIdentifiable> view;
	
	// debug logger
	protected static final Logger LOGGER;
	//Initialilze logger with file handler
	static {
		LOGGER = Logger.getLogger("org.cytoscape.intern.mapper.Mapper");
		FileHandler handler = null;
		
		try {
			handler = new FileHandler("log_Mapper.txt");
			handler.setLevel(Level.ALL);
			
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		
		LOGGER.addHandler(handler);
	}
	
	/**
	 * Initializes view field
	 * 
	 * @param view View that this mapper is being used to map to dot
	 */
	public Mapper(View<? extends CyIdentifiable> view) {
		this.view = view;
		lineTypeMap = new HashMap<LineType, String>();
		populateMaps();
	}
	
	/**
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		lineTypeMap.put(LineTypeVisualProperty.LONG_DASH, "dashed");
		lineTypeMap.put(LineTypeVisualProperty.EQUAL_DASH, "dashed");
		lineTypeMap.put(LineTypeVisualProperty.SOLID, "solid");
		lineTypeMap.put(LineTypeVisualProperty.DOT, "dotted");
	}
	/**
	 * Given a color, returns the color in String format that .dot uses for color.
	 * Format is "#%rr%gg%bb%aa" -- red, green, blue, alpha in hexadecimal
	 * 
	 * @param color color being converted
	 * @param alpha alpha level of that color-- cytoscape does not use alpha in Paint class
	 * @return String representation of color in .dot format of rgba
	 */
	protected String mapColorToDot(Color color, Integer alpha) {
		LOGGER.info("Creating .dot color attribute string");
		Integer red = color.getRed();
		Integer green = color.getGreen();
		Integer blue = color.getBlue();
		String result = String.format("#%02X%02X%02X%02X", red, green, blue, alpha);
		LOGGER.info("Created .dot color attribute string. Result: " + result);
		return result;
	}
	
	/**
	 * Given a font, returns the .dot equivalent in String form
	 * 
	 * @param font font to be converted
	 * @param size size of font to be converted
	 * @return String that is .dot representation of the provided font
	 */
	protected String mapFont(Font font, Integer size) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the .dot equivalent in String form for style attribute. Only handles linestyle
	 */
	protected String mapDotStyle() {
		// TODO
		/**
		 * Pseudocode:
		 * if instanceof edge check BVL.EDGE_LINE_TYPE
		 * else if instanceof node check BVL.NODE_BORDER_LINE_TYPE
		 * Retrieve .dot string from lineType hashmap
		 */
		StringBuilder dotStyle = new StringBuilder();
		if (view.getModel() instanceof CyNode) {
			LineType lineType = view.getVisualProperty(BasicVisualLexicon.NODE_BORDER_LINE_TYPE);
			String lineStr = lineTypeMap.get(lineType);
			if (lineStr == null) {
				lineStr = "solid";
				LOGGER.warning("Cytoscape property doesn't map to a .dot attribute. Setting to default");
			}
			String style = String.format("style = \"%s,", lineStr);
			dotStyle.append(style);
		} else if (view.getModel() instanceof CyEdge) {
			LineType lineType = view.getVisualProperty(BasicVisualLexicon.EDGE_LINE_TYPE);
			String lineStr = lineTypeMap.get(lineType);
			if (lineStr == null) {
				lineStr = "solid";
				LOGGER.warning("Cytoscape property doesn't map to a .dot attribute. Setting to default");
			}
			String style = String.format("style = \"%s,", lineStr);
			dotStyle.append(style);
		}
		return dotStyle.toString();
	}
	
	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	public abstract String getElementString ();
}

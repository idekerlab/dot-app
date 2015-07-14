package org.cytoscape.intern.mapper;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
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
	private static final HashMap<LineType, String> LINE_TYPE_MAP = new HashMap<LineType, String>();
	static {
		LINE_TYPE_MAP.put(LineTypeVisualProperty.LONG_DASH, "dashed");
		LINE_TYPE_MAP.put(LineTypeVisualProperty.EQUAL_DASH, "dashed");
		LINE_TYPE_MAP.put(LineTypeVisualProperty.SOLID, "solid");
		LINE_TYPE_MAP.put(LineTypeVisualProperty.DOT, "dotted");
	}
	
	// view that this mapper object is mapping
	protected View<? extends CyIdentifiable> view;
	
	protected static final double PPI = 96;
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
	protected String mapFont(Font font, Integer size, Color color, Integer transparency) {
		// TODO
		
		//in Cytoscape default size is 12, default fontname is HelveticaNeue-Ultralight
		//in .Dot default size is 14, default fontname is Times-Roman
		
		//Question: not sure if font size in .dot is all integer or it also can be float
	    //color in .dot has three expressions: I. ColorName(e.g. grey)  II. three floats (0.8396, 0.4862, 0.8549)
		//III. hexadecimal number (between #000000 and #FFFFFF). So should we add two other overloaded methods 
		//that can take in the other two color expressions as parameters. Or we can assume we will only pass 
		//in only one expression if that's possible (Although I don't think the colorName can cover all the possible colors,
		//but I'm pretty sure both II(3 floats) and III(hexadecimal number) can cover all the possible colors in cyto)
		
		//needs double check with the codes below, can't think of any corner case/error checking for now
		/**
		 * LOGGER.info("Label font and size translation");
		 * 
		 * String returnValue = "";
		 * 
		 * returnValue += "fontname= " + font.getFontName() + ",";
		 * 
		 * returnValue += " fontsize= " + size.toString() + ",";
		 * 
		 * returnValue += " fontcolor= " + color.toString() + ",";
		 * 
		 * //might need error checking for transparency, since transparency has to be within 0 to 255
		 * 
		 * returnValue += " transparency= " + transparency.toString() + ",";
		 * 
		 * LOGGER.info("Dot attribute associate with font is: " + returnValue);
		 * 
		 * return returnValue;
		 */
		return null;
		
		
	}
	
	/**
	 * Returns the .dot equivalent in String form for style attribute. Only handles linestyle
	 * Does not include "style=" bit
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
			String lineStr = LINE_TYPE_MAP.get(lineType);
			if (lineStr == null) {
				lineStr = "solid";
				LOGGER.warning("Cytoscape property doesn't map to a .dot attribute. Setting to default");
			}
			String style = String.format("style = \"%s,", lineStr);
			dotStyle.append(style);
		} else if (view.getModel() instanceof CyEdge) {
			LineType lineType = view.getVisualProperty(BasicVisualLexicon.EDGE_LINE_TYPE);
			String lineStr = LINE_TYPE_MAP.get(lineType);
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
	 * Returns the String that denotes a position in .dot format
	 * 
	 * @param x x coordinate of position
	 * @param y y coordinate of position
	 * @return String in form %x,%y?
	 */
	protected String mapPosition(Double x, Double y) {
		return String.format("%f,%f", x/PPI, y/PPI);
	}
	
	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	public abstract String getElementString();
}

package org.cytoscape.intern.write.mapper;

import org.cytoscape.intern.FileHandlerManager;
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
	
	// Pixel per inch scaling factor
	protected static final double PPI = 72;
	
	// debug logger
	protected static final Logger LOGGER;
	
	private static final FileHandlerManager FILE_HANDLER_MGR = FileHandlerManager.getManager();
	// Initialize logger with file handler
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
		FILE_HANDLER_MGR.registerFileHandler(handler);
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
	 * Format is "#rrggbbaa" -- red, green, blue, alpha in hexadecimal
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
	 * Given a font, returns the .dot equivalent in String form including the
	 * font name, size and color
	 * 
	 * @param font font to be converted
	 * @param size size of font to be converted
	 * @param color color of font
	 * @param transparency transparency of font from 0-255
	 * @return String that is .dot representation of the provided font
	 */
	protected String mapFont(Font font, Integer size, Color color, Integer transparency) {
		
		LOGGER.info("Label font, size, color, and transparency translation");
		 
		String returnValue = "";
		
		returnValue += "fontname = \"" + font.getFontName() + "\",";  
		returnValue += "fontsize = \"" + size.toString() + "\","; 
		returnValue += "fontcolor = \"" + mapColorToDot(color, transparency) + "\"";

		LOGGER.info("Dot attributes associate with font is: " + returnValue);
		
		return returnValue;		
		
	}
	
	/**
	 * Returns the .dot equivalent in String form for style attribute. Only handles linestyle
	 * Does not include "style=" bit
	 */
	protected String mapDotStyle() {
		StringBuilder dotStyle = new StringBuilder();
		if (view.getModel() instanceof CyNode) {
			LineType lineType = view.getVisualProperty(BasicVisualLexicon.NODE_BORDER_LINE_TYPE);
			// get .dot equivalent of line style
			String lineStr = LINE_TYPE_MAP.get(lineType);
			if (lineStr == null) {
				lineStr = "solid";
				LOGGER.warning("Cytoscape property doesn't map to a .dot attribute. Setting to default");
			}
			String style = String.format("style = \"%s,", lineStr);
			dotStyle.append(style);
		} 
		else if (view.getModel() instanceof CyEdge) {
			LineType lineType = view.getVisualProperty(BasicVisualLexicon.EDGE_LINE_TYPE);
			String lineStr = LINE_TYPE_MAP.get(lineType);
			if (lineStr == null) {
				lineStr = "solid";
				LOGGER.warning("Cytoscape property doesn't map to a .dot attribute. Setting to default");
			}
			String style = String.format("style = \"%s\"", lineStr);
			dotStyle.append(style);
		}
		return dotStyle.toString();
	}
	
	/**
	 * Returns the String that denotes a position in .dot format
	 * Note: Positive in graphviz is up and right, positive in cytoscape
	 * is down and right. Therefore, we negate the y-values
	 * 
	 * @param x x coordinate of position
	 * @param y y coordinate of position
	 * @return String in form %x,%y!
	 */
	protected String mapPosition(Double x, Double y) {
		return String.format("%f,%f", x, -1*y);
	}
	
	/**
	 * Changes a string to comply with dot ID requirements and returns the result
	 * Dot names must be a string of alphanumeric characters and underscores,
	 * not beginning with a digit
	 * 
	 * @param input is String we are modifying
	 * @return is .dot-compliant ID String where all leading numbers are removed
	 * and put at the end of the string and all dis-allowed characters are replaced
	 * with underscores
	 */
	/*
	 *  An ID is one of the following:
	 *	Any string of alphabetic ([a-zA-Z\200-\377]) characters, underscores ('_') or digits ([0-9]), not beginning with a digit;
	 *	a numeral [-]?(.[0-9]+ | [0-9]+(.[0-9]*)? );
	 *	any double-quoted string ("...") possibly containing escaped quotes (\")1;
	 *	an HTML string (<...>). 
	 */
	public static String filterString(String input) {
		LOGGER.info("Preparing to transform string");
		String alphaNumRegEx = "[a-zA-Z\200-\377_]+[0-9]*";
		String numericRegEx = "[-]?[.][0-9]+|[0-9]+([.][0-9]*)?";
		String quotedRegEx = "\".*\"";
		String htmlRegEx = "<.*>";
		if (input.matches(alphaNumRegEx)) {
			LOGGER.info("Alphanumeric ID");
			return input;
		}
		if (input.matches(numericRegEx)) {
			LOGGER.info("Numeric ID");
			return input;
		}
		if (input.matches(quotedRegEx)) {
			LOGGER.info("Quoted ID");
			return input;
		}
		if (input.matches(htmlRegEx)) {
			LOGGER.info("HTML ID");
			return input;
		}
		LOGGER.info("None of the above. Transforming to Quoted ID");
		StringBuilder output = new StringBuilder(input.length() + 2);
		output.append('\"');
		// replace any quotations from name string with underscore
		input = input.replace('\"', '_');
		output.append(input);
		output.append('\"');
		return output.toString();
	}
	
	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	public abstract String getElementString();
}

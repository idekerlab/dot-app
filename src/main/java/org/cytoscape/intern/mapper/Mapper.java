package org.cytoscape.intern.mapper;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.VisualPropertyValue;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Font;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	protected HashMap<VisualProperty<?>, String> simpleVisPropsToDot; 
	
	// maps Cytoscape line types to the equivalent string used in .dot
	private HashMap<LineType, String> lineTypeMap; // TODO
	
	// view that this mapper object is mapping
	protected View<? extends CyIdentifiable> view;
	
	// debug logger
	protected static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.mapper.Mapper");
	
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
		// TODO
		LOGGER.info("Creating .dot color attribute string");
		Integer red = color.getRed();
		Integer green = color.getGreen();
		Integer blue = color.getBlue();
		String result = String.format("#%02X%02X%02X%02X", red, green, blue, alpha);
		LOGGER.info("Created .dot color attribute string. Result: " + result);
		return result;
		/**
		 * Pseudocode:
		 * Retrieve individual color bits using getRed() getGreen() getBlue()
		 * Convert to a string using String.format("#%02X%02X%02X%02X", red, green, blue, alpha)
		 * return string
		 */
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
	 * Given a LineStyle, returns the .dot equivalent in String form
	 * @param
	 */
	protected String mapDotStyle(LineType lineType) {
		// TODO
		/**
		 * Pseudocode:
		 * Retrieve .dot string from lineType hashmap
		 */
		return null;
	}
	
	/**
	 * Returns String that is name of this element-- not label, name
	 * 
	 * @return String that is name of this element
	 */
	protected String getElementName() {
		CyIdentifiable element = (CyIdentifiable)view.getModel();
		CyNetwork network = null;		
		
		// get CyNetwork that element is in
		if(element instanceof CyNetwork) {
			network = (CyNetwork)view.getModel();
		}
		else if(element instanceof CyNode) {
			network = ((CyNode)element).getNetworkPointer();
		}
		else if(element instanceof CyEdge) {
			network = ((CyEdge) element).getSource().getNetworkPointer();
		}
		
		// return string in "name" column of network table-- return name of element
		return network.getRow(element).get("name", String.class);
	}
	
	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	public abstract String getElementString ();
}










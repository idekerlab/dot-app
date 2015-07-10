package org.cytoscape.intern;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.VisualPropertyValue;

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
	private HashMap<String, String> simpleVisPropsToDot; 
	
	/**
	 * maps Cytoscape VisualProperty TYPES by their String ID to a HashMap that contains the 
	 * cytoscape to *.dot mappings for that type
	 */
	private HashMap<String, HashMap<VisualPropertyValue, String> >discreteMappingTypes;
	
	// maps Cytoscape line types to the equivalent string used in .dot
	protected HashMap<LineType, String> lineTypeMap; // TODO
	
	// debug logger
	protected static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.Mapper");
	
	/**
	 * Takes a VisualProperty and returns the String put in the .dot file to represent it
	 * 
	 * @param property VisualProperty to be converted
	 * @return String that is .dot equivalent of the visual property
	 */
	public <T> String  mapVisToDot(VisualProperty<T> property, T value) {
		/**
		 * Pseudocode:
		 * Attempt to retrieve .dot string from simpleVisPropsToDot by using ID string
		 * If found: concatenate value to .dot string
		 * Else: Attempt to retrieve hashmap from discreteMappingTypes by using ID string
		 * If found: attempt to retreive .dot string from retrieved hashmap using value
		 * If found: return .dot string
		 * Else: return default .dot string
		 */
		String output = "";
		
		// if property is simple
		if(simpleVisPropsToDot.get(property) != null) {
			
		}
		else {
			// check discrete
			// get value from map it points to
			// return value
		}
		
		// property not found
		LOGGER.log(Level.SEVERE, "Property not found in maps");
		return null;
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
		/**
		 * Pseudocode:
		 * Retrieve individual color bits using getRed() getGreen() getBlue()
		 * Convert to a string using String.format("#%02X%02X%02X%02X", red, green, blue, alpha)
		 * return string
		 */
		return null;
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
}










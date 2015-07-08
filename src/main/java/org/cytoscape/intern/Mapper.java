package org.cytoscape.intern;

import java.util.HashMap;
import java.awt.Color;
import java.awt.Font;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.model.VisualProperty;

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

	// TODO -- access level of 3 variables below
	// maps Cytoscape properties  by their ID Strings to their .dot equivalents if relationship is simple equivalency
	HashMap<String, String> simpleVisPropsToDot; // TODO fill in
	
	/**
	 * maps Cytoscape VisualProperty TYPES by their String ID to a HashMap that contains the 
	 * cytoscape to *.dot mappings for that type
	 */
	HashMap<String, HashMap<Object, Object> >discreteMappingTypes; // TODO
	
	// maps Cytoscape line types to the equivalent string used in .dot
	HashMap<LineTypeVisualProperty, String> lineTypeMap; // TODO
	
	
	/**
	 * Takes a VisualProperty and returns the String put in the .dot file to represent it
	 * 
	 * @param property VisualProperty to be converted
	 * @return String that is .dot equivalent of the visual property
	 */
	public String mapVisToDot(VisualProperty<Object> property) {
		//TODO
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
	public String mapColorToDot(Color color, Integer alpha) {
		// TODO
		return null;
	}
	
	/**
	 * Given a font, returns the .dot equivalent in String form
	 * 
	 * @param font font to be converted
	 * @param size size of font to be converted
	 * @return String that is .dot representation of the provided font
	 */
	public String mapFont(Font font, Integer size) {
		// TODO
		return null;
	}
}










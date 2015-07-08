package org.cytoscape.intern;

import java.util.HashMap;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;

/**
 * Runs the program
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public abstract class Mapper {

	// maps Cytoscape properties  by their ID Strings to their .dot equivalents if relationship is simple equivalency
	HashMap<String, String> simpleVisPropsToDot; // TODO fill in
	
	/**
	 * maps Cytoscape VisualProperty TYPES by their String ID to a HashMap that contains the 
	 * cytoscape to *.dot mappings for that type
	 */
	HashMap<String, HashMap<Object, Object> >discreteMappingTypes;
	
	// maps Cytoscape line types to the equivalent string used in .dot
	HashMap<LineTypeVisualProperty, String> lineTypeMap;
	
	
	/**
	 * 
	 */
}

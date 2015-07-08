package org.cytoscape.intern;

import java.util.HashMap;

import org.cytoscape.view.presentation.property.LineTypeVisualProperty;

/**
 * Handles mapping of CyNode properties to .dot equivalent Strings
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class NodePropertyMapper extends Mapper {
	
	// maps Cytoscape properties  by their ID Strings to their .dot equivalents if relationship is simple equivalency
	private HashMap<String, String> simpleVisPropsToDot; // TODO fill in
	
	/**
	 * maps Cytoscape VisualProperty TYPES by their String ID to a HashMap that contains the 
	 * cytoscape to *.dot mappings for that type
	 */
	private HashMap<String, HashMap<Object, Object> > discreteMappingTypes; // TODO
	
	// maps Cytoscape line types to the equivalent string used in .dot
	protected HashMap<LineTypeVisualProperty, String> nodeShapeMap; // TODO

}

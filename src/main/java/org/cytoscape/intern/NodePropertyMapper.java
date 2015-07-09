package org.cytoscape.intern;

import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.presentation.property.values.VisualPropertyValue;

import java.util.HashMap;


/**
 * Handles mapping of CyNode properties to .dot equivalent Strings
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class NodePropertyMapper extends Mapper {
	
	/**
	 * maps Cytoscape properties by their ID strings to their .dot equivalents if relationship is simple
	 */
	private HashMap<String, String> simpleVisPropsToDot; // TODO fill in
	
	/**
	 * maps Cytoscape VisualProperty TYPES by their String ID to a HashMap that contains the 
	 * cytoscape to *.dot mappings for that type
	 */
	private HashMap<String, HashMap<VisualPropertyValue, String> > discreteMappingTypes; // TODO
	
	/**
	 *  maps Cytoscape node shape types to the equivalent string used in .dot
	 */
	protected HashMap<NodeShape, String> nodeShapeMap; // TODO

}

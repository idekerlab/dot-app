package org.cytoscape.intern.mapper;

import java.util.HashMap;

import org.cytoscape.view.model.View;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;

public class NetworkPropertyMapper extends Mapper {
	
	/**
	 * Contructs NetworkPropertyMapper object
	 * 
	 * @param view of network we are converting
	 */
	public NetworkPropertyMapper(CyNetworkView view) {
		super(view);
		
		simpleVisPropsToDot = new HashMap< VisualProperty<?>, String>();
		
		populateMaps();		
	}
	
	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	@Override
	public String getElementString() {
		/**
		 * all properties we need to write
		 * 
		 * directed
		 * bgcolor-- NETWORK_BACKGROUND_POINT
		 * fixedsize -- true
		 * fontpath -- maybe something
		 * scale -- NETWORK_SCALE_FACTOR -- try ignoring first
		 * label -- NETWORK_TITLE -- maybe -- test
		 * 
		 * pseudocode -- NOTE TABS ARE OFF
		 * 
		 * String output = "";
		 * 
		 * output += getDirectedString(view);
		 * output += ", bgcolor = " + 
		 * 		nodeMapper.mapColorToDot( view.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_COLOR), 255 );
		 * output += ", fixedsize = true";
		 * output += ", label = " + view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);
		 * 
		 * return output;
		 */
		return null;
	}
	
	/**
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		
	}

	/**
	 * Returns dot string that represents if graph is directed or not
	 * 
	 * @param view CyNetworkView of network being checked
	 * @return String that is either "graph {" or "digraph {"
	 */
	private String getDirectedString() {
		String output = (isDirected()) ? "digraph {":"graph {";
		return output;
	}
	
	/**
	 * Determines whether the graph is visibly directed or not
	 * 
	 * @param view View of network we are checking for direction
	 * @return true if graph is directed, false otherwise
	 */
	private boolean isDirected() {
		/**
		 * pseudocode
		 * 
		 * CyNetwork network = view.getModel();
		 * ArrayList<CyEdge> edgeList = network.getEdgeList();
		 * 
		 * M-- note that indenting is off below
		 * for(CyEdge edge: edgeList) {
		 * 		if(edge.isDirected()) {
		 * 			return true;
		 * 		}
		 * }
		 * return false;
		 */
		return false;
	}
}

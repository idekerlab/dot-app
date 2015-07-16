package org.cytoscape.intern.mapper;

import java.awt.Color;
import java.util.Collection;
import java.util.ArrayList;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;

public class NetworkPropertyMapper extends Mapper {

	private boolean directed = false;
	/**
	 * Constructs NetworkPropertyMapper object
	 * 
	 * @param view of network we are converting
	 */
	public NetworkPropertyMapper(CyNetworkView netView, boolean directed) {
		super(netView);
		simpleVisPropsToDot = new ArrayList<String>();
		this.directed = directed;
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
		 * output += ", label = " + view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);
		 * 
		 * return output;
		 */
		//Get network name from model. Remove spaces from name
		CyNetwork network = (CyNetwork)view.getModel();
		String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
		// remove dis-allowed characters to avoid errors
		networkName = networkName.replace(" ", "");
		networkName = networkName.replace(".", "");

		//Build the network properties string
		StringBuilder elementString = new StringBuilder();
		
		//Header of the dot file of the form (di)graph [NetworkName] {
		String graphDeclaration = String.format("%s %s {", getDirectedString(), networkName);
		elementString.append(graphDeclaration + "\n");
		
		//Get .dot strings for simple dot attributes. Append to attribute string
		for (String dotAttribute : simpleVisPropsToDot) {
		        elementString.append(dotAttribute + "\n");
		}
		
		return elementString.toString();
	}
	
	/**
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		
		//Background color of graph
		Color netBgColor = (Color)view.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
		String dotBgColor = String.format("bgcolor = \"%s\"", mapColorToDot(netBgColor, netBgColor.getAlpha()));
		simpleVisPropsToDot.add(dotBgColor);
		
		//label attribute of graph
		String label = view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);
		String dotLabel = String.format("label = \"%s\"", label);
		simpleVisPropsToDot.add(dotLabel);
	}

	/**
	 * Returns dot string that represents if graph is directed or not
	 * 
	 * @return String that is either "graph" or "digraph"
	 */
	private String getDirectedString() {
		String output = (directed) ? "digraph":"graph";
		return output;
	}
	
	/**
	 * Determines whether the graph is visibly directed or not
	 * 
	 * @param networkView The network being tested for directedness
	 * @return true if graph is directed, false otherwise
	 */
	public static boolean isDirected(CyNetworkView networkView) {
		//get the current network view
		//CyNetworkView networkView = (CyNetworkView)view.getModel();
        
		//get all the edge views from the current networkview
		Collection<View<CyEdge>> edgeViews = networkView.getEdgeViews();
		
		/**
		 * iterate each edgeview to check whether there is a target arrow shape
		 * or a source arrow shape for that edge
		 */
		ArrowShape noArrow = ArrowShapeVisualProperty.NONE;
        for (View<CyEdge> edge: edgeViews){
        	ArrowShape sourceArrow = edge.getVisualProperty(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE);
        	ArrowShape targetArrow = edge.getVisualProperty(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE);
        	if(!targetArrow.equals(noArrow) || !sourceArrow.equals(noArrow)) {
        		//return true if there is at least one not NONE arrow shape
        		return true;
        	}
        }
        
        //return false if the graph is undirected (has only NONE arrow shapes)
        return false;
		
	}
}

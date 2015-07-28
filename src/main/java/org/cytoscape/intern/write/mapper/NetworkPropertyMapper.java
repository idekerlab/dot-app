package org.cytoscape.intern.write.mapper;

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
	
	// Value of splines attribute
	private String splinesVal;
	
	/**
	 * Constructs NetworkPropertyMapper object
	 * 
	 * @param view of network we are converting
	 */
	public NetworkPropertyMapper(CyNetworkView netView, boolean directed, String splinesVal) {
		super(netView);
		simpleVisPropsToDot = new ArrayList<String>();
		this.directed = directed;
		this.splinesVal = splinesVal;
		
		populateMaps();		
	}
	
	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	@Override
	public String getElementString() {
		// Get network name from model. Remove spaces from name
		CyNetwork network = (CyNetwork)view.getModel();
		String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
		// remove dis-allowed characters to avoid errors
		// filter out disallowed chars
		networkName = Mapper.filterString(networkName);

		// Build the network properties string
		StringBuilder elementString = new StringBuilder();
		
		// Header of the dot file of the form (di)graph [NetworkName] {
		String graphDeclaration = String.format("%s %s {", getDirectedString(), networkName);
		elementString.append(graphDeclaration + "\n");
		
		//added outputorder = edgesfirst at the beginning of the file to make sure all the nodes 
		//are on the top of the edges.
		
		// Get .dot strings for simple dot attributes. Append to attribute string
		for (String dotAttribute : simpleVisPropsToDot) {
		        elementString.append(dotAttribute + "\n");
		}
		
		return elementString.toString();
	}
	
	/**
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		// label attribute of graph
		String label = view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);
		String dotLabel = String.format("label = \"%s\"", label);
		simpleVisPropsToDot.add(dotLabel);

		// Background color of graph
		Color netBgColor = (Color)view.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
		String dotBgColor = String.format("bgcolor = \"%s\"", mapColorToDot(netBgColor, netBgColor.getAlpha()));
		simpleVisPropsToDot.add(dotBgColor);
		
		// splines value
		simpleVisPropsToDot.add(String.format("splines = \"%s\"", splinesVal));
		
		// output order
		simpleVisPropsToDot.add("outputorder = \"edgesfirst\"");
		
		// esep=0 so splines can always be routed around nodes
		simpleVisPropsToDot.add("esep = \"0\"");
		
		simpleVisPropsToDot.add("pad = \"1\"");
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
		// get all the edge views from the current networkview
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
        
        // return false if the graph is undirected (has only NONE arrow shapes)
        return false;
		
	}
}

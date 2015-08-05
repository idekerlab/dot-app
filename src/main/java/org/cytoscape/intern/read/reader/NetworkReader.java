package org.cytoscape.intern.read.reader;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;

import com.alexmerz.graphviz.objects.Graph;

/**
 * Class that contains definitions and some implementation for converting a
 * dot graph to a CyNetwork. Data is passed in as a JPGD Graph object.
 * This subclass handles importing of network/graph properties
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class NetworkReader extends Reader{

	// Represents .dot representation of graph we are importing
	private Graph dotGraph;
	
	/**
	 * Constructs a NetworkReader object
	 * 
	 * @param networkView View of network being exported
	 * @param vizStyle VisualStyle being applied to network-- needed for defaults
	 * @param dotGraph  .dot representation of graph being exported
	 */
	public NetworkReader(CyNetworkView networkView, VisualStyle vizStyle, Graph dotGraph) {
		
		super(networkView, vizStyle);
		this.dotGraph = dotGraph;
	}
	
	/**
	 * Converts the specified .dot attribute to Cytoscape equivalent
	 * by modifying internal data structures like networkView or vizStyle
	 * 
	 * @param name Name of attribute
	 * @param val Value of attribute
	 * @param isDefault Whether attribute is to be set as default or bypass,
	 * 			doesn't matter for some attributes
	 */
	protected void convertAttribute(String name, String value, boolean isDefault) {
		
	}
}
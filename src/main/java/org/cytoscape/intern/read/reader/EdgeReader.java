package org.cytoscape.intern.read.reader;

import java.util.ArrayList;
import java.util.Map;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.presentation.property.values.ArrowShape;

import com.alexmerz.graphviz.objects.Edge;

/**
 * Class that contains definitions and some implementation for converting a
 * dot graph to a CyNetwork. Data is passed in as a list of JPGD Edge objects
 * This subclass handles importing of edge properties
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class EdgeReader extends Reader{

	// List of all edges in network being imported
	private ArrayList<Edge> edges;
	
	// Map to convert from .dot arrow shape to Cytoscape
	private static final Map<String, ArrowShape> ARROW_SHAPE_MAP = null;
	

	/**
	 * Constructs a EdgeReader object
	 * 
	 * @param networkView View of network being exported
	 * @param vizStyle VisualStyle being applied to network-- needed for defaults
	 * @param edges List of all edges in graph
	 */
	public EdgeReader(CyNetworkView networkView, VisualStyle vizStyle, ArrayList<Edge> edges) {
		super(networkView, vizStyle);
		
		this.edges = edges;
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
	protected void convertAttribute(String name, String val, boolean isDefault) {
		// TODO
		// remember to export edge weights
	}
	
}


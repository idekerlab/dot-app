package org.cytoscape.intern.read.reader;

import java.util.ArrayList;
import java.util.Map;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.presentation.property.values.NodeShape;

import com.alexmerz.graphviz.objects.Node;

/**
 * Class that contains definitions and some implementation for converting a
 * dot graph to a CyNetwork. Data is passed in as a list of JPGD Node objects
 * This subclass handles importing of node properties
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class NodeReader extends Reader{

	// List of all nodes in network being imported
	private ArrayList<Node> nodes;
	
	// Map to convert from .dot node shape to Cytoscape
	private static final Map<String, NodeShape> NODE_SHAPE_MAP = null;
	

	/**
	 * Constructs a NodeReader object
	 * 
	 * @param networkView View of network being exported
	 * @param vizStyle VisualStyle being applied to network-- needed for defaults
	 * @param nodes List of all nodes in graph
	 */
	public NodeReader(CyNetworkView networkView, VisualStyle vizStyle, ArrayList<Node> nodes) {
		super(networkView, vizStyle);
		
		this.nodes = nodes;
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
		
		//to modify internal data structures like networkView or visualStyle
		
		//For default one, vizStyle.setDefaultValue(VisualProperty<V> vp, S value);
		//@param vp: (VisualProperty) target VisualProperty 
		//@param value: (actual default value) value to be set as default
		//Void function
		
		//For bypass one, (all kinds of Views)View.setLockedValue(VisualProperty vp, V value)
		//@param vp: (VisualProperty) target VisualProperty
		//@param value: (value of VisualProperty) the value that will bypass the style
		//Void function
	}
	
}


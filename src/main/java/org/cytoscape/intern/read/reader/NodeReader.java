package org.cytoscape.intern.read.reader;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;

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

	// Map to convert from .dot node shape to Cytoscape
	private static final Map<String, NodeShape> NODE_SHAPE_MAP = null;
	

	/**
	 * Constructs an object of type Reader. Sets up Logger.
	 * 
	 * @param networkView view of network we are creating/modifying
	 * @param vizStyle VisualStyle that we are applying to the network
	 * @param defaultAttrs Map that contains default attributes for Reader of this type
	 * eg. for NodeReader will be a list of default
	 * @param elementMap Map where keys are JPGD node objects and Values are corresponding Cytoscape CyNodes
	 */
	public NodeReader(CyNetworkView networkView, VisualStyle vizStyle, Map<String, String> defaultAttrs, Map<Object, CyIdentifiable> elementMap) {
		super(networkView, vizStyle, defaultAttrs);
		this.elementMap = elementMap;

		NODE_SHAPE_MAP.put("triangle", NodeShapeVisualProperty.TRIANGLE);
		NODE_SHAPE_MAP.put("diamond", NodeShapeVisualProperty.DIAMOND);
		NODE_SHAPE_MAP.put("ellipse", NodeShapeVisualProperty.ELLIPSE);
		NODE_SHAPE_MAP.put("hexagon", NodeShapeVisualProperty.HEXAGON);
		NODE_SHAPE_MAP.put("octagon", NodeShapeVisualProperty.OCTAGON);
		NODE_SHAPE_MAP.put("parallelogram", NodeShapeVisualProperty.PARALLELOGRAM);
		NODE_SHAPE_MAP.put("rectangle", NodeShapeVisualProperty.ROUND_RECTANGLE);
		NODE_SHAPE_MAP.put("rectangle", NodeShapeVisualProperty.RECTANGLE);     
	}
	
	
	/**
	 * Converts the specified .dot attribute to Cytoscape equivalent
	 * and returns the corresponding VisualProperty and its value
	 * Must be overidden and defined in each sub-class
	 * 
	 * @param name Name of attribute
	 * @param val Value of attribute
	 * 
	 * @return Pair where left value is VisualProperty and right value
	 * is the value of that VisualProperty. VisualProperty corresponds to graphviz
	 * attribute
	 */
	protected Pair<VisualProperty, Object> convertAttribute(String name, String val) {
		/**
		 * properties to Map:
		 * 
		 * shape
		 * fill color
		 * border color
		 * border line type
		 * size
		 * label
		 * tooltip
		 * label font
		 */
		return null;
	}
	
	/**
	 * Sets VisualProperties for each node related to location of node.
	 * Here because cannot return 2 VisualProperties from convertAttribute
	 * and want to make exception clear
	 */
	private void setPositions() {
		
	}
}


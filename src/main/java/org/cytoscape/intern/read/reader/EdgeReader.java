package org.cytoscape.intern.read.reader;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.vizmap.VisualStyle;

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

	// Map to convert from .dot arrow shape to Cytoscape
	private static final Map<String, ArrowShape> ARROW_SHAPE_MAP = null;
	
	/**
	 * Constructs an object of type Reader. Sets up Logger.
	 * 
	 * @param networkView view of network we are creating/modifying
	 * @param vizStyle VisualStyle that we are applying to the network
	 * @param defaultAttrs Map that contains default attributes for Reader of this type
	 * eg. for NodeReader will be a list of default
	 * @param elementMap Map where keys are JPGD node objects and Values are corresponding Cytoscape CyNodes
	 */
	public EdgeReader(CyNetworkView networkView, VisualStyle vizStyle, Map<String, String> defaultAttrs, Map<Object, CyIdentifiable> elementMap) {
		super(networkView, vizStyle, defaultAttrs);
		this.elementMap = elementMap;

	}
	
	/**
	 * Sets defaults and bypass attributes for each node and sets positions
	 */
	@Override
	public void setProperties() {
		super.setProperties();
		setEdgeWeights();
	}
	
	/**
	 * Converts edge weights by putting into a new column in the table
	 */
	private void setEdgeWeights(){

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
	@Override
	@SuppressWarnings("rawtypes")
	protected Pair<VisualProperty, Object> convertAttribute(String name, String val) {
		return null;
	}

	@Override
	protected void setBypasses() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setStyle(String attrVal, VisualStyle vizStyle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setStyle(String attrVal,
			View<? extends CyIdentifiable> elementView) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setColor(String attrVal, VisualStyle vizStyle,
			ColorAttribute attr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setColor(String attrVal,
			View<? extends CyIdentifiable> elementView, ColorAttribute attr) {
		// TODO Auto-generated method stub
		
	}
	
}


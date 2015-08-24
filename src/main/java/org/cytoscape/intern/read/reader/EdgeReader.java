package org.cytoscape.intern.read.reader;

import java.util.Map;
import java.awt.Color;

import org.apache.commons.lang3.tuple.Pair;

import org.cytoscape.model.CyIdentifiable;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;

import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LINE_TYPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY;

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
	/*@Override
	public void setProperties() {
		super.setProperties();
		setEdgeWeights();
	}*/
	
	/**
	 * Converts edge weights by putting into a new column in the table
	 */
	private void setWeight() {

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
		
		/*
		 * attributes to convert:
		 * color
		 * line type / style
		 * width
		 * curve/spline maybe
		 * label
		 * label font/color/size
		 * tooltip
		 * source/target arrow size
		 * source/target arrow shape
		 * visibility-- check style for "invis"
		 */
		
		return null;
	}

	/**
	 * Sets all the bypass Visual Properties in Cytoscape for this type of reader
	 * eg. NetworkReader sets all network props, same for nodes
	 * Modifies CyNetworkView networkView, VisualStyle vizStyle etc. 
	 */
	@Override
	protected void setBypasses() {
		/*
		 * for each entry in elementMap
		 * 		bypassMap = getAttrMap(elementMap.getKey())
		 * 		for each entry in bypassMap
		 * 			if(name == "style")
		 * 				setStyle(val, elementView)
		 * 				continue;
		 * 			if(name == "weight")
		 * 				setWeight()
		 * 				continue;
		 * 			if(name == "color")
		 * 				setColor(...)
		 * 				continue
		 * 			
		 * 			Pair p = convertAttribute(name, val);
		 * 			VP = p.left()
		 * 			val = p.right()
		 * 			getValue().setLockedValue( VP, val);	
		 */
	}

	/**
	 * Converts the "style" attribute from graphviz for default value of Cytoscape.
	 * Handles node border line type only.
	 * 
	 * @param attrVal String that is the value of "style" 
	 * eg. "dashed, rounded"
	 * @param vizStyle VisualStyle that "style" is being applied to
	 */
	@Override
	protected void setStyle(String attrVal, VisualStyle vizStyle) {
		String[] styleAttrs = attrVal.split(",");

		for (String styleAttr : styleAttrs) {
			styleAttr = styleAttr.trim();
			LineType lineType = LINE_TYPE_MAP.get(styleAttr);
			
			// set line type if defined
			if (lineType != null) {
				vizStyle.setDefaultValue(EDGE_LINE_TYPE, lineType);
			}
			// make invisible if needed
			if(styleAttr.equals("invis")) {
				vizStyle.setDefaultValue(EDGE_TRANSPARENCY, 0);
			}
		}
	}

	/**
	 * Converts the "style" attribute from graphviz for bypass value of Cytoscape.
	 * Only handles node border line type.
	 * 
	 * @param attrVal String that is the value of "style" 
	 * eg. "dashed, rounded"
	 * @param elementView View of element that "style" is being applied to eg. View<CyNode> 
	 */
	@Override
	protected void setStyle(String attrVal,
			View<? extends CyIdentifiable> elementView) {
		
		String[] styleAttrs = attrVal.split(",");

		for (String styleAttr : styleAttrs) {
			styleAttr = styleAttr.trim();
			LineType lineType = LINE_TYPE_MAP.get(styleAttr);
			
			// set line type if defined
			if (lineType != null) {
				elementView.setLockedValue(EDGE_LINE_TYPE, lineType);
			}
			// make invisible if needed
			if(styleAttr.equals("invis")) {
				elementView.setLockedValue(EDGE_TRANSPARENCY, 0);
			}
		}
	}

	/**
	 * Converts .dot color to Cytoscape default value. Does not handle
	 * colors of edge arrows
	 * 
	 * @param attrVal String that is value of color from dot file
	 * @param vizStyle VisualStyle that this color is being used in
	 * @param attr enum for type of color: COLOR, FILLCOLOR or FONTCOLOR 
	 */
	@Override
	protected void setColor(String attrVal, VisualStyle vizStyle,
			ColorAttribute attr) {
		
		Color color = convertColor(attrVal);
		Integer transparency = color.getAlpha();

		switch (attr) {
			case COLOR: {
				vizStyle.setDefaultValue(EDGE_STROKE_UNSELECTED_PAINT, color);
				vizStyle.setDefaultValue(EDGE_TRANSPARENCY, transparency);
				break;
			}
			case FONTCOLOR: {
				vizStyle.setDefaultValue(EDGE_LABEL_COLOR, color);
				vizStyle.setDefaultValue(EDGE_LABEL_TRANSPARENCY, transparency);
				break;
			}
		}
	}

	/**
	 * Converts .dot color to Cytoscape bypass value. Does not handle arrow
	 * colors
	 * 
	 * @param attrVal String that is value of color from dot file
	 * @param elementView View of element that color is being applied to
	 * @param attr enum for type of color: COLOR, FILLCOLOR or FONTCOLOR 
	 */
	@Override
	protected void setColor(String attrVal,
			View<? extends CyIdentifiable> elementView, ColorAttribute attr) {

		Color color = convertColor(attrVal);
		Integer transparency = color.getAlpha();

		switch (attr) {
			case COLOR: {
				elementView.setLockedValue(EDGE_STROKE_UNSELECTED_PAINT, color);
				elementView.setLockedValue(EDGE_TRANSPARENCY, transparency);
				break;
			}
			case FONTCOLOR: {
				elementView.setLockedValue(EDGE_LABEL_COLOR, color);
				elementView.setLockedValue(EDGE_LABEL_TRANSPARENCY, transparency);
				break;
			}
		}
	}
	
}


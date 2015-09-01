package org.cytoscape.intern.read.reader;

import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.awt.Color;
import java.awt.Font;

import com.alexmerz.graphviz.objects.Edge;

import org.apache.commons.lang3.tuple.Pair;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LINE_TYPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TOOLTIP;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_FONT_FACE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_FONT_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE;

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

	// reference default CyEdge table for network from networkView
	CyTable edgeTable;

	// maps GraphViz arrow shapes with corresponding Cytoscape arrow shapes
	private static final Map<String, ArrowShape> ARROW_SHAPE_MAP = new HashMap<String, ArrowShape>();
	static {
		ARROW_SHAPE_MAP.put("vee", ArrowShapeVisualProperty.ARROW);
		ARROW_SHAPE_MAP.put("dot", ArrowShapeVisualProperty.CIRCLE);
		ARROW_SHAPE_MAP.put("normal", ArrowShapeVisualProperty.DELTA);
		ARROW_SHAPE_MAP.put("diamond", ArrowShapeVisualProperty.DIAMOND);
		ARROW_SHAPE_MAP.put("ornormal", ArrowShapeVisualProperty.HALF_BOTTOM);
		ARROW_SHAPE_MAP.put("olnormal", ArrowShapeVisualProperty.HALF_TOP);
		ARROW_SHAPE_MAP.put("none", ArrowShapeVisualProperty.NONE);
		ARROW_SHAPE_MAP.put("tee", ArrowShapeVisualProperty.T);
	}
	
	/*
	 * maps GraphViz attributes with a single Cytoscape VisualProperty
	 * equivalent. Other GraphViz attributes are handled separately
	 */
	private static final Map<String, VisualProperty<?>> DOT_TO_CYTOSCAPE = new HashMap<String, VisualProperty<?>>();
	static {
		DOT_TO_CYTOSCAPE.put("label", EDGE_LABEL);
		DOT_TO_CYTOSCAPE.put("xlabel", EDGE_LABEL);
		DOT_TO_CYTOSCAPE.put("fontname", EDGE_LABEL_FONT_FACE);
		DOT_TO_CYTOSCAPE.put("fontsize", EDGE_LABEL_FONT_SIZE);
		DOT_TO_CYTOSCAPE.put("penwidth", EDGE_WIDTH);
		DOT_TO_CYTOSCAPE.put("arrowhead", EDGE_TARGET_ARROW_SHAPE);
		DOT_TO_CYTOSCAPE.put("arrowtail", EDGE_SOURCE_ARROW_SHAPE);
		DOT_TO_CYTOSCAPE.put("tooltip", EDGE_TOOLTIP);
	}
	
	/**
	 * Constructs an object of type Reader.
	 * 
	 * 
	 * @param networkView view of network we are creating/modifying
	 * @param vizStyle VisualStyle that we are applying to the network
	 * @param defaultAttrs Map that contains default attributes
	 * @param elementMap Map of which the keys are JPGD Edge objects and the 
	 * values are corresponding Cytoscape CyEdge objects 
	 */
	public EdgeReader(CyNetworkView networkView, VisualStyle vizStyle, Map<String, String> defaultAttrs, 
			Map<Edge, CyEdge> elementMap) {
		
		super(networkView, vizStyle, defaultAttrs);
		this.elementMap = elementMap;
		
        //get the edgeTable and create a new column for edge weight with default value of 1
		edgeTable = networkView.getModel().getDefaultEdgeTable();
		edgeTable.createColumn("weight", Double.class, false, 1d);

		LOGGER.info("EdgeReader constructed");
	}
	
	/**
	 * Converts edge weights by putting into a new column in the table
	 * 
	 * @param weight the edge weight
	 * @param elementView the edgeView corresponding to the edge of which the
	 * weight is an attribute
	 */
	private void setWeight(String weight, View<CyEdge> elementView) {
		//get the current row and put the weight into the row
		CyRow currentRow = edgeTable.getRow(elementView.getModel().getSUID());
		currentRow.set("weight", new Double(Double.parseDouble(weight)));
	}

	/**
	 * Converts the specified GraphViz attribute and value to its Cytoscape 
	 * equivalent VisualProperty and VisualPropertyValue. If an equivalent value
	 * is not found, then a default Cytoscape VisualPropertyValue is used.
	 * This method only handles GraphViz attributes that do not correspond to
	 * more than one Cytoscape VisualProperty.
	 * 
	 * @param name the name of the attribute
	 * @param val the value of the attribute
	 * 
	 * @return Pair object of which the left value is the VisualProperty and the right value
	 * is the VisualPropertyValue.
	 */
	@Override
	@SuppressWarnings("rawtypes")
	protected Pair<VisualProperty, Object> convertAttribute(String name, String val) {
		LOGGER.info(
			String.format("Converting GraphViz attribute %s with value %s", name, val)
		);

		VisualProperty retrievedProp = DOT_TO_CYTOSCAPE.get(name);
		Object retrievedVal = null;
		switch(name) {
			case "xlabel": {
				// Fall through to label case
			}
			case "label" : {
				retrievedVal = val;
				break;
			}
			case "penwidth": {
				retrievedVal = Double.parseDouble(val);
				break;
			}
			case "fontname": {
				retrievedVal = Font.decode(val);
				break;
			}
			case "fontsize": {
				retrievedVal = Integer.parseInt(val);
				break;
			}
			case "arrowhead": {
				retrievedVal = ARROW_SHAPE_MAP.get(val);
				break;
			}
			case "arrowtail": {
				retrievedVal = ARROW_SHAPE_MAP.get(val);
				break;
			}
		}
		
		return Pair.of(retrievedProp, retrievedVal);
	}

	/**
	 * Sets all the bypass Visual Properties values for Cytoscape View objects
	 * corresponding to CyEdge objects
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void setBypasses() {
		LOGGER.info("Setting the Bypass values for edge views...");
	
		for(Entry<? extends Object, ? extends CyIdentifiable> entry: elementMap.entrySet() ) {
			// get map of attributes for this edge and the View for this CyEdge
			Map<String, String> bypassAttrs = getAttrMap(entry.getKey());
			CyEdge element = (CyEdge)entry.getValue();
			View<CyEdge> elementView = networkView.getEdgeView(element);
			
			// loop through attribute list for edge
			for (Entry<String, String> attrEntry : bypassAttrs.entrySet()) {
				String attrKey = attrEntry.getKey();
				String attrVal = attrEntry.getValue();
				LOGGER.info(
					String.format("Converting DOT attribute: %s", attrKey)
				);
				
				// Handle special cases
				if(attrKey.equals("style")) {
					setStyle(attrVal, elementView);
					continue;
				}
				if(attrKey.equals("weight")){
					setWeight(attrVal, elementView);
					continue;
				}
				if (attrKey.equals("color") || attrKey.equals("fillcolor")
						|| attrKey.equals("fontcolor")) {
					switch (attrKey) {
						case "fillcolor": {
							// DO NOTHING. Can't handle arrow colors yet.
							break;
						}
						case "color": {
							setColor(attrVal, elementView, ColorAttribute.COLOR);
							break;
						}
						case "fontcolor": {
							setColor(attrVal, elementView, ColorAttribute.FONTCOLOR);
							break;
						}
				
					}
					continue;
				}
				
				// handle normal cases
				// get corresponding VisualProperty
				Pair<VisualProperty, Object> p = convertAttribute(attrEntry.getKey(), attrEntry.getValue());
				if (p == null) {
					// Abort if conversion not found
					continue;
				}

				// Apply the VisualProperty
				VisualProperty vizProp = p.getLeft();
				Object val = p.getRight();
				if (vizProp == null || val == null) {
					// Abort if conversion not found
					continue;
				}
				LOGGER.info("Updating Visual Style...");
				LOGGER.info(String.format("Setting Visual Property %S...", vizProp));
				elementView.setLockedValue(vizProp, val);
			}
		}
	}

	/**
	 * Converts the GraphViz "style" attribute into default VisualProperty
	 * values for a Cytoscape VisualStyle
	 * 
	 * @param attrVal String that is the value of "style" 
	 * eg. "dashed, invis"
	 * @param vizStyle VisualStyle that "style" is being applied to
	 */
	@Override
	protected void setStyle(String attrVal, VisualStyle vizStyle) {
		attrVal.toLowerCase();
		String[] styleAttrs = attrVal.split(",");

		for (String styleAttr : styleAttrs) {
			styleAttr = styleAttr.trim();
			LineType lineType = LINE_TYPE_MAP.get(styleAttr);
			
			// set line type if defined
			if (lineType != null) {
				vizStyle.setDefaultValue(EDGE_LINE_TYPE, lineType);
			}
		}
		// make invisible if needed
		if(attrVal.contains("invis")) {
			vizStyle.setDefaultValue(EDGE_VISIBLE, false);
		}
	}

	/**
	 * Converts the GraphViz "style" attribute into VisualProperty bypass values
	 * for a Cytoscape View object
	 * 
	 * @param attrVal String that is the value of "style" (eg. "dashed, round")
	 * @param elementView view to which "style" is being applied
	 */
	@Override
	protected void setStyle(String attrVal,
			View<? extends CyIdentifiable> elementView) {
		
		attrVal.toLowerCase();
		String[] styleAttrs = attrVal.split(",");
	
		// Default to visible
		elementView.setLockedValue(EDGE_VISIBLE, true);

		for (String styleAttr : styleAttrs) {
			styleAttr = styleAttr.trim();
			LineType lineType = LINE_TYPE_MAP.get(styleAttr);
			
			// set line type if defined
			if (lineType != null) {
				elementView.setLockedValue(EDGE_LINE_TYPE, lineType);
			}
		}
		// make invisible if needed
		if(attrVal.contains("invis")) {
			elementView.setLockedValue(EDGE_VISIBLE, false);
		}
	}

	/**
	 * Converts a GraphViz color attribute into the corresponding default
	 * VisualProperty values for a Cytoscape VisualStyle. Does not support
	 * fillcolor.
	 * 
	 * @param attrVal GraphViz color string
	 * @param vizStyle VisualStyle that this color is being used in
	 * @param attr enum for type of color: COLOR, FILLCOLOR, FONTCOLOR, BGCOLOR
	 */
	@Override
	protected void setColor(String attrVal, VisualStyle vizStyle,
			ColorAttribute attr) {
	
		LOGGER.info("Edge color: " + attrVal + " being set...");
		Color color = convertColor(attrVal);
		Integer transparency = color.getAlpha();

		switch (attr) {
			case COLOR: {
				LOGGER.info("Edge stroke color being set to: " + color.toString());
				vizStyle.setDefaultValue(EDGE_STROKE_UNSELECTED_PAINT, color);
				vizStyle.setDefaultValue(EDGE_UNSELECTED_PAINT, color);
				vizStyle.setDefaultValue(EDGE_TRANSPARENCY, transparency);
				break;
			}
			case FONTCOLOR: {
				LOGGER.info("Edge font color being set to: " + color.toString());
				vizStyle.setDefaultValue(EDGE_LABEL_COLOR, color);
				vizStyle.setDefaultValue(EDGE_LABEL_TRANSPARENCY, transparency);
				break;
			}
			default: {
				break;
			}
		}
	}

	/**
	 * Converts a GraphViz color attribute into corresponding VisualProperty
	 * bypass values for a Cytoscape View object. Does not support fillcolor.
	 * 
	 * @param attrVal GraphViz color string
	 * @param elementView View of Cytoscape element to which a color 
	 * VisualProperty is being set
	 * @param attr enum for type of color: COLOR, FILLCOLOR, FONTCOLOR, BGCOLOR
	 */
	@Override
	protected void setColor(String attrVal,
			View<? extends CyIdentifiable> elementView, ColorAttribute attr) {

		Color color = convertColor(attrVal);
		Integer transparency = color.getAlpha();

		switch (attr) {
			case COLOR: {
				elementView.setLockedValue(EDGE_UNSELECTED_PAINT, color);
				elementView.setLockedValue(EDGE_TRANSPARENCY, transparency);
				break;
			}
			case FONTCOLOR: {
				elementView.setLockedValue(EDGE_LABEL_COLOR, color);
				elementView.setLockedValue(EDGE_LABEL_TRANSPARENCY, transparency);
				break;
			}
			default: {
				break;
			}
		}
	}

}


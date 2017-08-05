/**************************
 * Copyright © 2015-2017 Braxton Fitts, Ziran Zhang, Massoud Maher
 * 
 * This file is part of dot-app.
 * dot-app is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * dot-app is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with dot-app.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cytoscape.intern.read.reader;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_FONT_FACE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_FONT_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LINE_TYPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TOOLTIP;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_WIDTH;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.VisualStyle;

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

	// maps GraphViz arrow shapes with corresponding Cytoscape arrow shapes
	private static final Map<String, ArrowShape> ARROW_SHAPE_MAP = new HashMap<String, ArrowShape>();

	static {
		ARROW_SHAPE_MAP.put("vee", ArrowShapeVisualProperty.ARROW);
		ARROW_SHAPE_MAP.put("dot", ArrowShapeVisualProperty.CIRCLE);
		ARROW_SHAPE_MAP.put("odot", ArrowShapeVisualProperty.CIRCLE);
		ARROW_SHAPE_MAP.put("normal", ArrowShapeVisualProperty.DELTA);
		ARROW_SHAPE_MAP.put("onormal", ArrowShapeVisualProperty.DELTA);
		ARROW_SHAPE_MAP.put("diamond", ArrowShapeVisualProperty.DIAMOND);
		ARROW_SHAPE_MAP.put("odiamond", ArrowShapeVisualProperty.DIAMOND);
		ARROW_SHAPE_MAP.put("rvee", ArrowShapeVisualProperty.HALF_BOTTOM);
		ARROW_SHAPE_MAP.put("lvee", ArrowShapeVisualProperty.HALF_TOP);
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
	// reference default CyEdge table for network from networkView
	CyTable edgeTable;
	
	/**
	 * Constructs an object of type Reader.
	 * 
	 * @param networkView view of network we are creating/modifying
	 * @param vizStyle VisualStyle that we are applying to the network
	 * @param defaultAttrs Map that contains default attributes for Reader
	 * of this type eg. for NodeReader will be a list of default
	 * @param rendEngMgr RenderingEngineManager that contains the default
	 * VisualLexicon needed for gradient support
	 * @param elementMap Map where keys are JPGD node objects and Values 
	 * are corresponding Cytoscape CyNodes
	 */
	public EdgeReader(CyNetworkView networkView, VisualStyle vizStyle, Map<String, String> defaultAttrs, 
			RenderingEngineManager rendEngMgr, Map<Edge, CyEdge> elementMap) {
		
		super(networkView, vizStyle, defaultAttrs, rendEngMgr);
		this.elementMap = elementMap;
		
		edgeTable = networkView.getModel().getDefaultEdgeTable();

		LOGGER.trace("EdgeReader constructed");
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
		LOGGER.trace("Setting weight attribute for edge");
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
		LOGGER.debug(
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
	 * Sets all the default Visual Properties values of the Cytoscape
	 * VisualStyle. Subclasses handle different visual properties
	 * (eg. NetworkReader sets all network props, NodeReader sets all node
	 * properties, and EdgeReader sets all edge properties)
	 */
	protected void setDefaults() {
		super.setDefaults();
		String dir = defAttrs.containsKey("dir") ? defAttrs.get("dir") : "";
		switch (dir) {
			case "forward" : {
				vizStyle.setDefaultValue(EDGE_SOURCE_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);
				break;
			}
			case "back" : {
				vizStyle.setDefaultValue(EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);
				break;
			}
			case "none" : {
				vizStyle.setDefaultValue(EDGE_SOURCE_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);
				vizStyle.setDefaultValue(EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);
				break;
			}
		}
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
			String colorScheme = bypassAttrs.containsKey("colorscheme") ? bypassAttrs.get("colorscheme") : null;
			CyEdge element = (CyEdge)entry.getValue();
			View<CyEdge> elementView = networkView.getEdgeView(element);
			
			// loop through attribute list for edge
			for (Entry<String, String> attrEntry : bypassAttrs.entrySet()) {
				String attrKey = attrEntry.getKey();
				String attrVal = attrEntry.getValue();
				LOGGER.debug(
					String.format("Converting DOT attribute: %s", attrKey)
				);
				switch (attrKey) {
					case "style" : {
						setStyle(attrVal, elementView);
						continue;
					}
					case "weight" : {
						setWeight(attrVal, elementView);
						continue;
					}
					case "color" : {
						setColor(attrVal, elementView, ColorAttribute.COLOR, colorScheme);
						continue;
					}
					case "fillcolor" : {
						continue;
					}
					case "fontcolor" : {
						setColor(attrVal, elementView, ColorAttribute.FONTCOLOR, colorScheme);
						continue;
					}
					case "dir" : {
						switch (attrVal) {
							case "forward" : {
								elementView.setLockedValue(EDGE_SOURCE_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);
								break;
							}
							case "back" : {
								elementView.setLockedValue(EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);
								break;
							}
							case "none" : {
								elementView.setLockedValue(EDGE_SOURCE_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);
								elementView.setLockedValue(EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);
								break;
							}
						}
					}
					default : {
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
						LOGGER.trace("Updating Visual Style...");
						LOGGER.debug(String.format("Setting Visual Property %S...", vizProp));
						elementView.setLockedValue(vizProp, val);
					}
				}
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
	 * @param colorScheme Scheme from dot. Either "x11" or "svg"
	 */
	@Override
	protected void setColor(String attrVal,
			View<? extends CyIdentifiable> elementView, ColorAttribute attr, String colorScheme) {

		LOGGER.trace("A color attribute is being applied to edge {}. Color: {}",
			networkView.getModel().getRow(elementView.getModel()).get(CyNetwork.NAME, String.class),
			attrVal
		);
		Color color = convertColor(attrVal, colorScheme);
		if (color == null) {
			return;
		}
		List<Pair<Color, Float>> colorListVals = convertColorList(attrVal, colorScheme);
		if (colorListVals != null) {
			color = colorListVals.get(0).getLeft();
		}
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

	/**
	 * Converts a GraphViz color attribute into the corresponding default
	 * VisualProperty values for a Cytoscape VisualStyle. Does not support
	 * fillcolor.
	 * 
	 * @param attrVal GraphViz color string
	 * @param vizStyle VisualStyle that this color is being used in
	 * @param attr enum for type of color: COLOR, FILLCOLOR, FONTCOLOR, BGCOLOR
	 * @param colorScheme Scheme from dot. Either "x11" or "svg"
	 */
	@Override
	protected void setColor(String attrVal, VisualStyle vizStyle,
			ColorAttribute attr, String colorScheme) {
	
		LOGGER.trace("A color attribute is being applied to VisualStyle. Color: {}", attrVal);
		Color color = convertColor(attrVal, colorScheme);
		if (color == null) {
			return;
		}
		List<Pair<Color, Float>> colorListVals = convertColorList(attrVal, colorScheme);
		if (colorListVals != null) {
			color = colorListVals.get(0).getLeft();
		}
		Integer transparency = color.getAlpha();

		switch (attr) {
			case COLOR: {
				LOGGER.trace("Default Edge stroke color being set to {}", color.toString());
				vizStyle.setDefaultValue(EDGE_STROKE_UNSELECTED_PAINT, color);
				vizStyle.setDefaultValue(EDGE_UNSELECTED_PAINT, color);
				vizStyle.setDefaultValue(EDGE_TRANSPARENCY, transparency);
				break;
			}
			case FONTCOLOR: {
				LOGGER.trace("Default Edge stroke color being set to {}", color.toString());
				vizStyle.setDefaultValue(EDGE_LABEL_COLOR, color);
				vizStyle.setDefaultValue(EDGE_LABEL_TRANSPARENCY, transparency);
				break;
			}
			default: {
				break;
			}
		}
	}

	@Override
	protected void setColorDefaults(VisualStyle vizStyle, String colorScheme) {
		String colorAttr = defAttrs.get("color");
		String fontColorAttr = defAttrs.get("fontcolor");
		if (colorAttr != null) {
			List<Pair<Color, Float>> colorListVals = convertColorList(colorAttr, colorScheme);
			if (colorListVals != null) {
				Color color = colorListVals.get(0).getLeft();
				colorAttr = String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(),
						color.getBlue(), color.getAlpha());
			}
			setColor(colorAttr, vizStyle, ColorAttribute.COLOR, colorScheme);
		}
		if (fontColorAttr != null) {
			setColor(fontColorAttr, vizStyle, ColorAttribute.FONTCOLOR, colorScheme);
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
		
		String[] styleAttrs = attrVal.split(",");

		// Get default node visibility
		boolean isVisibleDefault = vizStyle.getDefaultValue(EDGE_VISIBLE);
	
		for (String styleAttr : styleAttrs) {
			styleAttr = styleAttr.trim();
			LineType lineType = LINE_TYPE_MAP.get(styleAttr);
			
			// set line type if defined
			if (lineType != null) {
				elementView.setLockedValue(EDGE_LINE_TYPE, lineType);
			}
		}
		// check if invisible is enabled
		if( attrVal.contains("invis") ) {
			if (isVisibleDefault) {
				elementView.setLockedValue(EDGE_VISIBLE, false);
			}
		}
		else {
			if (!isVisibleDefault) {
				elementView.setLockedValue(EDGE_VISIBLE, true);
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

}


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

package org.cytoscape.intern.write.mapper;

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
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.VisualStyle;

/**
 * This class contains the mappings of edge VisualPropertys to their equivalent
 * dot attribute strings and performs the translations of Cytoscape edges to
 * dot edge declarations
 * @author Braxton Fitts
 * @author Massoud Maher
 * @author Ziran Zhang 
 * 
 */
public class EdgePropertyMapper extends Mapper {
	
	private CyNetworkView networkView;
	
	/**
	 * Constructs EdgePropertyMapper object
	 * 
	 * @param view of edge we are converting
	 */
	public EdgePropertyMapper(View<CyEdge> view, VisualStyle vizStyle, CyNetworkView networkView) {
		super(view, vizStyle);
		// initialize data structure
		simpleVisPropsToDot = new ArrayList<String>();
		this.networkView = networkView;
		populateMaps();		
	}
	
	@SuppressWarnings("unchecked")
	private boolean isVisible() {
		LOGGER.debug("Checking if edge should be visible");
		boolean visibleByProp = view.getVisualProperty(EDGE_VISIBLE);
		if (!visibleByProp) {
			LOGGER.trace("Edge not visible due to its own property.");
			return false;
		}
		CyEdge model = ((View<CyEdge>)view).getModel();
		CyNode source = model.getSource();
		CyNode target = model.getTarget();
		View<CyNode> sourceView = networkView.getNodeView(source);
		View<CyNode> targetView = networkView.getNodeView(target);
		boolean visibleBySource = sourceView.getVisualProperty(NODE_VISIBLE);
		boolean visibleByTarget = targetView.getVisualProperty(NODE_VISIBLE);
		if (!visibleBySource || !visibleByTarget) {
			LOGGER.trace("Edge not visible due to source node or target node's property.");
			return false;
		}
		LOGGER.trace("Edge is visible");
		return true;
	}
	
	/**
	 * Helper method that returns String that contains font face, size, color and transparency
	 * handles opacity.
	 * 
	 * @return String that defines fontname, fontcolor and fontsize attributes. Returns null if font should not be mapped
	 */
	private String mapFontHelper() {

		// Get label font information and append in proper format
		Color labelColor = (Color) view.getVisualProperty(EDGE_LABEL_COLOR);
		// Set alpha (opacity) to 0 if node is invisible, translate alpha otherwise
		Integer labelTransparency = ((Number)view.getVisualProperty(EDGE_LABEL_TRANSPARENCY)).intValue();
		Font labelFont = view.getVisualProperty(EDGE_LABEL_FONT_FACE);
		Integer labelSize = ((Number)view.getVisualProperty(EDGE_LABEL_FONT_SIZE)).intValue();
		String fontString = mapFont(labelFont, labelSize, labelColor, labelTransparency);
		if (fontString != null) {
			return fontString;
		}
		else {
			return null;
		}
	}
	
	/**
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		// Put Simple Props Key/Values
		if (!isEqualToDefault(EDGE_LABEL)) {
			String edgeLabel = view.getVisualProperty(EDGE_LABEL);
			edgeLabel = edgeLabel.replace("\\", "\\\\");
			edgeLabel = edgeLabel.replace("\"", "\\\"");
			simpleVisPropsToDot.add(String.format("label = \"%s\"", edgeLabel));
		}

		if (!isEqualToDefault(EDGE_WIDTH)) {
			Double width = view.getVisualProperty(EDGE_WIDTH);
			simpleVisPropsToDot.add(String.format("penwidth = \"%s\"", decimalFormatter.format(width)));
		}

		if (!isEqualToDefault(EDGE_TOOLTIP)) {
			String tooltip = view.getVisualProperty(EDGE_TOOLTIP);
			simpleVisPropsToDot.add(String.format("tooltip = \"%s\"", tooltip));
		}
		
		// block is non-functioning. only works for bypasses due to what we think is source error
		if (!isEqualToDefault(EDGE_TARGET_ARROW_SHAPE)) {
			ArrowShape targetArrow = view.getVisualProperty(EDGE_TARGET_ARROW_SHAPE);
			LOGGER.debug("Retrieving target/head arrow. CS version is: " + targetArrow);
			String dotTargetArrow = ARROW_SHAPE_MAP.get(targetArrow);
			LOGGER.debug("Target/head arrow retrieved. .dot verison is: " + dotTargetArrow);
			simpleVisPropsToDot.add(String.format("arrowhead = \"%s\"", dotTargetArrow));
		}
				
		if (!isEqualToDefault(EDGE_SOURCE_ARROW_SHAPE)) {
			ArrowShape sourceArrow = view.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE);
			LOGGER.debug("Retrieving source/tail arrow. CS version is: " + sourceArrow);
			String dotSourceArrow = ARROW_SHAPE_MAP.get(sourceArrow);
			LOGGER.debug("Source/tail arrow retrieved. .dot verison is: " + dotSourceArrow);
			simpleVisPropsToDot.add(String.format("arrowtail = \"%s\"", dotSourceArrow));
		}
	}
	
	@Override
	protected String mapDotStyle() {
		LOGGER.debug("Building style string for edge view...");
		LOGGER.trace("Determining need for style attr...");
		StringBuilder dotStyle = null;
		boolean isVisible = isVisible();
		if(!isEqualToDefault(EDGE_LINE_TYPE) || !isEqualToDefault(isVisible, EDGE_VISIBLE)) {
			LOGGER.trace("Not default style attr, building edge's own...");
			LineType lineType = view.getVisualProperty(EDGE_LINE_TYPE);
			String lineStr = LINE_TYPE_MAP.get(lineType);
			dotStyle = new StringBuilder();
			if (lineStr == null) {
				lineStr = "solid";
				LOGGER.warn("Cytoscape property doesn't map to a .dot attribute. Setting to default");
			}
			String invisString = (!isVisible) ? ",invis" : "";
			String style = String.format("style = \"%s%s\"", lineStr, invisString);
			dotStyle.append(style);
		}
		if(dotStyle == null) {
			return null;
		}

		return dotStyle.toString();
	}

	/*
	 * Returns a String that contains all relevant attributes for this element 
	 */
	@Override
	public String getElementString() {
		
		LOGGER.debug("Preparing to get .dot declaration for an edge.");

		// Build attribute string
		StringBuilder elementString = new StringBuilder("[");
		
		// Get .dot strings for simple dot attributes. Append to attribute string
		for (String dotAttribute : simpleVisPropsToDot) {
		        elementString.append(dotAttribute);
		        elementString.append(",");
		}
		
		// Get the color and fillcolor .dot strings. Append to attribute string
		if (!isEqualToDefault(EDGE_STROKE_UNSELECTED_PAINT) || !isEqualToDefault(EDGE_TRANSPARENCY)) {
			Color strokeColor = (Color) view.getVisualProperty(EDGE_STROKE_UNSELECTED_PAINT);
			Integer strokeTransparency = ((Number)view.getVisualProperty(EDGE_TRANSPARENCY)).intValue();
			String dotColor = String.format("color = \"%s\"", mapColorToDot(strokeColor, strokeTransparency));
			elementString.append(dotColor + ",");
		}
		
		String fontString = mapFontHelper();
		if(fontString != null) {
			elementString.append(fontString + ",");
		}

		// Style attr
		String styleString = mapDotStyle();
		if (styleString != null) {
			elementString.append(styleString);
		}
		
		if (elementString.charAt(elementString.length() - 1) == ',') {
			elementString.deleteCharAt(elementString.length() - 1);
		}
		elementString.append("]");
		String result = elementString.toString();
		if (result.matches("^\\[\\]$")) {
			result = "";
		}
		LOGGER.debug("Created .dot string. Result: " + result);
		return result;
	}
}

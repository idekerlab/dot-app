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
	
	/**
	 * Translates the Cytoscape Bend property of the View<CyEdge> object to the
	 * equivalent dot attribute string
	 * 
	 * @return String that represents edge bend attribute
	 */
	/*@SuppressWarnings("unchecked")
	private String mapEdgeBend(){
		// TODO
		StringBuilder coordinatesString = new StringBuilder();
		Bend edgeBend = view.getVisualProperty(EDGE_BEND);
		List<Handle> handles = edgeBend.getAllHandles();
		for (Handle handle : handles) {
			Point2D coords = handle.calculateHandleLocation(networkView, (View<CyEdge>)view);
			String singlePointString = mapPosition(coords.getX(), coords.getY());
			coordinatesString.append(singlePointString + " ");
		}
		if (coordinatesString.length() == 0) {
			return "";
		}
		coordinatesString.deleteCharAt(coordinatesString.length() - 1);
		String dotPos = String.format("pos = \"%s\"", coordinatesString.toString());
		return dotPos;
	}*/
	
	/**
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		// Put Simple Props Key/Values
		if (!isEqualToDefault(EDGE_WIDTH)) {
			String edgeLabel = view.getVisualProperty(EDGE_LABEL);
			edgeLabel = edgeLabel.replace("\"", "\\\"");
			simpleVisPropsToDot.add(String.format("label = \"%s\"", edgeLabel));
		}

		if (!isEqualToDefault(EDGE_WIDTH)) {
			Double width = view.getVisualProperty(EDGE_WIDTH);
			simpleVisPropsToDot.add(String.format("penwidth = \"%f\"", width));
		}

		if (!isEqualToDefault(EDGE_TOOLTIP)) {
			String tooltip = view.getVisualProperty(EDGE_TOOLTIP);
			simpleVisPropsToDot.add(String.format("tooltip = \"%s\"", tooltip));
		}
		
		// block is non-functioning. only works for bypasses due to what we think is source error
		if (!isEqualToDefault(EDGE_TARGET_ARROW_SHAPE)) {
			ArrowShape targetArrow = view.getVisualProperty(EDGE_TARGET_ARROW_SHAPE);
			LOGGER.info("Retrieving target/head arrow. CS version is: " + targetArrow);
			String dotTargetArrow = ARROW_SHAPE_MAP.get(targetArrow);
			LOGGER.info("Target/head arrow retrieved. .dot verison is: " + dotTargetArrow);
			simpleVisPropsToDot.add(String.format("arrowhead = \"%s\"", dotTargetArrow));
		}
				
		if (!isEqualToDefault(EDGE_SOURCE_ARROW_SHAPE)) {
			ArrowShape sourceArrow = view.getVisualProperty(EDGE_SOURCE_ARROW_SHAPE);
			LOGGER.info("Retrieving target/head arrow. CS version is: " + sourceArrow);
			String dotSourceArrow = ARROW_SHAPE_MAP.get(sourceArrow);
			LOGGER.info("Target/head arrow retrieved. .dot verison is: " + dotSourceArrow);
			simpleVisPropsToDot.add(String.format("arrowhead = \"%s\"", dotSourceArrow));
		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean isVisible() {
		LOGGER.info("Checking if edge should be visible");
		boolean visibleByProp = view.getVisualProperty(EDGE_VISIBLE);
		if (!visibleByProp) {
			LOGGER.finest("Edge not visible due to its own property.");
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
			LOGGER.finest("Edge not visible due to source or target's property.");
			return false;
		}
		LOGGER.finest("Edge is visible");
		return true;
	}
	
	protected String mapDotStyle() {
		if (!isEqualToDefault(EDGE_LINE_TYPE)) {
			return super.mapDotStyle();
		}
		return null;
	}
	/**
	 * Returns a String that contains all relevant attributes for this element 
	 */
	@Override
	public String getElementString() {
		final int TRANSPARENT = 0x00;
		
		LOGGER.info("Preparing to get .dot declaration for an edge.");

		// Build attribute string
		StringBuilder elementString = new StringBuilder("[");
		
		// Control for writing color attributes
		boolean visible = isVisible();

		// Get .dot strings for simple dot attributes. Append to attribute string
		for (String dotAttribute : simpleVisPropsToDot) {
		        elementString.append(dotAttribute);
		        elementString.append(",");
		}
		LOGGER.info("Built up .dot string from simple properties. Resulting string: " + elementString);
		
		LOGGER.info("Preparing to get color properties");
		// Get the color and fillcolor .dot strings. Append to attribute string
		Color strokeColor = (Color) view.getVisualProperty(EDGE_STROKE_UNSELECTED_PAINT);
		Integer strokeTransparency = (visible) ? ((Number)view.getVisualProperty(EDGE_TRANSPARENCY)).intValue()
											   : TRANSPARENT;
		if (!isEqualToDefault(EDGE_STROKE_UNSELECTED_PAINT) || !isEqualToDefault(EDGE_TRANSPARENCY)) {
			String dotColor = String.format("color = \"%s\"", mapColorToDot(strokeColor, strokeTransparency));
			elementString.append(dotColor + ",");
		}
		LOGGER.info("Appended color attributes to .dot string. Result: " + elementString);
		
		/*LOGGER.info("Preparing to map edge bends");
		String dotPos = mapEdgeBend();
		if (!(dotPos.equals(""))) {
			elementString.append(dotPos + ",");
		}
		LOGGER.info("Appended edge bend attributes to .dot string. Result: " + elementString);
		*/
		LOGGER.info("Preparing to map edge label attributes");
		// Get label font information and append in proper format
		Color labelColor = (Color) view.getVisualProperty(EDGE_LABEL_COLOR);
		// Set alpha (opacity) to 0 if node is invisible, translate alpha otherwise
		Integer labelTransparency = (visible) ? ((Number)view.getVisualProperty(EDGE_LABEL_TRANSPARENCY)).intValue()
											  : TRANSPARENT;
		Font labelFont = view.getVisualProperty(EDGE_LABEL_FONT_FACE);
		Integer labelSize = ((Number)view.getVisualProperty(EDGE_LABEL_FONT_SIZE)).intValue();
		String fontString = mapFont(labelFont, labelSize, labelColor, labelTransparency);
		if (fontString != null) {
			elementString.append(mapFont(labelFont, labelSize, labelColor, labelTransparency) + ",");
			LOGGER.info("Appened label attributes to .dot string. Result: " + elementString);
		}
		
		LOGGER.info("Appending style attribute to .dot string");
		String styleString = mapDotStyle();
		if (styleString != null) {
			elementString.append(mapDotStyle());
			LOGGER.info("Appended style attribute. Result: " + elementString);
		}
		
		// append dir=both so both arrowShapes show up and close off attr string
		if (!visible) {
			elementString.append(",dir = \"none\"");
		}
		if (elementString.charAt(elementString.length() - 1) == ',') {
			elementString.deleteCharAt(elementString.length() - 1);
		}
		elementString.append("]");
		LOGGER.info("Created .dot string. Result: " + elementString);
		return elementString.toString();
	}
}

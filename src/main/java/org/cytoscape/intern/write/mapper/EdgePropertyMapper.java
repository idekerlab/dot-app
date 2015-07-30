package org.cytoscape.intern.write.mapper;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.Handle;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

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
	
	/**
	 * Maps Cytoscape arrowhead types to the equivalent dot attribute
	 */
	private static final HashMap<ArrowShape, String> ARROW_SHAPE_MAP = new HashMap<ArrowShape, String>(); // TODO fill in
	static {
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.ARROW, "vee");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.CIRCLE, "dot");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.DELTA, "normal");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.DIAMOND, "diamond");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.HALF_BOTTOM, "ornormal");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.HALF_TOP, "olnormal");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.NONE, "none");
		ARROW_SHAPE_MAP.put(ArrowShapeVisualProperty.T, "tee");
	}
	
	private CyNetworkView networkView;
	
	/**
	 * Constructs EdgePropertyMapper object
	 * 
	 * @param view of edge we are converting
	 */
	public EdgePropertyMapper(View<CyEdge> view, CyNetworkView networkView) {
		super(view);
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
	@SuppressWarnings("unchecked")
	private String mapEdgeBend(){
		// TODO
		StringBuilder coordinatesString = new StringBuilder();
		Bend edgeBend = view.getVisualProperty(BasicVisualLexicon.EDGE_BEND);
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
	}
	
	/**
	 * Helper method to fill the hashmap instance variable with constants we need
	 */
	private void populateMaps() {
		// Put Simple Props Key/Values
		String edgeLabel = view.getVisualProperty(BasicVisualLexicon.EDGE_LABEL);
		edgeLabel = edgeLabel.replace("\"", "\\\"");
		simpleVisPropsToDot.add(String.format("label = \"%s\"", edgeLabel));

		Double width = view.getVisualProperty(BasicVisualLexicon.EDGE_WIDTH);
		simpleVisPropsToDot.add(String.format("penwidth = \"%f\"", width));

		String tooltip = view.getVisualProperty(BasicVisualLexicon.EDGE_TOOLTIP);
		simpleVisPropsToDot.add(String.format("tooltip = \"%s\"", tooltip));
		
		// block is non-functioning. only works for bypasses due to what we think is source error
		ArrowShape targetArrow = view.getVisualProperty(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE);
		LOGGER.info("Retrieving target/head arrow. CS version is: " + targetArrow);
		String dotTargetArrow = ARROW_SHAPE_MAP.get(targetArrow);
		LOGGER.info("Target/head arrow retrieved. .dot verison is: " + dotTargetArrow);
		simpleVisPropsToDot.add(String.format("arrowhead = \"%s\"", dotTargetArrow));
				
		ArrowShape sourceArrow = view.getVisualProperty(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE);
		LOGGER.info("Retrieving source/tail arrow. CS version is: " + sourceArrow);
		String dotSourceArrow = ARROW_SHAPE_MAP.get(sourceArrow);
		LOGGER.info("Source/tail arrow retrieved. .dot verison is: " + dotSourceArrow);
		simpleVisPropsToDot.add(String.format("arrowtail = \"%s\"", dotSourceArrow));
	}
	
	@SuppressWarnings("unchecked")
	private boolean isVisible() {
		LOGGER.info("Checking if edge should be visible");
		boolean visibleByProp = view.getVisualProperty(BasicVisualLexicon.EDGE_VISIBLE);
		if (!visibleByProp) {
			LOGGER.finest("Edge not visible due to its own property.");
			return false;
		}
		CyEdge model = ((View<CyEdge>)view).getModel();
		CyNode source = model.getSource();
		CyNode target = model.getTarget();
		View<CyNode> sourceView = networkView.getNodeView(source);
		View<CyNode> targetView = networkView.getNodeView(target);
		boolean visibleBySource = sourceView.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE);
		boolean visibleByTarget = targetView.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE);
		if (!visibleBySource || !visibleByTarget) {
			LOGGER.finest("Edge not visible due to source or target's property.");
			return false;
		}
		LOGGER.finest("Edge is visible");
		return true;
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
		Color strokeColor = (Color) view.getVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		Integer strokeTransparency = (visible) ? ((Number)view.getVisualProperty(BasicVisualLexicon.EDGE_TRANSPARENCY)).intValue()
											   : TRANSPARENT;
		String dotColor = String.format("color = \"%s\"", mapColorToDot(strokeColor, strokeTransparency));
		elementString.append(dotColor + ",");
		LOGGER.info("Appended color attributes to .dot string. Result: " + elementString);
		
		LOGGER.info("Preparing to map edge bends");
		String dotPos = mapEdgeBend();
		if (!(dotPos.equals(""))) {
			elementString.append(dotPos + ",");
		}
		LOGGER.info("Appended edge bend attributes to .dot string. Result: " + elementString);
		
		LOGGER.info("Preparing to map edge label attributes");
		// Get label font information and append in proper format
		Color labelColor = (Color) view.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_COLOR);
		// Set alpha (opacity) to 0 if node is invisible, translate alpha otherwise
		Integer labelTransparency = (visible) ? ((Number)view.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY)).intValue()
											  : TRANSPARENT;
		Font labelFont = view.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_FONT_FACE);
		Integer labelSize = ((Number)view.getVisualProperty(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE)).intValue();
		elementString.append(mapFont(labelFont, labelSize, labelColor, labelTransparency) + ",");
		LOGGER.info("Appened label attributes to .dot string. Result: " + elementString);
		
		LOGGER.info("Appending style attribute to .dot string");
		elementString.append(mapDotStyle() + ",");
		LOGGER.info("Appended style attribute. Result: " + elementString);
		
		// append dir=both so both arrowShapes show up and close off attr string
		String dir = (visible) ? "dir = \"both\"]" : "dir = \"none\"]";
		elementString.append(dir);
		LOGGER.info("Created .dot string. Result: " + elementString);
		return elementString.toString();
	}
}

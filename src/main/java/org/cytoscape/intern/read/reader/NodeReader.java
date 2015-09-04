package org.cytoscape.intern.read.reader;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_LINE_TYPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_FILL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_FONT_FACE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_FONT_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_TOOLTIP;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.cytoscape.intern.GradientListener;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualStyle;

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
	private static final Map<String, NodeShape> NODE_SHAPE_MAP = new HashMap<String, NodeShape>();
	static {
		NODE_SHAPE_MAP.put("triangle", NodeShapeVisualProperty.TRIANGLE);
		NODE_SHAPE_MAP.put("diamond", NodeShapeVisualProperty.DIAMOND);
		NODE_SHAPE_MAP.put("ellipse", NodeShapeVisualProperty.ELLIPSE);
		NODE_SHAPE_MAP.put("hexagon", NodeShapeVisualProperty.HEXAGON);
		NODE_SHAPE_MAP.put("octagon", NodeShapeVisualProperty.OCTAGON);
		NODE_SHAPE_MAP.put("parallelogram", NodeShapeVisualProperty.PARALLELOGRAM);
		NODE_SHAPE_MAP.put("rectangle", NodeShapeVisualProperty.RECTANGLE);     
	}
	
	/*
	 * Map to convert .dot attributes with a single Cytoscape VisualProperty equivalent
	 * Other .dot attributes are handled separately
	 */
	private static final Map<String, VisualProperty<?>> DOT_TO_CYTOSCAPE = new HashMap<String, VisualProperty<?>>(9);
	static {
		DOT_TO_CYTOSCAPE.put("label", NODE_LABEL);
		DOT_TO_CYTOSCAPE.put("xlabel", NODE_LABEL);
		DOT_TO_CYTOSCAPE.put("penwidth", NODE_BORDER_WIDTH);
		DOT_TO_CYTOSCAPE.put("height", NODE_HEIGHT);
		DOT_TO_CYTOSCAPE.put("width", NODE_WIDTH);
		DOT_TO_CYTOSCAPE.put("tooltip", NODE_TOOLTIP);
		DOT_TO_CYTOSCAPE.put("shape", NODE_SHAPE);
		DOT_TO_CYTOSCAPE.put("fontname", NODE_LABEL_FONT_FACE);
		DOT_TO_CYTOSCAPE.put("fontsize", NODE_LABEL_FONT_SIZE);
	}
	
	//True if "fillcolor" attribute has already been consumed for a node
	private boolean usedFillColor = false;
	
	//Used to retrieve CyCustomGraphics2Factories used for gradients;
	private GradientListener gradientListener;
	
	/**
	 * Constructs an object of type Reader.
	 * 
	 * 
	 * @param networkView view of network we are creating/modifying
	 * @param vizStyle VisualStyle that we are applying to the network
	 * @param defaultAttrs Map that contains default attributes for Reader of this type
	 * eg. for NodeReader will be a list of default
	 * @param rendEngMgr RenderingEngineManager needed to retrieve the default VisualLexicon
	 * @param elementMap Map where keys are JPGD node objects and Values are corresponding Cytoscape CyNodes
	 * @param gradientListener ServiceListener used to get Gradient Factories
	 */
	public NodeReader(CyNetworkView networkView, VisualStyle vizStyle, Map<String, String> defaultAttrs, RenderingEngineManager rendEngMgr, Map<Node, CyNode> elementMap, GradientListener gradientListener) {
		super(networkView, vizStyle, defaultAttrs, rendEngMgr);
		this.elementMap = elementMap;
		this.gradientListener = gradientListener;
	}
	
	/**
	 * Sets all the bypass Visual Properties in Cytoscape for this type of reader
	 * eg. NetworkReader sets all network props, same for nodes
	 * Modifies CyNetworkView networkView, VisualStyle vizStyle etc. 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void setBypasses() {
		LOGGER.info("Setting the Bypass values for Visual Style...");
		/*
		 * for each entry in elementMap
		 * 		bypassMap = getAttrMap(elementMap.getKey())
		 * 		for each entry in bypassMap
		 * 			Pair p = convertAttribute(name, val);
		 * 			VP = p.left()
		 * 			val = p.right()
		 * 			getValue().setLockedValue( VP, val);	
		 */
		// for each element, get bypass attributes
		for (Entry<? extends Object, ? extends CyIdentifiable> entry : elementMap.entrySet()) {
			Map<String, String> bypassAttrs = getAttrMap(entry.getKey()); 
			CyNode element = (CyNode)entry.getValue();
			View<CyNode> elementView = networkView.getNodeView(element);

			//reset the usedFillColor boolean for each node
			usedFillColor = false;

			// for each bypass attribute
			for (Entry<String, String> attrEntry : bypassAttrs.entrySet()) {
				String attrKey = attrEntry.getKey();
				String attrVal = attrEntry.getValue();
				LOGGER.info(
					String.format("Converting DOT attribute: %s", attrKey)
				);

				// Handle specialty attributes
				if (attrKey.equals("pos")) {
					setPositions(attrVal, elementView);
					continue;
				}
				/*if (attrKey.equals("style")) {
					setStyle(attrVal, elementView);
					continue;
				}*/
				/*if (attrKey.equals("color") || attrKey.equals("fillcolor")
						|| attrKey.equals("fontcolor")) {
					switch (attrKey) {
						case "color": {
							setColor(attrVal, elementView, ColorAttribute.COLOR);
							break;
						}
						case "fillcolor": {
							setColor(attrVal, elementView, ColorAttribute.FILLCOLOR);
							usedFillColor = true;
							break;
						}
						case "fontcolor": {
							setColor(attrVal, elementView, ColorAttribute.FONTCOLOR);
							break;
						}
					}
					continue;
				}*/
				if (attrKey.equals("fontcolor")) {
					setColor(attrVal, elementView, ColorAttribute.FONTCOLOR);
				}

				Pair<VisualProperty, Object> p = convertAttribute(attrEntry.getKey(), attrEntry.getValue());
				if (p == null) {
					continue;
				}

				VisualProperty vizProp = p.getLeft();
				Object val = p.getRight();
				if (vizProp == null || val == null) {
					continue;
				}
				LOGGER.info("Updating Visual Style...");
				LOGGER.info(String.format("Setting Visual Property %S...", vizProp));
				elementView.setLockedValue(vizProp, val);
			}
			String styleAttribute = bypassAttrs.get("style");
			String colorAttribute = null;
			String fillAttribute = null;
			String gradientAngle = bypassAttrs.get("gradientangle");
			
			boolean isBypassColorAttr;
			boolean isBypassFillAttr;

			if (bypassAttrs.get("color") != null) {
				isBypassColorAttr = true;
				colorAttribute = bypassAttrs.get("color");
			}
			else {
				isBypassColorAttr = false;
				colorAttribute = defaultAttrs.get("color");
			}
			

			if (bypassAttrs.get("fillcolor") != null) {
				isBypassFillAttr = true;
				fillAttribute = bypassAttrs.get("fillcolor");
			}
			else {
				isBypassFillAttr = false;
				fillAttribute = defaultAttrs.get("fillcolor");
			}
			
			if (styleAttribute != null) {
				setStyle(styleAttribute, elementView);
			}

			if (fillAttribute != null) {
				usedFillColor = true;
				List<Pair<Color, Float>> colorListValues = convertColorList(fillAttribute);
				if (colorListValues != null) {
					if (gradientAngle == null) {
						gradientAngle = "0";
					}
					if (styleAttribute != null && !styleAttribute.equals(defaultAttrs.get("style"))) {
						createGradient(colorListValues, elementView, styleAttribute, gradientAngle);
					}
				}
				else {
					if (isBypassFillAttr) {
						setColor(fillAttribute, elementView, ColorAttribute.FILLCOLOR);
					}
				}
			}
			if (colorAttribute != null) {
				List<Pair<Color, Float>> colorListValues = convertColorList(colorAttribute);
				if (colorListValues != null) {
					Color color = colorListValues.get(0).getLeft();
					colorAttribute = String.format("#%2x%2x%2x%2x", color.getRed(), color.getGreen(),
						color.getBlue(), color.getAlpha());
					if (gradientAngle == null) {
						gradientAngle = "0";
					}
					if (styleAttribute != null && !styleAttribute.equals(defaultAttrs.get("style"))) {
						createGradient(colorListValues, elementView, styleAttribute, gradientAngle);
					}
				}
				else {
					if (isBypassColorAttr) {
						setColor(colorAttribute, elementView, ColorAttribute.COLOR);
					}
				}
			}
		}
	}

	/**
	 * Sets defaults and bypass attributes for each node and sets positions
	 */
	/*@Override
	public void setProperties() {
		LOGGER.info("NodeReader: Setting properties for VisualStyle...");
		super.setProperties();
	}*/
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createGradient(List<Pair<Color, Float>> colorListValues,
			VisualStyle vizStyle, String styleAttribute, String gradientAngle) {
		LOGGER.info("Retrieving VisualProperty NODE_CUSTOM_GRAPHICS_1");

		VisualProperty<CyCustomGraphics> nodeGradientProp = 
				(VisualProperty<CyCustomGraphics>) vizLexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");

		if (nodeGradientProp == null) {
			LOGGER.warning("Current Renderer doesn't support CustomGraphics");
			return;
		}

		float start = 0;
		float remain = 1;
		boolean adjustStart = false;
		/*
		 * Determine which Gradient graphic factory to get based on style attribute
		 * if it contains "radial" get the radial factory
		 * otherwise get the linear
		 */

		CyCustomGraphics2Factory<?> factory = gradientListener.getLinearFactory();
		if (styleAttribute.contains("radial")) {
			factory = gradientListener.getRadialFactory();
		}
		List<Color> colors = new ArrayList<Color>(colorListValues.size());
		List<Float> weights = new ArrayList<Float>(colorListValues.size());

		for (Pair<Color, Float> colorAndWeightPair : colorListValues) {
			colors.add(colorAndWeightPair.getLeft());
			Float weight = colorAndWeightPair.getRight();
			if (weight == null) {
				adjustStart = true;
				weights.add(new Float(start));
				continue;
			}
			if (adjustStart) {
				start = remain - weight.floatValue();
			}
			weights.add(new Float(start));
			start = start + weight.floatValue();
		}
		if (start == 0 && remain == 1) {
			weights = new ArrayList<Float>(colorListValues.size());
			for (; start < remain; start += (1f/colorListValues.size())) {
				weights.add(start);
			}
		}
		LOGGER.info("Number of colors in gradient: " + colors.size());
		HashMap<String, Object> gradientProps = new HashMap();
		gradientProps.put("cy_gradientFractions", weights);
		gradientProps.put("cy_gradientColors", colors);
		gradientProps.put("cy_angle", Double.parseDouble(gradientAngle));
		vizStyle.setDefaultValue(nodeGradientProp, factory.getInstance(gradientProps));
		
		
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createGradient(List<Pair<Color, Float>> colorListValues,
			View<CyNode> elementView, String styleAttribute, String gradientAngle) {
		LOGGER.info("Retrieving VisualProperty NODE_CUSTOM_GRAPHICS_1");

		VisualProperty<CyCustomGraphics> nodeGradientProp = 
				(VisualProperty<CyCustomGraphics>) vizLexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");

		if (nodeGradientProp == null) {
			LOGGER.warning("Current Renderer doesn't support CustomGraphics");
			return;
		}

		float start = 0;
		float remain = 1;
		boolean adjustStart = false;
		/*
		 * Determine which Gradient graphic factory to get based on style attribute
		 * if it contains "radial" get the radial factory
		 * otherwise get the linear
		 */

		CyCustomGraphics2Factory<?> factory = gradientListener.getLinearFactory();
		if (styleAttribute != null && styleAttribute.contains("radial")) {
			factory = gradientListener.getRadialFactory();
		}
		List<Color> colors = new ArrayList<Color>(colorListValues.size());
		List<Float> weights = new ArrayList<Float>(colorListValues.size());

		for (Pair<Color, Float> colorAndWeightPair : colorListValues) {
			colors.add(colorAndWeightPair.getLeft());
			Float weight = colorAndWeightPair.getRight();
			if (weight == null) {
				adjustStart = true;
				weights.add(new Float(start));
				continue;
			}
			if (adjustStart) {
				start = remain - weight.floatValue();
			}
			weights.add(new Float(start));
			start = start + weight.floatValue();
		}
		if (start == 0 && remain == 1) {
			weights = new ArrayList<Float>(colorListValues.size());
			for (; start < remain; start += (1f/colorListValues.size())) {
				weights.add(start);
			}
		}
		LOGGER.info("Number of colors in gradient: " + colors.size());
		HashMap<String, Object> gradientProps = new HashMap();
		gradientProps.put("cy_gradientFractions", weights);
		gradientProps.put("cy_gradientColors", colors);
		gradientProps.put("cy_angle", Double.parseDouble(gradientAngle));

		elementView.setLockedValue(nodeGradientProp, factory.getInstance(gradientProps));
	}

	/**
	 * Sets VisualProperties for each node related to location of node.
	 * Here because cannot return 2 VisualProperties from convertAttribute
	 * and want to make exception clear
	 * @param attrVal 
	 * @param elementView 
	 */
	private void setPositions(String attrVal, View<CyNode> elementView) {
		/*
		 * Get pos attribute
		 * Split string by ","
		 * Convert parts to Doubles
		 * Multiple Y coordinate by -1
		 * Set NODE_X_POSITION and NODE_Y_POSITION
		 */
		String[] coords = attrVal.split(",");
		Double x = Double.parseDouble(coords[0]);
		Double y = -1 * Double.parseDouble(coords[1]);
		//Position attributes are not set with bypasses
		elementView.setVisualProperty(NODE_X_LOCATION, x);
		elementView.setVisualProperty(NODE_Y_LOCATION, y);
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
	@SuppressWarnings({ "rawtypes" })
	protected Pair<VisualProperty, Object> convertAttribute(String name, String val) {
		/**
		 * properties to Map:
		 * 
		 * shape
		 * fill color
		 * border color/transparency
		 * border line type
		 * border width
		 * size
		 * label
		 * label position
		 * tooltip
		 * label font/size/color
		 */
		
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
			case "width": {
				//Fall through to height case
			}
			case "height": {
				retrievedVal = Double.parseDouble(val) * 72.0;
				break;
			}
			case "shape": {
				retrievedVal = NODE_SHAPE_MAP.get(val);
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
		}
		return Pair.of(retrievedProp, retrievedVal);

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
		attrVal.toLowerCase();
		String[] styleAttrs = attrVal.split(",");
		
		for (String styleAttr : styleAttrs) {
			styleAttr = styleAttr.trim();
			LineType lineType = LINE_TYPE_MAP.get(styleAttr);

			if (lineType != null) {
				vizStyle.setDefaultValue(NODE_BORDER_LINE_TYPE, lineType);
			}
		}
		
		// check if rounded rectangle and set
		if( attrVal.contains("rounded") && 
				(vizStyle.getDefaultValue(NODE_SHAPE)).equals(NodeShapeVisualProperty.RECTANGLE) ) {
				
			vizStyle.setDefaultValue(NODE_SHAPE, NodeShapeVisualProperty.ROUND_RECTANGLE);
			
		}
		// check if invisible is enabled
		if( attrVal.contains("invis") ) {
			vizStyle.setDefaultValue(NODE_VISIBLE, false);
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
		attrVal.toLowerCase();

		// Get default node visibility
		boolean isVisibleDefault = vizStyle.getDefaultValue(NODE_VISIBLE);

		for (String styleAttr : styleAttrs) {
			styleAttr = styleAttr.trim();

			LineType lineType = LINE_TYPE_MAP.get(styleAttr);
			if (lineType != null) {
				elementView.setLockedValue(NODE_BORDER_LINE_TYPE, lineType);
			}
		}
		
		// check if rounded rectangle and set
		if( attrVal.contains("rounded") && 
				(elementView.getVisualProperty(NODE_SHAPE)).equals(NodeShapeVisualProperty.RECTANGLE) ) {
				
			elementView.setLockedValue(NODE_SHAPE, NodeShapeVisualProperty.ROUND_RECTANGLE);
			
		}
		// check if invisible is enabled
		if( attrVal.contains("invis") ) {
			if (isVisibleDefault) {
				elementView.setLockedValue(NODE_VISIBLE, false);
			}
		}
		else {
			if (!isVisibleDefault) {
				elementView.setLockedValue(NODE_VISIBLE, true);
			}
		}
	}

	/**
	 * Converts .dot color to Cytoscape default value
	 * 
	 * @param attrVal String that is value of color from dot file
	 * @param vizStyle VisualStyle that this color is being used in
	 * @param attr enum for type of color: COLOR, FILLCOLOR or FONTCOLOR 
	 */
	@Override
	protected void setColor(String attrVal, VisualStyle vizStyle,
			ColorAttribute attr) {

		Color color = convertColor(attrVal);
		List<Pair<Color, Float>> colorListValues = convertColorList(attrVal);
		if (colorListValues != null) {
			color = colorListValues.get(0).getLeft();
		}
		Integer transparency = color.getAlpha();

		switch (attr) {
			case COLOR: {
				LOGGER.info("Setting default values for NODE_BORDER_PAINT and"
						+ " NODE_BORDER_TRANSPARENCY");
				vizStyle.setDefaultValue(NODE_BORDER_PAINT, color);
				vizStyle.setDefaultValue(NODE_BORDER_TRANSPARENCY, transparency);
				if (usedDefaultFillColor) {
					LOGGER.finest("Setting only NODE_BORDER_PAINT and"
							+ " NODE_BORDER_TRANSPARENCY");
					//default fillcolor has already been applied, should not redo
					//with color attribute
					break;
				}
				//color attribute used for NODE_FILL_COLOR if
				//fillcolor not present
			}
			case FILLCOLOR: {
				vizStyle.setDefaultValue(NODE_FILL_COLOR, color);
				vizStyle.setDefaultValue(NODE_TRANSPARENCY, transparency);
				break;
			}
			case FONTCOLOR: {
				vizStyle.setDefaultValue(NODE_LABEL_COLOR, color);
				vizStyle.setDefaultValue(NODE_LABEL_TRANSPARENCY, transparency);
				break;
			}
			default: {
				break;
			}
		}
		
	}


	/**
	 * Converts .dot color to Cytoscape bypass value
	 * 
	 * @param attrVal String that is value of color from dot file
	 * @param elementView View of element that color is being applied to
	 * @param attr enum for type of color: COLOR, FILLCOLOR or FONTCOLOR 
	 */
	@Override
	protected void setColor(String attrVal,
			View<? extends CyIdentifiable> elementView, ColorAttribute attr) {

		Color color = convertColor(attrVal);
		List<Pair<Color, Float>> colorListValues = convertColorList(attrVal);
		if (colorListValues != null) {
			color = colorListValues.get(0).getLeft();
		}
		Integer transparency = color.getAlpha();

		switch (attr) {
			case COLOR: {
				elementView.setLockedValue(NODE_BORDER_PAINT, color);
				elementView.setLockedValue(NODE_BORDER_TRANSPARENCY, transparency);
				//default fillcolor has already been applied, should not redo
				//with color attribute
				if (usedFillColor) {
					break;
				}
				//color attribute used for NODE_FILL_COLOR if
				//fillcolor not present
				//Fall through to fillcolor case
			}
			case FILLCOLOR: {
				elementView.setLockedValue(NODE_FILL_COLOR, color);
				elementView.setLockedValue(NODE_TRANSPARENCY, transparency);
				break;
			}
			case FONTCOLOR: {
				elementView.setLockedValue(NODE_LABEL_COLOR, color);
				elementView.setLockedValue(NODE_LABEL_TRANSPARENCY, transparency);
				break;
			}
			default: {
				break;
			}
		}
		
	}

	@Override
	protected void setColorDefaults(VisualStyle vizStyle) {
		// TODO Auto-generated method stub
		String fillAttribute = defaultAttrs.get("fillcolor");
		String colorAttribute = defaultAttrs.get("color");
		String gradientAngle = defaultAttrs.get("gradientangle");
		String styleAttribute = defaultAttrs.get("style");
		if (fillAttribute != null) {
			usedDefaultFillColor = true;
			List<Pair<Color, Float>> colorListValues = convertColorList(fillAttribute);
			if (colorListValues != null) {
				if (gradientAngle == null) {
					gradientAngle = "0";
				}
				createGradient(colorListValues, vizStyle, styleAttribute, gradientAngle);
			}
			else {
				setColor(fillAttribute, vizStyle, ColorAttribute.FILLCOLOR);
			}
		}
		if (colorAttribute != null) {
			List<Pair<Color, Float>> colorListValues = convertColorList(colorAttribute);
			if (colorListValues != null) {
				Color color = colorListValues.get(0).getLeft();
				colorAttribute = String.format("#%2x%2x%2x%2x", color.getRed(), color.getGreen(),
						color.getBlue(), color.getAlpha());
				if (gradientAngle == null) {
					gradientAngle = "0";
				}
				if (!usedDefaultFillColor) {
					createGradient(colorListValues, vizStyle, styleAttribute, gradientAngle);
				}
			}
			setColor(colorAttribute, vizStyle, ColorAttribute.COLOR);
		}
	}
}


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
import java.awt.geom.Point2D;
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

	
	// maps GraphViz node shapes with corresponding Cytoscape node shapes
	private static final Map<String, NodeShape> NODE_SHAPE_MAP = new HashMap<String, NodeShape>();
	static {
		NODE_SHAPE_MAP.put("triangle", NodeShapeVisualProperty.TRIANGLE);
		NODE_SHAPE_MAP.put("diamond", NodeShapeVisualProperty.DIAMOND);
		NODE_SHAPE_MAP.put("Mdiamond", NodeShapeVisualProperty.DIAMOND);
		NODE_SHAPE_MAP.put("ellipse", NodeShapeVisualProperty.ELLIPSE);
		NODE_SHAPE_MAP.put("circle", NodeShapeVisualProperty.ELLIPSE);
		NODE_SHAPE_MAP.put("Mcircle", NodeShapeVisualProperty.ELLIPSE);
		NODE_SHAPE_MAP.put("hexagon", NodeShapeVisualProperty.HEXAGON);
		NODE_SHAPE_MAP.put("octagon", NodeShapeVisualProperty.OCTAGON);
		NODE_SHAPE_MAP.put("parallelogram", NodeShapeVisualProperty.PARALLELOGRAM);
		NODE_SHAPE_MAP.put("rectangle", NodeShapeVisualProperty.RECTANGLE);
		NODE_SHAPE_MAP.put("box", NodeShapeVisualProperty.RECTANGLE);
		NODE_SHAPE_MAP.put("rect", NodeShapeVisualProperty.RECTANGLE);
		NODE_SHAPE_MAP.put("square", NodeShapeVisualProperty.RECTANGLE);
		NODE_SHAPE_MAP.put("Msquare", NodeShapeVisualProperty.RECTANGLE);
	}
	
	/*
	 * maps GraphViz attributes with a single Cytoscape VisualProperty
	 * equivalent. Other GraphViz attributes are handled separately
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
	// Value used to convert DOT's width and height values from inches to points
	private static final int PPI = 72;
	// true if "fillcolor" attribute has already been consumed for a node
	private boolean usedFillColor = false;
	// true if the default shapes for nodes are regular polygons
	private boolean isDefaultRegularShape = false;
	
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
	 * @param rendEngMgr RenderingEngineManager that contains the default
	 * VisualLexicon needed for gradient support
	 * @param elementMap Map where keys are JPGD node objects and Values are corresponding Cytoscape CyNodes
	 * @param gradientListener ServiceListener used to get Gradient Factories
	 */
	public NodeReader(CyNetworkView networkView, VisualStyle vizStyle, Map<String, String> defaultAttrs, RenderingEngineManager rendEngMgr, Map<Node, CyNode> elementMap, GradientListener gradientListener) {
		super(networkView, vizStyle, defaultAttrs, rendEngMgr);
		this.elementMap = elementMap;
		this.gradientListener = gradientListener;
	}
	
	/**
	 * Converts an angle into a coordinate to be used by the Cytoscape Radial
	 * gradient factory
	 * @param angle the angle to convert
	 * @return a coordinate representing the angle
	 */
	private Point2D convertAngleToPoint(double angle) {
		double center = 0.5;
		if (angle == 0.0) {
			return new Point2D.Double(center, center);
		}
		Point2D.Double doublePoint;
		double x = (.5 * Math.cos(Math.toRadians(angle))) + center;
		double y = (-.5 * Math.sin(Math.toRadians(angle))) + center;
		doublePoint = new Point2D.Double(x, y);
		return doublePoint;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void createGradient(List<Pair<Color, Float>> colorListVals,
			View<CyNode> elementView, String styleAttr, String gradientAngle) {
		LOGGER.trace("Creating gradient...");

		LOGGER.debug("Retrieving VisualProperty NODE_CUSTOMGRAPHICS_1");
		VisualProperty<CyCustomGraphics> nodeGradientProp = 
				(VisualProperty<CyCustomGraphics>) vizLexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");

		if (nodeGradientProp == null) {
			LOGGER.warn("Current Renderer doesn't support CustomGraphics");
			return;
		}

		float start = 0;
		float remain = 1;
		boolean adjustStart = false;
		boolean usingLinearFactory = true;
		/*
		 * Determine which Gradient graphic factory to get based on style attribute
		 * if it contains "radial" get the radial factory
		 * otherwise get the linear
		 */

		LOGGER.trace("Retrieving Gradient factory...");
		CyCustomGraphics2Factory<?> factory = gradientListener.getLinearFactory();
		if (styleAttr.contains("radial")) {
			factory = gradientListener.getRadialFactory();
			usingLinearFactory = false;
			LOGGER.trace("Retrieved Radial Gradient factory.");
		}
		List<Color> colors = new ArrayList<Color>(colorListVals.size());
		List<Float> weights = new ArrayList<Float>(colorListVals.size());

		for (Pair<Color, Float> colorWeightPair : colorListVals) {
			Color retrievedColor = colorWeightPair.getLeft();
			Float retrievedWeight = colorWeightPair.getRight();
			LOGGER.debug(
				String.format("Retrieved color %s with weight %f",
					retrievedColor, retrievedWeight)
			);
			colors.add(retrievedColor);
			if (retrievedWeight == null) {
				adjustStart = true;
				weights.add(new Float(start));
				continue;
			}
			if (adjustStart) {
				start = remain - retrievedWeight.floatValue();
				adjustStart = false;
			}
			weights.add(new Float(start));
			start = start + retrievedWeight.floatValue();
		}
		if (start == 0 && remain == 1) {
			weights = new ArrayList<Float>(colorListVals.size());
			LOGGER.debug(
				String.format("Each color will now take up %f of gradient", 
					1f/colorListVals.size())
			);
			for (; start < remain; start += (1f/colorListVals.size())) {

				weights.add(start);
			}
		}
		LOGGER.debug("Number of colors in gradient: " + colors.size());
		HashMap<String, Object> gradientProps = new HashMap();
		gradientProps.put("cy_gradientFractions", weights);
		gradientProps.put("cy_gradientColors", colors);
		if (usingLinearFactory) {
			gradientProps.put("cy_angle", Double.parseDouble(gradientAngle));
		}
		else {
			Point2D point = convertAngleToPoint(Double.parseDouble(gradientAngle));
			gradientProps.put("cy_center", point);
		}

		elementView.setLockedValue(nodeGradientProp, factory.getInstance(gradientProps));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createGradient(List<Pair<Color, Float>> colorListVals,
			VisualStyle vizStyle, String styleAttr, String gradientAngle) {
		LOGGER.trace("Creating gradient...");

		LOGGER.debug("Retrieving VisualProperty NODE_CUSTOMGRAPHICS_1");
		VisualProperty<CyCustomGraphics> nodeGradientProp = 
				(VisualProperty<CyCustomGraphics>) vizLexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");

		if (nodeGradientProp == null) {
			LOGGER.warn("Current Renderer doesn't support CustomGraphics");
			return;
		}

		float start = 0;
		float remain = 1;
		boolean adjustStart = false;
		boolean usingLinearFactory = true;
		/*
		 * Determine which Gradient graphic factory to get based on style attribute
		 * if it contains "radial" get the radial factory
		 * otherwise get the linear
		 */

		LOGGER.trace("Retrieving Gradient factory...");
		CyCustomGraphics2Factory<?> factory = gradientListener.getLinearFactory();
		if (styleAttr.contains("radial")) {
			factory = gradientListener.getRadialFactory();
			usingLinearFactory = false;
			LOGGER.trace("Retrieved Radial Gradient factory.");
		}
		List<Color> colors = new ArrayList<Color>(colorListVals.size());
		List<Float> weights = new ArrayList<Float>(colorListVals.size());

		for (Pair<Color, Float> colorWeightPair : colorListVals) {
			Color retrievedColor = colorWeightPair.getLeft();
			Float retrievedWeight = colorWeightPair.getRight();
			LOGGER.debug(
				String.format("Retrieved color %s with weight %f",
					retrievedColor, retrievedWeight)
			);
			colors.add(retrievedColor);
			if (retrievedWeight == null) {
				adjustStart = true;
				weights.add(new Float(start));
				continue;
			}
			if (adjustStart) {
				start = remain - retrievedWeight.floatValue();
				adjustStart = false;
			}
			weights.add(new Float(start));
			start = start + retrievedWeight.floatValue();
		}
		if (start == 0 && remain == 1) {
			weights = new ArrayList<Float>(colorListVals.size());
			LOGGER.debug(
				String.format("Each color will now take up %f of gradient", 
					1f/colorListVals.size())
			);
			for (; start < remain; start += (1f/colorListVals.size())) {

				weights.add(start);
			}
		}
		LOGGER.debug("Number of colors in gradient: {}", colors.size());
		LOGGER.debug("Angle of gradient: {}", gradientAngle);
		HashMap<String, Object> gradientProps = new HashMap();
		gradientProps.put("cy_gradientFractions", weights);
		gradientProps.put("cy_gradientColors", colors);
		if (usingLinearFactory) {
			gradientProps.put("cy_angle", Double.parseDouble(gradientAngle));
		}
		else {
			Point2D point = convertAngleToPoint(Double.parseDouble(gradientAngle));
			gradientProps.put("cy_center", point);
		}

		vizStyle.setDefaultValue(nodeGradientProp, factory.getInstance(gradientProps));
	}


	/**
	 * Sets VisualProperties for each node related to location of node.
	 * Here because cannot return 2 VisualProperties from convertAttribute
	 * and want to make exception clear
	 * @param attrVal 
	 * @param elementView 
	 */
	private void setPositions(String attrVal, View<CyNode> elementView) {
		String[] coords = attrVal.split(",");
		Double x = Double.parseDouble(coords[0]);
		
		//Y coordinate is different between GraphViz and Java.
		Double y = -1 * Double.parseDouble(coords[1]);

		//Position attributes are not set with bypasses.
		elementView.setVisualProperty(NODE_X_LOCATION, x);
		elementView.setVisualProperty(NODE_Y_LOCATION, y);
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
	@SuppressWarnings({ "rawtypes" })
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
			case "width": {
				//Fall through to height case
			}
			case "height": {
				retrievedVal = Double.parseDouble(val) * PPI;
				break;
			}
			case "shape": {
				/* 
				 * Loop through and use contains because
				 * Graphviz has things like Mdiamond which is a diamond
				 * with lines through it, this just becomes diamond
				 */
				for (String key: NODE_SHAPE_MAP.keySet()) {
					if(val.contains(key)) {
						retrievedVal = NODE_SHAPE_MAP.get(key);
					}
				}
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
	 * Sets all the default Visual Properties values for Cytoscape View Objects
	 * corresponding to CyNode objects in the elementMap
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void setDefaults() {
		super.setDefaults();
		LOGGER.debug("Adjusting node size if supposed to be regular polygon");
		//Handle node size change if shape is a regular polygon
		if (defAttrs.containsKey("shape")) {
			String shape = defAttrs.get("shape");
			LOGGER.debug("Shape set as default for nodes is {}", shape);
			if (shape.contains("square") || shape.contains("circle")) {
				isDefaultRegularShape = true;
				if (defAttrs.containsKey("width")) {
					String width = defAttrs.get("width");
					if (defAttrs.containsKey("height")) {
						String height = defAttrs.get("height");
						if (Double.parseDouble(width) > Double.parseDouble(height)) {
							Pair<VisualProperty, Object> p = convertAttribute("height", width);
							LOGGER.debug("Fixing node's height to equal node's width");
							vizStyle.setDefaultValue(p.getLeft(), p.getRight());
						} else if (Double.parseDouble(width) < Double.parseDouble(height)) {
							Pair<VisualProperty, Object> p = convertAttribute("width", height);
							LOGGER.debug("Fixing node's width to equal node's height");
							vizStyle.setDefaultValue(p.getLeft(), p.getRight());
						}
					} else {
						Pair<VisualProperty, Object> p = convertAttribute("height", width);
						LOGGER.debug("Fixing node's height to equal node's width");
						vizStyle.setDefaultValue(p.getLeft(), p.getRight());
					}
				} else if (defAttrs.containsKey("height")) {
					String height = defAttrs.get("height");
					Pair<VisualProperty, Object> p = convertAttribute("width", height);
					LOGGER.debug("Fixing node's width to equal node's height");
					vizStyle.setDefaultValue(p.getLeft(), p.getRight());
				} else {
					//Change the implicit default height to the implicit default width since width is greater
					LOGGER.debug("Fixing node's height to equal node's width");
					Double width = vizStyle.getDefaultValue(NODE_WIDTH);
					vizStyle.setDefaultValue(NODE_HEIGHT, width);
				}
			}
		}
	}
	/**
	 * Sets all the bypass Visual Properties values for Cytoscape View objects
	 * corresponding to CyNode objects in the elementMap
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void setBypasses() {
		LOGGER.info("Setting the Bypass values for node views...");

		// for each element, get bypass attributes
		for (Entry<? extends Object, ? extends CyIdentifiable> entry : elementMap.entrySet()) {
			Map<String, String> bypsAttrs = getAttrMap(entry.getKey()); 
			String colorScheme = (bypsAttrs.containsKey("colorscheme")) ? bypsAttrs.get("colorscheme") : null;
			
			//Get the node view
			CyNode element = (CyNode)entry.getValue();
			View<CyNode> elementView = networkView.getNodeView(element);

			//reset the usedFillColor boolean for each node
			usedFillColor = false;

			//reference variables for attribute handling
			String styleAttr = null;
			String colorAttr = null;
			String fillAttr = null;
			String gradientAngle = null;
			boolean isRegularShape = isDefaultRegularShape;
			

			for (Entry<String, String> attrEntry : bypsAttrs.entrySet()) {
				String attrKey = attrEntry.getKey();
				String attrVal = attrEntry.getValue();
				LOGGER.debug("Converting GraphViz attribute: {}", attrKey);

				switch (attrKey) {
					case "style": {
						styleAttr = attrVal;
						continue;
					}
					case "pos": {
						setPositions(attrVal, elementView);
						continue;
					}
					case "color": {
						colorAttr = (attrVal.equals("")) ? "black" : attrVal;
						continue;
					}
					case "fillcolor": {
						fillAttr = (attrVal.equals("")) ? "lightgrey" : attrVal;
						continue;
					}
					case "fontcolor": {
						String fontAttr = (attrVal.equals("")) ? "black" : attrVal;
						setColor(fontAttr, elementView, ColorAttribute.FONTCOLOR, colorScheme);
					}
					case "gradientangle": {
						gradientAngle = attrVal;
						continue;
					}
					case "shape": {
						if (!(attrVal.contains("square") || attrVal.contains("circle"))) {
							LOGGER.debug("Shape of node is not regular polygon");
							//Flag code to skip regular shape specific code after iterating through DOT attributes
							isRegularShape = false;
						}
					}
					default: {
						// handle simple attributes
						Pair<VisualProperty, Object> p = convertAttribute(attrKey, attrVal);
						if (p == null) {
							continue;
						}
	
						VisualProperty vizProp = p.getLeft();
						Object val = p.getRight();
						if (vizProp == null || val == null) {
							continue;
						}
						LOGGER.trace("Updating Visual Style...");
						LOGGER.debug("Setting Visual Property {}", vizProp);
						elementView.setLockedValue(vizProp, val);
					}
				}
			}
			
			//Handle node height change if shape is regular polygon
			if (isRegularShape) {
				if (bypsAttrs.containsKey("width")) {
					String width = bypsAttrs.get("width");
					if (bypsAttrs.containsKey("height")) {
						String height = bypsAttrs.get("height");
						if (Double.parseDouble(width) > Double.parseDouble(height)) {
							Pair<VisualProperty, Object> p = convertAttribute("height", width);
							LOGGER.debug("Fixing node's height to equal node's width");
							elementView.setLockedValue(p.getLeft(), p.getRight());
						} else if (Double.parseDouble(width) < Double.parseDouble(height)) {
							Pair<VisualProperty, Object> p = convertAttribute("width", height);
							LOGGER.debug("Fixing node's width to equal node's height");
							elementView.setLockedValue(p.getLeft(), p.getRight());
						}
					} else {
						Pair<VisualProperty, Object> p = convertAttribute("height", width);
						LOGGER.debug("Fixing node's height to equal node's width");
						elementView.setLockedValue(p.getLeft(), p.getRight());
					}
				} else if (bypsAttrs.containsKey("height")) {
					String height = bypsAttrs.get("height");
					Pair<VisualProperty, Object> p = convertAttribute("width", height);
					LOGGER.debug("Fixing node's width to equal node's height");
					elementView.setLockedValue(p.getLeft(), p.getRight());
				}
			}
			//Handle gradient creation and color setting now

			LOGGER.trace("Handle style and node color attributes");
			//Attempt to get the default gradient angle
			String defGradAngle = defAttrs.containsKey("gradientangle") ? defAttrs.get("gradientangle") : "0";

			
			//Assume that the "color" and "fillcolor" attributes are from
			//the element
			boolean isBypassColorAttr = true;
			boolean isBypassFillAttr = true;

			//If a value was not found for "color" use the value from default list
			if (colorAttr == null) {
				isBypassColorAttr = false;
				colorAttr = defAttrs.get("color");
			}
			
			//If a value was not found for "fillcolor" use default list value
			if (fillAttr == null) {
				isBypassFillAttr = false;
				fillAttr = defAttrs.get("fillcolor");
			}
			
			//handle "fillcolor" attribute first since "color" can replace it
			//if not found
			if (fillAttr != null) {
				usedFillColor = true;
				List<Pair<Color, Float>> colorListValues = convertColorList(fillAttr, colorScheme);
				if (colorListValues != null) {
					if (gradientAngle == null) {
						//default gradient angle is 0
						gradientAngle = "0";
					}
					/* A gradient needs to be applied if the color is from the
					 * default list but the gradient style is different or angle
					 * is different
					 */
					if (styleAttr != null && 
						(!styleAttr.equals(defAttrs.get("style")) ||
							!gradientAngle.equals(defGradAngle))) {
						createGradient(colorListValues, elementView, styleAttr, gradientAngle);
					}
				}
				else {
					if (isBypassFillAttr) {
						setColor(fillAttr, elementView, ColorAttribute.FILLCOLOR, colorScheme);
					}
				}
			}
			if (colorAttr != null) {
				List<Pair<Color, Float>> colorListValues = convertColorList(colorAttr, colorScheme);
				if (colorListValues != null) {
					Color color = colorListValues.get(0).getLeft();
					colorAttr = String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(),
						color.getBlue(), color.getAlpha());
					if (gradientAngle == null) {
						gradientAngle = "0";
					}
					/* A gradient needs to be applied if the color is from the
					 * default list but the gradient style is different or angle
					 * is different
					 */
					if (styleAttr != null && 
						(!styleAttr.equals(defAttrs.get("style")) ||
							!gradientAngle.equals(defGradAngle))) {
						createGradient(colorListValues, elementView, styleAttr, gradientAngle);
					}
				}
				if (isBypassColorAttr) {
					setColor(colorAttr, elementView, ColorAttribute.COLOR, colorScheme);
				}
			}
			//Use style attribute to perform visual transformations of node
			if (styleAttr != null) {
				setStyle(styleAttr, elementView);
			}
		}
	}

	/**
	 * Converts a GraphViz color attribute into corresponding VisualProperty
	 * bypass values for a Cytoscape View object
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

		Color color = convertColor(attrVal, colorScheme);
		if (color == null) {
			return;
		}
		List<Pair<Color, Float>> colorListValues = convertColorList(attrVal, colorScheme);
		if (colorListValues != null) {
			color = colorListValues.get(0).getLeft();
		}
		Integer transparency = color.getAlpha();
		

		switch (attr) {
			case COLOR: {
				elementView.setLockedValue(NODE_BORDER_PAINT, color);
				elementView.setLockedValue(NODE_BORDER_TRANSPARENCY, transparency);

				//fillcolor has already been applied, should not redo
				//with color attribute
				if (usedFillColor) {
					break;
				}

			/*
			 * color attribute used for NODE_FILL_COLOR if
			 * fillcolor attribute not present, thus fall through
			 * to fillcolor case
			 */
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

	/**
	 * Converts a GraphViz color attribute into the corresponding default
	 * VisualProperty values for a Cytoscape VisualStyle
	 * 
	 * @param attrVal GraphViz color string
	 * @param vizStyle VisualStyle that this color is being used in
	 * @param attr enum for type of color: COLOR, FILLCOLOR or FONTCOLOR 
	 * @param colorScheme Scheme from dot. Either "x11" or "svg"
	 */
	@Override
	protected void setColor(String attrVal, VisualStyle vizStyle,
			ColorAttribute attr, String colorScheme) {

		Color color = convertColor(attrVal, colorScheme);
		if (color == null) {
			return;
		}
		List<Pair<Color, Float>> colorListValues = convertColorList(attrVal, colorScheme);
		if (colorListValues != null) {
			color = colorListValues.get(0).getLeft();
		}
		Integer transparency = color.getAlpha();

		switch (attr) {
			case COLOR: {
				LOGGER.trace("Setting default values for NODE_BORDER_PAINT and"
						+ " NODE_BORDER_TRANSPARENCY");
				vizStyle.setDefaultValue(NODE_BORDER_PAINT, color);
				vizStyle.setDefaultValue(NODE_BORDER_TRANSPARENCY, transparency);
				if (hasUsedDefFill) {
					LOGGER.trace("Setting only NODE_BORDER_PAINT and"
							+ " NODE_BORDER_TRANSPARENCY");
					//default fillcolor has already been applied, should not redo
					//with color attribute
					break;
				}
				//color attribute used for NODE_FILL_COLOR if
				//fillcolor not present
			}
			case FILLCOLOR: {
				LOGGER.trace("Setting default values for NODE_FILL_COLOR and"
						+ " NODE_TRANSPARENCY");
				vizStyle.setDefaultValue(NODE_FILL_COLOR, color);
				vizStyle.setDefaultValue(NODE_TRANSPARENCY, transparency);
				break;
			}
			case FONTCOLOR: {
				LOGGER.trace("Setting default values for NODE_LABEL_FONT_COLOR and"
						+ " NODE_LABEL_TRANSPARENCY");
				vizStyle.setDefaultValue(NODE_LABEL_COLOR, color);
				vizStyle.setDefaultValue(NODE_LABEL_TRANSPARENCY, transparency);
				break;
			}
			default: {
				break;
			}
		}
		
	}

	@Override
	protected void setColorDefaults(VisualStyle vizStyle, String colorScheme) {
		LOGGER.info("Setting node property default values for color attributes");
		String fillAttr = defAttrs.containsKey("fillcolor") ? defAttrs.get("fillcolor") : null;
		String colorAttr = defAttrs.containsKey("color") ? defAttrs.get("color") : null;
		String fontColorAttr = defAttrs.containsKey("fontcolor") ? defAttrs.get("fontcolor") : null;
		String gradientAngle = defAttrs.containsKey("gradientAngle") ? defAttrs.get("gradientangle") : null;
		String styleAttr = defAttrs.containsKey("style") ? defAttrs.get("style") : null;
		if (fillAttr != null) {
			fillAttr = fillAttr.equals("") ? "lightgrey" : fillAttr;
			hasUsedDefFill = true;
			List<Pair<Color, Float>> colorListVals = convertColorList(fillAttr, colorScheme);
			if (colorListVals != null) {
				if (gradientAngle == null) {
					gradientAngle = "0";
				}
				createGradient(colorListVals, vizStyle, styleAttr, gradientAngle);
			}
			else {
				setColor(fillAttr, vizStyle, ColorAttribute.FILLCOLOR, colorScheme);
			}
		}
		if (colorAttr != null) {
			colorAttr = colorAttr.equals("") ? "black" : colorAttr;
			List<Pair<Color, Float>> colorListVals = convertColorList(colorAttr, colorScheme);
			if (colorListVals != null) {
				Color color = colorListVals.get(0).getLeft();
				colorAttr = String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(),
						color.getBlue(), color.getAlpha());
				if (gradientAngle == null) {
					gradientAngle = "0";
				}
				if (!hasUsedDefFill) {
					createGradient(colorListVals, vizStyle, styleAttr, gradientAngle);
				}
			}
			setColor(colorAttr, vizStyle, ColorAttribute.COLOR, colorScheme);
		}
		if (fontColorAttr != null) {
			fontColorAttr = fontColorAttr.equals("") ? "black" : fontColorAttr;
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

		LOGGER.debug("Setting style for node {}. Style string: {}", elementView, attrVal);
		String[] styleAttrs = attrVal.split(",");

		// Get default node visibility
		boolean isVisibleDefault = vizStyle.getDefaultValue(NODE_VISIBLE);
		// Get default node border line type
		LineType defaultLineType = vizStyle.getDefaultValue(NODE_BORDER_LINE_TYPE);

		for (String styleAttr : styleAttrs) {
			styleAttr = styleAttr.trim();

			LineType lineType = LINE_TYPE_MAP.get(styleAttr);
			if (lineType != null && !lineType.equals(defaultLineType)) {
				elementView.setLockedValue(NODE_BORDER_LINE_TYPE, lineType);
			}
		}
		
		// check if rounded rectangle and set
		NodeShape elementShape = elementView.getVisualProperty(NODE_SHAPE);
		NodeShape defaultShape = vizStyle.getDefaultValue(NODE_SHAPE);
		if (attrVal.contains("rounded") && 
				elementShape.equals(NodeShapeVisualProperty.RECTANGLE)) {
			if (!elementShape.equals(defaultShape)) {
				elementView.setLockedValue(NODE_SHAPE, NodeShapeVisualProperty.ROUND_RECTANGLE);
			}
		}
		// check if invisible is enabled
		if (attrVal.contains("invis")) {
			if (isVisibleDefault) {
				elementView.setLockedValue(NODE_VISIBLE, false);
			}
		}
		else {
			if (!isVisibleDefault) {
				elementView.setLockedValue(NODE_VISIBLE, true);
			}
		}
		// if node is not filled
		LOGGER.debug("Checking if style string contains filled. {}", attrVal.contains("filled"));
		if(!attrVal.contains("filled")) {
			elementView.setLockedValue(NODE_TRANSPARENCY, 0);
			LOGGER.debug("Did transparency get set to 0 for node {}? {}", elementView, elementView.getVisualProperty(NODE_TRANSPARENCY).intValue() == 0);
		}
	}

	/**
	 * Converts the GraphViz "style" attribute into default VisualProperty
	 * values for a Cytoscape VisualStyle
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
		// if node is not filled
		if(!attrVal.contains("filled")) {
			vizStyle.setDefaultValue(NODE_TRANSPARENCY, 0);
		}
	}
}


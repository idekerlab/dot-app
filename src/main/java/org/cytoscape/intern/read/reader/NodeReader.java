package org.cytoscape.intern.read.reader;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualStyle;

import com.alexmerz.graphviz.objects.Node;

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
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;


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
		NODE_SHAPE_MAP.put("ellipse", NodeShapeVisualProperty.ELLIPSE);
		NODE_SHAPE_MAP.put("hexagon", NodeShapeVisualProperty.HEXAGON);
		NODE_SHAPE_MAP.put("octagon", NodeShapeVisualProperty.OCTAGON);
		NODE_SHAPE_MAP.put("parallelogram", NodeShapeVisualProperty.PARALLELOGRAM);
		NODE_SHAPE_MAP.put("rectangle", NodeShapeVisualProperty.RECTANGLE);     
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
	
	// true if "fillcolor" attribute has already been consumed for a node
	private boolean usedFillColor = false;
	
	/**
	 * Constructs an object of type Reader.
	 * 
	 * 
	 * @param networkView view of network we are creating/modifying
	 * @param vizStyle VisualStyle that we are applying to the network
	 * @param defaultAttrs Map that contains default attributes
	 * @param elementMap Map of which the keys are JPGD Node objects and the 
	 * values are corresponding Cytoscape CyNode objects 
	 */
	public NodeReader(CyNetworkView networkView, VisualStyle vizStyle, Map<String, String> defaultAttrs, Map<Node, CyNode> elementMap) {
		super(networkView, vizStyle, defaultAttrs);
		this.elementMap = elementMap;
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
			Map<String, String> bypassAttrs = getAttrMap(entry.getKey()); 
			String colorScheme = bypassAttrs.get("colorscheme");
			CyNode element = (CyNode)entry.getValue();
			View<CyNode> elementView = networkView.getNodeView(element);

			//reset the usedFillColor boolean for each node
			usedFillColor = false;

			String styleVal = null;
			// for each bypass attribute
			for (Entry<String, String> attrEntry : bypassAttrs.entrySet()) {
				String attrKey = attrEntry.getKey();
				String attrVal = attrEntry.getValue();
				LOGGER.info(
					String.format("Converting GraphViz attribute: %s", attrKey)
				);

				// handle special attributes
				// These attributes map to more than one VisualProperty.
				if (attrKey.equals("pos")) {
					setPositions(attrVal, elementView);
					continue;
				}
				if (attrKey.equals("style")) {
					// Save style value so we can call setStyle later when nodeShape dependency is guaranteed
					styleVal = attrVal;
					continue;
				}
				if (attrKey.equals("color") || attrKey.equals("fillcolor")
						|| attrKey.equals("fontcolor")) {
					switch (attrKey) {
						case "color": {
							setColor(attrVal, elementView, ColorAttribute.COLOR, colorScheme);
							break;
						}
						case "fillcolor": {
							setColor(attrVal, elementView, ColorAttribute.FILLCOLOR, colorScheme);
							usedFillColor = true;
							break;
						}
						case "fontcolor": {
							setColor(attrVal, elementView, ColorAttribute.FONTCOLOR, colorScheme);
							break;
						}
					}
					continue;
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
			// set style if it was declared
			if(styleVal != null) {
				setStyle(styleVal, elementView);
			}
		}
	}

	/**
	 * Sets the NODE_X_LOCATION and NODE_Y_LOCATION Cytoscape visual properties
	 * from the given GraphViz "pos" value.
	 * @param attrVal GraphViz "pos" value 
	 * @param elementView view for which the visual properties are being set
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
	 * Converts the GraphViz "style" attribute into default VisualProperty
	 * values for a Cytoscape VisualStyle
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
		// if node is not filled
		if(!attrVal.contains("filled")) {
			vizStyle.setDefaultValue(NODE_TRANSPARENCY, 0);
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
		attrVal.toLowerCase();

		// Default to visible
		elementView.setLockedValue(NODE_VISIBLE, true);

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
			elementView.setLockedValue(NODE_VISIBLE, false);
		}
		// if node is not filled
		if(!attrVal.contains("filled")) {
			elementView.setLockedValue(NODE_TRANSPARENCY, 0);
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
		Integer transparency = color.getAlpha();

		switch (attr) {
			case COLOR: {
				vizStyle.setDefaultValue(NODE_BORDER_PAINT, color);
				vizStyle.setDefaultValue(NODE_BORDER_TRANSPARENCY, transparency);
				if (usedDefaultFillColor) {
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
}


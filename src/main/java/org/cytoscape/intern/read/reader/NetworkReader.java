package org.cytoscape.intern.read.reader;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_BACKGROUND_PAINT;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualStyle;

import com.alexmerz.graphviz.objects.Graph;

/**
 * Class that contains definitions and some implementation for converting a
 * dot graph to a CyNetwork. Data is passed in as a JPGD Graph object.
 * This subclass handles importing of network/graph properties
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class NetworkReader extends Reader{


	// JPGD object that contains visual information for this network view
	private Graph graph;
	/**
	 * Constructs an object of type Reader. Sets up Logger.
	 * 
	 * @param networkView view of network we are creating/modifying
	 * @param vizStyle VisualStyle that we are applying to the network
	 * @param defaultAttrs Map that contains default attributes for Reader of this type
	 * eg. for NodeReader will be a list of default
	 */
	public NetworkReader(CyNetworkView networkView, VisualStyle vizStyle, Map<String, String> defaultAttrs, Graph graph) {
		super(networkView, vizStyle, defaultAttrs);
		this.graph = graph;

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
		//Network doesn't set bypass value with the Graph object's attributes
		//overrides the defaults set in setDefault()
		LOGGER.info("Setting the Bypass values for Visual Style...");
		/*
		 * for attribute in graph.getAttributes() 
		 * 		Pair p = convertAttribute(name, val);
		 * 		VP = p.left()
		 * 		val = p.right()
		 * 		vizStyle.setDefaultValue( VP, val);	
		 */
		String colorScheme = graph.getAttributes().get("colorscheme");
		for (Entry<String, String> attrEntry : graph.getAttributes().entrySet()) {
			String attrKey = attrEntry.getKey();
			String attrVal = attrEntry.getValue();
			if (attrKey.equals("bgcolor")) {
				setColor(attrVal, vizStyle, ColorAttribute.BGCOLOR, colorScheme);
			}
		}
	}

	@Override
	protected void setStyle(String attrVal, VisualStyle vizStyle) {
		//Network doesn't have properties set with style attribute
	}

	@Override
	protected void setStyle(String attrVal,
			View<? extends CyIdentifiable> elementView) {
		//Network doesn't have properties set with style attribute
	}

	@Override
	protected void setColor(String attrVal, VisualStyle vizStyle,
			ColorAttribute attr, String colorScheme) {
		Color color = convertColor(attrVal, colorScheme);
		switch (attr) {
			case BGCOLOR: {
				vizStyle.setDefaultValue(NETWORK_BACKGROUND_PAINT, color);
				break;
			}
			default: {
				break;
			}
		}
	}

	@Override
	protected void setColor(String attrVal,
			View<? extends CyIdentifiable> elementView, ColorAttribute attr, String colorScheme) {
		//Network doesn't set Background color with bypass
	}

	@Override
	public void setProperties(){
		LOGGER.info("NetworkReader: Setting properties for VisualStyle...");
		super.setProperties();
	}
}
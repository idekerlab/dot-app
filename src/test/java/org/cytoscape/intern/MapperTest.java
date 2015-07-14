package org.cytoscape.intern;

import static org.junit.Assert.*;

import org.junit.Test;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.intern.mapper.Mapper;
import org.cytoscape.intern.mapper.NetworkPropertyMapper;
import org.cytoscape.intern.mapper.NodePropertyMapper;
import org.cytoscape.intern.mapper.EdgePropertyMapper;

import java.awt.Color;
public class MapperTest {

	//@Test
	public void testNodeGetElementString() {
		NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		CyNode node = network.addNode();
		network.getRow(node).set(CyNetwork.NAME, "TestNode1");
		View<CyNode> nodeView = new TestNodeView(node);
		String label = "Hello World!";
		String tooltip = "Hello!";
		Double height = nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
		Double width = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
		Double bwidth = nodeView.getVisualProperty(BasicVisualLexicon.NODE_BORDER_WIDTH);

		String labelString = null;
		String tooltipString = null;
		String colorString = null;
		String fillColorString = null;
		String expectedDotString = null;
		String actualDotString = null;
		String heightString = String.format("height = \"%f\"", height);
		String widthString = String.format("width = \"%f\"", width);
		String bwidthString = String.format("penwidth = \"%f\"", bwidth);

		/**
		 * Case 1: Translating a simple Cytoscape property to .dot string
		 */
		
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL, label);
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_BORDER_PAINT, new Color(0xFF, 0x00, 0x00));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY, new Integer(0xFF));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_TOOLTIP, tooltip);
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, new Double(0));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, new Double(0));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, new Color(0x00, 0xDD, 0x99));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY, new Integer(0xFF));
		
		labelString = String.format("label = \"%s\"", label);
		tooltipString = String.format("tooltip = \"%s\"", tooltip);
		colorString = "color = \"#FF0000FF\"";
		fillColorString = "fillcolor = \"#00DD99FF\"";
		expectedDotString = String.format("[%s,%s,%s,%s,%s,%s,%s,shape = \"ellipse\","
				+ "style = \"solid,filled\",pos = \"%f,%f\",fixedsize = true]",
				labelString, bwidthString, heightString, widthString, tooltipString, colorString, fillColorString,
				new Double(0), new Double(0)); 

		Mapper mapper = new NodePropertyMapper(nodeView);
		actualDotString = mapper.getElementString();

		assertEquals("Node Cytoscape property translation failed. CASE 1", expectedDotString, actualDotString);
		
		/**
		 * Case 2: Writing the attribute string of a node with a set shape
		 */
		
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.TRIANGLE);

		String shapeString = "shape = \"triangle\"";
		expectedDotString = String.format("[%s,%s,%s,%s,%s,%s,%s,%s,"
				+ "style = \"solid,filled\",pos = \"%f,%f\",fixedsize = true]",
				labelString, bwidthString, heightString, widthString, tooltipString, colorString, fillColorString,
				shapeString, new Double(0), new Double(0)); 

		mapper = new NodePropertyMapper(nodeView);
		actualDotString = mapper.getElementString();
		assertEquals("Node Cytoscape property translation failed. CASE 2", expectedDotString, actualDotString);
		
		/**
		 * Case 3: Writing the attribute string of a node that sets the style attribute with one value
		 */
		/*
		 * nodeView.setVisualProperty(BasicVisualLexicon.NODE_BORDER_LINE_TYPE, LineTypeVisualProperty.LONG_DASH);
		 * String styleString = "style = \"filled,dash\"";
		 * expectedDotString = String.format("[%s,%s,%s,%s,fixedsize = true]", labelString, colorString, shapeString, styleString);
		 * actualDotString = mapper.getElementString();
		 * assertEquals("Node Cytoscape property translation failed. CASE 3", expectedDotString, actualDotString);
		 */

		/**
		 * Case 4: Writing the attribute string of a node that sets the style attribute with multiple
		 * values
		 */
		/*
		 * nodeView.setVisualProperty(BasicVisualLexicon.NODE_BORDER_LINE_TYPE, LineTypeVisualProperty.LONG_DASH);
		 * nodeView.setVisualProperty(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ROUNDED_RECTANGLE);
		 * String styleString = "style = \"filled,dash,rounded\"";
		 * expectedDotString = String.format("[%s,%s,%s,%s,fixedsize = true]", labelString, colorString, shapeString, styleString);
		 * actualDotString = mapper.getElementString();
		 * assertEquals("Node Cytoscape property translation failed. CASE 4", expectedDotString, actualDotString);
		 */

	}
	
	//@Test --I commented so I can debug how nothing is being written to file
	public void testEdgeGetElementString() {
		NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		CyNode node = network.addNode();
		CyNode node2 = network.addNode();
		CyEdge edge = network.addEdge(node, node2, true);
		network.getRow(node).set(CyNetwork.NAME, "TestNode1");
		network.getRow(node2).set(CyNetwork.NAME, "TestNode2");
		network.getRow(edge).set(CyNetwork.NAME, "TestEdge1");
		View<CyEdge> edgeView = new TestEdgeView(edge);
		String label = "Hello World!";

		String labelString = null;
		String colorString = null;
		String expectedDotString = null;
		String actualDotString = null;

		/**
		 * Case 1: Writing the attribute string of an edge with only simple properties set
		 */
		
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_LABEL, label);
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, new Color(0x33, 0x33, 0x33));
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_TRANSPARENCY, new Integer(0xFF));
		labelString = String.format("label = \"%s\"", label);
		colorString = "color = \"#333333FF\"";
		expectedDotString = String.format("[%s,%s,arrowtail = \"null\",arrowhead = \"null\"]", labelString, colorString); 

		Mapper mapper = new EdgePropertyMapper(edgeView);
		actualDotString = mapper.getElementString();

		assertEquals("Edge Cytoscape property translation failed. CASE 1", expectedDotString, actualDotString);
		
		/**
		 * Case 2: Writing the attribute string of an edge with a set source arrow shape
		 */
		
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE, ArrowShapeVisualProperty.DIAMOND);

		String sourceShapeString = "arrowtail = \"diamond\",arrowhead = \"null\"";
		expectedDotString = String.format("[%s,%s,%s]", labelString, colorString, sourceShapeString);

		mapper = new EdgePropertyMapper(edgeView);
		actualDotString = mapper.getElementString();
		assertEquals("Edge Cytoscape property translation failed. CASE 2", expectedDotString, actualDotString);
		
		/**
		 * Case 3: Writing the attribute string of an edge that sets the style attribute with one value
		 */
		/*
		 * edgeView.setVisualProperty(BasicVisualLexicon.NODE_BORDER_LINE_TYPE, LineTypeVisualProperty.LONG_DASH);
		 * String styleString = "style = \"filled,dash\"";
		 * expectedDotString = String.format("[%s,%s,%s,%s,fixedsize = true]", labelString, colorString, shapeString, styleString);
		 * actualDotString = mapper.getElementString();
		 * assertEquals("Node Cytoscape property translation failed. CASE 3", expectedDotString, actualDotString);
		 */

		/**
		 * Case 4: Writing the attribute string of an edge that sets the style attribute with multiple
		 * values
		 */
		/*
		 * edgeView.setVisualProperty(BasicVisualLexicon.NODE_BORDER_LINE_TYPE, LineTypeVisualProperty.LONG_DASH);
		 * edgeView.setVisualProperty(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ROUNDED_RECTANGLE);
		 * String styleString = "style = \"filled,dash,rounded\"";
		 * expectedDotString = String.format("[%s,%s,%s,%s,fixedsize = true]", labelString, colorString, shapeString, styleString);
		 * actualDotString = mapper.getElementString();
		 * assertEquals("Node Cytoscape property translation failed. CASE 4", expectedDotString, actualDotString);
		 */
		
	}
}
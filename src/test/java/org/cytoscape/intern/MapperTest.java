package org.cytoscape.intern;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.Font;

import org.cytoscape.intern.write.mapper.EdgePropertyMapper;
import org.cytoscape.intern.write.mapper.Mapper;
import org.cytoscape.intern.write.mapper.NetworkPropertyMapper;
import org.cytoscape.intern.write.mapper.NodePropertyMapper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.junit.Test;

public class MapperTest {

	@Test
	public void testNodeGetElementString() {
		NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		CyNode node = network.addNode();
		TestVisualStyle vizStyle = new TestVisualStyle();
		network.getRow(node).set(CyNetwork.NAME, "\"Test\"Node1\"\"");
		CyNetworkView networkView = new TestNetworkView(network);
		View<CyNode> nodeView = networkView.getNodeView(node);
		String label = "\"Hello World!\"";
		String tooltip = "Hello!";

		String escLabel = label.replace("\"", "\\\"");
		String labelString = String.format("label = \"%s\"", escLabel);
		String tooltipString = String.format("tooltip = \"%s\"", tooltip);
		String fillColorString = "fillcolor = \"#95DDEEFF\"";
		String expectedDotString = null;
		String actualDotString = null;
		String fontString = String.format("fontname = \"%s\"", new Font(Font.DIALOG, Font.PLAIN, 12).getFontName());
		String fontSizeString = "fontsize = \"21\"";
		String fontColor = "fontcolor = \"#FFFF00FF\"";

		
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL, label);
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_TOOLTIP, tooltip);
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, new Double(0));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, new Double(0));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, new Color(0x95, 0xDD, 0xEE));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_FACE, new Font(Font.DIALOG, Font.PLAIN, 21));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, new Integer(21));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL_COLOR, new Color(0xFF, 0xFF, 0x00));
		
		/* original, had to move pos around
		expectedDotString = String.format("[%s,%s,%s,"
				+ "pos = \"%f,%f\",%s,%s,%s]",
				labelString, tooltipString, fillColorString,
				new Double(0), new Double(0) * -1.0,fontString, fontSizeString, fontColor); 
		*/

		expectedDotString = String.format("[%s,%s,"
				+ "pos = \"%f,%f\",%s,%s,%s,%s]",
				labelString, tooltipString,
				new Double(0), new Double(0) * -1.0, fillColorString, fontString, fontSizeString, fontColor); 
		
		// todo
		Mapper mapper = new NodePropertyMapper(nodeView, vizStyle, "t");
		actualDotString = mapper.getElementString();

		assertEquals("Node Cytoscape property translation failed.", expectedDotString, actualDotString);
		
	}
	
	@Test 
	public void testEdgeGetElementString() {
		NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		CyNode node = network.addNode();
		CyNode node2 = network.addNode();
		CyEdge edge = network.addEdge(node, node2, true);
		TestVisualStyle vizStyle = new TestVisualStyle();
		network.getRow(node).set(CyNetwork.NAME, "TestNode1");
		network.getRow(node2).set(CyNetwork.NAME, "TestNode2");
		network.getRow(edge).set(CyNetwork.NAME, "TestEdge1");
		CyNetworkView networkView = new TestNetworkView(network);
		View<CyEdge> edgeView = networkView.getEdgeView(edge);
		String label = "Hello World!";
		String tooltip = "Hello!";
		Double width = new Double(25);

		String labelString = String.format("label = \"%s\"", label);
		String tooltipString = String.format("tooltip = \"%s\"", tooltip);
		String expectedDotString = null;
		String actualDotString = null;
		String widthString = String.format("penwidth = \"%f\"", width);
		String fontColor = "fontcolor = \"#FF00FFFF\"";
		String colorString = "color = \"#FFFF00FF\"";

		
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_LABEL, label);
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_WIDTH, width);
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_TOOLTIP, tooltip);
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, new Color(0xFF, 0xFF, 0x00));
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_LABEL_COLOR, new Color(0xFF, 0x00, 0xFF));
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_LINE_TYPE, LineTypeVisualProperty.EQUAL_DASH);
		labelString = String.format("label = \"%s\"", label);
		expectedDotString = String.format("[%s,%s,%s,%s,%s,style = \"dashed\"]", labelString, widthString,
				tooltipString, colorString,  fontColor);

		Mapper mapper = new EdgePropertyMapper(edgeView, vizStyle, networkView);
		actualDotString = mapper.getElementString();

		assertEquals("Edge Cytoscape property translation failed", expectedDotString, actualDotString);
		
	}
	
	@Test
	public void testNetworkGetElementString() {
		NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		CyNode node = network.addNode();
		CyNode node2 = network.addNode();
		TestVisualStyle vizStyle = new TestVisualStyle();
		CyEdge edge = network.addEdge(node, node2, true);
		network.getRow(node).set(CyNetwork.NAME, "TestNode1");
		network.getRow(node2).set(CyNetwork.NAME, "TestNode2");
		network.getRow(edge).set(CyNetwork.NAME, "TestEdge1");
		network.getRow(network).set(CyNetwork.NAME, "TestNetwork");
		CyNetworkView networkView = new TestNetworkView(network);
		View<CyEdge> edgeView = networkView.getEdgeView(edge);
		String label = "Hello World!";
		networkView.setVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, new Color(0xAA, 0x95, 0x00, 0xFF));
		networkView.setVisualProperty(BasicVisualLexicon.NETWORK_TITLE, label);
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.ARROW);
		String labelString = String.format("label = \"%s\"", label);
		String labelLocString = String.format("labelloc = %s", "b");
		String colorString = "bgcolor = \"#AA9500FF\"";
		String splinesString = "splines = \"false\"";
		String outputString = "outputorder = \"edgesfirst\"";
		String esepString = "esep = \"0\"";
		String marginString = "pad = \"2\"";
		String nodeDefaults = "node [label = \"\",penwidth = \"2.000000\",height = \"0.555556\",width = \"0.833333\",tooltip = \"\",color = \"#000000FF\",fillcolor = \"#C80000FF\",shape = \"ellipse\",style = \"solid,filled\",fontname = \"SansSerif.plain\",fontsize = \"12\",fontcolor = \"#000000FF\",fixedsize = \"true\",labelloc = \"c\"]";
		String edgeDefaults = "edge [label = \"\",penwidth = \"1.000000\",tooltip = \"\",arrowhead = \"none\",arrowtail = \"none\",color = \"#404040FF\",fontname = \"SansSerif.plain\",fontsize = \"10\",fontcolor = \"#000000FF\",style = \"solid\",dir = \"both\"]";
		String expectedDotString = String.format("digraph TestNetwork {\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n", 
				labelString, labelLocString, colorString, splinesString, outputString, esepString, marginString, nodeDefaults, edgeDefaults); 

		Mapper mapper = new NetworkPropertyMapper(networkView, NetworkPropertyMapper.isDirected(networkView), "false", "b" , "c", vizStyle);
		String actualDotString = mapper.getElementString();

		assertEquals("Network Properties and Visual Style translation failed", expectedDotString, actualDotString);
	}
	
	@Test
	public void testModifyElementID() {
		assertEquals("ModifyElementId is wrong", "TestNode1", Mapper.modifyElementId("TestNode1"));
		assertEquals("ModifyElementId is wrong", ".59", Mapper.modifyElementId(".59"));
		assertEquals("ModifyElementId is wrong", "\"9.-\"", Mapper.modifyElementId("9.-"));
		assertEquals("ModifyElementId is wrong", "8.8", Mapper.modifyElementId("8.8"));
		assertEquals("ModifyElementId is wrong", "\"Hello\"", Mapper.modifyElementId("\"Hello\""));
		assertEquals("ModifyElementId is wrong", "<Hello>", Mapper.modifyElementId("<Hello>"));
		assertEquals("ModifyElementId is wrong", "\"123baba\"", Mapper.modifyElementId("123baba"));
		assertEquals("ModifyElementId is wrong", "\"\\\"Hi\\\"Harry\\\"\\\"\"", Mapper.modifyElementId("\"Hi\"Harry\"\""));
		
	}
}
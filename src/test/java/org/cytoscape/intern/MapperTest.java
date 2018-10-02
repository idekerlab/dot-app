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

package org.cytoscape.intern;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.Font;
import java.util.HashSet;
import java.util.Set;

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
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
//import org.junit.Ignore;
import org.junit.Test;

public class MapperTest {

	//@Ignore
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
		Double width = Double.valueOf(25.0);

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
	
	//@Ignore()
	@Test
	public void testModifyElementID() {
		assertEquals("ModifyElementId is wrong", "TestNode1", Mapper.modifyElementID("TestNode1"));
		assertEquals("ModifyElementId is wrong", ".59", Mapper.modifyElementID(".59"));
		assertEquals("ModifyElementId is wrong", "\"9.-\"", Mapper.modifyElementID("9.-"));
		assertEquals("ModifyElementId is wrong", "8.8", Mapper.modifyElementID("8.8"));
		assertEquals("ModifyElementId is wrong", "\"Hello\"", Mapper.modifyElementID("\"Hello\""));
		assertEquals("ModifyElementId is wrong", "<Hello>", Mapper.modifyElementID("<Hello>"));
		assertEquals("ModifyElementId is wrong", "\"123baba\"", Mapper.modifyElementID("123baba"));
		assertEquals("ModifyElementId is wrong", "\"\\\"Hi\\\"Harry\\\"\\\"\"", Mapper.modifyElementID("\"Hi\"Harry\"\""));
		assertEquals("ModifyElementId is wrong", "\"\\\\\\\"b\\\\\\\"\"", Mapper.modifyElementID("\\\"b\\\""));
		System.out.println(String.valueOf(Character.valueOf('\u2014')));
		assertEquals("ModifyElementId is wrong", "\227", Mapper.modifyElementID("\227"));
		
	}
	
	//@Ignore
	@Test
	public void testNetworkGetElementString() {
		NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		CyNode node = network.addNode();
		CyNode node2 = network.addNode();
		TestVisualStyle vizStyle = new TestVisualStyle();
		vizStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, Double.valueOf(1.25));
		vizStyle.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, Double.valueOf(1.25));
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
		String nodeDefaults = "node [label = \"\",penwidth = \"1.250000\",height = \"0.555556\",width = \"0.833333\",tooltip = \"\",color = \"#000000FF\",fillcolor = \"#C80000FF\",shape = \"ellipse\",style = \"solid,filled\",fontname = \"SansSerif.plain\",fontsize = \"12\",fontcolor = \"#000000FF\",fixedsize = \"true\",labelloc = \"c\"]";
		String edgeDefaults = "edge [label = \"\",penwidth = \"1.250000\",tooltip = \"\",arrowhead = \"none\",arrowtail = \"none\",color = \"#404040FF\",fontname = \"SansSerif.plain\",fontsize = \"10\",fontcolor = \"#000000FF\",style = \"solid\",dir = \"both\"]";
		String expectedDotString = String.format("digraph TestNetwork {\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n", 
				labelString, labelLocString, colorString, splinesString, outputString, esepString, marginString, nodeDefaults, edgeDefaults); 

		Mapper mapper = new NetworkPropertyMapper(networkView, NetworkPropertyMapper.isDirected(networkView), "false", "b" , "c", vizStyle);
		String actualDotString = mapper.getElementString();

		assertEquals("Network Properties and Visual Style translation failed", expectedDotString, actualDotString);
	}
	
	//@Ignore()
	@Test
	public void testNodeGetElementString() {
		NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		CyNode node = network.addNode();
		TestVisualStyle vizStyle = new TestVisualStyle();
		BasicVisualLexicon bvl = new BasicVisualLexicon(new NullVisualProperty("root", "Root Property"));
		
		//Testing node with NODE_SIZE prop used instead of NODE_HEIGHT and NODE_WIDTH
		final Set<VisualProperty<Double>> nodeSizeVisualProperties = new HashSet<VisualProperty<Double>>();
		nodeSizeVisualProperties.add(BasicVisualLexicon.NODE_WIDTH);
		nodeSizeVisualProperties.add(BasicVisualLexicon.NODE_HEIGHT);

		VisualPropertyDependency<Double> vpDep = new VisualPropertyDependency<Double>(
			"nodeSizeLocked", "Lock node width and height", nodeSizeVisualProperties, bvl
		);
		vpDep.setDependency(false);
		vizStyle.addVisualPropertyDependency(vpDep);

		network.getRow(node).set(CyNetwork.NAME, "\"Test\"Node1\"\"");
		CyNetworkView networkView = new TestNetworkView(network);
		View<CyNode> nodeView = networkView.getNodeView(node);
		String label = "\"Hello World!\"";
		String tooltip = "Hello!";

		String escLabel = label.replace("\"", "\\\"");
		String labelString = String.format("label = \"%s\"", escLabel);
		String heightString = String.format("height = \"%f\"", 46.99/72.0);
		String widthString = String.format("width = \"%f\"", 35.55/72.0);
		String tooltipString = String.format("tooltip = \"%s\"", tooltip);
		String fillColorString = "fillcolor = \"#95DDEEFF\"";
		String expectedDotString = null;
		String actualDotString = null;
		String fontString = String.format("fontname = \"%s\"", new Font(Font.DIALOG, Font.PLAIN, 12).getFontName());
		String fontSizeString = "fontsize = \"21\"";
		String fontColor = "fontcolor = \"#FFFF00FF\"";
		String styleString = "style = \"solid,invis,filled\"";

		
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL, label);
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_TOOLTIP, tooltip);
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT, Double.valueOf(46.99));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_WIDTH, Double.valueOf(35.55));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, Double.valueOf(0));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, Double.valueOf(0));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, new Color(0x95, 0xDD, 0xEE));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_FACE, new Font(Font.DIALOG, Font.PLAIN, 21));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, Integer.valueOf(21));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_LABEL_COLOR, new Color(0xFF, 0xFF, 0x00));
		nodeView.setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, Boolean.FALSE);
		
		/* original, had to move pos around
		expectedDotString = String.format("[%s,%s,%s,"
				+ "pos = \"%f,%f\",%s,%s,%s]",
				labelString, tooltipString, fillColorString,
				Double.valueOf(0), Double.valueOf(0) * -1.0,fontString, fontSizeString, fontColor); 
		*/

		expectedDotString = String.format("[%s,%s,%s,%s,"
				+ "pos = \"%f,%f\",%s,%s,%s,%s,%s]",
				labelString, heightString, widthString, tooltipString,
				Double.valueOf(0), Double.valueOf(0) * -1.0, fillColorString, styleString, fontString, fontSizeString, fontColor); 
		
	
		Mapper mapper = new NodePropertyMapper(nodeView, vizStyle, "t");
		actualDotString = mapper.getElementString();

		assertEquals("Node Cytoscape property translation failed.", expectedDotString, actualDotString);
		
	}
}

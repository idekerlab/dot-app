package org.cytoscape.intern;

import static org.junit.Assert.*;

import org.junit.Test;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;

import org.cytoscape.intern.mapper.*;

public class MapperTest {

	@Test
	public void testGetElementStringSimpleProps() {
		/**
		 * Case 1: Translating a simple Cytoscape property to .dot string
		 */
		NetworkTestSupport nts = new NetworkTestSupport();
		CyNetwork network = nts.getNetwork();
		CyNode node = network.addNode();
		String cyNodeName = network.getRow(node).get(CyNetwork.NAME, String.class);
		View<CyNode> nodeView = new TestNodeView(node);
		Mapper mapper = new NodePropertyMapper(nodeView);
		VisualProperty<String> labelProp = BasicVisualLexicon.NODE_LABEL;
		String label = "Hello World!";
		nodeView.setVisualProperty(labelProp, label);
		String expectedDotString = String.format("%s [%s]", cyNodeName, "label = \"Hello World!\""); 
		String actualDotString = mapper.getElementString();
		assertEquals("Cytoscape property translation failed.", expectedDotString, actualDotString);
	}
	
	@Test
	public void testGetElementStringDiscreteProps() {
		/**
		 * Case 2: Translating a discrete Cytoscape property to .dot string
		 */
		final NetworkTestSupport nts = new NetworkTestSupport();
		final CyNetwork network = nts.getNetwork();
		final CyNode node = network.addNode();
		String cyNodeName = network.getRow(node).get(CyNetwork.NAME, String.class);
		final TestNodeView nodeView = new TestNodeView(node);
		final Mapper mapper = new NodePropertyMapper(nodeView);
		final VisualProperty<NodeShape> shapeProp = BasicVisualLexicon.NODE_SHAPE;
		final NodeShape shape = NodeShapeVisualProperty.TRIANGLE;
		nodeView.setVisualProperty(shapeProp, shape);
		final String expectedDotString = String.format("%s [%s]", cyNodeName, "shape = \"ellipse\""); 
		final String actualDotString = mapper.getElementString();
		assertEquals("Cytoscape property translation failed.", expectedDotString, actualDotString);
	}

}

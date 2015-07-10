package org.cytoscape.intern;

import static org.junit.Assert.*;
import org.junit.Test;

import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.model.CyNode;

import org.cytoscape.intern.mapper.Mapper;
import org.cytoscape.intern.mapper.NetworkPropertyMapper;
import org.cytoscape.intern.mapper.NodePropertyMapper;
import org.cytoscape.intern.mapper.EdgePropertyMapper;

public class MapperTest {

	@Test
	public void testSimpleMapVisToDot() {
		/**
		 * Case 1: Translating a simple Cytoscape property to .dot string
		 */
		Mapper mapper = new NodePropertyMapper();
		VisualProperty<String> labelProp = BasicVisualLexicon.NODE_LABEL;
		String label = "Hello World!";
		String expectedDotString = "label=\"Hello World!\"";
		String actualDotString = mapper.mapVisToDot(labelProp, label);
		assertEquals("Cytoscape property translation failed.", expectedDotString, actualDotString);
	}
	
	@Test
	public void testDiscreteMapVistoDot() {
		/**
		 * Case 2: Translating a discrete Cytoscape property to .dot string
		 */
		Mapper mapper = new NodePropertyMapper(new View() );
		VisualProperty<NodeShape> nodeShapeProp = BasicVisualLexicon.NODE_SHAPE;
		NodeShape nodeShape = NodeShapeVisualProperty.RECTANGLE;
		String expectedDotString ="shape=\"rectangle\"";
		String actualDotString = mapper.mapVisToDot(nodeShapeProp, nodeShape);
		assertEquals("Cytoscape property translation failed.", expectedDotString, actualDotString);
	}

}

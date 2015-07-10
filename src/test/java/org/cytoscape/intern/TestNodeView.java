package org.cytoscape.intern;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;

import java.util.HashMap;

public class TestNodeView implements View<CyNode> {
	private HashMap<VisualProperty<Object>, Object> visualProperties;
	private CyNode model;
	
	@SuppressWarnings("unchecked")
	public TestNodeView(CyNode node) {
		model = node;
		visualProperties = new HashMap<VisualProperty<Object>, Object>();
		BasicVisualLexicon bvl = new BasicVisualLexicon(new NullVisualProperty("ROOT", "Root"));
		for (VisualProperty<?> prop: bvl.getAllDescendants(BasicVisualLexicon.NODE)) {
			visualProperties.put((VisualProperty<Object>) prop, prop.getDefault());
		}
	}
	@Override
	public Long getSUID() {
		return null;
	}

	@Override
	public void clearValueLock(VisualProperty<?> arg0) {
	}

	@Override
	public void clearVisualProperties() {
	}

	@Override
	public CyNode getModel() {
		return model;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getVisualProperty(VisualProperty<T> arg0) {
		T returnVal = (T)visualProperties.get(arg0);
		return returnVal;
	}

	@Override
	public boolean isDirectlyLocked(VisualProperty<?> arg0) {
		return false;
	}

	@Override
	public boolean isSet(VisualProperty<?> arg0) {
		return false;
	}

	@Override
	public boolean isValueLocked(VisualProperty<?> arg0) {
		return false;
	}

	@Override
	public <T, V extends T> void setLockedValue(
			VisualProperty<? extends T> arg0, V arg1) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, V extends T> void setVisualProperty(
			VisualProperty<? extends T> arg0, V arg1) {
		visualProperties.put((VisualProperty<Object>) arg0, arg1);
	}

}

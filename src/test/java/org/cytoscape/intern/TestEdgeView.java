package org.cytoscape.intern;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;

import java.util.HashMap;

public class TestEdgeView implements View<CyEdge> {
	private HashMap<VisualProperty<Object>, Object> visualProperties;
	private CyEdge model;
	private Long SUID;
	
	@SuppressWarnings("unchecked")
	public TestEdgeView(CyEdge edge) {
		model = edge;
		SUID = model.getSUID();
		visualProperties = new HashMap<VisualProperty<Object>, Object>();
		BasicVisualLexicon bvl = new BasicVisualLexicon(new NullVisualProperty("ROOT", "Root"));
		for (VisualProperty<?> prop: bvl.getAllDescendants(BasicVisualLexicon.EDGE)) {
			visualProperties.put((VisualProperty<Object>) prop, prop.getDefault());
		}
	}
	@Override
	public Long getSUID() {
		return SUID;
	}

	@Override
	public void clearValueLock(VisualProperty<?> arg0) {
	}

	@Override
	public void clearVisualProperties() {
	}

	@Override
	public CyEdge getModel() {
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
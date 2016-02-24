package org.cytoscape.intern;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;

public class TestVisualStyle implements VisualStyle {

	HashMap<Object, Object> vizPropDefaults;
	HashSet<VisualPropertyDependency<?>> vizDependencies;
	
	public TestVisualStyle() {
		BasicVisualLexicon bvl = new BasicVisualLexicon(new NullVisualProperty("root", "Root Property"));
		Collection<VisualProperty<?>> collection = bvl.getAllDescendants(bvl.getRootVisualProperty());
		vizPropDefaults = new HashMap<Object, Object>(collection.size());
		for (VisualProperty<?> vizProp : collection) {
			vizPropDefaults.put(vizProp, vizProp.getDefault());
		}
		vizDependencies = new HashSet<VisualPropertyDependency<?>>();
	}
	@Override
	public void addVisualMappingFunction(VisualMappingFunction<?, ?> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addVisualPropertyDependency(VisualPropertyDependency<?> arg0) {
		vizDependencies.add(arg0);
	}

	@Override
	public void apply(CyNetworkView arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void apply(CyRow arg0, View<? extends CyIdentifiable> arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<VisualMappingFunction<?, ?>> getAllVisualMappingFunctions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<VisualPropertyDependency<?>> getAllVisualPropertyDependencies() {
		return vizDependencies;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> V getDefaultValue(VisualProperty<V> arg0) {
		// TODO Auto-generated method stub
		return (V)vizPropDefaults.get(arg0);
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <V> VisualMappingFunction<?, V> getVisualMappingFunction(
			VisualProperty<V> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeVisualMappingFunction(VisualProperty<?> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeVisualPropertyDependency(VisualPropertyDependency<?> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public <V, S extends V> void setDefaultValue(VisualProperty<V> arg0, S arg1) {
		vizPropDefaults.put(arg0, arg1);
	}

	@Override
	public void setTitle(String arg0) {
		// TODO Auto-generated method stub

	}

}
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

	}

	@Override
	public void addVisualPropertyDependency(VisualPropertyDependency<?> arg0) {
		vizDependencies.add(arg0);
	}

	@Override
	public void apply(CyNetworkView arg0) {

	}

	@Override
	public void apply(CyRow arg0, View<? extends CyIdentifiable> arg1) {

	}

	@Override
	public Collection<VisualMappingFunction<?, ?>> getAllVisualMappingFunctions() {
		return null;
	}

	@Override
	public Set<VisualPropertyDependency<?>> getAllVisualPropertyDependencies() {
		return vizDependencies;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> V getDefaultValue(VisualProperty<V> arg0) {
		return (V)vizPropDefaults.get(arg0);
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public <V> VisualMappingFunction<?, V> getVisualMappingFunction(
			VisualProperty<V> arg0) {
		return null;
	}

	@Override
	public void removeVisualMappingFunction(VisualProperty<?> arg0) {

	}

	@Override
	public void removeVisualPropertyDependency(VisualPropertyDependency<?> arg0) {

	}

	@Override
	public <V, S extends V> void setDefaultValue(VisualProperty<V> arg0, S arg1) {
		vizPropDefaults.put(arg0, arg1);
	}

	@Override
	public void setTitle(String arg0) {

	}

}
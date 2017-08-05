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

import java.util.HashMap;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NullVisualProperty;

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
	public void clearValueLock(VisualProperty<?> arg0) {
	}

	@Override
	public void clearVisualProperties() {
	}

	@Override
	public CyEdge getModel() {
		return model;
	}

	@Override
	public Long getSUID() {
		return SUID;
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
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

import java.util.Map;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

/**
 * Service Listener used to retrieve the Cytoscape Gradient factories
 * @author bfitts
 *
 */
public class GradientListener {
	private static final String LINEAR_FACTORY_ID = "org.cytoscape.LinearGradient";
	private static final String RADIAL_FACTORY_ID = "org.cytoscape.RadialGradient";
	private CyCustomGraphics2Factory<?> linearFactory;
	private CyCustomGraphics2Factory<?> radialFactory;
	        
	public void addCustomGraphicsFactory(CyCustomGraphics2Factory<?> factory, Map<Object,Object> serviceProps) {
		if(LINEAR_FACTORY_ID.equals(factory.getId())) {
			this.linearFactory = factory;
		}
		else if (RADIAL_FACTORY_ID.equals(factory.getId())) {
			this.radialFactory = factory;
		}
	}
	        
	public CyCustomGraphics2Factory<?> getLinearFactory() {
		return linearFactory;
	}
	        
	public CyCustomGraphics2Factory<?> getRadialFactory() {
		return radialFactory;
	}
	public void removeCustomGraphicsFactory(CyCustomGraphics2Factory<?> factory, Map<Object,Object> serviceProps) {
		if (linearFactory != null && linearFactory.equals(factory)) {
	        this.linearFactory = null;
		}
		else if (radialFactory != null && radialFactory.equals(factory)) {
			this.radialFactory = null;
		}
	}
}

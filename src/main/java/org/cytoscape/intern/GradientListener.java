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

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

import java.util.Properties;

import org.cytoscape.intern.read.DotReaderFactory;
import org.cytoscape.intern.write.DotWriterFactory;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.ServiceProperties;
import org.osgi.framework.BundleContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs the program-- fetches all needed services
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class DotActivator extends AbstractCyActivator {

	// Logger that outputs to Cytoscape standard log file:  .../CytoscapeConfiguration/3/framework-cytoscape.log
	private static final Logger LOGGER = LoggerFactory.getLogger(DotActivator.class);
	
	/**
	 * Method that runs when class is activated-- will start dot app
	 * 
	 * @param context the OSGi context in which this class is activated
	 */
	@Override
	public void start(BundleContext context) {
		
		//Create the GraphViz file filter
		String[] extensions = {"dot", "gv"};
		String[] contentTypes = {"text/plain"};
		DataCategory category = DataCategory.NETWORK;
		StreamUtil streamUtil = getService(context, StreamUtil.class);
		BasicCyFileFilter fileFilter = new BasicCyFileFilter(extensions, contentTypes, "GraphViz files", category, streamUtil);
				 
		// get necessary services for factories
		RenderingEngineManager rendEngMgr = getService(context, RenderingEngineManager.class);
		CyNetworkViewFactory netViewFact = getService(context, CyNetworkViewFactory.class);
		CyNetworkFactory netFact = getService(context, CyNetworkFactory.class);
		CyNetworkManager netMgr = getService(context, CyNetworkManager.class);
		CyRootNetworkManager rootNetMgr = getService(context, CyRootNetworkManager.class);
		VisualMappingManager vizMapMgr = getService(context, VisualMappingManager.class);
		VisualStyleFactory vizStyleFact = getService(context, VisualStyleFactory.class);
		
		// create properties for TaskFactories
		Properties dotWriterFactProps = new Properties();
		Properties dotReaderFactProps = new Properties();
		dotWriterFactProps.put(ServiceProperties.ID, "dotWriterFactory");
		dotReaderFactProps.put(ServiceProperties.ID, "dotReaderFactory");

		// initialize the GradientListener for later use
		LOGGER.info("Constructing Gradient Listener...");
		GradientListener gradientListener = new GradientListener();
		
		// initialize the DotWriterFactory for later use
		LOGGER.info("Constructing Writer Factory...");
		DotWriterFactory dotWriteFact = new DotWriterFactory(fileFilter, vizMapMgr);
		
		// initialize the DotReaderFactory for later use
		LOGGER.info("Constructing Reader Factory...");
		DotReaderFactory dotReadFact = new DotReaderFactory(fileFilter, netViewFact,
				netFact, netMgr, rootNetMgr, vizMapMgr, vizStyleFact, gradientListener, rendEngMgr);
		
		
		LOGGER.info("Registering Writer Factory as OSGI service...");
		//register DotWriterFactory as an OSGI service
		registerAllServices(context, dotWriteFact, dotWriterFactProps);
		
		LOGGER.info("Registering Reader Factory as OSGI service...");
		//register DotReaderFactory as an OSGI service
		registerAllServices(context, dotReadFact, dotReaderFactProps);
		
		LOGGER.info("Registering GradientListener as OSGI service listener...");
		registerServiceListener(context, gradientListener, "addCustomGraphicsFactory", "removeCustomGraphicsFactory", CyCustomGraphics2Factory.class);

	}
}
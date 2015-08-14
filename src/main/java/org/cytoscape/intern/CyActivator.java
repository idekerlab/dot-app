package org.cytoscape.intern;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.ServiceProperties;
import org.osgi.framework.BundleContext;

/**
 * Runs the program-- fetches all needed services
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class CyActivator extends AbstractCyActivator {
	
	private static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.CyActivator");
	private static final FileHandlerManager FILE_HANDLER_MGR = FileHandlerManager.getManager();
	
	/**
	 * Method that runs when class is activated-- will start dot app
	 * 
	 * @param context the OSGi context in which this class is activated
	 */
	@Override
	public void start(BundleContext context) {
		
		// Make logger write to file
		FileHandler handler = null;
		try {
			handler = new FileHandler("log_CyActivator.txt");
			handler.setLevel(Level.ALL);
			
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		LOGGER.addHandler(handler);
		FILE_HANDLER_MGR.registerFileHandler(handler);
     
		//Create the GraphViz file filter
		String[] extensions = {"dot", "gv"};
		String[] contentTypes = {"text/plain"};
		DataCategory category = DataCategory.NETWORK;
		StreamUtil streamUtil = getService(context, StreamUtil.class);
		BasicCyFileFilter fileFilter = new BasicCyFileFilter(extensions, contentTypes, "GraphViz files", category, streamUtil);
				 
		// get necessary services for factories
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

		
		// initialize the DotWriterFactory for later use
		LOGGER.info("Constructing Writer Factory...");
		DotWriterFactory dotWriteFact = new DotWriterFactory(fileFilter);
		
		// initialize the DotReaderFactory for later use
		LOGGER.info("Constructing Reader Factory...");
		DotReaderFactory dotReadFact = new DotReaderFactory(fileFilter, netViewFact,
				netFact, netMgr, rootNetMgr, vizMapMgr, vizStyleFact);
		
		LOGGER.info("Registering Writer Factory as OSGI service...");
		//register DotWriterFactory as an OSGI service
		registerAllServices(context, dotWriteFact, dotWriterFactProps);
		
		LOGGER.info("Registering Reader Factory as OSGI service...");
		//register DotReaderFactory as an OSGI service
		registerAllServices(context, dotReadFact, dotReaderFactProps);

	}
	
	@Override
	public void shutDown() {
		FILE_HANDLER_MGR.closeAllFileHandlers();
		super.shutDown();
		
	}
}
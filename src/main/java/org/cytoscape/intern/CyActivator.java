package org.cytoscape.intern;

import org.cytoscape.intern.write.DotWriterFactory;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.io.IOException;

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
     
		// initialize two hashsets
		HashSet<String> extensions = new HashSet<String>();
		HashSet<String> contentTypes = new HashSet<String>();
		
		// captures the types of data the cytoscape.io package can read and write
		DataCategory category = DataCategory.NETWORK;
			
		// register the service of supporting InputStreams and URL connections over the network
		StreamUtil streamUtil = getService(context, StreamUtil.class);
		 
		// add .dot and .gv, which have the same meaning, to the export menu
		extensions.add("dot");
		extensions.add("gv");
		contentTypes.add("text/plain");
				 
		// initialize (Basic)CyFileFilter, which handles the file type
		BasicCyFileFilter fileFilter = new BasicCyFileFilter(extensions, contentTypes, "GraphViz files", category, streamUtil);
		
		// initialize the DotWriterFactory for later use
		DotWriterFactory dotFac = new DotWriterFactory(fileFilter);
		LOGGER.info("Writer factory constructed");
		
		//registerService from CyNetworkViewWriterFactory interface
		registerService(context, dotFac, CyNetworkViewWriterFactory.class, new Properties());

		/**
		 * getService for:
		 * CyNetworkFactory
		 * CyNetworkViewFactory
		 * VisualStyleFactory
		 * 
		 * create a VisualStyle and pass into DotReaderFactory
		 * Pass CyNetworkFactory and CyNetworkViewFactory in
		 * because we do not want to create a network until we know user is reading.
		 * And we need a network to create a networkView
		 */
		
	}
	
	@Override
	public void shutDown() {
		FILE_HANDLER_MGR.closeAllFileHandlers();
		super.shutDown();
		
	}
}
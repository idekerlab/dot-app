package org.cytoscape.intern;

import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.application.CyApplicationManager;
org.cytoscape.io.write
//import org.cytoscape.model.CyNetworkManager;

import java.util.Properties;

/**
 * Runs the program
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class CyActivator extends AbstractCyActivator{
	
	/**
	 * Method that runs when class is activated-- will start dot app
	 * 
	 * @param context the OSGi context in which this class is activated
	 */
	@Override
	public void start(BundleContext context) {
		
		/** 
		 * pseudocode
		 * 
		 * CyApplicationManager cyAppMgr = getService(context, CyApplicationManager.class)
		 * CyFileFilter fileFilter = getService(context, CyFileFilter.class)
		 * DotWriterFactory dotFac = new DotWriterFactory(cyAppMgr, cyFileFilter);
		 * 
		 * Not sure if we need this below because of all the fileFilter stuff
		 * 
		 * Properties menuProperties = new Properties();
		 * props.setProperty("preferredMenu","Apps.dot");
		 * props.setProperty("title","Export network to dot");
		 * 
		 * B - Have to register it as a CyNetworkVieWWriterFactory so the File -> Export works
		 * registerService(context, dotFac, CyNetworkViewWriterFactory.class, props);
		 */
		

	}

}

package org.cytoscape.intern;

import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

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
		 * Properties menuProperties = new Properties();
		 * props.setProperty("preferredMenu","Apps.dot");
		 * props.setProperty("title","Export network to dot");
		 * 
		 * registerService(context, dotFac, TaskFactory.class, props);
		 */
		

	}

}

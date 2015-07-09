package org.cytoscape.intern;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

import java.util.HashSet;
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
		 * HashSet<String> extensions = new HashSet();
		 * HashSet<String> contentTypes = new HashSet();
		 * DataCategory category = DataCategory.NETWORK;
		 * StreamUtil streamUtil = getService(context, StreamUtil.class);
		 * extensions.add(".dot");
		 * extensions.add(".gv");
		 * contentTypes.add("text/plain");
		 * 
		 * BasicCyFileFilter fileFilter = new BasicCyFileFilter(extensions, contentTypes, "GraphViz files", category, streamUtil);
		 * DotWriterFactory dotFac = new DotWriterFactory(cyFileFilter);
		 * 
		 * 
		 * registerService(context, dotFac, CyNetworkViewWriterFactory.class, new Properties());
		 */
		

	}

}

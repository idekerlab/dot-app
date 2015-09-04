package org.cytoscape.intern.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.SwingUtilities;

import org.cytoscape.intern.FileHandlerManager;
import org.cytoscape.intern.GradientListener;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskIterator;

/**
 * Allows the input stream to be set for reader task factories
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */

public class DotReaderFactory implements InputStreamTaskFactory, NetworkViewAddedListener {
    
	
	//debug logger declaration 
	private static final Logger LOGGER = Logger.getLogger("org.cytoscape.intern.read.DotReaderFactory");
	private static final FileHandlerManager FILE_HANDLER_MGR = FileHandlerManager.getManager();

	
	//Variable Declarations
	private CyFileFilter fileFilter;
	private CyNetworkViewFactory netViewFact;
	private CyNetworkFactory netFact;
	private CyNetworkManager netMgr;
	private CyRootNetworkManager rootNetMgr;
	private VisualMappingManager vizMapMgr;
	private VisualStyleFactory vizStyleFact;
	private GradientListener gradientListener;
	private RenderingEngineManager rendEngMgr;
	
	
	/**
	 * Sets the DotReaderFactory with associate fileFilter
	 * 
	 * @param fileFilter CyFileFilter associated with this factory
	 * @param netViewFact CyNetworkViewFactory needed for DotReaderTask
	 * @param netFact CyNetworkFactory needed for DotReaderTask
	 * @param netMgr CyNetworkManager needed for DotReaderTask
	 * @param rootNetMgr CyRootNetworkManager needed for DotReaderTask
	 * @param vizMapMgr VisualMappingManager needed for DotReaderTask
	 * @param vizStyleFact VisualStyleFactory needed for DotReaderTask
	 * @param gradientListener GradientListener needed for DotReaderTask
	 * @param rendEngMgr TODO
	 */
	public DotReaderFactory(CyFileFilter fileFilter, CyNetworkViewFactory netViewFact,
			CyNetworkFactory netFact, CyNetworkManager netMgr, CyRootNetworkManager rootNetMgr,
			VisualMappingManager vizMapMgr, VisualStyleFactory vizStyleFact, GradientListener gradientListener, RenderingEngineManager rendEngMgr) {

		// make logger write to file
		FileHandler handler = null;
		try {
			handler = new FileHandler("log_DotReaderFactory.txt");
			handler.setLevel(Level.ALL);
			handler.setFormatter(new SimpleFormatter());
		}
		catch(IOException e) {
			// to prevent compiler error
		}
		LOGGER.addHandler(handler);
		FILE_HANDLER_MGR.registerFileHandler(handler);
		
		this.fileFilter = fileFilter;
		this.netViewFact = netViewFact;
		this.netFact = netFact;
		this.netMgr = netMgr;
		this.rootNetMgr = rootNetMgr;
		this.vizMapMgr = vizMapMgr;
		this.vizStyleFact = vizStyleFact;
		this.gradientListener = gradientListener;
		this.rendEngMgr = rendEngMgr;

	}
	
	
	/**
	 * Returns CyFileFilter associated with this factory
	 * 
	 * @return CyFileFilter for this factory
	 */
	@Override
	public CyFileFilter getFileFilter() {
		return fileFilter;
	}

	
	/**
	 * Sets the input stream that will be read by the Reader created from this factory
	 * 
	 * @param inStream The InputStream to be read
	 * @param inputName The name of the input
	 * 
	 * @return TaskIterator created by calling DotReaderTask()
	 */
	@Override
	public TaskIterator createTaskIterator(InputStream inStream, String inputName) {
		LOGGER.info("create TaskIterator with params");
		
		return new TaskIterator(new DotReaderTask(inStream, netViewFact,
				netFact, netMgr, rootNetMgr, vizMapMgr, vizStyleFact, gradientListener, rendEngMgr));
	}

	
	/**
	 * Returns true if the factory is ready to produce a TaskIterator and false otherwise.
	 * 
	 * @param inStream The InputStream to be read
	 * @param inputName The name of the input
	 * 
	 * @return Boolean indicating the factory is ready to produce a TaskIterator
	 */
	@Override
	public boolean isReady(InputStream inStream, String inputName) {
		
		// check file extension
		if (inStream != null && inputName != null) {
			LOGGER.info("Valid input is found");
			
			String[] parts = inputName.split(".");
			String extension = parts[parts.length-1];
			if (extension.matches(("gv|dot"))) {
				
				LOGGER.info("gv|dot extention is matched");
				return true;
			}
		}
		
		return false;
	}


	private boolean isDotNetwork(CyNetwork network) {
		CyTable hidden = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		return hidden.getRow(network.getSUID()).get("DOT_network", Boolean.class);
	}

	/**
	 * Applies the VisualStyle created for the newly created CyNetworkView
	 * 
	 * @param arg0 event fired when a CyNetworkView is added to
	 * CyNetworkViewManager
	 */
	@Override
	public void handleEvent(NetworkViewAddedEvent arg0) {
		LOGGER.info("NetworkView was added. Apply VisualStyle if DOT network");
		final CyNetworkView networkView = arg0.getNetworkView();
		CyNetwork model = networkView.getModel();
		if (!isDotNetwork(model)) {
			return;
		}
		LOGGER.info("Network is a DOT network. Applying VisualStyle...");
		String name = model.getRow(model).get(CyNetwork.NAME, String.class);
		String vizStyleName = String.format("%s vizStyle", name);
		for (final VisualStyle vizStyle : vizMapMgr.getAllVisualStyles()) {
			if (vizStyle.getTitle().equals(vizStyleName)) {
				SwingUtilities.invokeLater( new Runnable()
				{
					@Override
					public void run() {
						vizMapMgr.setVisualStyle(vizStyle, networkView);
						vizStyle.apply(networkView);
						networkView.updateView();
					}
				});
			}
		}
		
	}

}
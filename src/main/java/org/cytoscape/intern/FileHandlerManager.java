package org.cytoscape.intern;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.FileHandler;

/**
 * Singleton class that handles the closing of FileHandlers used by
 * the Java Loggers.
 * @author bfitts
 *
 */
public class FileHandlerManager {

	private static final FileHandlerManager INSTANCE = new FileHandlerManager();
	public static FileHandlerManager getManager() {
		return INSTANCE;
	}
	
	private ArrayList<FileHandler> fileHandlers = null;

	private FileHandlerManager() {
		fileHandlers = new ArrayList<FileHandler>();
	}
	
	public void closeAllFileHandlers() {
		Iterator<FileHandler> fileHandlersIter = fileHandlers.iterator();
		while (fileHandlersIter.hasNext()) {
			FileHandler handler = fileHandlersIter.next();
			handler.close();
			fileHandlersIter.remove();
		}
	}

	public void closeFileHandler(FileHandler handler) {
		if (fileHandlers.contains(handler)) {
			handler.close();
			fileHandlers.remove(handler);
		}
	}

	public void registerFileHandler(FileHandler handler) {
		fileHandlers.add(handler);
	}
}

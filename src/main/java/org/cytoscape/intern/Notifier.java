package org.cytoscape.intern;

import java.awt.Dialog;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Utility class for notifying user with pop-up box
 * 
 * @author Massoud Maher
 * @author Braxton Fitts
 * @author Ziran Zhang
 */
public class Notifier {
	
	public enum MessageType {
		WARNING, ERROR, INFO;
	}
	
	/**
	 * Display a pop-up box of a certain type
	 * @param message message to be displayed in pop-up box
	 * @param type type of message to be displayed (warning, error, info)
	 */
	public static void showMessage(final String message, final MessageType type) {
		SwingUtilities.invokeLater(
				
			new Runnable() {
				
				@Override
				public void run() {
					JOptionPane prompt = new JOptionPane();
					prompt.setOptionType(JOptionPane.DEFAULT_OPTION);
					JDialog dialogWindow = prompt.createDialog(null);
					switch (type) {
						case WARNING: {
							prompt.setMessage(message);
							prompt.setMessageType(JOptionPane.WARNING_MESSAGE);
							dialogWindow.setTitle("Warning");
							break;
						}
						case ERROR: {
							prompt.setMessage(message);
							prompt.setMessageType(JOptionPane.ERROR_MESSAGE);
							dialogWindow.setTitle("Error");
							break;
						}
						case INFO:{
							prompt.setMessage(message);
							prompt.setMessageType(JOptionPane.INFORMATION_MESSAGE);
							dialogWindow.setTitle("Info");
							break;
						}
					}
					
					dialogWindow.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
					dialogWindow.setLocationRelativeTo(null);
					
					dialogWindow.add(prompt);
					dialogWindow.pack();
					dialogWindow.setAlwaysOnTop(true);
					dialogWindow.setVisible(true);
				}
		});
	}
}
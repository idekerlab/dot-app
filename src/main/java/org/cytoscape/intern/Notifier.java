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
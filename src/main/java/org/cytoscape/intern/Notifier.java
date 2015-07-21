package org.cytoscape.intern;

import java.awt.Dialog;

import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

public class Notifier {
	public static void showWarning(final String message) {
		SwingUtilities.invokeLater(
			new Runnable() {
				@Override
				public void run() {
					JOptionPane prompt = new JOptionPane(message, JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION);
					JDialog dialogWindow = prompt.createDialog("Warning");
					dialogWindow.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
					dialogWindow.setLocationRelativeTo(null);
					
					dialogWindow.add(prompt);
					dialogWindow.pack();
					dialogWindow.setAlwaysOnTop(true);
					dialogWindow.setVisible(true);
				}
		});
	}
	public static void showError(final String message) {
		SwingUtilities.invokeLater(
			new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
				}
		});
	}
}
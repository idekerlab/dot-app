package org.cytoscape.intern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Notifier {
	public static void showWarning(final String message) {
		SwingUtilities.invokeLater(
			new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, message, "Warning", JOptionPane.WARNING_MESSAGE);
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
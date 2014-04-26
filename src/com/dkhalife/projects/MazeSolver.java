package com.dkhalife.projects;

/**
 * 
 * @author Dany Khalife
 * @version 1.0
 * @since December 09, 2012
 * 
 */

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * 
 * This class is responsible for creating the MazeSolver UI The maze grid is
 * created using a couple of user inputs specifying the size of the maze
 * 
 * @author Dany Khalife
 * 
 */
public class MazeSolver {
	// The size of the maze
	private static int size = 0;

	/**
	 * 
	 * The maze grid needs to know the size of the maze to be created. The size
	 * of the grid is provided via 2 user inputs.
	 * 
	 */
	public static void main(String[] args) {
		try {
			// Read the maze size
			while (size < 10) {
				size = Integer.parseInt((String) JOptionPane.showInputDialog(null, "Enter the size of the maze (>=10):", "Maze Solver", JOptionPane.PLAIN_MESSAGE,
						null, null, ""));
			}

			// Create the window
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					createAndShowGUI();
				}
			});
		}
		catch (IndexOutOfBoundsException e) {
			// In case no argument was detected
			System.out.println("Please specify a width and a height for the maze using the command line.");
		}
	}

	/**
	 * 
	 * This method will create and show the window for the Maze Solver
	 * 
	 * @author Dany Khalife
	 * @param w The width of the maze
	 * @param h The height of the maze
	 * 
	 */
	private static void createAndShowGUI() {
		// First lets create the panel where the maze will be drawn
		Panel mp = new Panel(new Maze(size, size));

		// Next, we need a JFrame to hold the panel
		JFrame f = new JFrame("Maze Solver");

		// We don't want the window to be resizable, at least not for now
		f.setResizable(false);

		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Append the panel and show the GUI
		f.add(mp);
		f.pack();
		f.setVisible(true);
	}
}

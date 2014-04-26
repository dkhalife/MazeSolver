package com.dkhalife.projects;
/**
 * 
 * @author Dany Khalife
 * @version 1.0
 * @since December 09, 2012
 * 
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Line2D;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * This class represents the area where the maze will be drawn and where user
 * interactions will be processed
 * 
 * @author Dany Khalife, Thierry Lavoie
 * 
 */
class Panel extends JPanel {
	// Eclipse generated UID
	private static final long serialVersionUID = 2396636661038057893L;

	// Panel dimensions
	private int pHeight;
	private int pWidth;

	// Horizontal and vertical resolutions
	private static final int hres = 20;
	private static final int wres = 20;

	// The last mouse coordinates where a mouse event occured
	private Integer mouseX = null;
	private Integer mouseY = null;

	// The coords for the line to be drawn
	private Integer[] coords = new Integer[4];

	// Flag to tell when a delete operation is taking place
	private boolean erasing = false;

	// The actual maze
	private Maze maze;

	// Are we currently in the process of solving the maze (waiting for the user to choose the rooms)? 
	private boolean solving = false;

	// These are the first and last rooms chosen by the user
	private int firstRoom = -1;
	private int lastRoom = -1;

	/**
	 * 
	 * The panel is initialized with a reference to the Maze data model
	 * 
	 * @param m The maze to draw
	 * 
	 */
	public Panel(Maze m) {
		// Store bidirectional references
		maze = m;
		maze.panel = this;

		// Calculate both dimensions
		pWidth = maze.width * wres;
		pHeight = maze.height * hres;

		// Allow focus events
		setFocusable(true);

		// Draw a thick black border around the maze
		setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

		// Listen for keyboard events
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyChar()) {
				// C and R will reset the grid
					case 'c':
					case 'r':
						maze.reset();
						coords = new Integer[4];
					break;

					// G will generate a random maze
					case 'g':
						coords = new Integer[4];
						maze.reset();
						maze.generate();
					break;

					// Holding s will wait for two clicks, first room and last room, when clicked by the user, the maze will be solved
					case 's':
						// We only need to reset when we just started solving
						if (!solving) {
							firstRoom = -1;
							lastRoom = -1;

							solving = true;
						}
					break;

					// T will toggle the solving method (DFS or BFS)
					case 't':
						maze.toggle();
					break;
				}

				repaint();
			}

			public void keyReleased(KeyEvent e) {
				switch (e.getKeyChar()) {
				// Releasing the s will check inputs and solve the maze if both rooms were selected
					case 's':
						solving = false;
					break;
				}
			}
		});

		// Listen for mouse events
		addMouseListener(new MouseAdapter() {
			// Mouse down
			public void mousePressed(MouseEvent e) {
				// If we are not solving, then we are drawing
				if (!solving) {
					if (SwingUtilities.isLeftMouseButton(e)) {
						// If we are doing a left click, then its a draw operation
						erasing = false;
					}
					else if (SwingUtilities.isRightMouseButton(e)) {
						// If we are doing a right click, then its an erase
						// operation
						erasing = true;
					}
					else {
						return;
					}

					// Filter only single clicks, no double clicks allowed
					if (e.getClickCount() == 1) {
						// When a click occurs, the maze is partially reset, meaning
						// if it was previously solved,
						// the solution needs to be recalculated
						maze.setCustom(true);
						maze.setSolved(false);

						// Figure out the closest point on the grid where the user
						// clicked
						mouseX = snapToGrid(e.getX(), wres);
						mouseY = snapToGrid(e.getY(), hres);

						// Store the mouse coordinates
						coords[0] = mouseX; // Start X
						coords[1] = mouseY; // Start Y
						coords[2] = mouseX; // End X (Defaults to the same spot, to
											// detect if no movement occured)
						coords[3] = mouseY; // End Y (Defaults to the same spot, to
											// detect if no movement occured)

						// Clear the solution's path
						maze.path.clear();
					}
				}
				else {
					if (SwingUtilities.isLeftMouseButton(e)) {
						// We are waiting for the first room
						if (firstRoom == -1) { // 
							firstRoom = snapToRoom(e.getX(), e.getY());
						}
						else if (lastRoom == -1) { // We are waiting for the last room
							lastRoom = snapToRoom(e.getX(), e.getY());
							maze.solve(firstRoom, lastRoom);
							repaint();
						}
						else {
							firstRoom = snapToRoom(e.getX(), e.getY());
							lastRoom = -1;
						}
					}
				}
			}

			// Mouse Up
			public void mouseReleased(MouseEvent e) {
				// Forget about this click if the user is choosing the rooms
				if (solving) {
					return;
				}

				// If for some reason we did not get a starting point, lets skip this click
				if (mouseX == null || mouseY == null) {
					return;
				}

				// Figure out the closest point on the grid where the user
				// clicked
				int x = snapToGrid(e.getX(), wres);
				int y = snapToGrid(e.getY(), hres);

				// Force a single direction (either vertical or horizontal,
				// depending on the largest offset (dx or dy))
				int deltaX = Math.abs(x - mouseX);
				int deltaY = Math.abs(y - mouseY);
				// Replacing the target X or Y with the start X or Y forces the
				// direction
				if (deltaX > deltaY) {
					y = mouseY;
				}
				else {
					x = mouseX;
				}

				// If a movement occured
				if (mouseX != x || mouseY != y) {
					// Lets add the new walls one by one
					int j = (int) Math.floor(Math.min(x, mouseX) / wres);
					int i = (int) Math.floor(Math.min(y, mouseY) / hres);

					// Horizontal walls
					if (coords[1].equals(coords[3])) {
						// Start and end points for the wall(s)
						int current = Math.min(coords[0], coords[2]);
						int target = Math.max(coords[0], coords[2]);

						if (!erasing) {
							while (current != target) {
								// Calculate the target rooms for the wall
								int room1 = j + i * maze.height;
								int room2 = j + (i - 1) * maze.height;

								// Add the new wall
								maze.maze.add(new Wall(room1, room2));

								// Step horizontally
								current += wres;
								++j;
							}
						}
						else {
							while (current != target) {
								// Calculate the target rooms for the wall
								int room1 = j + i * maze.height;
								int room2 = j + (i - 1) * maze.height;

								// Remove the wall (if any)
								maze.maze.remove(new Wall(room1, room2));

								// Step horizontally
								current += wres;
								++j;
							}

							// Hide the dragged line
							coords[0] = coords[2] = 0;
						}
					}
					else { // Vertical walls
							// Start and end points for the wall(s)
						int current = Math.min(coords[1], coords[3]);
						int target = Math.max(coords[1], coords[3]);

						if (!erasing) {
							while (current != target) {
								// Calculate the target rooms for the wall
								int room1 = j + i * maze.height;
								int room2 = j - 1 + i * maze.height;

								// Add the new wall
								maze.maze.add(new Wall(room1, room2));

								// Step vertically
								current += hres;
								++i;
							}
						}
						else {
							while (current != target) {
								// Calculate the target rooms for the wall
								int room1 = j + i * maze.height;
								int room2 = j - 1 + i * maze.height;

								// Remove the wall (if any)
								maze.maze.remove(new Wall(room1, room2));

								// Step vertically
								current += hres;
								++i;
							}

							// Hide the dragged line
							coords[1] = coords[3] = 0;
						}
					}
				}

				// Reset the start coordinates
				mouseX = mouseY = null;

				repaint();
			}
		});

		// Listen to mouse motion events
		addMouseMotionListener(new MouseMotionAdapter() {
			// Mouse dragged
			public void mouseDragged(MouseEvent e) {
				// Forget about this drag if the user is choosing the rooms
				if (solving) {
					return;
				}

				// If, for some reason the mouse was dragged but we don't have
				// start coordinates, then stop
				if (mouseX == null || mouseY == null) {
					return;
				}

				// Lets snap our current coordinates to the grid
				int x = snapToGrid(e.getX(), wres);
				int y = snapToGrid(e.getY(), hres);

				// Find out for each direction how much we scrolled
				int deltaX = Math.abs(x - mouseX);
				int deltaY = Math.abs(y - mouseY);

				// Force only the drag in the direction that was dragged the
				// most
				if (deltaX > deltaY) {
					y = mouseY;
				}
				else {
					x = mouseX;
				}

				// If we did move (after snapping to grid)
				if (x != mouseX || y != mouseY) {
					// Store the current mouse coordinates
					coords[2] = x;
					coords[3] = y;

					repaint();
				}
			}
		});
	}

	/**
	 * 
	 * This method allows snapping coordinates to a room in the maze
	 * 
	 * @param x The x to snap to a room
	 * @param y The y to snap to a room
	 * 
	 * @return The room ID that the user clicked on
	 * 
	 */
	private int snapToRoom(int x, int y) {
		int i = x / wres;
		int j = y / hres;

		return i + j * maze.height;
	}

	/**
	 * 
	 * This method allows snapping coordinates to the maze grid
	 * 
	 * @param d A coordinate (could be an X, Y, Z ...)
	 * @param res The resolution for the coordinate direction (resX, resY,
	 *            resZ...)
	 * @return The closest coordinate to snap to according to the provided
	 *         resolution.
	 * 
	 */
	private int snapToGrid(int d, int res) {
		// Compute the first point that is farther than the current point (wrt
		// the origin)
		int snapDown = (int) (res * Math.round(d / res));
		// Compute the first point that is closer than the current point (wrt
		// the origin)
		int snapUp = snapDown + res;

		// Return the closest coordinate between the two
		if (d - snapDown < snapUp - d) {
			return snapDown;
		}
		else {
			return snapUp;
		}
	}

	/**
	 * 
	 * This method is responsible for returning the dimension to our JFrame so
	 * that the maze is fully drawn and not cropped
	 * 
	 * @return The width and height of our maze
	 * 
	 */
	public Dimension getPreferredSize() {
		return new Dimension(pWidth, pHeight);
	}

	/**
	 * This method actually prints the view every time a repaint is needed
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Lets start with a tiny, dashed grey stroke for the grid
		((Graphics2D) g).setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 0, new float[] { 3 }, 0));
		g.setColor(Color.GRAY);

		// Paint the vertical lines
		for (int i = wres; i < pWidth; i += wres) {
			((Graphics2D) g).draw(new Line2D.Double(i, 0, i, pHeight));
		}
		// Paint the horizontal lines
		for (int i = hres; i < pHeight; i += hres) {
			((Graphics2D) g).draw(new Line2D.Double(0, i, pWidth, i));
		}

		// Now, we'll need a thick black stroke to paint the walls
		((Graphics2D) g).setStroke(new BasicStroke(2));
		g.setColor(Color.BLACK);

		for (Wall w : maze.maze) {
			// Figure out if the wall blocks two consecutive rooms (diff = 1) or
			// one on top of the other
			int diff = w.room1 - w.room2;
			if (diff == 1) {
				int x1 = w.room2 % maze.width + 1;
				int y1 = w.room2 / maze.width;
				((Graphics2D) g).draw(new Line2D.Double(wres * x1, hres * y1, wres * x1, hres * y1 + hres));
			}
			else {
				int x1 = w.room1 % maze.width;
				int y1 = w.room1 / maze.width;
				((Graphics2D) g).draw(new Line2D.Double(wres * x1, hres * y1, wres * x1 + wres, hres * y1));
			}
		}

		// Now we need to paint the dragged line (Black for inserting, and red
		// for deleting)
		g.setColor(!erasing ? Color.BLACK : Color.RED);
		if (coords != null && coords[0] != coords[2] || coords[1] != coords[3]) {
			((Graphics2D) g).draw(new Line2D.Double(coords[0], coords[1], coords[2], coords[3]));
		}

		// And finally we'll need to paint the path if the maze is solved
		g.setColor(Color.BLUE);

		int x1 = -1;
		int y1 = -1;
		for (Integer i : maze.path) {
			int x2 = i % maze.width * hres + hres / 2;
			int y2 = i / maze.width * wres + wres / 2;

			if (x1 != -1)
				((Graphics2D) g).draw(new Line2D.Double(x1, y1, x2, y2));

			x1 = x2;
			y1 = y2;
		}

	}

	/**
	 * 
	 * A JavaScript-like alert box using Swing's corresponding function
	 * 
	 * @param msg The message to display
	 * 
	 */
	protected void alert(String msg) {
		JOptionPane.showMessageDialog(this, msg);
	}
}

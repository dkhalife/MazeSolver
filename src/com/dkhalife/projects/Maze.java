package com.dkhalife.projects;

/**
 * 
 * @author Dany Khalife
 * @version 1.0
 * @since December 09, 2012
 * 
 */

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

/**
 * This class implements a Disjoint Set using path compression and union by
 * depth
 * 
 * @author Dany Khalife
 * @version 1.0
 * @since April, 2012
 * 
 */
public class Maze {
	// We will need to know the walls and which rooms they block
	public Vector<Wall> maze;
	// We will also need a graph with the connexity of each room
	public Vector<Room> graph;
	// This vector will hold the path from the top-left-most room to the bottom-right-most room
	public Vector<Integer> path;

	// The size of the maze (in squares)
	public int height;
	public int width;

	// These are the indices of our first and last rooms
	private int firstRoom;
	private int lastRoom;

	// A random for maze generation
	private Random generator;

	// A disjoint set is used to check/keep the connexity between the rooms
	private DisjointSet ds;

	// Is the current maze solved / custom drawn / filled ?
	private boolean solved = false;
	private boolean custom = true;
	private boolean filled = false;

	// Which method are we using? (DFS = false, BFS = true)
	private boolean BFS = false;

	// We need to keep a reference to the containing window
	Panel panel = null;

	/**
	 * 
	 * A maze is constructed with a width and a height
	 * 
	 * @param w
	 * @param h
	 * 
	 */
	public Maze(int w, int h) {
		width = w;
		height = h;

		reset();
	}

	/**
	 * The maze can be cleared using this method
	 */
	public void reset() {
		// Reset all variables
		ds = new DisjointSet(width * height);
		path = new Vector<Integer>();
		graph = new Vector<Room>();
		maze = new Vector<Wall>();

		filled = false;
		solved = false;
	}

	/**
	 * Calling this method will generate a random maze
	 */
	public void generate() {
		// Fill the maze with some walls
		for (int i = 0; i < height; ++i) {
			for (int j = 0; j < width; ++j) {
				if (i > 0) {
					maze.add(new Wall(j + i * height, j + (i - 1) * height));
				}
				if (j > 0) {
					maze.add(new Wall(j + i * height, j - 1 + i * height));
				}
			}
		}

		// Create the graph by adding all the rooms
		for (int i = 0; i < height * width; ++i)
			graph.add(new Room(i));

		// Shuffle all the walls
		generator = new Random();
		for (int i = 0; i < maze.size(); ++i) {
			int rnd = generator.nextInt(maze.size());

			Wall tmp = maze.get(rnd);
			maze.set(rnd, maze.get(i));
			maze.set(i, tmp);
		}

		// Make sure the connexity propperty is achieved by removing all walls that block two disjoint rooms
		for (int i = maze.size() - 1; i >= 0; --i) {
			Wall w = maze.get(i);
			int r1 = w.room1;
			int r2 = w.room2;

			// If the rooms blocked by this wall are disjoint 
			if (ds.find(r1) != ds.find(r2)/* || ds.find(r1) == -1 */) {
				// Remove this wall
				maze.remove(w);
				// Add both rooms to the same set 
				ds.union(r1, r2);

				// Add a path from each room to the other 
				Room room1 = graph.get(r1);
				Room room2 = graph.get(r2);
				room1.paths.add(room2.id);
				room2.paths.add(room1.id);
			}
		}

		filled = true;
	}

	/**
	 * This method will solve the current maze. The maze will only be solved
	 * once unless it is modified.
	 */
	public void solve(int start, int end) {
		// Only solve once
		if (solved && start == firstRoom && end == lastRoom) {
			return;
		}

		// Set the new start and end
		firstRoom = start;
		lastRoom = end;

		// If it is a user input 
		if (custom) {
			// Reset our variables
			path.clear();
			ds = new DisjointSet(width * height);
			graph = new Vector<Room>();

			// Refill the graph with all the rooms
			for (int i = 0; i < height; ++i) {
				for (int j = 0; j < width; ++j) {
					graph.add(new Room(i * width + j));
				}
			}

			// Fill the graph with adjacent vertices
			for (int i = 0; i < height; ++i) {
				for (int j = 0; j < width; ++j) {
					int r1 = i * width + j;

					// Worst case, we have 4 adjacent rooms, lets examine each one if its available

					if (i > 0) {
						// We have a top
						// Can we find a wall blocking this room and the one to its top?
						int r2 = (i - 1) * width + j;
						if (!maze.contains(new Wall(r1, r2))) {
							ds.union(r1, r2);

							graph.get(r1).paths.add(r2);
							graph.get(r2).paths.add(r1);
						}
					}

					if (i < height - 1) {
						// We have a  bottom
						// Can we find a wall blocking this room and the one to its bottom?
						int r2 = (i + 1) * width + j;
						if (!maze.contains(new Wall(r1, r2))) {
							ds.union(r1, r2);

							graph.get(r1).paths.add(r2);
							graph.get(r2).paths.add(r1);
						}
					}

					if (j > 0) {
						// We have a left
						// Can we find a wall blocking this room and the one to its left
						int r2 = r1 - 1;
						if (!maze.contains(new Wall(r1, r2))) {
							ds.union(r1, r2);

							graph.get(r1).paths.add(r2);
							graph.get(r2).paths.add(r1);
						}
					}

					if (j < width - 1) {
						// We have a right
						// Can we find a wall blocking this room and the one to its right
						int r2 = r1 + 1;
						if (!maze.contains(new Wall(r1, r2))) {
							ds.union(r1, r2);

							graph.get(r1).paths.add(r2);
							graph.get(r2).paths.add(r1);
						}
					}
				}
			}
		}

		// Test of connexity
		if (ds.find(firstRoom) != ds.find(lastRoom) || ds.find(firstRoom) == -1) {
			alert("Please make sure the first and last room are connected!");
			return;
		}

		// Sove using the desired method, and alert in case of error
		if (!(BFS ? BFSSolve() : DFSSolve(firstRoom))) {
			alert("No path was found!");
		}

		solved = true;
	}

	/**
	 * 
	 * This method will perform a DFS (or Depth-First-Search) in order to solve
	 * the maze. This does not produce a minimal path.
	 * 
	 * @param i The index of the room we reached
	 * @return True if a path passing by this room took us to the lastRoom
	 * 
	 */
	private boolean DFSSolve(Integer i) {
		// Get the current room
		Room r = graph.get(i);

		// This room has been explored
		r.visited = true;

		// Since we passed by here, lets add the room to the path
		path.add(i);

		// If we are at the last room, we don't need to go further, lets return true and the cascade will make sure we get the right path
		if (i == lastRoom) {
			return true;
		}

		// If we reached a dead-end (there is only path leaving the current room) then lets step back, removing this room from the path, the cascade will retrace the path too
		// This condition is also true for the first room, so we'll skip i = 0 to avoid considering the first room as a dead-end
		if (i != 0 && r.paths.size() == 1) {
			path.remove(i);
			return false;
		}

		// This will tell us wether a path was from from this room to the last room
		boolean pathFound = false;

		// Lets check each path going from this room
		for (Integer p : r.paths) {
			Room target = graph.get(p);

			if (!target.visited) { // we exclude any path going to a visited room (which also means, a path going backwards)
				if (DFSSolve(p)) { // recursively check each adjacent room
					pathFound = true; // once we find a path we need to stop looking
					break;
				}
			}
		}

		// If no path was found from here, then we need to remove this room from the path and step back
		if (!pathFound) {
			path.remove(i);
		}

		// Return this cascadingly, so that any room leading here will also have the same result
		return pathFound;
	}

	/**
	 * 
	 * This method will perform a BFS (or Breadth-First-Search) in order to
	 * solve the maze. This produces a minimal path.
	 * 
	 * @param i The room to start from
	 * 
	 * @return True if the maze was solved
	 * 
	 */
	private boolean BFSSolve() {
		// This will let us know when the maze is solved, to stop looking
		boolean pathFound = false;

		// To perform a BFS we'll need a queue
		Queue<RoomNode> q = new LinkedList<RoomNode>();

		// Lets get our first room
		Room r = graph.get(firstRoom);

		// Push it on the queue
		q.offer(new RoomNode(r, null));

		// Mark it as visited
		r.visited = true;

		// This will the node linking a room to the we came to it from 
		RoomNode rn = null;

		// As long as we don't have a solution, and we still haven't visited all the possible paths
		while (!q.isEmpty() && !pathFound) {
			// Lets take our next room
			rn = q.poll();
			Room room = rn.getRoom();

			// Now we need to know where we can go from this room that we haven't been to before
			for (Integer p : room.paths) {
				Room t = graph.get(p);

				if (t.visited) {
					continue;
				}

				// Mark current as visited now, not outside of the for loop
				// because this way it is guaranteed that we can't add an element
				// multiple times to the queue and it's a lot more efficent
				t.visited = true;

				// Enque children
				q.offer(new RoomNode(t, rn));

				// If we reached the LASTROOM, we'll need to stop
				if (p == lastRoom) {
					pathFound = true;
					break;
				}
			}
		}

		// If a path was found, we'll need to retrace our steps
		if (pathFound) {
			// We'll start with the LASTROOM
			path.add(lastRoom);

			// And using our auxiliary class RoomNode, we have the possibility to get the previous room
			while (rn != null) {
				path.add(rn.getRoom().getId());
				rn = rn.getPrevious();
			}

			// At the end, the path is generated backwards, but it doesn't matter as it will be drawn as a 
			// line with no distinction between the start and the end so there is absolutely no need to reverse it
		}

		return pathFound;
	}

	/**
	 * 
	 * Show an alert box with a custom message (JavaScript Style)
	 * 
	 * @param msg The message to show
	 * 
	 */
	private void alert(String msg) {
		// Pass this to the panel
		panel.alert(msg);
	}

	/**
	 * 
	 * Getter for the solved variable
	 * 
	 * @return True if the maze is solved
	 * 
	 */
	public boolean isSolved() {
		return solved;
	}

	/**
	 * 
	 * Setter for the solved variable
	 * 
	 * @param s The new solved value
	 * 
	 */
	public void setSolved(boolean s) {
		solved = s;
	}

	/**
	 * 
	 * Setter for the custom variable
	 * 
	 * @param c The new custom value
	 * 
	 */
	public void setCustom(boolean c) {
		custom = c;
	}

	/**
	 * 
	 * Getter for the filled variable
	 * 
	 * @return True if the maze is filled
	 * 
	 */
	public boolean isFilled() {
		return filled;
	}

	/**
	 * 
	 * This method will toggle the solving algorithm. If the maze was solved, it
	 * will resolve it again with the new algorithm
	 * 
	 */
	public void toggle() {
		BFS = !BFS;

		if (isSolved()) {
			path.clear();
			solved = false;
			solve(firstRoom, lastRoom);
		}
	}
}

package com.dkhalife.projects;

/**
 * 
 * @author Dany Khalife
 * @version 1.0
 * @since December 09, 2012
 * 
 */

import java.util.Vector;

/**
 * This class represents a Room (or a Cell) inside our Maze
 * 
 * @author Dany Khalife
 * 
 */
public class Room {
	public int id; // The Room internal ID
	Vector<Integer> paths = new Vector<Integer>(); // The adjacent list
	boolean visited = false; // The visited flag for DFS or BFS

	/**
	 * 
	 * A room is identified by its unique ID
	 * 
	 * @param i The Room ID (Typically, this would be an offset)
	 * 
	 */
	public Room(int i) {
		// Initialize the id, 
		id = i;
	}

	/**
	 * 
	 * Getter for the Room ID
	 * 
	 * @return The room ID
	 * 
	 */
	public int getId() {
		return id;
	}
}
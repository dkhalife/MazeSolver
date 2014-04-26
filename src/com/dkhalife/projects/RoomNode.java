package com.dkhalife.projects;

/**
 * 
 * @author Dany Khalife
 * @version 1.0
 * @since December 09, 2012
 * 
 */

public class RoomNode {
	private Room room;
	private RoomNode previous;

	/**
	 * This class encapsulates a Room while allowing a backtrace for a path
	 * 
	 * @author Dany Khalife
	 * @param room The current Room
	 * @param previous The room that brought us to this room
	 * 
	 */
	public RoomNode(Room room, RoomNode previous) {
		this.room = room;
		this.previous = previous;
	}

	/**
	 * 
	 * Getter for the current room
	 * 
	 * @return The current room
	 * 
	 */
	public Room getRoom() {
		return room;
	}

	/**
	 * 
	 * Getter for the previous room
	 * 
	 * @return The room that brought us to this room
	 * 
	 */
	public RoomNode getPrevious() {
		return previous;
	}
}

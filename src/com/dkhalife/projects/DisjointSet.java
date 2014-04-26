package com.dkhalife.projects;
/**
 * This class implements a Disjoint Set using path compression and union by
 * depth
 * 
 * @author Thierry Lavoie
 * @version 1.0
 * @since April, 2012
 * 
 */
public class DisjointSet {

	/**
	 * 
	 * A Disjoint Set is created by specifying the universe size
	 * 
	 * @param size The universe size (How many elements are there in total)
	 * 
	 */
	public DisjointSet(int size) {
		s = new int[size];
		for (int i = 0; i < size; ++i) {
			s[i] = -1;
		}
	}

	/**
	 * 
	 * A union will join both elements' respective sets
	 * 
	 * @param el1 The first element
	 * @param el2 The second element
	 * 
	 */
	public void union(int el1, int el2) {
		int root1 = find(el1);
		int root2 = find(el2);
		if (root1 == root2) {
			return;
		}

		if (s[root2] < s[root1]) {
			s[root1] = root2;
		}
		else {
			if (s[root1] == s[root2]) {
				--s[root1];
			}
			s[root2] = root1;
		}
	}

	/**
	 * 
	 * This method is used to search an element's set (defined by its root)
	 * 
	 * @param x The element to look foor
	 * @return int The room element for the element's set
	 * 
	 */
	public int find(int x) {
		if (s[x] < 0) {
			return x;
		}
		else {
			s[x] = find(s[x]);
			return s[x];
		}
	}

	private int[] s;

}

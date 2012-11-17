/*
 * Copyright 2007, Ross Werner.
 * Distributed under the terms of the GPL version 2.
 * See LICENSE.TXT for details.
 */

package ancestris.modules.gedcom.utilities.matchers;

public class PotentialMatch <T> {
	private T left;
	private T right;
	private int certainty;
	
	public PotentialMatch(T left, T right, int certainty) {
		this.left = left;
		this.right = right;
		this.certainty = certainty;
	}

	public int getCertainty() {
		return certainty;
	}

	public T getLeft() {
		return left;
	}

	public T getRight() {
		return right;
	}
	
	@Override
	public String toString() {
		return certainty + "\t" + left + "\t" + right;
	}
}

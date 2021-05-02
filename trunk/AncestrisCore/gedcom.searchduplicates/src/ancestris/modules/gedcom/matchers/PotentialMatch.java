/*
 * Copyright 2007, Ross Werner.
 * Distributed under the terms of the GPL version 2.
 * See LICENSE.TXT for details.
 */
package ancestris.modules.gedcom.matchers;

public class PotentialMatch<T> {

    private T left;
    private T right;
    private int certainty;
    private boolean merged;

    public PotentialMatch(T left, T right, int certainty) {
        this.left = left;
        this.right = right;
        this.certainty = certainty;
        this.merged = false;
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    public boolean isMerged() {
        return merged;
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

    public void swap() {
        T temp = left;
        left = right;
        right = temp;
    }

    @Override
    public String toString() {
        return certainty + "\t" + left + "\t" + right;
    }
}

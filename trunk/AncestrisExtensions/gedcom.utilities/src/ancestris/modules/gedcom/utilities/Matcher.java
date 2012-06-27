/*
 * Copyright 2007, Ross Werner.
 * Distributed under the terms of the GPL version 2.
 * See LICENSE.TXT for details.
 */
package ancestris.modules.gedcom.utilities;

import java.util.List;

public interface Matcher<T> {

    List<PotentialMatch<T>> getPotentialMatches(List<T> left, List<T> right);

    int compareEntities(T left, T right);
}

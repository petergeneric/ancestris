// @formatter:off
/*
 * Copyright 2012, J. Pol
 *
 * This file is part of free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation.
 *
 * This package is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details. A copy of the GNU General Public License is
 * available at <http://www.gnu.org/licenses/>.
 */
// @formatter:on
package genjreports.rdf.semweb;

import java.util.HashMap;
import java.util.Map;

public enum Prefix
{
    rdf("http://www.w3.org/1999/02/22-rdf-syntax-ns#"), //
    rdfs("http://www.w3.org/2000/01/rdf-schema#"), //
    xsd("http://www.w3.org/2001/XMLSchema#"), //
    ;
    public final String uri;
    public static final Map<String, String> NAME_ID_MAP = new HashMap<String, String>();
    static
    {
        for (Prefix p : values())
            NAME_ID_MAP.put(p.name(), p.uri);
    }

    private Prefix(final String uri)
    {
        this.uri = uri;
    }
}

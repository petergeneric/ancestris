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

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createProperty;
import static genjreports.rdf.semweb.Prefix.*;

import com.hp.hpl.jena.rdf.model.Property;

public enum Predicate
{
    seeAlso(rdfs), //
    sameAs(owl), //
    hasLabel(rdfs), //
    isDefinedBy(rdfs), //
    type(rdf), //
    ;
    private final Prefix prefix;
    private final Property property;

    Predicate(final Prefix prefix)
    {
        this.prefix = prefix;
        this.property = createProperty(prefix.uri + name());
    }

    public String toString()
    {
        return prefix + ":" + name();
    }

    public String toUri()
    {
        return prefix.uri + name();
    }

    public Property toProperty()
    {
        return property;
    }
}

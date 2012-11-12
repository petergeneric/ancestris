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
package genjreports.rdf.gedsem;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyXRef;
import genj.gedcom.time.PointInTime;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.PrintUtil;

public class SemanticGedcomUtil {

	private SemanticGedcomModel rdfModel;

	/**
	 * Recursively adds anonymous properties to a resource.
	 * 
	 * @param resource
	 * @param properties
	 *            each {@link genj.gedcom.Property} is added as a
	 *            {@link com.hp.hpl.jena.rdf.model.Property} to the specified
	 *            {@link Resource}, children are added to children.
	 */
	private void addProperties(final Resource resource, final Property... properties) {
		if (properties == null)
			return;
		for (final Property property : properties) {
			final String tag = property.getTag();
			if (property instanceof PropertyDate) {
				final PropertyDate date = (PropertyDate) property;
				// TODO after/before, ranges, julian etc
				if (date.isValid() && date.getFormat().toString().equals("")) {
					final PointInTime start = date.getStart();
					if (start.isComplete() && start.isGregorian()) {
						final Resource propertyResource = rdfModel.addProperty(resource, tag, null);
						rdfModel.addLiteral(propertyResource, toXsdDateTime(start));
						continue;
						// other dates get default treatment

						// TODO typed dates:
						// see e.g. http://tech.groups.yahoo.com/group/jena-dev/message/33075
//						String lex=null;
//						RDFDatatype dtype=new XSDYearMonthType(typename);
//						rdfModel.getModel().createTypedLiteral(lex, dtype);

					}
				}
			}
			final String value = property.getValue();
			if ((property instanceof PropertyXRef) && value.startsWith("@")) {
				final String id = value.replaceAll("@", "");
				final String referredTag = property.getGedcom().getEntity(id).getTag();
				rdfModel.addConnection(resource, id, tag, referredTag);
			} else {
				final Resource propertyResource = rdfModel.addProperty(resource, tag, value);
				addProperties(propertyResource, property.getProperties());
				if (property instanceof PropertyName) {
					final PropertyName name = (PropertyName) property;
					rdfModel.addLiteral(propertyResource, name.getFirstName());
					rdfModel.addLiteral(propertyResource, name.getLastName());
					rdfModel.addLiteral(propertyResource, name.getSuffix());
				}
			}
		}
	}

	private static XSDDateTime toXsdDateTime(final PointInTime pit) {
		//String x = pit.toString()+" "+pit.getValue()+" "+pit.getYear()+" "+ pit.getMonth()+" "+ pit.getDay();
		final Calendar calendar = new GregorianCalendar(pit.getYear(), pit.getMonth(), pit.getDay()+2);
		return new XSDDateTime(calendar);
	}

	public Model toRdf(final Gedcom gedcom, final Map<String, String> uriFormats) {
		rdfModel = new SemanticGedcomModel(uriFormats);
		for (final Entity entity : gedcom.getEntities()) {
                    if ("HEAD".equals(entity.getTag()))
                        continue;
                    final Resource resource = rdfModel.addEntity(entity.getId(), entity.getTag());
                    addProperties(resource, entity.getProperties());
		}
		return rdfModel.getModel();
	}

	public InfModel getInfModel(final String rules) {
		
		for (String key:SemanticGedcomModel.PREFIXES.keySet()) {
			PrintUtil.registerPrefix(key, SemanticGedcomModel.PREFIXES.get(key));
		}
		final GenericRuleReasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		reasoner.setMode(GenericRuleReasoner.HYBRID);
		
		final InfModel infModel = ModelFactory.createInfModel(reasoner, rdfModel.getModel());
		infModel.prepare();
		return infModel;
	}
}

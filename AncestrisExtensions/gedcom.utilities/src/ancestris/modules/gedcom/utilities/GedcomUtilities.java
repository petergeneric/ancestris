/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Dominique Baron (lemovice-at-ancestris-dot-org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
/**
 * Extract from GenJ - Report - ReportToolBox
 *
 * @author Frederic Lapeyre <frederic@lapeyre-frederic.com>
 * @version 1.0
 *
 */
package ancestris.modules.gedcom.utilities;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

public class GedcomUtilities {

    private final static Logger LOG = Logger.getLogger(GedcomUtilities.class.getName(), null);
    public final static int ENT_ALL = 0;
    public final static int ENT_INDI = 1;
    public final static int ENT_FAM = 2;
    public final static int ENT_NOTE = 3;
    public final static int ENT_SOUR = 4;
    public final static int ENT_SUBM = 5;
    public final static int ENT_REPO = 6;
    public final static String entityTypes[] = {
        "All",
        Gedcom.INDI,
        Gedcom.FAM,
        Gedcom.NOTE,
        Gedcom.SOUR,
        Gedcom.SUBM,
        Gedcom.REPO
    };

    public static void deleteTags(Gedcom gedcom, String tagToRemove, int entityType) {
        LOG.log(Level.INFO, "deleting_tag {0}", tagToRemove);

        Collection<? extends Entity> entities;
        Entity entity;
        int iCounter = 0;

        if (entityType == ENT_ALL) {
            entities = gedcom.getEntities();
        } else {
            entities = gedcom.getEntities(entityTypes[entityType]);
        }

        List<Property> propsToDelete = new ArrayList<Property>();
        for (Iterator<? extends Entity> it = entities.iterator(); it.hasNext();) {
            entity = it.next();
            getPropertiesRecursively(entity, propsToDelete, tagToRemove);
            for (Iterator<Property> props = propsToDelete.iterator(); props.hasNext();) {
                Property prop = props.next();
                if (prop != null) {
                    Property parent = prop.getParent();
                    if (parent != null) {
                        String propText = parent.getTag() + " " + tagToRemove + " '" + prop.toString() + "'";
                        parent.delProperty(prop);
                        iCounter++;
                        LOG.log(Level.INFO, "deleting_tag {0} {1} {2}", new Object[]{entity.getTag(), entity.toString(), propText});
                    }
                }
            }
        }
        LOG.log(Level.INFO, "DeletedNb {0}", iCounter);
    }

    private static void getPropertiesRecursively(Property parent, List<Property> props, String tag) {
        Property[] children = parent.getProperties();
        for (int c = 0; c < children.length; c++) {
            Property child = children[c];
            if (child.getTag().compareTo(tag) == 0) {
                props.add(child);
            }
            getPropertiesRecursively(child, props, tag);
        }
    }

    /*
     * Merge 2 entities
     * dest the final entity
     * src the source entity
     * properties the property list to be included in the dest entity
     */
    public static void MergeEntities(Gedcom gedcom, Entity dest, Entity src, List<Property> properties) {
        LOG.log(Level.INFO, "Merging {0} with {1}", new Object[]{src.getId(), dest.getId()});

        for (Property rightProperty : properties) {
            try {
                movePropertyRecursively(rightProperty, dest);
            } catch (GedcomException ex) {
                LOG.log(Level.SEVERE, "Unexpected Gedcom exception {0}", ex);
                Exceptions.printStackTrace(ex);
            }
        }

        // Update linked entities
        for (Entity reference : PropertyXRef.getReferences(src)) {
            for (Iterator<PropertyXRef> it = reference.getProperties(PropertyXRef.class).iterator(); it.hasNext();) {
                PropertyXRef propertyXRef = it.next();
                Entity targetEntity = propertyXRef.getTargetEntity();
                if (targetEntity != null && targetEntity.equals(src)) {
                    propertyXRef.unlink();
                    propertyXRef.setValue(dest.getId());

                    /* 
                     * Try to cope with PropertyForeignXRef exception on link
                     * as the visibility of the class is restricted to gedcom package
                     * use this workaround as it  seem to be the unique Xref properties 
                     * for which transient property true
                     */
                    if (propertyXRef.isTransient() == false) {
                        try {
                            propertyXRef.link();
                        } catch (GedcomException e) {
                            LOG.log(Level.SEVERE, "unexpected", e);
                        }
                    }
                }
            }
        }

        // delete merged entity
        gedcom.deleteEntity(src);
    }

    /*
     * this function move property propertysrc  in parent property parentPropertyDest
     * and update Xref properties
     */
    public static void movePropertyRecursively(Property propertySrc, Property parentPropertyDest) throws GedcomException {
        Property propertyDest;
        if (propertySrc.getMetaProperty().isSingleton()) {
            propertyDest = parentPropertyDest.getProperty(propertySrc.getTag());
            if (propertyDest != null) {
                parentPropertyDest.delProperty(propertyDest);
            }
        }
        propertyDest = parentPropertyDest.addProperty(propertySrc.getTag(), propertySrc.getValue());

        // Move children properties
        for (Property children : propertySrc.getProperties()) {
            movePropertyRecursively(children, propertyDest);
        }

        propertySrc.getParent().delProperty(propertySrc);
        if (propertyDest instanceof PropertyXRef) {
            ((PropertyXRef) propertyDest).link();
        }
    }
}

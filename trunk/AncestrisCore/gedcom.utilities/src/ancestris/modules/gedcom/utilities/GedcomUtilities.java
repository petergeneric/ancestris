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
import genj.gedcom.PropertyForeignXRef;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
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
        LOG.log(Level.FINER, "deleting_tag {0}", tagToRemove);

        Collection<? extends Entity> entities;
        int iCounter = 0;

        if (entityType == ENT_ALL) {
            entities = gedcom.getEntities();
        } else {
            entities = gedcom.getEntities(entityTypes[entityType]);
        }

        List<Property> propsToDelete;
        for (Iterator<? extends Entity> it = entities.iterator(); it.hasNext();) {
            Entity entity = it.next();
            propsToDelete = getPropertiesRecursively(entity, tagToRemove);
            for (Iterator<Property> props = propsToDelete.iterator(); props.hasNext();) {
                Property prop = props.next();
                if (prop != null) {
                    Property parent = prop.getParent();
                    if (parent != null) {
                        String propText = parent.getTag() + " " + tagToRemove + " '" + prop.toString() + "'";
                        parent.delProperty(prop);
                        iCounter++;
                        LOG.log(Level.FINER, "deleting_tag {0} {1} {2}", new Object[]{entity.getTag(), entity.toString(), propText});
                    }
                }
            }
        }

        LOG.log(Level.FINER, "DeletedNb {0}", iCounter);
    }

    private static List<Property> getPropertiesRecursively(Property parent, String tag) {
        Property[] children = parent.getProperties();
        List<Property> propertiesList = new ArrayList<Property>();

        if (parent.getTag().compareTo(tag) == 0) {
            propertiesList.add(parent);
        }

        for (int c = 0; c < children.length; c++) {
            Property child = children[c];
            propertiesList.addAll(getPropertiesRecursively(child, tag));
        }

        return propertiesList;
    }

    /*
     * Merge 2 entities for selected properties:
     *
     *    Copy all properties from the src entity to the dest entity
     *
     *    Clean list of properties to merge:
     *    - For properties which include both a child and its parent, only include parent, remove child
     *
     *    Copy/Merge propoerties using their absolute path
     *    - For properties which are singleton : replace them
     *    - For properties which are not singleton, add them
     *    - For xref left attached to the entity which will be deleted, do not do anything unless they are in properties
     *    - For asso links attached to the entity which will be deleted, do not do anything unless they are in properties
     *
     *    Paremeters
     *    - dest : the final entity which will be kept
     *    - src : the source entity which will be deleted at the end
     *    - properties : the list of properties of the source to be kept and moved to the dest entity
     */
    public static void MergeEntities(Gedcom gedcom, Entity dest, Entity src, List<Property> allProperties) {
        LOG.log(Level.FINER, "Merging {0} with {1}", new Object[]{src.getId(), dest.getId()});

        // Clean properties : only keep properties for which no ancestors is includeed in the list
        List<Property> properties = new ArrayList<Property>();
        boolean found = false;
        for (Property prop : allProperties) {
            if (prop == null) {
                continue;
            }
            found = false;
            for (Property parent : getAncestors(prop)) {
                if (allProperties.contains(parent)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
               properties.add(prop); 
            }
        }
        
        // Copy / add each property to dest entity at the parent level
        for (Property prop : properties) {
            TagPath tagPath = prop.getParent().getPath();
            Property propDest = dest.getProperty(tagPath);
            try {
                if (prop instanceof PropertyXRef && !(prop instanceof PropertyForeignXRef)) {   // unlink FAMS for instance, but do not unlink pointers to ASSO
                    ((PropertyXRef) prop).unlink();
                }
                movePropertyRecursively(prop, propDest);
            } catch (GedcomException ex) {
                LOG.log(Level.SEVERE, "Unexpected Gedcom exception {0}", ex);
                Exceptions.printStackTrace(ex);
            }
        }
        
          // Update linked entities  : if source entity is removed, entities pointing to it will point in the vaccum.
          // So make these pointers point to the copied entity.
// FL : 2017-12-12 : this is not neccessarily what the user wants. If he wants to do so, he needs to select the properties before pressing the merge button.        
//        for (Entity reference : PropertyXRef.getReferences(src)) {
//            for (Iterator<PropertyXRef> it = reference.getProperties(PropertyXRef.class).iterator(); it.hasNext();) {
//                PropertyXRef propertyXRef = it.next();
//                Entity targetEntity = propertyXRef.getTargetEntity();
//                if (targetEntity != null && targetEntity.equals(src)) {
//                    propertyXRef.unlink();
//                    Property parent = propertyXRef.getParent();
//                    boolean alreadyLinked = false;
//                    for (PropertyXRef PropertyXRef : parent.getProperties(PropertyXRef.class)) {
//                        if (PropertyXRef.getValue().replaceAll("@","").equals(dest.getId())) {
//                            alreadyLinked = true;
//                        }
//                    }
//                    if (alreadyLinked == false) {
//                        propertyXRef.setValue(dest.getId());
//                    }
//
//                    /*
//                     * Try to cope with PropertyForeignXRef exception on link as
//                     * the visibility of the class is restricted to gedcom
//                     * package use this workaround as it seem to be the unique
//                     * Xref properties for which transient property true
//                     */
//                    if (propertyXRef.isTransient() == false) {
//                        try {
//                            propertyXRef.link();
//                        } catch (GedcomException e) {
//                            LOG.log(Level.SEVERE, "unexpected", e);
//                        }
//                    }
//                }
//            }
//        }

        // Delete merged entity
        gedcom.deleteEntity(src);
    }
    
    private static List<Property> getAncestors(Property prop) {
        List<Property> ancestors = new ArrayList<Property>();
        Property parent = prop.getParent();
        while (parent != null) {
            ancestors.add(parent);
            parent = parent.getParent();
        }
        return ancestors;
    }
    

    /*
     * This function moves property propertysrc in parent property
     * parentPropertyDest and update Xref properties
     */
    public static void movePropertyRecursively(Property propertySrc, Property parentPropertyDest) throws GedcomException {
        Property propertyDest;
        
        // Remove dest equivalent property from dest first in case property to be moved is singleton
        if (propertySrc.getMetaProperty().isSingleton()) {
            propertyDest = parentPropertyDest.getProperty(propertySrc.getTag());
            if (propertyDest != null) {
                parentPropertyDest.delProperty(propertyDest);
            }
        }
        
        // Attach parentPropertyDest entity to Parent of PropertySrc in case it is a PropertyForeignXRef
        if (propertySrc instanceof PropertyForeignXRef) {
            PropertyForeignXRef pfxref = (PropertyForeignXRef) propertySrc;
            PropertyXRef pxref = pfxref.getTarget();
            pxref.unlink();
            pxref.setValue(parentPropertyDest.getEntity().getId());
            pxref.link();
        } 
        // ... else attach src property to dest property in case it is not a PropertyForeignXref
        else {
            int n = parentPropertyDest.getNoOfProperties();
            propertyDest = parentPropertyDest.addProperty(propertySrc.getTag(), propertySrc.getValue(), n);  // add to the end
            if (propertyDest instanceof PropertyXRef) {
                ((PropertyXRef) propertyDest).link();
            }
            
            // Continue moving children properties
            for (Property children : propertySrc.getProperties()) {
                movePropertyRecursively(children, propertyDest);
            }
            
            // Remove src property
            propertySrc.getParent().delProperty(propertySrc);
            
        }


    }

    public static <T> List<T> searchProperties(Gedcom gedcom, Class<T> type, int entityType) {

        Collection<? extends Entity> entities;

        LOG.log(Level.FINER, "Searching for property {0}", type.getClass());

        if (entityType == ENT_ALL) {
            entities = gedcom.getEntities();
        } else {
            entities = gedcom.getEntities(entityTypes[entityType]);
        }

        List<T> foundProperties = new ArrayList<T>();
        for (Iterator<? extends Entity> it = entities.iterator(); it.hasNext();) {
            Entity entity = it.next();
            foundProperties.addAll(searchPropertiesRecursively(entity, type));
        }

        LOG.log(Level.FINER, "found  {0}", foundProperties.size());

        return foundProperties;
    }

    private static <T> List<T> searchPropertiesRecursively(Property parent, Class<T> type) {
        List<T> foundProperties = new ArrayList<T>();
        for (Property child : parent.getProperties()) {
            if (type.isAssignableFrom(child.getClass())) {
                foundProperties.add((T) child);
            }
            foundProperties.addAll(searchPropertiesRecursively(child, type));
        }
        return foundProperties;
    }

    
    
    
    
}

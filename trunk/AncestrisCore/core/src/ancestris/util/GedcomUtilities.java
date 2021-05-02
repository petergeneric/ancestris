/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Frederic Lapeyre <frederic@ancestris.org> & Dominique Baron (lemovice-at-ancestris-dot-org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.util;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyForeignXRef;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

public class GedcomUtilities {

    private final static Logger LOG = Logger.getLogger(GedcomUtilities.class.getName(), null);


    public static <T> List<T> searchProperties(Gedcom gedcom, Class<T> type, String entityTag) {

        Collection<? extends Entity> entities;

        LOG.log(Level.FINER, "Searching for property {0}", type.getClass());

        if (entityTag == null || entityTag.isEmpty()) {
            entities = gedcom.getEntities();
        } else {
            entities = gedcom.getEntities(entityTag);
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

    
    
    public static int deleteTags(Gedcom gedcom, String tagToRemove, String entityTag, boolean emptyTagOnly) {
        LOG.log(Level.FINER, "deleting_tag {0}", tagToRemove);

        if (tagToRemove.trim().isEmpty()) {
            return 0;
        }
        
        Collection<? extends Entity> entities;
        int iCounter = 0;

        if (entityTag == null || entityTag.isEmpty()) {
            entities = gedcom.getEntities();
        } else {
            entities = gedcom.getEntities(entityTag);
        }

        List<Property> propsToDelete;
        for (Entity entity : entities) {
            propsToDelete = entity.getAllProperties(tagToRemove); // tagToRemove can be a tagpath value
            for (Property prop : propsToDelete) {
                boolean isEmpty = prop.getValue().length() == 0 && prop.getNoOfProperties() == 0;
                if (emptyTagOnly && !isEmpty) {
                    continue;
                }
                Property parent = prop.getParent();
                if (parent != null) {
                    String propText = parent.getTag() + " " + tagToRemove + " '" + prop.toString() + "'";
                    parent.delProperty(prop);
                    iCounter++;
                    LOG.log(Level.FINER, "deleting_tag {0} {1} {2}", new Object[]{entity.getTag(), entity.toString(), propText});
                }
            }
        }

        LOG.log(Level.FINER, "DeletedNb {0}", iCounter);
        return iCounter;
    }

    /*
     * Merge 2 entities for selected properties:
     *
     *    Copy all properties from the src entity to the dest entity
     *
     *    Clean list of properties to merge:
     *    - For properties which include both a child and its parent, only include parent, remove child
     *
     *    Copy/Merge properties using their absolute path
     *    - For properties which are singleton : replace them
     *    - For properties which are not singleton, add them
     *    - For xref left attached to the entity which will be deleted, do not do anything unless they are in properties
     *    - For asso links attached to the entity which will be deleted, do not do anything unless they are in properties
     *
     *    Parameters
     *    - dest : the final entity which will be kept
     *    - src : the source entity which will be deleted at the end
     *    - properties : the list of properties of the source to be kept and moved to the dest entity
     */
    public static void MergeEntities(Entity dest, Entity src) {
        MergeEntities(dest, src, false);
    }

    public static void MergeEntities(Entity dest, Entity src, boolean addOnly) {
        MergeEntities(dest, src, Arrays.asList(src.getProperties()), addOnly);
    }

    public static void MergeEntities(Entity dest, Entity src, List<Property> allProperties) {
        MergeEntities(dest, src, allProperties, false);
    }
    
    private static void MergeEntities(Entity dest, Entity src, List<Property> allProperties, boolean addOnly) {

        LOG.log(Level.FINER, "Merging {0} with {1}", new Object[]{src.getId(), dest.getId()});

        // Clean properties : only keep properties for which none of them is an ancestor of another one
        List<Property> properties = new ArrayList<>();
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
            
            // We need the path of dest parent property
            // Make sure dest path starts with same entity type in case of merging entities of different types (validate / handleWarnings)
            TagPath tagPath = prop.getParent().getPath();
            String[] pathArray = tagPath.toArray();
            pathArray[0] = dest.getTag();
            TagPath destTagPath = new TagPath(pathArray, null);
            
            // Now get dest path
            Property propDest = dest.getProperty(destTagPath);
            
            // If destination is null, create it
            if (propDest == null) {
                dest.setValue(tagPath, "");
                propDest = dest.getProperty(tagPath);
            }
        
            // move sub properties to the destination tagpath
            try {
                movePropertyRecursively(prop, propDest, addOnly);
            } catch (GedcomException ex) {
                LOG.log(Level.SEVERE, "Unexpected Gedcom exception {0}", ex);
                Exceptions.printStackTrace(ex);
            }
        }
        
        // Delete merged entity
        dest.getGedcom().deleteEntity(src);
    }


    
    private static List<Property> getAncestors(Property prop) {
        List<Property> ancestors = new ArrayList<>();
        Property parent = prop.getParent();
        while (parent != null) {
            ancestors.add(parent);
            parent = parent.getParent();
        }
        return ancestors;
    }
    

    /**
     * This function moves property propertysrc (level n+1) to parent property parentPropertyDest (level n) and update links
     * 
     * Example :    moves NAME to INDI
     *              moves MARR to FAM
     *              moves DATE to BIRTH
     * 2 ways to move depending on addOnly:
     * - changing existing destination data (addOnly = false)
     * - not changing existing destination data, and only adding tags if not singleton tags (addOnly = true)
     * 
     * In the examples above:
     * - Move NAME to INDI replaces an existing NAME elements if any with those of source if addOnly is false ; it creates another NAME branch otherwise
     * - Move MARR to INDI replaces an existing MARRÂ event with that of source if addOnly is false ; if addOnly is true, it only adds subtags if MARRÂ already exists because MARR is a singleton 
     * (there cannot be 2 MARR tags in a FAM)
     * 
     * Logic : 
     * - if tag does not already exists, create it
     * - else, if addOnly is false (can be replaced), replace value
     * - else, if addOnly is true, if singleton, continue with subtags without changing the value, 
     * - else, there can be multiple tags : create another tag after the last one of its kind
     * - once done, erase source property (after recursivity to make sure its children have been moved first)
     * 
     * Special cases of creating/update : 
     * - PropertyName => do not assign value as it would generate subtags too early and generate an exception
     * - ForeignXRef (eg NOTE used by INDI) => unlink INDI, change ID of new note, relink INDI
     * - XRef (ex: INDIÂ using a NOTE) => move link value to new INDI and link
     */
    public static void movePropertyRecursively(Property propertySrc, Property parentPropertyDest) throws GedcomException {
                movePropertyRecursively(propertySrc, parentPropertyDest, false);
    }

    private static void movePropertyRecursively(Property propertySrc, Property parentPropertyDest, boolean addOnly) throws GedcomException {

        // Get flags
        Property propertyDest = parentPropertyDest.getProperty(propertySrc.getTag());
        boolean tagAlreadyExists = propertyDest != null;
        boolean isSingleton = propertySrc.getMetaProperty().isSingleton();
        int n = tagAlreadyExists ? parentPropertyDest.getPropertyPosition(propertyDest) + 1 : parentPropertyDest.getNoOfProperties();
        boolean isSameValue = tagAlreadyExists && propertyDest.getValue().equals(propertySrc.getValue());

        // Special case of PropertyForeignXRef independant from flags: (eg. Linked by)
        // => attach parentPropertyDest entity to Parent of PropertySrc
        if (propertySrc instanceof PropertyForeignXRef) {
            PropertyForeignXRef pfxref = (PropertyForeignXRef) propertySrc;
            PropertyXRef pxref = pfxref.getTarget();
            pxref.unlink();
            pxref.setValue(parentPropertyDest.getEntity().getId());
            pxref.link();
            return;
        } 
        
        // Create dest property
        if (!tagAlreadyExists || (addOnly && !isSingleton && !isSameValue)) {

            // Special case of property name
            if (propertySrc instanceof PropertyName) {
                // No need to set value for NAME : value will be created from the subtags that will be created recursively in the next call to movePropertyRecursively  
                propertyDest = parentPropertyDest.addProperty(propertySrc.getTag(), "", n);  
            } else {
                propertyDest = parentPropertyDest.addProperty(propertySrc.getTag(), propertySrc.getValue(), n);
            }

            // If xref, build link
            if (propertyDest instanceof PropertyXRef) {  // eg. FAMC, FAMS, etc...
                try {
                    ((PropertyXRef) propertyDest).link();
                } catch (Exception e) {
                    //Exceptions.printStackTrace(e);
                    parentPropertyDest.delProperty(propertyDest);
                }
            }
            
            
        } else if (!addOnly && !isSameValue) {

            // If xref, remove link
            if (propertyDest instanceof PropertyXRef) {
                try {
                    ((PropertyXRef) propertyDest).unlink();
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
            
            if (propertySrc instanceof PropertyName) {
                // nothing, name will be rebuilt from subtags
            } else {
                propertyDest.setValue(propertySrc.getValue());
            }
 
            // If xref, rebuild link
            if (propertyDest instanceof PropertyXRef) {
                try {
                    ((PropertyXRef) propertyDest).link();
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }
            }
            
        } 


        // Continue moving children properties
        for (Property children : propertySrc.getProperties()) {
            movePropertyRecursively(children, propertyDest, addOnly);
        }

            
        // Remove src property or unlink it 
        // - unlink FAMS for instance, but do not unlink pointers to ASSO (ex: PropertyForeignXRef)
        if (propertySrc instanceof PropertyXRef) {
            PropertyXRef pxref = (PropertyXRef) propertySrc;
            Entity ent = pxref.getTargetEntity();
            ent.delProperty(pxref.getTarget());
            propertySrc = null;
            
        } else {
            propertySrc.getParent().delProperty(propertySrc);
        }


    }

    /**
     * Copy an entity from one Gedcom to another (or same one)
     * It copies all the entities directly linked to it if parameter richCopy is set to true
     * It assumes target gedcom exists and sourceEntity exists
     * 
     * To avoid a recursive copy whereby all adjacent entities would then get copied in Full with their adjacent entities,
     * a hierarchy of copy must be established:
     * - Fam  : copy all first level entities which are not Fam (but do include indis)
     * - Indi : copy all first level entities which are not Fam, not indis
     * - Sour : copy all first level entities which are not Fam, not indis and not Sour
     * - Obje : copy all first level entities which are not Fam, not indis, not Sour, not Obje, not Repo, not Subm
     * - Repo : dry copy only (no linked entities) 
     * - Note : dry copy only (no linked entities) 
     * - Subm : dry copy only (no linked entities) 
     * - other: dry copy only (no linked entities) 
     * 
     * @param sourceEntity
     * @param targetGedcom
     * @return 
     */
    // Build hierarchy
    private static Map<String, Set<String>> allows = new HashMap<>();
    static {
        allows.put("FAM", new HashSet<String>(Arrays.asList("INDI", "SOUR", "OBJE", "NOTE")));
        allows.put("INDI", new HashSet<String>(Arrays.asList("SOUR", "OBJE", "NOTE")));
        allows.put("SOUR", new HashSet<String>(Arrays.asList("OBJE", "NOTE", "REPO")));
        allows.put("OBJE", new HashSet<String>(Arrays.asList("NOTE")));
    }
    
    public static Entity copyEntity(Entity sourceEntity, Gedcom targetGedcom, boolean richCopy) {
        
        Entity newEntity = null;
        
        try {
            newEntity = targetGedcom.createEntity(sourceEntity.getTag());
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (newEntity == null) {
            return newEntity;
        }
        
        copyPropertiesRecursively(sourceEntity, newEntity, richCopy);
        
        return newEntity;
    }

     
    /**
     * Copy properties beneath a property to another property (copy a cluster)
     * @param srcProperty
     * @param destProperty : assumed to exist and be empty
     * @param richCopy
     */
    public static void copyPropertiesRecursively(Property srcProperty, Property destProperty, boolean richCopy) {

        if (srcProperty == null || destProperty == null) {
            return;
        }

        // loop over children of prop recursively keeping same order of positions
        for (int i = 0; i < srcProperty.getNoOfProperties(); i++) {
            Property child = srcProperty.getProperty(i);
            
            // Skip CHAN properties and PropertyForeignXRef 
            if (child.getTag().equals("CHAN") || (child instanceof PropertyForeignXRef)) {
                // nothing to do
                continue;
                
            // Copy linked property if necessary and link it    
            } else if (child instanceof PropertyXRef) {
                if (!richCopy) {
                    continue;
                }
                Set<String> allowed = GedcomUtilities.allows.get(srcProperty.getEntity().getTag());
                if (allowed == null) {
                    continue;
                }
                Entity entityToCopy = ((PropertyXRef) child).getTargetEntity();
                if (entityToCopy != null) {
                    String tag = entityToCopy.getTag();
                    if (allowed.contains(tag)) {
                        Entity ent = copyEntity(entityToCopy, destProperty.getGedcom(), richCopy);
                        try {
                            Property addedProperty = destProperty.addProperty(child.getTag(), child.getValue(), i);
                            ((PropertyXRef) addedProperty).setValue(ent.getId());
                            ((PropertyXRef) addedProperty).link();
                            copyPropertiesRecursively(child, addedProperty, richCopy);

                        } catch (GedcomException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                
                
            // For propertyname, special copy because subtags are generated automatically
            // Specific subtag would therefore not be copied
            } else if (child instanceof PropertyName) {
                try {
                    Property addedProperty = destProperty.addProperty(child.getTag(), "", i);    
                    copyPropertiesRecursively(child, addedProperty, richCopy);
                    
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
            // Copy any other property except Xref properties which shall not be copied    
            } else { 
                try {
                    Property addedProperty = destProperty.addProperty(child.getTag(), child.getValue(), i);
                    copyPropertiesRecursively(child, addedProperty, richCopy);
                    
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
         }
    }
    
    /**
     * Attach imported entity to root of Target entity
     * If entities are part of different gedcoms, we first copy in full the imported entity to the target Gedcom
     * @param importedEntity
     * @param targetEntity
     * @return 
     */
    public static Entity attach(Entity importedEntity, Property targetProperty) {
        
        Gedcom gedcom = targetProperty.getGedcom();
        Entity attachedEntity = importedEntity;
        if (gedcom.compareTo(importedEntity.getGedcom()) != 0) {
            attachedEntity = copyEntity(importedEntity, gedcom, true);
        }
        
        try {
            Property xref = targetProperty.addProperty(attachedEntity.getTag(), '@' + attachedEntity.getId() + '@', targetProperty.getNoOfProperties());
            if (xref != null && xref instanceof PropertyXRef) {
                ((PropertyXRef) xref).link();
            }
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return targetProperty.getEntity();
    }

    /**
     * Attach indi as husband (or wife) of family fam
     * Creates family in gedcom if it does not exist
     * @param husband
     * @param indi
     * @param fam
     * @param gedcom
     * @return 
     */
    public static Entity createParent(boolean isHusband, Indi indi, Fam fam, Gedcom gedcom) {
        
        Indi targetIndi = indi;
        Fam targetEntity = fam;
        
        if (fam.getGedcom().compareTo(gedcom) != 0) {
            targetEntity = (Fam) copyEntity(fam, gedcom, true);
        } else {
            targetIndi = (Indi) copyEntity(indi, gedcom, true);
        }
        
        Property xref = null;
        try {
            xref = isHusband ? targetEntity.setHusband(targetIndi) : targetEntity.setWife(targetIndi);
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
            
        return xref != null ? xref.getEntity() : null;
        
    }

    public static Entity createChild(Indi child, Fam fam, Gedcom gedcom) {

        Indi targetIndi = child;
        Fam targetEntity = fam;
        
        if (fam.getGedcom().compareTo(gedcom) != 0) {
            targetEntity = (Fam) copyEntity(fam, gedcom, true);
        } else {
            targetIndi = (Indi) copyEntity(child, gedcom, true);
        }
        
        Property xref = null;
        try {
            xref = targetEntity.addChild(targetIndi);
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
            
        return xref != null ? xref.getEntity() : null;

    }

    public static Fam createFamily(Gedcom gedcom, Indi husb, Indi wife, Indi child) {

        // Create family
        Fam fam = null;
        try {
            fam = (Fam) gedcom.createEntity(Gedcom.FAM);
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }

        if (husb != null) {
            try {
                fam.setHusband(husb);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (wife != null) {
            try {
                fam.setWife(husb);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (child != null) {
            try {
                fam.addChild(child);
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return fam;    
    }
 
    
}
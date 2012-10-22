/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: lemovice (lemovice-at-ancestris-dot-org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.mergefile;

import ancestris.core.pluginservice.AncestrisPlugin;
import genj.gedcom.*;
import genj.util.Origin;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lemovice
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
public class GedcomMerge extends AncestrisPlugin {

    private final static Logger LOG = Logger.getLogger(GedcomMerge.class.getName(), null);

    public void merge(final Gedcom gedcomA, Gedcom gedcomB, final Gedcom mergedGedcom) {
        Map<String, Integer> entityId = new HashMap<String, Integer>();

        /*
         * Duplicate Gedcom file
         */
        final Gedcom gedcomACopy = copyGedcom(gedcomA);
        linkGedcom(gedcomACopy);

        final Gedcom gedcomBCopy = copyGedcom(gedcomB);
        linkGedcom(gedcomBCopy);

        /*
         * Re number all entities Ids for GedcomA
         */
        for (String entityType : Gedcom.ENTITIES) {
            entityId.put(entityType, settingIDs(gedcomACopy, entityType, 1));
        }

        /*
         * Re number all entities Ids for GedcomA
         */
        for (String entityType : Gedcom.ENTITIES) {
            settingIDs(gedcomBCopy, entityType, entityId.get(entityType));
        }

        /*
         * remap places of entities in the GedcomB
         */
        int[] placeMap = mapPlaceFormat(gedcomA, gedcomB);
        if (placeMap != null) {
            remapPlaces(gedcomBCopy.getEntities(), placeMap);
        }
        try {
            gedcomA.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    /*
                     * Copy all GedcomA entities in GedcomA
                     */
                    List<Entity> gedcomAEntities = gedcomACopy.getEntities();
                    for (Entity srcEntity : gedcomAEntities) {
                        try {
                            Entity destEntity = mergedGedcom.createEntity(srcEntity.getTag(), srcEntity.getId());
                            copyPropertiesCluster(srcEntity, destEntity);
                        } catch (GedcomException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    }

                    /*
                     * Copy all GedcomB entities in GedcomA
                     */
                    List<Entity> gedcomBEntities = gedcomBCopy.getEntities();
                    for (Entity srcEntity : gedcomBEntities) {
                        try {
                            Entity destEntity = mergedGedcom.createEntity(srcEntity.getTag(), srcEntity.getId());
                            copyPropertiesCluster(srcEntity, destEntity);
                        } catch (GedcomException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
        } catch (GedcomException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * duplicate a gedcom object
     */
    private Gedcom copyGedcom(Gedcom srcGedcom) {
        File GedcomFile = new File(srcGedcom.getOrigin().getFile().getAbsolutePath() + "~");
        if (GedcomFile.exists()) {
            GedcomFile.delete();
        }

        Gedcom destGedcom = null;

        try {
            destGedcom = new Gedcom(Origin.create(new URL("file", "", GedcomFile.getAbsolutePath())));
            destGedcom.setEncoding(srcGedcom.getEncoding());
            destGedcom.setLanguage(srcGedcom.getLanguage());
            destGedcom.setPassword(srcGedcom.getPassword());
            destGedcom.setPlaceFormat(srcGedcom.getPlaceFormat());

            // Get all entities from srcGedcom and copy them to destGedcom
            List<Entity> originEntities = srcGedcom.getEntities();
            for (Entity srcEntity : originEntities) {
                LOG.log(Level.INFO, "copying entity {0} Id {1}", new Object[]{srcEntity.getTag(), srcEntity.getId()});
                Entity destEntity = destGedcom.createEntity(srcEntity.getTag(), srcEntity.getId());
                copyPropertiesCluster(srcEntity, destEntity);
            }
        } catch (MalformedURLException ex) {
            destGedcom = null;
            LOG.log(Level.SEVERE, null, ex);
        } catch (GedcomException ex) {
            destGedcom = null;
            LOG.log(Level.SEVERE, null, ex);
        }

        return destGedcom;
    }

    /**
     * Links Gedcom XReferences
     */
    private boolean linkGedcom(Gedcom gedcomX) {
        // Links gedcom XReferences
        List<Entity> ents = gedcomX.getEntities();
        for (Iterator<Entity> it = ents.iterator(); it.hasNext();) {
            Entity ent = it.next();
            List<PropertyXRef> ps = ent.getProperties(PropertyXRef.class);
            for (Iterator<PropertyXRef> it2 = ps.iterator(); it2.hasNext();) {
                PropertyXRef xref = it2.next();
                Property target = xref.getTarget();
                if (target == null) {
                    try {
                        xref.link();
                    } catch (GedcomException e) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * ### 1 ### Re-Generation of Ids in Gedcom file
     */
    public int settingIDs(Gedcom gedcom, String entityType, int startFrom) {

        LOG.log(Level.INFO, "SettingIDs for {0} starting from {1}", new Object[]{entityType, startFrom});

        Collection<? extends Entity> entities = gedcom.getEntities(entityType);
        String entityIDPrefix = Gedcom.getEntityPrefix(entityType);

        Map<Integer, Entity> IDs2Entities = new TreeMap<Integer, Entity>();
        Map<Entity, Integer> Entities2IDs = new HashMap<Entity, Integer>();

        // loop to get the list of all ids
        Pattern intsOnly = Pattern.compile("\\d+");
        for (Entity entity : entities) {
            // Convert Id to Integer
            Matcher matcher = intsOnly.matcher(entity.getId());
            matcher.find();
            IDs2Entities.put(Integer.parseInt(matcher.group()), entity);
        }

        // Allocate new ids
        int iCounter = startFrom;

        while (!IDs2Entities.isEmpty()) {
            if (IDs2Entities.containsKey(iCounter) == true) {
                // Entity is already numbered correctly
                Entities2IDs.put(IDs2Entities.get(iCounter), iCounter);
                IDs2Entities.remove(iCounter);
                iCounter++;
            }

            Iterator<Integer> it = IDs2Entities.keySet().iterator();
            if (it.hasNext()) {
                Integer id = it.next();
                Entities2IDs.put(IDs2Entities.get(id), iCounter);
                IDs2Entities.remove(id);
                iCounter++;
            }
        }

        // set final ids
        String format = String.format("%s%%0%dd", entityIDPrefix, GedcomOptions.getInstance().getEntityIdLength());
        for (Entity entity : Entities2IDs.keySet()) {
            String newID = String.format(format, Entities2IDs.get(entity));
            if (newID.equals(entity.getId())) {
                continue;
            }
            try {
                LOG.log(Level.INFO, "SettingIDs for {0} old id {1} new id {2}", new Object[]{entity.getValue(), entity.getId(), newID});
                entity.setId(newID);

            } catch (GedcomException e) {
                LOG.log(Level.SEVERE, e.getMessage());
            }
        }

        LOG.log(Level.INFO, "First Free Id {0}", startFrom + entities.size());

        return startFrom + entities.size();
    }

    /**
     * Copy properties beneath a property to another property (copy a cluster)
     */
    private void copyPropertiesCluster(Property srcProperty, Property destProperty) {

        if (srcProperty == null || destProperty == null) {
            return;
        }

        Property[] srcProperties = srcProperty.getProperties();

        for (Property property : srcProperties) {
            // Xref properties shall not be copy
            if (!property.getTag().equals("XREF")) {
                copyPropertiesCluster(property, destProperty.addProperty(property.getTag(), property.getValue()));
            }
        }
    }

    private int[] mapPlaceFormat(Gedcom gedcomX, Gedcom gedcomY) {
        String pf1 = gedcomX.getPlaceFormat();
        String pf2 = gedcomY.getPlaceFormat();

        // If one file does not have a format, return (default will be as per option)
        if ((pf1.length() == 0) || (pf2.length() == 0)) {
            return null;
        }

        // If same formats, return (default will be as per option)
        if (pf1.compareTo(pf2) == 0) {
            return null;
        }

        // Both format exist and are not null
        String[] tags1 = pf1.split("\\,");
        int[] placeMap = new int[tags1.length];
        ArrayList<String> tags2 = new ArrayList<String>((Collection<String>) Arrays.asList(pf2.split("\\,")));
        ArrayList<String> tagsTemp = new ArrayList<String>((Collection<String>) Arrays.asList(pf2.split("\\,")));
        for (int i = 0; i < tags1.length; i++) {
            String tag = tags1[i];
            String selection = tags2.get(0);
            int iSel = tags2.indexOf(selection);
            placeMap[i] = tagsTemp.indexOf(selection);
            if (tags2.size() > 1) {
                tags2.remove(iSel);
            }
        }

        return placeMap;
    }

    /**
     * Remap a list of jurisdictions
     */
    private void remapPlaces(List<Entity> entities, int[] placeMap) {

        if (placeMap == null) {
            return;
        }

        // Loop on all entities to get their place tag and remap it
        for (Iterator<Entity> it = entities.iterator(); it.hasNext();) {
            Entity entity = it.next();
            List<PropertyPlace> places = entity.getProperties(PropertyPlace.class);
            for (Iterator<PropertyPlace> itp = places.iterator();
                    itp.hasNext();) {
                Property propPlace = itp.next();
                String place = propPlace.toString();
                //log.write("remap:"+place);
                String[] placeTab = place.split("\\,", -1);
                String newPlace = "";
                for (int i = 0; i < placeMap.length; i++) {
                    if (placeMap[i] < placeTab.length) {
                        newPlace += placeTab[placeMap[i]] + ",";
                    } else {
                        newPlace = place + ",,,,,,,,,,,,,,,,".substring(0, placeMap.length - placeTab.length + 1);
                        break;
                    }
                }
                newPlace = newPlace.substring(0, newPlace.length() - 1); // remove last comma
                propPlace.setValue(newPlace);
            }
        }
    }
}

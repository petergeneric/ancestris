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
import ancestris.gedcom.GedcomDirectory;
import ancestris.gedcom.GedcomMgr;
import static ancestris.modules.gedcom.mergefile.Bundle.open_mergeGedcom;
import genj.gedcom.*;
import genj.util.AncestrisPreferences;
import genj.util.Origin;
import genj.util.Registry;
import java.awt.Dialog;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lemovice
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
@NbBundle.Messages({
    "open.mergeGedcom=Open result file",})
public class GedcomMerge extends AncestrisPlugin implements Runnable {

    private final static Logger LOG = Logger.getLogger(GedcomMerge.class.getName(), null);
    private final File leftGedcomFile;
    private final File rightGedcomFile;
    private final File gedcomMergeFile;

    public GedcomMerge() {
        this.leftGedcomFile = null;
        this.rightGedcomFile = null;
        this.gedcomMergeFile = null;
    }

    public GedcomMerge(File leftGedcom, File rightGedcom, File gedcomMergeFile) {
        this.leftGedcomFile = leftGedcom;
        this.rightGedcomFile = rightGedcom;
        this.gedcomMergeFile = gedcomMergeFile;
    }

    @Override
    public void run() {
        Map<String, Integer> entityId = new HashMap<String, Integer>();
        Context leftGedcomContext;
        Context rightGedcomContext;
        Context mergedGedcomContext;
        final Gedcom leftGedcom;
        final Gedcom rightGedcom;
        final Gedcom mergedGedcom;
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(org.openide.util.NbBundle.getMessage(Bundle.class, "merge.progress"));

        if (leftGedcomFile == null) {
            return;
        }

        progressHandle.start();

        // Open Gedcom
        leftGedcomContext = GedcomMgr.getDefault().openGedcom(FileUtil.toFileObject(rightGedcomFile));
        if (leftGedcomContext == null) {
            return;
        }
        leftGedcom = leftGedcomContext.getGedcom();

        rightGedcomContext = GedcomMgr.getDefault().openGedcom(FileUtil.toFileObject(leftGedcomFile));
        if (rightGedcomContext == null) {
            return;
        }
        rightGedcom = rightGedcomContext.getGedcom();

        try {
            mergedGedcom = new Gedcom(Origin.create(gedcomMergeFile.toURI().toURL()));
        } catch (MalformedURLException ex) {
            LOG.log(Level.WARNING, "unexpected exception creating new gedcom", ex);
            return;
        }

        /*
         * Duplicate original Gedcom files
         */
        final Gedcom leftGedcomCopy = copyGedcom(leftGedcom);
        linkGedcom(leftGedcomCopy);

        final Gedcom rightGedcomCopy = copyGedcom(rightGedcom);
        linkGedcom(rightGedcomCopy);

        /*
         * Re number all entities Ids for leftGedcom
         */
        for (String entityType : Gedcom.ENTITIES) {
            entityId.put(entityType, settingIDs(leftGedcomCopy, entityType, 1));
        }

        /*
         * Re number all entities Ids for rightGedcom
         */
        for (String entityType : Gedcom.ENTITIES) {
            settingIDs(rightGedcomCopy, entityType, entityId.get(entityType));
        }

        /*
         * remap places of entities in the rightGedcom
         */
        int[] placeMap = mapPlaceFormat(leftGedcom, rightGedcom);
        if (placeMap != null) {
            remapPlaces(rightGedcomCopy.getEntities(), placeMap);
        }
        try {
            mergedGedcom.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {

                    List<Entity> leftGedcomEntities = leftGedcomCopy.getEntities();
                    for (Entity srcEntity : leftGedcomEntities) {
                        try {
                            Entity destEntity = mergedGedcom.createEntity(srcEntity.getTag(), srcEntity.getId());
                            copyPropertiesCluster(srcEntity, destEntity);
                        } catch (GedcomException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    }

                    /*
                     * Copy all rightGedcom entities in mergedGedcom
                     */
                    List<Entity> rightGedcomEntities = rightGedcomCopy.getEntities();
                    for (Entity srcEntity : rightGedcomEntities) {
                        try {
                            Entity destEntity = mergedGedcom.createEntity(srcEntity.getTag(), srcEntity.getId());
                            copyPropertiesCluster(srcEntity, destEntity);
                        } catch (GedcomException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    }

                    // Create submitter
                    AncestrisPreferences submPref = Registry.get(genj.gedcom.GedcomOptions.class);

                    Submitter submitter = (Submitter) mergedGedcom.createEntity(Gedcom.SUBM);
                    submitter.setName(submPref.get("submName", ""));
                    submitter.setCity(submPref.get("submCity", ""));
                    submitter.setPhone(submPref.get("submPhone", ""));
                    submitter.setEmail(submPref.get("submEmail", ""));
                    submitter.setCountry(submPref.get("submCountry", ""));
                    submitter.setWeb(submPref.get("submWeb", ""));

                    // set Gedcom Header
                    mergedGedcom.createEntity("HEAD", "");

                    mergedGedcom.setSubmitter(submitter);

                    String placeFormat = leftGedcom.getPlaceFormat();
                    if (placeFormat != null) {
                        mergedGedcom.setPlaceFormat(placeFormat);
                    }

                    Boolean[] showJuridictions = leftGedcom.getShowJuridictions();
                    if (showJuridictions != null) {
                        mergedGedcom.setShowJuridictions(showJuridictions);
                    }

                    String placeSortOrder = leftGedcom.getPlaceSortOrder();
                    if (placeSortOrder != null) {
                        mergedGedcom.setPlaceSortOrder(placeSortOrder);
                    }

                    String placeDisplayFormat = leftGedcom.getPlaceDisplayFormat();
                    if (placeDisplayFormat != null) {
                        mergedGedcom.setPlaceDisplayFormat(placeDisplayFormat);
                    }
                }
            });
        } catch (GedcomException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        GedcomMgr.getDefault().gedcomClose(rightGedcomContext);
        GedcomMgr.getDefault().gedcomClose(leftGedcomContext);

        progressHandle.finish();

        GedcomMergeResultPanel gedcomMergeResultPanel = new GedcomMergeResultPanel(leftGedcom, rightGedcom, mergedGedcom);
        JButton openMergeGedcomButton = new JButton(open_mergeGedcom());
        // display merge result
        DialogDescriptor gedcomMergeResultDescriptor = new DialogDescriptor(
                gedcomMergeResultPanel,
                org.openide.util.NbBundle.getMessage(Bundle.class, "merge.result.dialog"),
                true,
                new Object[]{openMergeGedcomButton, NotifyDescriptor.CANCEL_OPTION},
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        Dialog gedcomMergeResultDialog = DialogDisplayer.getDefault().createDialog(gedcomMergeResultDescriptor);
        gedcomMergeResultDialog.setVisible(true);

        if (gedcomMergeResultDescriptor.getValue() == openMergeGedcomButton) {
            mergedGedcomContext = GedcomMgr.getDefault().setGedcom(mergedGedcom);
            Indi firstIndi = (Indi) mergedGedcomContext.getGedcom().getFirstEntity(Gedcom.INDI);

            // save gedcom file
            GedcomMgr.getDefault().saveGedcom(new Context(firstIndi), FileUtil.toFileObject(mergedGedcom.getOrigin().getFile()));

            // and reopens the file
            GedcomDirectory.getDefault().openGedcom(FileUtil.toFileObject(mergedGedcom.getOrigin().getFile()));
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

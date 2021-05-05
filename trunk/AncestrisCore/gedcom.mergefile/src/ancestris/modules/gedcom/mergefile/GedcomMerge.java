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
import genj.gedcom.*;
import genj.util.Origin;
import java.awt.Dialog;
import java.io.File;
import java.net.MalformedURLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.*;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author lemovice
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
public class GedcomMerge extends AncestrisPlugin implements Runnable {
    
    private final static Logger LOG = Logger.getLogger(GedcomMerge.class.getName(), null);
    private static String[] ENTITIES = {Gedcom.SUBM, Gedcom.INDI, Gedcom.FAM, Gedcom.OBJE, Gedcom.NOTE, Gedcom.SOUR, Gedcom.REPO};   // change order compared to Gedcom declaration
    private final File leftGedcomFile;
    private final File rightGedcomFile;
    private final File gedcomMergeFile;
    private ProgressHandle progressHandle;
    private int progressCounter = 0;
    private Context leftGedcomContext;
    private Context rightGedcomContext;
    private static RequestProcessor RP = null;
    
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
        final Gedcom leftGedcom;
        final Gedcom rightGedcom;

        // Quit if one gedcom file is null
        if (leftGedcomFile == null || rightGedcomFile == null) {
            return;
        }

        // Open Left Gedcom quietly (GedcomMgr) - do not open the windows nor change the context
        leftGedcomContext = GedcomMgr.getDefault().openGedcom(FileUtil.toFileObject(leftGedcomFile));
        if (leftGedcomContext == null) {
            return;
        }
        leftGedcom = leftGedcomContext.getGedcom();

        // Open Right Gedcom (GedcomMgr) - do not open the windows nor change the context
        rightGedcomContext = GedcomMgr.getDefault().openGedcom(FileUtil.toFileObject(rightGedcomFile));
        if (rightGedcomContext == null) {
            return;
        }
        rightGedcom = rightGedcomContext.getGedcom();

        // Run merge task in as a progressed running task
        progressHandle = ProgressHandle.createHandle(org.openide.util.NbBundle.getMessage(getClass(), "merge.progress"), () -> false);
        if (RP == null) {
            RP = new RequestProcessor("GedcomMerge");            
        }
        Runnable runnable = () -> {
            mergeGedcom(leftGedcom, rightGedcom);
        };
        RequestProcessor.Task task = RP.create(runnable);        
        task.schedule(0);
        
    }
    
    private void mergeGedcom(Gedcom leftGedcom, Gedcom rightGedcom) {
        
        final Gedcom mergedGedcom;
        progressHandle.setInitialDelay(0);
        progressHandle.start(30);
        progressCounter = 0;
        progressHandle.progress(progressCounter++);

        // Create output gedcom with header and submitter
        try {
            mergedGedcom = new Gedcom(Origin.create(gedcomMergeFile.toURI().toURL()));
            Entity srcEntity = leftGedcom.getFirstEntity("HEAD");
            // Copy left header
            Entity destEntity = mergedGedcom.createEntity("HEAD", "");
            copyPropertiesCluster(srcEntity, destEntity);

            // Create submitter if none in left nor right gedcom
            Submitter submitter = (Submitter) leftGedcom.getFirstEntity("SUBM");
            if (submitter == null) {
                submitter = (Submitter) rightGedcom.getFirstEntity("SUBM");
            }
            if (submitter == null) {
                submitter = (Submitter) mergedGedcom.createEntity(Gedcom.SUBM);
                submitter.addDefaultProperties();
            }

            // Language and Encoding
            mergedGedcom.setGrammar(leftGedcom.getGrammar());
            mergedGedcom.setEncoding(leftGedcom.getEncoding());
            mergedGedcom.setLanguage(leftGedcom.getLanguage());
            
        } catch (GedcomException | MalformedURLException ex) {
            LOG.log(Level.WARNING, "unexpected exception creating new gedcom", ex);
            return;
        }

        // Remap places of entities in the rightGedcom based on the leftGeddcom
        int[] placeMap = mapPlaceFormat(leftGedcom, rightGedcom);
        if (placeMap != null) {
            remapPlaces(rightGedcom.getEntities(), placeMap);
        }
        progressHandle.progress(progressCounter++);

        // First loop on entities to renumber all IDs 
        for (String entityType : ENTITIES) {
            List<Entity> leftGedcomEntities = new ArrayList<>(leftGedcom.getEntities(entityType));
            Collections.sort(leftGedcomEntities);
            List<Entity> rightGedcomEntities = new ArrayList<>(rightGedcom.getEntities(entityType));
            Collections.sort(rightGedcomEntities);
            String entityIDPrefix = Gedcom.getEntityPrefix(entityType);
            String format = String.format("%s%%0%dd", entityIDPrefix, String.valueOf(leftGedcomEntities.size() + rightGedcomEntities.size()).length());

            // Re-number IDs
            int lastID = settingIDs(leftGedcom, leftGedcomEntities, format, 1);
            progressHandle.progress(progressCounter++);
            settingIDs(rightGedcom, rightGedcomEntities, format, lastID);
            progressHandle.progress(progressCounter++);
        }

        // Second loop on entities to create them the in mergedGedcom (after renumbering all entities otherwise links get lossed)
        for (String entityType : ENTITIES) {

            // Copy left Gedcom
            List<Entity> leftGedcomEntities = new ArrayList<>(leftGedcom.getEntities(entityType));
            Collections.sort(leftGedcomEntities);
            for (Entity srcEntity : leftGedcomEntities) {
                try {
                    Entity destEntity = mergedGedcom.createEntity(srcEntity.getTag(), srcEntity.getId());
                    copyPropertiesCluster(srcEntity, destEntity);
                } catch (GedcomException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
            progressHandle.progress(progressCounter++);

            // Copy right Gedcom
            List<Entity> rightGedcomEntities = new ArrayList<>(rightGedcom.getEntities(entityType));
            Collections.sort(rightGedcomEntities);
            for (Entity srcEntity : rightGedcomEntities) {
                try {
                    Entity destEntity = mergedGedcom.createEntity(srcEntity.getTag(), srcEntity.getId());
                    copyPropertiesCluster(srcEntity, destEntity);
                } catch (GedcomException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
            progressHandle.progress(progressCounter++);
        }

        // Update submitter after SUBM have been copied to avoid recreating one if already exists
        Submitter submitter = (Submitter) mergedGedcom.getFirstEntity("SUBM");
        mergedGedcom.setSubmitter(submitter);
        progressHandle.progress(progressCounter++);
        progressHandle.finish();

        // Display results and ask user if s/he wants to open the resulting file
        GedcomMergeResultPanel gedcomMergeResultPanel = new GedcomMergeResultPanel(leftGedcom, rightGedcom, mergedGedcom);
        JButton openMergeGedcomButton = new JButton(org.openide.util.NbBundle.getMessage(getClass(), "open.mergeGedcom"));
        DialogDescriptor gedcomMergeResultDescriptor = new DialogDescriptor(
                gedcomMergeResultPanel,
                org.openide.util.NbBundle.getMessage(getClass(), "merge.result.dialog"),
                true,
                new Object[]{openMergeGedcomButton, NotifyDescriptor.CANCEL_OPTION},
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                (HelpCtx) null,
                null);
        Dialog gedcomMergeResultDialog = DialogDisplayer.getDefault().createDialog(gedcomMergeResultDescriptor);
        gedcomMergeResultDialog.setVisible(true);

        // Close gedcoms
        WindowManager.getDefault().invokeWhenUIReady(() -> {
            GedcomMgr.getDefault().gedcomClose(rightGedcomContext);
            GedcomMgr.getDefault().gedcomClose(leftGedcomContext);
        });

        // Open merged gedcom if user chooses to do so
        if (gedcomMergeResultDescriptor.getValue() == openMergeGedcomButton) {
            WindowManager.getDefault().invokeWhenUIReady(() -> {
                // Save merged gedcom file quietly (GedcomMgr)
                Indi firstIndi = (Indi) mergedGedcom.getFirstEntity(Gedcom.INDI);
                GedcomMgr.getDefault().saveGedcom(new Context(firstIndi));
                
                // And reopens the file officially (GedcomDirectory)
                GedcomDirectory.getDefault().openGedcom(FileUtil.toFileObject(mergedGedcom.getOrigin().getFile()));
            });
        }
    }

    /**
     * Re-Generation of Ids in Gedcom file
     */
    public int settingIDs(Gedcom gedcom, Collection<? extends Entity> entities, String format, int startFrom) {
        
        if (entities == null || entities.isEmpty()) {
            return startFrom;
        }
        LOG.log(Level.FINE, "SettingIDs for {0} starting from {1}", new Object[]{entities.iterator().next().getTag(), startFrom});

        // Entities must be sorted on increasing IDs
        List<Entity> sortedEntities = new ArrayList<>(entities);
        Collections.sort(sortedEntities, entities.iterator().next().getComparator());
        
        try {
            // First loop to renumber entities from 1 (pack them to the smallest consecutive numbers)
            int iCounter = 1;
            for (Entity entity : sortedEntities) {
                String newID = String.format(format, iCounter);
                if (!entity.getId().equals(newID)) {
                    LOG.log(Level.FINE, "SettingIDs for {0} old id {1} new id {2}", new Object[]{entity.getValue(), entity.getId(), newID});
                    entity.setId(newID);
                }
                iCounter++;
            }

            // Second loop to renumber entities from startFrom (smartFrom must be greater than sortedEntities.size() to avoid duplicate IDs)
            if (startFrom != 1) {
                iCounter = Math.max(startFrom, sortedEntities.size() + 1);
                for (Entity entity : sortedEntities) {
                    String newID = String.format(format, iCounter);
                    if (!entity.getId().equals(newID)) {
                        LOG.log(Level.FINE, "SettingIDs for {0} old id {1} new id {2}", new Object[]{entity.getValue(), entity.getId(), newID});
                        entity.setId(newID);
                    }
                    iCounter++;
                }
            }
        } catch (GedcomException e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }
        
        LOG.log(Level.FINE, "First Free Id {0}", startFrom + entities.size());
        
        return startFrom + entities.size();
    }
    
    /**
     * Copy properties beneath a property to another property (copy a cluster)
     */
    private void copyPropertiesCluster(Property srcProperty, Property destProperty) throws GedcomException {

        if (srcProperty == null || destProperty == null) {
            return;
        }

        // loop over children of prop recursively keeping same order of positions
        for (int i = 0; i < srcProperty.getNoOfProperties(); i++) {
            Property child = srcProperty.getProperty(i);
            // CHAN property needs special copy
            if (child.getTag().equals("CHAN")) {
                Property addedProperty = destProperty.addProperty(child.getTag(), child.getValue());
                ((PropertyChange) addedProperty).setTime(((PropertyChange) child).getTime());
            } else if (child instanceof PropertyName) {
                Property addedProperty = destProperty.addProperty(child.getTag(), "", i);
                copyPropertiesCluster(child, addedProperty);
            } else if (!child.getTag().equals("XREF")) { // Xref properties shall not be copied
                Property addedProperty;
                addedProperty = destProperty.addProperty(child.getTag(), child.getValue(), i);
                copyPropertiesCluster(child, addedProperty);
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
        ArrayList<String> tags2 = new ArrayList<>((Collection<String>) Arrays.asList(pf2.split("\\,")));
        ArrayList<String> tagsTemp = new ArrayList<>((Collection<String>) Arrays.asList(pf2.split("\\,")));
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

/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.genids;

import ancestris.util.TimingUtility;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author frederic
 */
public class GenerateIDTaskFactory {

    public static GenerateIDTask create(Gedcom gedcom, GenIDPanel.Settings settings) {
        return new Impl(gedcom, settings);
    }

    private static class Impl implements GenerateIDTask {

        private final static Logger log = Logger.getLogger(GenerateIDTaskFactory.class.getName());
        private Gedcom gedcom = null;
        private GenIDPanel.Settings settings = null;

        private int counter = 0, maxCounter = 0;
        private boolean cancel = false;
        private String taskName = "";
        private String state = "";

        private Impl(Gedcom gedcom, GenIDPanel.Settings settings) {
            this.gedcom = gedcom;
            this.settings = settings;
            this.taskName = NbBundle.getMessage(GenerateIDTaskFactory.class, "GenerateIDAction.AskParams");
        }

        @Override
        public void run() {
            // Calcultage number of changes (maxCounter)
            maxCounter = 1;
            if (settings.isCat) {
                for (String tag : settings.entsTags) {
                    if (!tag.isEmpty()) {
                        maxCounter += gedcom.getEntities(tag).size()*3;
                    }
                }
            }
            if (generateIDs()) {
                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                    @Override
                    public void run() {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GenerateIDAction.class, "GenerateIDAction.Done"), NotifyDescriptor.INFORMATION_MESSAGE));
                    }
                });
            }
        }

        @Override
        public void cancelTrackable() {
            cancel = true;
        }

        @Override
        public int getProgress() {
            int progress = 100 * counter / maxCounter;
            if (progress % 5 == 0) {
                log.log(Level.FINE, "Time="+TimingUtility.getInstance().getTime() + " - state = "+getState() + " - progress = "+progress);
            }
            return progress;
        }

        @Override
        public String getTaskName() {
            return taskName;
        }

        @Override
        public String getState() {
            return state;
        }

        private boolean generateIDs() {

            TimingUtility.getInstance().reset();
            counter = 0;
            
            // Case all entites IDs are to be generated
            if (settings.isCat) {
                int pad = 1;
                if (settings.len > 0 && settings.len < 11) {
                    pad = settings.len;
                }
                if (settings.len < 0) {
                    pad = 0;
                }
                if (settings.len > 11) {
                    pad = 11;
                }
                final DecimalFormat formatNbrs = new DecimalFormat("000000000000".substring(0, pad));

                // Loop over all entity types
                for (String tag : settings.entsTags) {
                    if (cancel) {
                        return false;
                    }
                    if (tag.isEmpty()) {
                        continue;
                    }
                    state = Gedcom.getName(tag) + " 1/3";
                    Collection<? extends Entity> entities = gedcom.getEntities(tag);
                    final String entityIDPrefix = gedcom.getNextAvailableID(tag).substring(0, 1);
                    final Map<String, String> listID = new TreeMap<>(); // sorted mapping list
                    String key, ID;

                    // First loop to get list of ids and sort on value of entity
                    for (Entity entity : entities) {
                        counter++;
                        ID = entity.getId();
                        key = entity.toString();
                        listID.put(key, ID);
                    } // end loop

                    // Second loop to give temp ids in order to avoid duplicates
                    state = Gedcom.getName(tag) + " 2/3";
                    int iCounter = 0;
                    try {
                        for (Iterator<String> it = listID.keySet().iterator(); it.hasNext();) {
                            if (cancel) {
                                return false;
                            }
                            counter++;
                            key = it.next();
                            String oldID = listID.get(key);
                            Entity entity = gedcom.getEntity(oldID);
                            iCounter++;
                            String newID = entityIDPrefix + settings.prefix + "XYZAWZ" + iCounter + settings.suffix;  // Just a weird string ensuring no duplicates with existing ids
                            entity.setId(newID);
                            listID.put(key, newID);
                        } // proceed with other entity
                    } catch (GedcomException e) {
                        Exceptions.printStackTrace(e);
                        return false;
                    }

                    // Third loop to give final ids
                    state = Gedcom.getName(tag) + " 3/3";
                    iCounter = 0;
                    try {
                        for (Iterator<String> it = listID.keySet().iterator(); it.hasNext();) {
                            if (cancel) {
                                return false;
                            }
                            counter++;
                            key = it.next();
                            String oldID = listID.get(key);
                            Entity entity = gedcom.getEntity(oldID);
                            iCounter++;
                            String newID = entityIDPrefix + settings.prefix + formatNbrs.format(iCounter) + settings.suffix;
                            entity.setId(newID);
                        } // proceed with other entity
                    } catch (GedcomException e) {
                        Exceptions.printStackTrace(e);
                        return false;
                    }

                } // proceed with other entity type

                // Case we only change 1 ID    
            } else {
                final Entity entityFrom = gedcom.getEntity(settings.IDFrom);
                Entity entityTo = gedcom.getEntity(settings.IDTo);
                if (settings.IDFrom.isEmpty() || settings.IDTo.isEmpty() || entityFrom == null || entityTo != null) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GenerateIDAction.class, "GenerateIDAction.IncorrectIDs"), NotifyDescriptor.ERROR_MESSAGE));
                    return false;
                }
                String letterFrom = Gedcom.getEntityPrefix(entityFrom.getTag());
                String letterTo = settings.IDTo.substring(0, 1);
                if (!letterFrom.equals(letterTo)) {
                    if (DialogManager.OK_OPTION != DialogManager.create(NbBundle.getMessage(GenerateIDAction.class, "GenerateIDAction.TTL_ConfirmChange"),
                            NbBundle.getMessage(GenerateIDAction.class, "GenerateIDAction.MSG_ConfirmChange", letterFrom, letterTo, settings.IDFrom, settings.IDTo))
                            .setMessageType(DialogManager.WARNING_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION).show()) {
                        return false;
                    }
                }
                try {
                    entityFrom.setId(settings.IDTo);
                } catch (GedcomException e) {
                    Exceptions.printStackTrace(e);
                    return false;
                }
            }
            return true;
        }

    }

}

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

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.openide.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
@ActionID(id = "ancestris.modules.gedcom.genids.GenerateIDAction", category = "Edit")
@ActionRegistration(
        displayName = "#CTL_GenerateIDAction",
        iconInMenu = true,
        lazy = false)
@ActionReference(path = "Menu/Edit", name = "GenerateIDAction", position = 2450)
public final class GenerateIDAction extends AbstractAncestrisContextAction {

    private Gedcom gedcom = null;
    
    public GenerateIDAction() {
        super();
        setImage("ancestris/modules/gedcom/genids/GenIDsIcon.png");
        setText(NbBundle.getMessage(GenerateIDAction.class, "CTL_GenerateIDAction"));
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    @Override
    protected void actionPerformedImpl(ActionEvent event) {
        // Ask user to choose numbering preferences
        gedcom = getContext().getGedcom();
        GenIDPanel genidPanel = new GenIDPanel(getContext());
        Object choice = DialogManager.create(NbBundle.getMessage(GenerateIDAction.class, "GenerateIDAction.AskParams"), genidPanel)
                .setMessageType(DialogManager.QUESTION_MESSAGE)
                .setOptionType(DialogManager.OK_CANCEL_OPTION)
                .setDialogId("genidPanel")
                .show();

        if (choice == DialogManager.OK_OPTION) {
            genidPanel.savePreferences();
            if (generateIDs(genidPanel.getSettings())) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GenerateIDAction.class, "GenerateIDAction.Done"), NotifyDescriptor.INFORMATION_MESSAGE));
            }
        }
    }

    private boolean generateIDs(GenIDPanel.Settings settings) {

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
                if (tag.isEmpty()) {
                    continue;
                }
                Collection<? extends Entity> entities = gedcom.getEntities(tag);
                final String entityIDPrefix = gedcom.getNextAvailableID(tag).substring(0, 1);
                final Map<String, String> listID = new TreeMap<>(); // sorted mapping list
                String key, ID;

                // First loop to get list of ids and sort on value of entity
                for (Entity entity : entities) {
                    ID = entity.getId();
                    key = entity.toString();
                    listID.put(key, ID);
                } // end loop

                try {
                    gedcom.doUnitOfWork(new UnitOfWork() {
                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {

                            // Second loop to give temp ids in order to avoid duplicates
                            int iCounter = 0;
                            for (Iterator it = listID.keySet().iterator(); it.hasNext();) {
                                String key = (String) it.next();
                                String oldID = listID.get(key);
                                Entity entity = gedcom.getEntity(oldID);
                                iCounter++;
                                String newID = entityIDPrefix + settings.prefix + "XYZAWZ" + iCounter + settings.suffix;  // Just a weird string ensuring no duplicates with existing ids
                                try {
                                    entity.setId(newID);
                                    listID.put(key, newID);
                                } catch (GedcomException e) {
                                    Exceptions.printStackTrace(e);
                                }
                            } // proceed with other entity

                            // Third loop to give final ids
                            iCounter = 0;
                            for (Iterator it = listID.keySet().iterator(); it.hasNext();) {
                                String key = (String) it.next();
                                String oldID = listID.get(key);
                                Entity entity = gedcom.getEntity(oldID);
                                iCounter++;
                                String newID = entityIDPrefix + settings.prefix + formatNbrs.format(iCounter) + settings.suffix;
                                try {
                                    entity.setId(newID);
                                } catch (GedcomException e) {
                                    Exceptions.printStackTrace(e);
                                }
                            } // proceed with other entity

                        }
                    }); // end of doUnitOfWork
                } catch (GedcomException e) {
                    Exceptions.printStackTrace(e);
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
                gedcom.doUnitOfWork(new UnitOfWork() {
                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                            try {
                                entityFrom.setId(settings.IDTo);
                            } catch (GedcomException e) {
                                Exceptions.printStackTrace(e);
                            }
                    }
                }); // end of doUnitOfWork
            } catch (GedcomException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return true;
    }

}

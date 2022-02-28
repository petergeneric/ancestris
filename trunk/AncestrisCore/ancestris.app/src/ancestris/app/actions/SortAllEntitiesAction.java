/*
 * Ancestris - https://www.ancestris.org
 *
 * Copyright 2022 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.app.actions;

import ancestris.app.App;
import ancestris.util.ProgressListener;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.util.Trackable;
import java.awt.event.ActionEvent;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import spin.Spin;

/**
 * Sort all entities properties by date.
 *
 * @author zurga
 */
@ActionID(category = "Edit", id = "ancestris.app.actions.sortEntitiesProperties")
@ActionRegistration(
        displayName = "SortProperties",
        iconInMenu = true,
        lazy = false
)
@ActionReferences({
    @ActionReference(path = "Menu/Edit", position = 2650)})
public class SortAllEntitiesAction extends SortEntityAction {

    public SortAllEntitiesAction() {
        super();
        setText(NbBundle.getMessage(App.class, "action.sortAll"));
        setTip(NbBundle.getMessage(App.class, "action.sortAll.tip"));
    }

    @Override
    public void actionPerformedImpl(ActionEvent ae) {
        if (getContext() == null) {
            return;
        }

        SortEntityTask dj = (SortEntityTask) Spin.off(new DoJob(getGedcom()));
        ProgressListener.Dispatcher.processStarted(dj);
        try {
            getGedcom().doUnitOfWork((Gedcom g) -> {
                dj.run();
            });
        } catch (GedcomException e) {
            DialogManager.createError(null, e.getMessage()).show();
        }

        ProgressListener.Dispatcher.processStopped(dj);
    }

    private class DoJob implements SortEntityTask {

        private int counter = 0, maxCounter = 0;
        private boolean cancel = false;
        private Gedcom gedcom;

        DoJob(Gedcom g) {
            this.gedcom = g;
        }

        @Override
        public void run() {
            maxCounter = gedcom.getIndis().size() + gedcom.getFamilies().size();

            gedcom.getIndis().forEach(indi -> {
                List<Property> lp = doSortPropertyIndi(indi);
                indi.moveProperties(lp, 0);
                counter += 1;
            });

            gedcom.getFamilies().forEach(fam -> {
                List<Property> lp = doSortPropertyFam(fam);
                fam.moveProperties(lp, 0);
                counter += 1;
            });
        }

        @Override
        public void cancelTrackable() {
            cancel = true;
        }

        @Override
        public int getProgress() {
            return 100 * counter / maxCounter;
        }

        @Override
        public String getState() {
            return gedcom.getName();
        }

        @Override
        public String getTaskName() {
            return "SortEntitiesName";
        }
    }

}

/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2012-2013 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.edit.actions;

import ancestris.util.swing.DialogManager;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomListenerAdapter;
import javax.swing.JComponent;

/**
 * Editing dialog for a gedcom context that auto-dismisses on edit
 */
public class GedcomDialog extends DialogManager.ADialog {

    private Gedcom gedcom;
    private GedcomListener listener = new GedcomListenerAdapter() {

        @Override
        public void gedcomWriteLockAcquired(Gedcom gedcom) {
            cancel();
        }
    };

    public GedcomDialog(Gedcom gedcom, String title, final JComponent content) {
        super(title, content);
        this.gedcom = gedcom;
    }

    @Override
    public Object show() {
        try {
            gedcom.addGedcomListener(listener);
            return super.show();
        } finally {
            gedcom.removeGedcomListener(listener);
        }

    }
}
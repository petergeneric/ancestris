/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2013 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.view;

import ancestris.gedcom.GedcomDataObject;
import ancestris.gedcom.GedcomDirectory;
import ancestris.gedcom.GedcomDirectory.ContextNotFoundException;
import genj.gedcom.Context;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import java.awt.AWTEvent;

/**
 *
 * @author daniel
 */
public class SelectionDispatcher {

    private static int lock = 0;

    public static synchronized void muteSelection(boolean b) {
        if (b) {
            lock++;
        } else {
            lock--;
        }
        if (lock < 0) {
            lock = 0;
        }
    }

    //FIXME: is this functionality still necessary?
    private static synchronized boolean isMuted() {
        return lock != 0;
    }

    /**
     * Fire a selection event.
     *
     * @param context The seleted context. May be a property, an entity, an gedcom...
     */
    public static void fireSelection(Context context) {
        fireSelection(null, context);
    }

    /**
     * Fire a selection event.
     *
     * @param event   The AWTEvent originating this selection dispacth. if not available
     *                event may be null.
     * @param context The seleted context. May be a property, an entity, an gedcom...
     * @return true if change selection false otherwise
     */
    public static boolean fireSelection(AWTEvent event, Context context) {
        boolean retour = false;
        if (isMuted()) {
            return retour;
        }
        try {
            SelectionActionEvent saEvent = new SelectionActionEvent(event, context);
            //An action on an XREF Property selects targeted property
            if (saEvent.isAction()
                    && context.getProperties().size() == 1) {
                Property p = context.getProperty();
                if (p instanceof PropertyXRef) {
                    if (((PropertyXRef) p).getTarget() != null) {
                       context = new Context(((PropertyXRef) p).getTarget());
                    }
                    saEvent.setContext(context);
                    saEvent.setNotAction(true);
                    retour = true;
                }
            }
            GedcomDataObject gdao = GedcomDirectory.getDefault().getDataObject(context);
            gdao.assign(Context.class, context);
            gdao.assign(SelectionActionEvent.class, saEvent);
        } catch (ContextNotFoundException ex) {
        }
        return retour;
    }
}

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

import genj.gedcom.Context;
import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 *
 * @author daniel
 */
/**
 * This class is a container for an AWTEvent applying on a Context for global selection Lookup.
 * It is intended to be put in GlobalSelectionLookup when an ActionEvent is
 * applied on a Context object. This way a Listener on Lookup changes
 * can get the Action applied on and modify its behaviour.
 * This is, for instance used in TreeModule were double clic and single clic
 * are handled diferently.
 * <p/>
 * <b>Note:</b>event can be null and in this case component source may be null.
 * Also no particular processing other than selection event must be done and this
 * SelectionActionEvent may be created for consistency (ie always added in lookup
 * even for cases wher event is not provided)
 */
public class SelectionActionEvent {

    private AWTEvent event;
    private Context context;

    public SelectionActionEvent(AWTEvent event, Context context) {
        this.context = context;
        this.event = event;
    }

    /**
     * Returns the gedcom {@link Context} for this event.
     *
     * @return the gedcom Context for this event.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Returns true is this event is an Action type event (ie if a double clic occured).
     * The Action type events are:
     * <li/>{@link ActionEvent} with CTL Key pressed
     * <li/>{@link MouseEvent} with CTL Key pressed
     *
     * @return true is this event is an Action type event.
     */
    public boolean isAction() {
        boolean isActionPerformed = false;
        if (event != null) {
            if (event instanceof ActionEvent) {
                isActionPerformed |= (((ActionEvent) event).getModifiers() & ActionEvent.CTRL_MASK) != 0;
            }
            if (event instanceof MouseEvent) {
                int mod = ((MouseEvent) event).getModifiers();
                isActionPerformed |= (mod & (MouseEvent.CTRL_DOWN_MASK | MouseEvent.CTRL_MASK)) != 0;
                isActionPerformed |= ((MouseEvent) event).getClickCount() > 1; //MouseUtils.isDoubleClick((MouseEvent) event);
            }
        }
        return isActionPerformed;
    }

    /**
     * The object on which the Event initially occurred. Delegates to
     * {@link AWTEvent#getSource()}.
     *
     * @return The object on which the Event initially occurred or null if
     * unknown.
     */
    public Object getSource() {
        return event == null ? null : event.getSource();
    }
}

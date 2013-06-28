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

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.GedcomDirectory;
import ancestris.gedcom.GedcomDirectory.ContextNotFoundException;
import genj.gedcom.Context;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.view.SelectionListener;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 *
 * @author daniel
 */
/**
 * fire selection event
 *
 * @param context
 * @param isActionPerformed
 */
public class SelectionDispatcher {
    // TODO: faire autrement
    private static final int lock = 0;
    private static Integer muteSelection = new Integer(lock);
    //        private static Lookup.Result<Context> result = GlobalLookup.getDefault().lookupResult(Context.class);
    //        static {
    //            result.addLookupListener(ContextChangeListener.getDefault());
    ////            GlobalLookup.getDefault().addListener(Context.class, ContextChangeListener.getDefault());
    //        }

    public static void muteSelection(boolean b) {
        synchronized (muteSelection) {
            if (b) {
                muteSelection++;
            } else {
                muteSelection--;
            }
            if (muteSelection < 0) {
                muteSelection = 0;
            }
        }
    }

    //XXX: mute selection is disabled. This must be done differently if this functionality is still necessary
    private static boolean isMuted() {
        return false;
//                synchronized(muteSelection) {
//                    return muteSelection != 0;
//                }
    }

    public static void fireSelection(AWTEvent event, Context context) {
        if (!fireAction(event, context)) {
            fireSelection(context);
        }
    }

    /**
     *
     * @param event
     * @param context
     * @return
     */
    public static boolean fireAction(AWTEvent event, Context context) {
        boolean isActionPerformed = false;
        if (event != null) {
            if (event instanceof ActionEvent) {
                isActionPerformed |= (((ActionEvent) event).getModifiers() & ActionEvent.CTRL_MASK) != 0;
            }
            if (event instanceof MouseEvent) {
                isActionPerformed |= (((MouseEvent) event).getModifiers() & MouseEvent.CTRL_DOWN_MASK) != 0;
                isActionPerformed |= ((MouseEvent) event).getClickCount() > 1;
            }
        }
        //XXX: considere putting an action in lookup
        if (isActionPerformed) {
            Component source = (Component) event.getSource();
            if (!isMuted()) {
                // following a link?
                if (context.getProperties().size() == 1) {
                    Property p = context.getProperty();
                    if (p instanceof PropertyXRef) {
                        context = new Context(((PropertyXRef) p).getTarget());
                    }
                }
                for (SelectionListener listener : AncestrisPlugin.lookupAll(SelectionListener.class)) {
                    listener.setContext(context, true);
                }
            }
        }
        return isActionPerformed;
    }

    /**
     * Fire a selection event.
     *
     * @param context           The seleted context. May be a property, an entity, an gedcom...
     * @param isActionPerformed true if it is an actionn event ie if a double clic occured
     */
    public static void fireSelection(Context context) {
        try {
            GedcomDirectory.getDefault().getDataObject(context).assign(Context.class, context);
        } catch (ContextNotFoundException ex) {
            //                Exceptions.printStackTrace(ex);
        }
        //            if (true) {
        //                GlobalLookup.getDefault().add(context);
        //            } else {
        //                if (!isMuted()) {
        //                    // notify
        //                    //XXX: we must put selected nodes in global selection lookup (in fact use Explorer API)
        //                    for (SelectionListener listener : AncestrisPlugin.lookupAll(SelectionListener.class)) {
        ////            if (!listener.equals(from)) {
        //                        listener.setContext(context, false);
        ////            }
        //                    }
        ////        if (from != null) {
        ////            from.setMyContext(context, isActionPerformed);
        ////        }
        //                }
        //            }
    }
    
    /**
     * This class is a container for Action Event an context for global selection Lookup.
     * It is intended to be put in GlobalSelectionLookup when an ActionEvent is
     * applied on a Context object. This way a Listener on Lookup changes
     * can get the Action applied on and modify its behaviour.
     * This is, for instance used in TreeModule were double clic and single clic
     * are handled diferently.
     */
    public class AncestrisActionEvent {

        private AWTEvent event;
        private Context context;

        public AncestrisActionEvent(AWTEvent event, Context context) {
            this.context = context;
            this.event = event;
        }

        public Context getContext() {
            return context;
        }

        public AWTEvent getEvent() {
            return event;
        }
    }
}

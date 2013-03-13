package ancestris.view;

import ancestris.core.pluginservice.AncestrisPlugin;
import genj.gedcom.Context;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.util.swing.DialogHelper;
import genj.util.swing.DialogHelper.ComponentVisitor;
import genj.view.MySelectionListener;
import genj.view.SelectionListener;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.RootPaneContainer;

/**
 * A sink for selection events
 */
/**
 * XXX: must be removed as in netbeans selection is implemented with global context lookup
 * any component can receive a selection event thru a lookup.result listener
 */
public interface SelectionSink {

  /**
   * fire selection event
   * 
   * @param context
   * @param isActionPerformed
   */
  public void fireSelection(MySelectionListener from, Context context, boolean isActionPerformed);

  public class Dispatcher {

// TODO: faire autrement

      private static Integer muteSelection = 0;

    public static void muteSelection(boolean b) {
        synchronized(muteSelection) {
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

    private static boolean isMuted() {
        synchronized(muteSelection) {
            return muteSelection != 0;
        }
    }


      static SelectionSink theSink;
        public static void setSink(SelectionSink sink) {
           theSink = sink;
        }

    public static void fireSelection(AWTEvent event, Context context) {
        if (event == null)
            fireSelection((Component)null, context, false);
      boolean isActionPerformed = false;
      if (event instanceof ActionEvent)
        isActionPerformed |= (((ActionEvent)event).getModifiers()&ActionEvent.CTRL_MASK)!=0;
      if (event instanceof MouseEvent)
        isActionPerformed |= (((MouseEvent)event).getModifiers()&MouseEvent.CTRL_DOWN_MASK)!=0;
      fireSelection((Component)event.getSource(), context, isActionPerformed);
    }

    public static void fireSelection(Component source, Context context, boolean isActionPerformed) {

      MySelectionListener listener = (MySelectionListener)DialogHelper.visitOwners(source, new ComponentVisitor() {
        public Component visit(Component parent, Component child) {
          if (parent instanceof RootPaneContainer) {
            Container contentPane = ((RootPaneContainer)parent).getContentPane();
            if (contentPane.getComponentCount()>0 && contentPane.getComponent(0) instanceof SelectionSink)
              return contentPane.getComponent(0);
          }
          return parent instanceof MySelectionListener ? parent : null;
        }
      });
      if (!isMuted())
        fireSelection(listener,context, isActionPerformed);
    }
    
    
    private static void fireSelection(MySelectionListener from, Context context, boolean isActionPerformed) {
//TODO: mieux controler. Devra atre refait lors du basculement total dans l'environnement NB
//    // appropriate?
//    if (context.getGedcom()!= this.context.getGedcom()) {
//      LOG.log(Level.FINER, "context selection on unknown gedcom", new Throwable());
//      return;
//    }
        // following a link?
        if (isActionPerformed && context.getProperties().size() == 1) {
            Property p = context.getProperty();
            if (p instanceof PropertyXRef) {
                context = new Context(((PropertyXRef) p).getTarget());
            }
        }
//
//    // already known?
//    if (!isActionPerformed && this.context.equals(context))
//      return;
//
//    LOG.fine("fireSelection("+context+","+isActionPerformed+")");
//
//    // remember
//    this.context = context;
//
//    if (context.getGedcom()!=null)
//      REGISTRY.put(context.getGedcom().getName()+".context", context.toString());
//
        // notify
        //XXX: we must put selected nodes in global selection lookup (in fact use Explorer API)
        for (SelectionListener listener : AncestrisPlugin.lookupAll(SelectionListener.class)) {
            if (!listener.equals(from)) {
                listener.setContext(context, isActionPerformed);
            }
        }
        if (from != null) {
            from.setMyContext(context, isActionPerformed);
        }
    }



    /**
     * Fire a selection event.
     * @param context The seleted context. May be a property, an entity, an gedcom...
     * @param isActionPerformed true if it is an actionn event ie if a double clic occured
     */
    public static void fireSelection(Context context, boolean isActionPerformed) {
        fireSelection((Component)null, context, isActionPerformed);
    }

  }
}

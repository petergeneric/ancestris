package genj.view;

import genj.app.Workbench;
import genj.gedcom.Context;
import genj.util.swing.DialogHelper;
import genj.util.swing.DialogHelper.ComponentVisitor;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.RootPaneContainer;

/**
 * A sink for selection events
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

      static SelectionSink theSink;
        public static void setSink(SelectionSink sink) {
           theSink = sink;
        }

    public static void fireSelection(AWTEvent event, Context context) {
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
      
      theSink.fireSelection(listener,context, isActionPerformed);
    }

    public static void fireSelection(Workbench w, Context context, boolean isActionPerformed) {
        fireSelection((Component)null, context, isActionPerformed);
    }

  }
}

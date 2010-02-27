/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app;

import genj.util.swing.Action2;
import genj.view.ViewFactory;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.windows.TopComponent;

/** Opens a Genj top component.
 *
 * @author daniel
 */
/** Opens a top component.
 *
 * @author Jaroslav Tulach
 */
final class OpenGenjViewAction extends Action2  {
    private final Map<?,?> map;
    private final GenjViewTopComponent component;

    OpenGenjViewAction(GenjViewTopComponent component, Map<?,?> map) {
        this.component = component;
        this.map = map;
        setImage(component.getViewFactory().getImage());
        setText((String)map.get("displayName"));
//        setText(component.getViewFactory().getTitle(true));
//        putValue(Action.NAME,(String)map.get("displayName"));
//        putValue(Action.SMALL_ICON, component.getViewFactory().getImage());
    }

    /** execute callback */
    protected void execute() {
        GenjViewTopComponent win = component.create();
        win.init();
        win.open();
        win.requestActive();
    }

//    public void actionPerformed(ActionEvent e) {
//        execute();
//    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.app;

import genj.gedcom.Context;
import genj.util.swing.Action2;
import genj.util.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.util.Map;

/** Opens a Genj top component.
 *
 * @author daniel
 */
final class OpenGenjViewAction extends Action2  {
    private final Map<?,?> map;
    private final AncestrisTopComponent component;

    OpenGenjViewAction(AncestrisTopComponent component, Map<?,?> map) {
        this.component = component;
        this.map = map;
        setImage(new ImageIcon(component.getImageIcon()));
        setText((String)map.get("displayName"));
//        setText(component.getViewFactory().getTitle(true));
//        putValue(Action.NAME,(String)map.get("displayName"));
//        putValue(Action.SMALL_ICON, component.getViewFactory().getImage());
    }

    /** execute callback */
    @Override
  public void actionPerformed(ActionEvent e) {
        Context contextToOpen = App.center.getSelectedContext(true);
        if (contextToOpen != null){
            AncestrisTopComponent win = component.create();
            win.init(contextToOpen);
            win.open();
            win.requestActive();
        }
    }
}

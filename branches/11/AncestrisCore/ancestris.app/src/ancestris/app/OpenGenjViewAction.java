/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.view.AncestrisTopComponent;
import genj.gedcom.Context;
import ancestris.core.actions.AbstractAncestrisContextAction;
import genj.util.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.util.Map;

/**
 * Opens a Genj top component.
 *
 * @author daniel
 */
final public class OpenGenjViewAction extends AbstractAncestrisContextAction {

    private final Map<?, ?> map;
    private final AncestrisTopComponent component;

    public OpenGenjViewAction(AncestrisTopComponent component, Map<?, ?> map) {
        this.component = component;
        this.map = map;
        setImage(new ImageIcon(component.getImageIcon()));
        setText((String) map.get("displayName"));
//        setText(component.getViewFactory().getTitle(true));
//        putValue(Action.NAME,(String)map.get("displayName"));
//        putValue(Action.SMALL_ICON, component.getViewFactory().getImage());
    }

    @Override
    protected void contextChanged() {
        setEnabled(!contextProperties.isEmpty());
        super.contextChanged();
    }

    /**
     * execute callback
     */
    @Override
    public void actionPerformedImpl(ActionEvent e) {
        Context contextToOpen = getContext();
        if (contextToOpen != null) {
            AncestrisTopComponent win = component.create(contextToOpen);
//            win.init(contextToOpen);
            win.open();
            win.requestActive();
        }
    }
}

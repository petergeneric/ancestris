/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.util;

import genj.util.swing.Action2;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import org.openide.windows.TopComponent;

    /**
 *
 * @author daniel
 */
public final class GenjAction  extends Action2 {

/** Opens a top component.
 *
 * @author Jaroslav Tulach
 */
//    private final Map<?,?> map;
//    private final GenjViewTopComponent component;

//    GenjAction(GenjViewTopComponent component, Map<?,?> map) {
//        this.component = component;
//        this.map = map;
//        setImage(component.getViewFactory().getImage());
////        setText((String)map.get("displayName"));
//        setText(component.getViewFactory().getTitle(true));
////        putValue(Action.NAME,(String)map.get("displayName"));
////        putValue(Action.SMALL_ICON, component.getViewFactory().getImage());
//    }
//
//    /** execute callback */
//    protected void execute() {
//        GenjViewTopComponent win = component.create();
//        win.init();
//        win.open();
//        win.requestActive();
//    }
//
////    public void actionPerformed(ActionEvent e) {
////        execute();
////    }
//}
/**
     * Gets an action to display a GenjTopComponent. Used in layer.xml
     * @param component
     * @param displayName
     * @param iconBase
     * @param noIconInMenu
     * @return the action
     */
    static public Action createGenjAction(TopComponent component, String displayName, String iconBase, boolean noIconInMenu) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("displayName", displayName); // NOI18N
        map.put("iconBase", iconBase); // NOI18N
        map.put("noIconInMenu", noIconInMenu); // NOI18N
        map.put("component", component); // NOI18N

        return createGenjAction(map);
    }

    static public Action createGenjAction(Action delegate, String displayName, String iconBase, boolean noIconInMenu) {
        return org.openide.awt.Actions.alwaysEnabled(delegate,displayName,iconBase,noIconInMenu);
//        HashMap<String, Object> map = new HashMap<String, Object>();
//        map.put("displayName", displayName); // NOI18N
//        map.put("iconBase", iconBase); // NOI18N
//        map.put("noIconInMenu", noIconInMenu); // NOI18N
//        map.put("delegate", delegate); // NOI18N
//
//        return createGenjAction(map);
    }

    static public Action createGenjAction(Map map) {
        Action delegate = (Action)map.get("delegate");
        String displayName = (String)map.get("displayName"); // NOI18N
        String iconBase = (String)map.get("iconBase"); // NOI18N
        boolean noIconInMenu = (Boolean)map.get("noIconInMenu"); // NOI18N

        if (delegate instanceof Action2){
            displayName = ((Action2)delegate).getText();
            iconBase = ((Action2)delegate).getImage().toString();
        }

        return createGenjAction(delegate,displayName,iconBase,noIconInMenu);
        
//        HashMap<String, Object> map = new HashMap<String, Object>();
//        map.put("displayName", displayName); // NOI18N
//        map.put("iconBase", iconBase); // NOI18N
//        map.put("noIconInMenu", noIconInMenu); // NOI18N
//        map.put("delegate", delegate); // NOI18N
//
//
//
//        return org.openide.awt.Actions. alwaysEnabled(map);
//        return new GenjAction(map);
    }
}

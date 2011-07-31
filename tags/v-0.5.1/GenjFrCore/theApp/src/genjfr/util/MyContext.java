/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.util;

import genj.gedcom.Context;
import genj.util.swing.Action2;
import genj.view.ActionProvider;
import genj.view.ActionProvider.Purpose;
import genjfr.app.pluginservice.GenjFrPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;

/**
 *
 * @author daniel
 */
public class MyContext extends Context {

    public MyContext(Context context) {
        super(context);
    }

    public List<Action2> getPopupActions() {
        List<Action2> nodeactions = new ArrayList<Action2>(8);

        // get and merge all actions
        List<Action2> groups = new ArrayList<Action2>(8);
        List<Action2> singles = new ArrayList<Action2>(8);
        Map<Action2.Group, Action2.Group> lookup = new HashMap<Action2.Group, Action2.Group>();

        for (Action2 action : getProvidedActions(this)) {
            if (action instanceof Action2.Group) {
                Action2.Group group = lookup.get(action);
                if (group != null) {
                    group.add(new ActionProvider.SeparatorAction());
                    group.addAll((Action2.Group) action);
                } else {
                    lookup.put((Action2.Group) action, (Action2.Group) action);
                    groups.add((Action2.Group) action);
                }
            } else {
                singles.add(action);
            }
        }

        // add to menu
        nodeactions.add(null);
        nodeactions.addAll(groups);
        nodeactions.add(null);
        nodeactions.addAll(singles);

        // done
        return nodeactions;

    }

    private static Action2.Group getProvidedActions(Context context) {
        Action2.Group group = new Action2.Group("");
        // ask the action providers
        for (ActionProvider provider : GenjFrPlugin.lookupAll(ActionProvider.class)) {
            provider.createActions(context, Purpose.CONTEXT, group);
        }
        // done
        return group;
    }
}

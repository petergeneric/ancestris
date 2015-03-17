/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.editors.genealogyeditor.actions;

import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import org.openide.awt.Actions;
import org.openide.util.Exceptions;

/**
 *
 * @author daniel
 */
public class AActions{
    //
    // Factories
    //
    /**
     * Extension to Actions
     */


    /** Creates new action which is always enabled.
     * This method can also be used from
     * <a href="@org-openide-modules@/org/openide/modules/doc-files/api.html#how-layer">XML Layer</a>
     * directly by following XML definition:
     * <pre>
     * &lt;file name="your-pkg-action-id.instance"&gt;
     *   &lt;attr name="instanceCreate" methodvalue="org.openide.awt.Actions.alwaysEnabled"/&gt;
     *   &lt;attr name="delegate" methodvalue="your.pkg.YourAction.factoryMethod"/&gt;
     *   &lt;attr name="displayName" bundlevalue="your.pkg.Bundle#key"/&gt;
     *   &lt;attr name="iconBase" stringvalue="your/pkg/YourImage.png"/&gt;
     *   &lt;!-- if desired: &lt;attr name="noIconInMenu" boolvalue="true"/&gt; --&gt;
     *   &lt;!-- if desired: &lt;attr name="asynchronous" boolvalue="true"/&gt; --&gt;
     * &lt;/file&gt;
     * </pre>
     * In case the "delegate" is not just {@link ActionListener}, but also
     * {@link Action}, the returned action acts as a lazy proxy - it defers initialization
     * of the action itself, but as soon as it is created, it delegates all queries
     * to it. This way one can create an action that looks statically enabled, and as soon
     * as user really uses it, it becomes active - it can change its name, it can
     * change its enabled state, etc.
     *
     *
     * @param delegate the task to perform when action is invoked
     * @param displayName the name of the action
     * @param iconBase the location to the actions icon
     * @param noIconInMenu true if this icon shall not have an item in menu
     * @since 7.3
     */
    public static Action alwaysEnabled(
        ActionListener delegate, String displayName, String toolTip, String iconBase, boolean noIconInMenu
    ) {
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("delegate", delegate); // NOI18N
        map.put("displayName", displayName); // NOI18N
        map.put("iconBase", iconBase); // NOI18N
        map.put("noIconInMenu", noIconInMenu); // NOI18N
        map.put(Action.SHORT_DESCRIPTION, toolTip);
        return alwaysEnabled(map);
    }

    // use reflection to gain access to package-private methods
    @SuppressWarnings("rawtypes")
    static Action alwaysEnabled(Map map) {

        Method alwaysEnabledMethod = null;
        try {
            alwaysEnabledMethod = Actions.class.getDeclaredMethod("alwaysEnabled", Map.class);  // NOI18N
            alwaysEnabledMethod.setAccessible(true);
            return (Action) alwaysEnabledMethod.invoke(Action.class, map);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

}

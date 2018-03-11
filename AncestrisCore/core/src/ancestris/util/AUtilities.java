/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.util;

import ancestris.core.actions.CommonActions;
import ancestris.core.actions.SubMenuAction;
import genj.util.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JSeparator;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 * This class provides extension to {@link org.openide.util.Utilities} to handle
 * subMenus in context menus.
 * Each folder in System FileSystem from root path provided is shown as submenu.
 * As of version 7.3 of NetBeans plateform, there is no means to handle 
 * context submenus if layer.xml.
 *
 * @author daniel
 */
public class AUtilities {

    // Static members only
    private AUtilities() {
    }

    // this code borrows ideas from 
    // openide.util.Utilities.actionsForPath
    // http://blogs.kiyut.com/tonny/2007/09/20/netbeans-platform-parsing-layerxml/#.UIb3bHZnmhc
    // From http://forums.netbeans.org/ptopic43419.html
    /**
     * Load a menu sequence from a lookup path.
     * Any {@link Action} instances are returned as is;
     * any {@link JSeparator} instances are translated to nulls.
     * Warnings are logged for any other instances.
     *
     * @param path a path as given to {@link Lookups#forPath}, generally a layer folder name
     *
     * @return a list of actions interspersed with null separators
     *
     * @since org.openide.util 7.14
     */
    public static List<Action> actionsForPath(String path) {
        List<Action> actions = new ArrayList<Action>();

        FileObject fo = FileUtil.getConfigFile(path);

        if (fo != null) {
            buildActions(fo, actions);
        }

        return actions;
    }

    /** Recursive Actions
     *
     * @param fo      FileObject as the DataFolder
     * @param actions
     */
    static private void buildActions(FileObject fo, List<Action> actions) {
        DataObject[] childs = DataFolder.findFolder(fo).getChildren();
        Object instanceObj;

        for (DataObject dob : childs) {
            if (dob.getPrimaryFile().isFolder()) {
                FileObject childFo = dob.getPrimaryFile();
                List<Action> subActions = new ArrayList<Action>();
                buildActions(childFo, subActions);

                if (!subActions.isEmpty()) {
                    SubMenuAction a = new SubMenuAction(dob.getNodeDelegate().getDisplayName());
                    Object tip = childFo.getAttribute("shortDescription");
                    if (tip != null){
                        a.setTip(tip.toString());
                    }
                    Object iconResource = childFo.getAttribute("iconBase");
                    if (iconResource != null) {
                        a.setImage(new ImageIcon(ImageUtilities.loadImage(iconResource.toString())));
                    }
                    a.addActions(subActions);
                    actions.add(a);
                }
            } else {
                InstanceCookie ck = dob.getCookie(InstanceCookie.class);
                try {
                    instanceObj = ck.instanceCreate();
                } catch (Exception ex) {
                    instanceObj = null;
                }

                if (instanceObj == null) {
                    continue;
                }

                addActions(actions, instanceObj);
            }
        }
    }

    private static void addActions(List<Action> actions, Object instanceObj) {
        if (instanceObj!= null && !CommonActions.NOOP.equals(instanceObj)) {
            if (instanceObj instanceof JSeparator) {
                actions.add(null);
            } else if (instanceObj instanceof Action) {
                actions.add((Action) instanceObj);
            }
        }
    }
}

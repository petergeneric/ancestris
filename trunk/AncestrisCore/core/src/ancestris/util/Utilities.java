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
package ancestris.util;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author daniel
 */
public class Utilities {

    // static methods only
    private Utilities() {
    }

    public static String getClassName(Object o) {
        return getClassName(o.getClass());
    }

    public static String getClassName(Class c) {
        return c.getName().replace('.', '/');
    }

    /**
     * Helper to compare a string agains several words.
     *
     * @param text
   param pattern
     *
     * @return
     */
    public static boolean wordsMatch(String text, String pattern) {
        pattern = pattern.replaceAll(" +", ".+");
        return text.matches(".*" + pattern + ".*");
    }

    public static Locale getLocaleFromString(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        str.replaceAll(":", "_");
        String locale[] = (str + "__").split("_", 3);

        return new Locale(locale[0], locale[1], locale[2]);
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
    public static List<? extends Action> actionsForPath(String path,Object applicable) {
        List<Action> actions = new ArrayList<Action>();

        FileObject fo = FileUtil.getConfigFile(path);

        if (fo != null) {
            buildActions(fo, actions, applicable);
        }

        return actions;
    }

    /** Recursive Actions
     *
     * @param fo   FileObject as the DataFolder
     * @param actions
     */
    static private void buildActions(FileObject fo, List<Action> actions, Object applicable) {
        DataObject[] childs = DataFolder.findFolder(fo).getChildren();
        Object instanceObj;

        for (DataObject dob : childs) {
            if (dob.getPrimaryFile().isFolder()) {
                FileObject childFo = dob.getPrimaryFile();
                List<Action> subActions = new ArrayList<Action>();
                buildActions(childFo, subActions, applicable);

                if (!subActions.isEmpty()) {
                    SubMenuAction a = new SubMenuAction(dob.getNodeDelegate().getDisplayName());
                    a.setActions(subActions);
                    actions.add(a);
                }
            } else {
                InstanceCookie ck = (InstanceCookie) dob.getCookie(InstanceCookie.class);
                try {
                    instanceObj = ck.instanceCreate();
                } catch (Exception ex) {
                    instanceObj = null;
                }

                if (instanceObj == null) {
                    continue;
                }

                if (instanceObj instanceof JSeparator) {
                    actions.add(null);
                } else if (instanceObj instanceof Action) {
                    actions.add((Action) instanceObj);
                }
            }
        }
    }

    private static class SubMenuAction extends AbstractAction implements Presenter.Popup {

        private List<Action> actions;

        public SubMenuAction(String displayName) {
            super(displayName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // this = submenu => do nothing
        }

        void setActions(List<Action> actions) {
            this.actions = actions;
        }

        @Override
        public JMenuItem getPopupPresenter() {
            JMenu menu = new JMenu(this);

            for (Action a : actions) {
                menu.add(a);
            }

            return menu;
        }
    }


 static public String ctxPropertiesDisplayName(){
     Collection<? extends Property> properties = org.openide.util.Utilities.actionsGlobalContext().lookupAll(Property.class);
     String result = "";
     if (properties != null)
        result = "'"+Property.getPropertyNames(properties, 5)+"' ("+properties.size()+")";
     return result;
 }   

 static public String ctxPropertyDisplayName(){
     Property prop = org.openide.util.Utilities.actionsGlobalContext().lookup(Property.class);
     String result = "";
     if (prop != null)
        result = Property.LABEL+" '"+TagPath.get(prop).getName() + '\'';
     return result;
 }   
 static public String ctxEntityDisplayName(){
     Entity entity = org.openide.util.Utilities.actionsGlobalContext().lookup(Entity.class);
     String result = "";
     if (entity != null)
        result = Gedcom.getName(entity.getTag(),false)+" '"+entity.getId()+'\'';
     return result;
 }   
 static public String ctxGedcomDisplayName(){
     Gedcom gedcom = org.openide.util.Utilities.actionsGlobalContext().lookup(Gedcom.class);
     String result = "";
     if (gedcom != null)
        result = "Gedcom '"+gedcom.getName()+'\'';
     return result;
 }   
 static public Image getDN(){
     Property prop = org.openide.util.Utilities.actionsGlobalContext().lookup(Property.class);
     if (prop != null)
         return prop.getImage(false).getImage();
     return null;
 }   
}

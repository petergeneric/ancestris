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

package ancestris.modules.editors.standard;

import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;

/**
 *
 * @author daniel
 */
public class OLD_ResourcesSupport {
    static Map<String, ImageIcon> iconCache = new HashMap<String, ImageIcon>(5);
    static ImageIcon editorIcon = OLD_ResourcesSupport.getIcon("editeur_standard"); // NOI18N

    static ImageIcon getIcon(String name){
        if (iconCache.get(name)!=null)
            return iconCache.get(name);
        ImageIcon icon = new ImageIcon(OLD_ResourcesSupport.class.getResource(name+".png")); // NOI18N
        iconCache.put(name, icon);
        return icon;
    }
    static ImageIcon getIcon(String name, int size){
        if (size <=0 )
            return getIcon(name);
        String key = name + size;
        if (iconCache.get(key)!=null)
            return iconCache.get(key);
        URL url = OLD_ResourcesSupport.class.getResource(key+".png"); // NOI18N
        ImageIcon icon;
        if (url != null) {
            icon = new ImageIcon(OLD_ResourcesSupport.class.getResource(key+".png")); // NOI18N
        } else {
            icon = new ImageIcon(OLD_ResourcesSupport.class.getResource(name+".png")); // NOI18N
            icon = new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_DEFAULT));
        }
        iconCache.put(key, icon);
        return icon;
    }

    public static String getTitle(String bundleKey) {
        return NbBundle.getBundle(OLD_ResourcesSupport.class).getString(bundleKey+".title"); // NOI18N
    }
}

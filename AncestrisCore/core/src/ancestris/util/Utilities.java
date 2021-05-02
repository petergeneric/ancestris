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
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author daniel
 */
public class Utilities {

    // static methods only
    public Utilities() {
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
     * param pattern
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

    static public String ctxPropertiesDisplayName() {
        Collection<? extends Property> properties = org.openide.util.Utilities.actionsGlobalContext().lookupAll(Property.class);
        String result = "";
        if (properties != null) {
            result = "'" + Property.getPropertyNames(properties, 5) + "' (" + properties.size() + ")";
        }
        return result;
    }

    static public String ctxPropertyDisplayName() {
        Property prop = org.openide.util.Utilities.actionsGlobalContext().lookup(Property.class);
        String result = "";
        if (prop != null) {
            result = Property.LABEL + " '" + TagPath.get(prop).getName() + '\'';
        }
        return result;
    }

    static public String ctxEntityDisplayName() {
        Entity entity = org.openide.util.Utilities.actionsGlobalContext().lookup(Entity.class);
        String result = "";
        if (entity != null) {
            result = Gedcom.getName(entity.getTag(), false) + " '" + entity.getId() + '\'';
        }
        return result;
    }

    static public String ctxGedcomDisplayName() {
        Gedcom gedcom = org.openide.util.Utilities.actionsGlobalContext().lookup(Gedcom.class);
        if (gedcom == null) {
            Property prop = org.openide.util.Utilities.actionsGlobalContext().lookup(Property.class);
            if (prop != null) {
                gedcom = prop.getGedcom();
            }
        }
        String result = "";
        if (gedcom != null) {
            result = "Gedcom '" + gedcom.getName() + '\'';
        }
        return result;
    }

    static public Image getDN() {
        Property prop = org.openide.util.Utilities.actionsGlobalContext().lookup(Property.class);
        if (prop != null) {
            return prop.getImage(false).getImage();
        }
        return null;
    }

    /**
     * Finds Gedcom object from context:
     * <li/>either get Gedcom Object from context
     * <li/>or find Gedcom by looking for Property.getGecom()
     *
     * @param lookup
     *
     * @return Gedcom object or null if none is found
     */
    static public Gedcom getGedcomFromContext(Lookup lookup) {

        Gedcom gedcom = lookup.lookup(Gedcom.class);
        if (gedcom == null) {
            Property prop = lookup.lookup(Property.class);
            if (prop != null) {
                gedcom = prop.getGedcom();
            }
        }
        return gedcom;
    }
    
    /**
     * (added by FL for blueprint renderers, could be used for other purposes)
     * Utility method to convert HTML to text.
     * @param html The string containing HTML.
     * @return a String containing the derived text .
     */
    static public String html2text(String html) {
        EditorKit kit = new HTMLEditorKit();
        Document doc = kit.createDefaultDocument();
        doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
        try {
            Reader reader = new StringReader(html);
            kit.read(reader, doc, 0);
            return doc.getText(0, doc.getLength());
        } catch (Exception e) {
            return "";
        }
    }
    
    
    /**
     * (added by FL for quicksearch)
     * Utility method to extract bit of phrase from a text at a matching string.
     * @return a String containing the derived text or null.
     */
    static private int margin = 10;
    static private String bit = "...";

    static public String getPhraseBit(String text, String find) {
        
        text = text.replaceAll("(?:\\n|\\r)", " ");
        String str = text.toLowerCase();
        String q = find.toLowerCase();
        int index = str.indexOf(q);
        if (index == -1) {
            return null;
        }
        // Extract margin characters on each side and trim first and last word if not complete
        int start = Math.max(index - margin, 0), end = Math.min(text.length(), index + q.length() + margin);
        for (int i = start ; i >= 0 ; i--) {
            if (str.charAt(i) == ' ') {
                start = i+1;
                break;
            }
        }
        for (int i = end ; i < text.length() ; i++) {
            if (str.charAt(i) == ' ') {
                end = i;
                break;
            }
        }
        
        return bit + text.substring(start, end) + bit;
    }
    

    
    /**
     * Sets the cursor for Ancestris frame, all topComponents and panel in parameter
     * @param panel 
     */
    public static boolean setCursorWaiting(JPanel panel) {
        return setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR), panel);
    }

    public static boolean setCursorNormal(JPanel panel) {
        return setCursor(Cursor.getDefaultCursor(), panel);
    }

    private static boolean setCursor(Cursor cursor, JPanel panel) {
        boolean changed = false;
        // All frames
        for (Frame frame : Frame.getFrames()) {
            if (frame instanceof JFrame && ((JFrame) frame).getCursor() != cursor) {
                ((JFrame) frame).setCursor(cursor);
                changed = true;
            }
        }
        
        // All top components
        for (TopComponent tc : Lookup.getDefault().lookupAll(TopComponent.class)) {
            if (tc.getCursor() != cursor) {
                tc.setCursor(cursor);
                changed = true;
            }
        }
        
        // This panel
        if (panel != null && panel.getCursor() != cursor) {
            panel.setCursor(cursor);
            changed = true;
        }
        
        return changed;
    }

    
    /**
     * Get all entities depending from another entity
     * For instance, from an indi, get the families, the obje, the sources, the notes, and the repo of the sources, etc.
     * 
     * @param entity
     * @param indis
     * @param seen
     * @return 
     */
    public static Set<Entity> getDependingEntitiesRecursively(Entity entity) {
        Set<Entity> entities = new HashSet<Entity>();
        Set<Entity> seen = new HashSet<Entity>();
        return getDependingEntitiesRecursively(entity, entities, seen);
    }
    
    private static Set<Entity> getDependingEntitiesRecursively(Entity entity, Set<Entity> entities, Set<Entity> seen) {
        
        // If already seen that entity, return, else add it to seen
        if (seen.contains(entity)) {
            return entities;
        }
        seen.add(entity);
        
        // Add itself
        entities.add(entity);
        
        // Get all xref and collect target entities
        for (PropertyXRef xref : entity.getProperties(PropertyXRef.class)) {
            if (!xref.isValid()) {
                continue;
            }
            Entity target = xref.getTargetEntity();
            if (target instanceof Indi) { // continue if another indi, a REPO, a NOTE
                continue;
            }
            if (entity instanceof Repository && target instanceof Source) { // continue if going REPO to SOUR
                continue;
            }
            if (!(entity instanceof Indi) && target instanceof Fam) { // continue if going non indi to Fam
                continue;
            }
            entities.add(target);
            entities.addAll(getDependingEntitiesRecursively(target, entities, seen));
        }

        return entities;
    }

    
    
    public static File getResourceAsFile(Class clazz, String resourcePath, String ext) {
        try {
            InputStream in = clazz.getResourceAsStream(resourcePath);
            if (in == null) {
                return null;
            }

            File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ext);
            tempFile.deleteOnExit();

            FileOutputStream out = new FileOutputStream(tempFile);
            //copy stream
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void playSound(Class clazz, String sound) {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream is = clazz.getResourceAsStream(sound);
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(is);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioInputStream);
                    clip.start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    
    
}

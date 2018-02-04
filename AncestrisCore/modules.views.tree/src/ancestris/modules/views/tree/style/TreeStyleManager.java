/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2018 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.views.tree.style;

import genj.renderer.BlueprintManager;
import genj.tree.TreeMetrics;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ImageIcon;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author frederic
 */
public class TreeStyleManager {

    private final static Resources RESOURCES = Resources.get(TreeStyleManager.class);

    private final static String[] STYLES = {"default", "classic", "small_picture"};
    public final static String PERSOSTYLE = "perso";

    private static float MINZOOM = 0.1F;
    private static float MAXZOOM = 1.0F;
    private static float DEFZOOM = 0.5F;
    
    private static TreeStyleManager instance;
    private Map<String, Style> key2style = new LinkedHashMap<String, Style>();

    private BlueprintManager bpm;

    public static String[] ORDERCOLORS = new String[]{"background", "selects", "roots", "maleindis", "femaleindis", "unknownindis", "fams", "arcs"};
    private Map<String, Color> colors;
    private Font font;
    private TreeMetrics tm;

    /**
     * Singleton access
     */
    public static TreeStyleManager getInstance(Registry registry) {
        if (instance == null) {
            instance = new TreeStyleManager(registry);
        }
        return instance;
    }

    /**
     * Constructor
     */
    private TreeStyleManager(Registry registry) {

        bpm = BlueprintManager.getInstance();
        key2style.clear();

        // If registry is not null, load whatever is found in PERSO style
        if (registry != null) {
            key2style.put(PERSOSTYLE, getStyle(PERSOSTYLE, registry));
        }

        for (String key : STYLES) {
            Registry styleRegistry = new Registry(getClass().getResourceAsStream(key + ".properties"));
            Style style = getStyle(key, styleRegistry);
            key2style.put(key, style);
        }
        
    }

    /**
     * Load style. Default to default values (see default.properties)
     * @param key
     * @param registry
     * @return 
     */
    private Style getStyle(String key, Registry registry) {

        // Key
        if (key == null) {
            key = PERSOSTYLE;
        }

        String name = RESOURCES.getString("style." + key, false);

        colors = new HashMap<String, Color>();
        colors.put("background", Color.WHITE);
        colors.put("selects", new Color(255, 204, 0)); // dark yellow
        colors.put("roots", new Color(102, 0, 0)); // brown
        colors.put("maleindis", new Color(0, 0, 255)); // blue
        colors.put("femaleindis", new Color(255, 51, 255)); // pink
        colors.put("unknownindis", new Color(128, 128, 128)); // light gray
        colors.put("fams", new Color(204, 204, 204)); // dark gray
        colors.put("arcs", new Color(51, 51, 51));  // darker gray
        colors = registry.get("color", colors);

        font = new Font("SansSerif", 0, 11);
        font = registry.get("font", font);

        tm = new TreeMetrics(
                registry.get("windis", 115),
                registry.get("hindis", 28),
                registry.get("wfams", 91),
                registry.get("hfams", 13),
                registry.get("pad", 18),
                registry.get("indisthick", 3),
                registry.get("famsthick", 2)
        );

        Style style = new Style(
                key,
                name,
                new ImageIcon(this, key),
                bpm.getBlueprint("INDI", registry.get("blueprint.INDI", "").trim()),
                bpm.getBlueprint("FAM", registry.get("blueprint.FAM", "").trim()),
                colors,
                font,
                registry.get("bend", true),
                registry.get("marrs", true),
                registry.get("antial", true),
                registry.get("roundedrect", true),
                tm,
                Math.max(MINZOOM, Math.min(MAXZOOM, registry.get("zoom", DEFZOOM)))
        );
        
        return style;
    }

    public List<Style> getStyles() {
        return new ArrayList<Style>(key2style.values());
    }

    public Style getStyle(String key) {
        return key2style.get(key);
    }

    public Style getPersoStyle() {
        return key2style.get(PERSOSTYLE);
    }

    public void putStyle(Registry registry) {
        
        Style style = key2style.get(PERSOSTYLE);

        registry.put("color", style.colors);
        registry.put("font", style.font);
        registry.put("windis", style.tm.wIndis);
        registry.put("hindis", style.tm.hIndis);
        registry.put("wfams", style.tm.wFams);
        registry.put("hfams", style.tm.hFams);
        registry.put("pad", style.tm.pad);
        registry.put("indisthick", style.tm.indisThick);
        registry.put("famsthick", style.tm.famsThick);
        
        registry.put("bend", style.bend);
        registry.put("marrs", style.marr);
        registry.put("roundedrect", style.roundrect);
        registry.put("antial", style.antialiasing);
        registry.put("blueprint.INDI", style.blueprintIndi.getName());
        registry.put("blueprint.FAM", style.blueprintFam.getName());
        
        registry.put("zoom", (float) style.zoom);

    }

    
}

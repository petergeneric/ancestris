package ancestris.modules.views.tree.style;

/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2017 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */


import genj.renderer.Blueprint;
import genj.tree.TreeMetrics;
import genj.util.swing.ImageIcon;
import java.awt.Color;
import java.awt.Font;
import java.util.Map;

/**
 *
 * @author frederic
 */
public class Style {

    public String key;
    public ImageIcon icon;
    public String name;
    public Blueprint blueprintIndi;
    public Blueprint blueprintFam;
    public Map<String, Color> colors;
    public Font font;
    public boolean bend;
    public boolean marr;
    public boolean antialiasing;
    public boolean roundrect;
    public TreeMetrics tm;
    public double zoom;
    
    public Style(String key, 
            String name,
            ImageIcon icon,
            Blueprint blueprintIndi,
            Blueprint blueprintFam,
            Map<String, Color> colors,
            Font font,
            boolean bend,
            boolean marr,
            boolean antialiasing,
            boolean roundrect,
            TreeMetrics tm,
            double zoom) {

        this.key = key;
        this.icon = icon;
        this.name = name;
        this.blueprintIndi = blueprintIndi;
        this.blueprintFam = blueprintFam;
        this.colors = colors;
        this.font = font;
        this.bend = bend;
        this.marr = marr;
        this.antialiasing = antialiasing;
        this.roundrect = roundrect;
        this.tm = tm;
        this.zoom = zoom;
    }
    
    @Override
    public String toString() {
        return name;
    }

}

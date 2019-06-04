/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package ancestris.report.svgtree.graphics;

import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;

/**
 * Util class for graphics.
 * @author Zurga
 */


public class GraphicsUtil {
    
    private GraphicsUtil() {
        // Static Util pattern.
    }
    
    /**
     * Check if a font is available to display.
     * @param fontName name of font family
     * @return true if font is available, else false.
     */
    public static boolean checkFont(String fontName) {
        List<String> fonts = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        return fonts.contains(fontName);
    }
    
}

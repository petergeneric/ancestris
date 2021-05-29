/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.views.graph;

import genj.gedcom.Property;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.apache.fop.afp.util.StringUtils;

/**
 * Element to display for shortest Path.
 *
 * @author Zurga
 */
public class DisplayPathElement {

    private final static Logger LOG = Logger.getLogger("ancestris.app", null);

    private final static SimpleAttributeSet PLAIN = new SimpleAttributeSet();
    private final static SimpleAttributeSet BOLD = new SimpleAttributeSet();

    /**
     * background colors
     */
    private final static Color[] FG_COLORS = new Color[2];

    static {
        StyleConstants.setBold(BOLD, true);
        FG_COLORS[0] = Color.BLACK;
        FG_COLORS[1] = new Color(0, 51, 241);
    }

    /**
     * the property
     */
    private Property property;

    /**
     * an image (cached)
     */
    private ImageIcon img;

    /**
     * a document (cached)
     */
    private StyledDocument doc;

    /**
     * n-th entity
     */
    private int entity;

    /**
     * Constructor
     */
    public DisplayPathElement(Property setProp, int setEntity) {
        // keep property
        property = setProp;
        // cache localImg
        img = property.getImage(false);
        // keep sequence
        entity = setEntity;
        // prepare document
        doc = new DefaultStyledDocument();

        Color c = FG_COLORS[setEntity & 1];
        StyleConstants.setForeground(PLAIN, c);
        StyleConstants.setForeground(BOLD, c);

        try {
            int offset = 0;
            // indent
            doc.insertString(offset++, " ", PLAIN);

            final String value = setProp.getDisplayTitle();
            doc.insertString(offset, value, PLAIN);
            offset += value.length();
            
            // keep image
            SimpleAttributeSet localImg = new SimpleAttributeSet();
            StyleConstants.setIcon(localImg, setProp.getImage(false));
            doc.insertString(0, " ", localImg);

            // prefix with entity id
            String id = "[" + setProp.getEntity().getId() + "] ";
            id = StringUtils.rpad(id, ' ', 10);
            doc.insertString(0, id, PLAIN);
        } catch (BadLocationException e) {
            LOG.log(Level.INFO, "Error during hit creation", e);
        }
        // done
    }

    /**
     * Document
     */
    public StyledDocument getDocument() {
        return doc;
    }

    /**
     * Property
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Image
     */
    public ImageIcon getImage() {
        return img;
    }

    /**
     * n-th entity
     */
    public int getEntity() {
        return entity;
    }
    
    @Override
    public String toString() {
        return doc.toString();
    }

}

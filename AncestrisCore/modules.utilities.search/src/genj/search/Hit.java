/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.search;

import genj.gedcom.Entity;
import genj.gedcom.Property;
import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.swing.BorderFactory.createEmptyBorder;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * A search hit
 */
/*package*/ class Hit {
    
    private final static Logger LOG = Logger.getLogger("ancestris.app", null);

    private final static SimpleAttributeSet PLAIN = new SimpleAttributeSet(),
                                            RED = new SimpleAttributeSet(),
                                            BOLD = new SimpleAttributeSet();
    
    /** background colors */
    private final static Color[] FG_COLORS = new Color[2];
    static {
        StyleConstants.setForeground(RED, Color.RED);
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
    public Hit(Property setProp, String value, Matcher.Match[] matches, int setEntity, boolean isID) {
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
            String tag = setProp.getPropertyName();
            // indent
            doc.insertString(offset++, " ", PLAIN);

            if (setProp instanceof Entity) {
                tag = "";
            }
            
            // tag first for values and not IDs
            if (!isID) {
                doc.insertString(offset, tag, BOLD);
                offset += tag.length();
                doc.insertString(offset++, " ", PLAIN);
            }
            // keep value and format for matches
            doc.insertString(offset, value, PLAIN);
            if (matches != null) {
                for (int i = 0; i < matches.length; i++) {
                    Matcher.Match m = matches[i];
                    doc.setCharacterAttributes(offset + m.pos, m.len, RED, false);
                }
            }
            offset += value.length();
            // tag last for IDs
            if (isID) {
                doc.insertString(offset++, " ", PLAIN);
                doc.insertString(offset, tag, BOLD);
            }
            // keep image
            SimpleAttributeSet localImg = new SimpleAttributeSet();
           //StyleConstants.setIcon(localImg, setProp.getImage(false));  => this aligns iconn to the top
            JLabel jl = new JLabel(setProp.getImage(false)); // this aligns icon to the bottom...
            jl.setBorder(createEmptyBorder(0, 0, 10, 0));  // ...so we had some pixels at the bottom to center icon (tested with font sizes 8 to 30)
            StyleConstants.setComponent(localImg, jl);
            doc.insertString(0, " ", localImg);
            
            // prefix with entity id
            doc.insertString(0, "["+setProp.getEntity().getId()+"] ", PLAIN);
        } catch (BadLocationException e) {
            LOG.log(Level.INFO, "Error during hit creation", e);
        }
        // done
    }

    /**
     * Document
     */
    /*package*/ StyledDocument getDocument() {
        return doc;
    }

    /**
     * Property
     */
    /*package*/ Property getProperty() {
        return property;
    }

    /**
     * Image
     */
    /*package*/ ImageIcon getImage() {
        return img;
    }

    /**
     * n-th entity
     */
    /*package*/ int getEntity() {
        return entity;
    }


} //Hit

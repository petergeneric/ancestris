/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.util.swing;

import genj.util.EnvironmentChecker;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A component for choosing a font */
public class FontChooser extends JPanel {
  
  /** list of all font families */
  private static String[] families = null;
  
  /** combo for fonts */
  private JComboBox fonts;
  
  /** text for size */
  private JTextField size;
  
  /** 
   * apparently on some systems there might be a problem with 
   * accessing all fonts (vmcrash reported by dmoyne) when
   * we render each and every of those fonts in the font-selection-list
   */
  private final static boolean isRenderWithFont = 
    null == EnvironmentChecker.getProperty(FontChooser.class, "genj.debug.fontproblems", null, "supress font usage in font-selection-list");
    

  
  /**
   * Constructor   */
  public FontChooser() {
    
    // sub-components
    fonts = new JComboBox(getAllFonts());
    fonts.setEditable(false);
    fonts.setRenderer(new Renderer());
    size = new JTextField(3);
    
    //layout
    setAlignmentX(0F);
    
    setLayout(new BorderLayout());
    add(fonts, BorderLayout.CENTER);
    add(size , BorderLayout.EAST  );
    
    // done
  }
  
  /**
   * Patched max size
   */
  public Dimension getMaximumSize() {
    Dimension result = super.getPreferredSize();
    result.width = Integer.MAX_VALUE;
    return result;
  }

  /**
   * Accessor - selected font   */
  public void setSelectedFont(Font font) {
    String family = font.getFamily();
    Font[] fs = getAllFonts();
    for (int i = 0; i < fs.length; i++) {
      if (fs[i].getFamily().equals(family)) {
        fonts.setSelectedIndex(i);
        break;
      }
      
    }
    size.setText(""+font.getSize());
  }
  
  /**
   * Accessor - selected font   */
  public Font getSelectedFont() {
    Font font = (Font)fonts.getSelectedItem();
    if (font==null)
      font = getFont();
    return font.deriveFont((float)getSelectedFontSize());
  }
  
  /**
   * Calculates current selected size
   */
  private int getSelectedFontSize() {
    int result = 2;
    try {
      result = Integer.parseInt(size.getText());
    } catch (Throwable t) {
    }
    return Math.max(2,result);
  }
  
  /**
   * Calculate all available fonts
   */
  private static Font[] getAllFonts() {

    // initialize families
    if (families==null)
      families = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    
    // loop
    Font[] values = new Font[families.length];
    for (int i = 0; i < values.length; i++) {
      values[i] = new Font(families[i],0,12); 
    }
    
    // done
    return values;
  }
  
  /**
   * test
   */
  public static void main(String[] args) {
    
    System.out.println("Running font test");
    
    Font[] fonts = getAllFonts();

    System.out.println("Found "+fonts.length+" fonts");
    
    String txt = "GenealogyJ";
    FontRenderContext ctx = new FontRenderContext(null, false, false);
    
    for (int f = 0; f < fonts.length; f++) {
      Font font = fonts[f];
      
      System.out.println("Testing font "+font+"...");
      
      LineMetrics lm = font.getLineMetrics(txt, ctx);
      lm.getAscent();
      lm.getBaselineIndex();
      lm.getDescent();
      lm.getHeight();
      lm.getLeading();
      lm.getStrikethroughOffset();
      lm.getUnderlineOffset();
      lm.getUnderlineThickness();
      
      System.out.println("OK");
      
    }
    
    // done
  }
  
  private static class Renderer extends DefaultListCellRenderer {
    
    /**
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      if (value instanceof Font) {
        Font font = (Font)value;
        super.getListCellRendererComponent(list, font.getFamily(), index, isSelected, cellHasFocus);
        if (isRenderWithFont)
          setFont(font);
      } else {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
      return this;
    }
    
  } //Renderer
  
} //FontChooser

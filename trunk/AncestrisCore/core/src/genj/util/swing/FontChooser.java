/**
 * Ancestris
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2003 - 2018 Frederic Lapeyre <frederic@ancestris.org>
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
package genj.util.swing;

import ancestris.util.TimingUtility;
import genj.util.ChangeSupport;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeListener;

/**
 * A component for choosing a font
 */
public class FontChooser extends JPanel {

    /**
     * combo for fonts
     */
    private JComboBox fonts;
    private static String[] fontNames = null;
    private Runnable callback = null;

    /**
     * text for size
     */
    private JTextField size;

    private ChangeSupport changes = new ChangeSupport(this);

    /**
     * Apparently on some systems there might be a problem with accessing all
     * fonts (vmcrash reported by dmoyne) when we render each and every of those
     * fonts in the font-selection-list.
     * 
     * Additionnaly, some asian fonts (Han for instance) takes a very long time 
     * to render (in the getListCellRendererComponent renderer) therefore disable 
     * rendering in the combo box.
     * 
     */
    private boolean isRenderWithFont = false;
            

    /**
     * Constructor
     */
    public FontChooser() {

        // sub-components
        fonts = new JComboBox();
        getAllFonts(); // swing worker to fill in combobox in the background with fonts (long task)

        fonts.setEditable(false);
        fonts.setRenderer(new Renderer());
        size = new JTextField(3);

        size.getDocument().addDocumentListener(changes);

        //layout
        setAlignmentX(0F);

        setLayout(new BorderLayout());
        add(fonts, BorderLayout.CENTER);
        add(size, BorderLayout.EAST);

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
     * Accessor - selected font
     */
    public void setSelectedFont(Font font) {
        if (font == null) {
            fonts.setSelectedIndex(-1);
            size.setText("");
            return;
        }
        for (int i = 0; i < fonts.getItemCount(); i++) { // use string match because font object reference is not necessary in the list
            Font f = (Font) fonts.getItemAt(i);
            if (f.getName().equals(font.getName())) {
                fonts.setSelectedIndex(i);
                break;
            }
        }
        size.setText("" + font.getSize());
    }

    /**
     * Accessor - selected font
     */
    public Font getSelectedFont() {
        Font font = (Font) fonts.getSelectedItem();
        if (font == null) {
            return null;
        }
        return font.deriveFont((float) getSelectedFontSize());
    }

    /**
     * Calculates current selected size
     */
    private int getSelectedFontSize() {
        int result = 10;
        try {
            result = Integer.parseInt(size.getText());
        } catch (Throwable t) {
        }
        return Math.max(2, result);
    }

    /**
     * Calculate all available fonts in the background (long task : all fonts
     * are actually built with size 1)
     */
    private void getAllFonts() {

        TimingUtility.getInstance().reset();
        SwingWorker aWorker = new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                if (fontNames == null) {
                    GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    fontNames = env.getAvailableFontFamilyNames(); // long task
                }
                for (String fontName : fontNames) {
                    Font f = new Font(fontName, 0, 12);
                    if (f.canDisplay('A') && f.canDisplay(' ')) {
                        fonts.addItem(f); // add font to combobox only if can be displayed 
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                if (callback != null) {
                    callback.run();
                }
                fonts.addActionListener(changes);
            }
        };
        SwingUtilities.invokeLater(aWorker);
    }

    public void setCallBack(Runnable runnable) {
        callback = runnable;
    }

    private class Renderer extends DefaultListCellRenderer {

        /**
         * @see
         * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
         * java.lang.Object, int, boolean, boolean)
         */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Font) {
                Font font = (Font) value;
                super.getListCellRendererComponent(list, font.getFamily(), index, isSelected, cellHasFocus);
                if (isRenderWithFont) {
                    setFont(font);   // this could take very long for some fonts (ex: Han font)
                }
            } else {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
            return this;
        }

    } //Renderer

    public void addChangeListener(ChangeListener listener) {
        changes.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changes.removeChangeListener(listener);
    }

} //FontChooser

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
package genj.edit.beans;

import ancestris.util.swing.DialogManager;
import static genj.edit.beans.PropertyBean.LOG;
import static genj.edit.beans.PropertyBean.RESOURCES;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.io.FileAssociation;
import genj.util.GridBagHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.TextFieldWidget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JButton;
import javax.swing.JLabel;
import org.openide.util.Exceptions;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : UNKNOWN
 */
public class SimpleValueBean extends PropertyBean {

    private final GridBagHelper gh = new GridBagHelper(this);
    private JButton webLink = new JButton(new ImageIcon(MetaProperty.class, "images/Web"));

    /** members */
    private TextFieldWidget tfield;
    private JLabel tfieldRO;

    public SimpleValueBean() {

        tfield = new TextFieldWidget("", 8);
        tfield.setComponentPopupMenu(new CCPMenu(tfield));
        tfieldRO = new JLabel(""); // for RO properties
        tfield.addChangeListener(changeSupport);

        //setLayout(new BorderLayout());
        //add(BorderLayout.NORTH, tfield);
        
        // listen to selection of global and ask for confirmation
        webLink.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String link = property.getDisplayValue().replaceAll(" ", "%20");
                try {
                    FileAssociation.getDefault().execute(new URL(link));
                } catch (MalformedURLException ex) {
                    DialogManager.createError(RESOURCES.getString("link.error"), link).show();
                    LOG.severe("Error accessing link. Exception="+ex.getLocalizedMessage());
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        
    }

    /**
     * Finish editing a property through proxy
     */
    @Override
    protected void commitImpl(Property property) {
        if (!property.isReadOnly()) {
            // if commit, clear guessed flag
            property.setGuessed(false);
            property.setValue(tfield.getText());

            setPropertyImpl(property);
        }
    }

    /**
     * Editable depends on property
     */
    public boolean isEditable() {
        return tfield.isEditable();
    }

    /**
     * Set context to edit
     */
    public void setPropertyImpl(Property property) {

        removeAll();
        if (property == null) {
            tfield.setText("");
            tfield.setEditable(true);
            tfield.setVisible(true);
            gh.add(tfield, 0, 0, 1, 1, GridBagHelper.FILL_HORIZONTAL);
            defaultFocus = tfield;
            //add(BorderLayout.NORTH, tfield);
        } else {
            String txt = property.getDisplayValue();
            if (property.isReadOnly()) {
                tfieldRO.setText(txt);
                tfieldRO.setVisible(txt.length() > 0);
                defaultFocus = null;
                gh.add(tfieldRO, 0, 0, 1, 1, GridBagHelper.FILL_HORIZONTAL);
                //add(BorderLayout.NORTH, tfieldRO);
            } else {
                tfield.setText(txt);
                tfield.setEditable(true);
                tfield.setVisible(true);
                gh.add(tfield, 0, 0, 1, 1, GridBagHelper.FILL_HORIZONTAL);
                defaultFocus = tfield;
                //add(BorderLayout.NORTH, tfield);
            }
            if (txt.startsWith("http://") || txt.startsWith("https://")) {
                gh.add(webLink, 1, 0, 1, 1, GridBagHelper.FILL_HORIZONTAL);
            }
        }
        gh.addFiller(0, 1);
        
        // not changed
        changeSupport.setChanged(false);
    }

    @Override
    public synchronized void addKeyListener(KeyListener l) {
        tfield.addKeyListener(l);
    }

    public String getValue() {
        return tfield.getText();
    }
}

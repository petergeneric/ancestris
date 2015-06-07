/**
 * Ancestris - http://www.ancestris.org (Formerly GenJ - GenealogyJ)
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2010 - 2013 Ancestris
 * Author: Daniel Andre <daniel@ancestris.org>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.edit.actions;

import ancestris.util.swing.DialogManager;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Grammar;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.gedcom.TagPath;
import genj.gedcom.UnitOfWork;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.TextAreaWidget;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import modules.editors.gedcomproperties.GedcomPropertiesPlaceFormatPanel;
import modules.editors.gedcomproperties.utils.GedcomPlacesConverter;
import modules.editors.gedcomproperties.utils.PlaceFormatConverterPanel;
import modules.editors.gedcomproperties.utils.PlaceFormatInterface;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;

/**
 * Set the place hierarchy used in a gedcom file
 */
@ActionID(category = "Edit/Gedcom", id = "genj.edit.actions.SetPlaceHierarchy")
@ActionRegistration(displayName = "#place.hierarchy",
        lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Ancestris/Actions/GedcomProperty")})
public class SetPlaceHierarchy extends AbstractChange implements PlaceFormatInterface {

    private static final ImageIcon IMG = Grammar.V55.getMeta(TagPath.valueOf("INDI:BIRT:PLAC")).getImage();

    /** the place to use as the global example */
    private PropertyPlace place;

    /** main place panel */
    GedcomPropertiesPlaceFormatPanel placePanel;
            
    /** textfield for warning msg */
    private JLabel wrngMsg;

    /** place format converter */
    PlaceFormatConverterPanel pfc;


    
    public SetPlaceHierarchy() {
        super();
        setImageText(IMG, resources.getString("place.hierarchy"));
    }

    /**
     * Constructor
     */
    public SetPlaceHierarchy(PropertyPlace place) {
        this();
        this.place = place;
        contextChanged();
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends Property> props = lkpInfo.allInstances();
        if (props.size() != 1) {
            place = null;
        } else {
            Property p = props.iterator().next();
            if (p instanceof PropertyPlace) {
                place = (PropertyPlace) p;
            } else {
                place = null;
            }
        }
        super.resultChanged(ev);
    }

    @Override
    protected final void contextChanged() {
        if (place != null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }

    protected JComponent getComponent(String msg, int lines, Color color) {
        TextAreaWidget result = new TextAreaWidget(msg, lines, 40);
        result.setWrapStyleWord(true);
        result.setLineWrap(true);
        result.setEditable(false);
        result.setBackground(color);
        return new JScrollPane(result);
    }

    /**
     * no confirmation message needed
     */
    @Override
    protected String getConfirmMessage() {
        return "";
    }

    /**
     * Override content components to show to user to change place format
     */
    @Override
    protected JPanel getDialogContent() {
        JPanel result = new JPanel(new NestedBlockLayout("<col>"
                + "<row><text wx=\"1\" wy=\"1\" pad=\"5\"/></row>"
                + "<row><text wx=\"1\" wy=\"1\" pad=\"17\"/></row>"
                + "</col>"));        
        placePanel = new GedcomPropertiesPlaceFormatPanel(this);
        placePanel.setPLAC(getOriginalPlaceFormat());
        result.add(placePanel);
        wrngMsg = new JLabel("");
        // Add both lines below is warning is to be displayed (not so useful)
        //result.add(wrngMsg);
        //result.setPreferredSize(new Dimension(result.getPreferredSize().height, result.getPreferredSize().width));
        
        return result;
    }

    @Override
    public void warnVersionChange(boolean canBeConverted) {
        if (wrngMsg == null) {
            return;
        }
        if (canBeConverted) {
           wrngMsg.setText(NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "WNG_PlaceFormatChanged"));    
        } else {
           wrngMsg.setText("");    
        }
    }

    @Override
    public String getOriginalPlaceFormat() {
        return place.getGedcom().getPlaceFormat();
    }

    @Override
    public void setPlaceFormatConverter(PlaceFormatConverterPanel pfc) {
        this.pfc = pfc;
    }

    @Override
    public PlaceFormatConverterPanel getPlaceFormatConverter() {
        return this.pfc;
    }

    
    /**
     * Converts places to new place format
     * @param gedcom
     * @param event
     * @return 
     * @throws genj.gedcom.GedcomException
     */
    @Override
    protected Context execute(Gedcom gedcom, ActionEvent event) throws GedcomException {
        if (placePanel.getConversionToBeDone()) {
            Object o = DialogManager.createYesNo(
                    NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "GedcomPropertiesPlaceFormatPanel.jCheckBox1.text"),
                    NbBundle.getMessage(GedcomPropertiesPlaceFormatPanel.class, "WNG_ConfirmPlaceConversion")).setMessageType(DialogManager.YES_NO_OPTION).show();
            if (o == DialogManager.YES_OPTION) {
                String title = NbBundle.getMessage(SetPlaceHierarchy.class, "TITL_PlacesFormatModification");
                String msg = "";
                GedcomPlacesConverter placesConverter = new GedcomPlacesConverter(gedcom, getOriginalPlaceFormat(), placePanel.getPLAC(), pfc.getConversionMapAsString());
                if (placesConverter.convert()) {
                    msg = NbBundle.getMessage(SetPlaceHierarchy.class, "MSG_GedcomModified", placesConverter.getNbOfDifferentChangedPlaces(), placesConverter.getNbOfDifferentFoundPlaces());
                    place.getGedcom().setPlaceFormat(placePanel.getPLAC());
                } else {
                    msg = NbBundle.getMessage(SetPlaceHierarchy.class, "MSG_GedcomNotModified", placesConverter.getNbOfDifferentChangedPlaces(), placesConverter.getNbOfDifferentFoundPlaces());
                }
                DialogManager.create(title, msg).setMessageType(DialogManager.INFORMATION_MESSAGE).show();
            }
        }
        return new Context(place);
    }

    @Override
    public int getMode() {
        return GedcomPropertiesPlaceFormatPanel.DEFAULT_MODE;
    }
} //SetPlaceFormat


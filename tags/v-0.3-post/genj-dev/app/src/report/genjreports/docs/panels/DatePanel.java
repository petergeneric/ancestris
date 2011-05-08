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
package genjreports.docs.panels;

import genjreports.docs.HelperDocs;
import genjreports.docs.DataSet;

import genj.gedcom.*;
import genj.util.swing.*;
import genj.gedcom.time.PointInTime;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;


/**
 * The panel for entering an event date and official information
 */
public class DatePanel extends JPanel implements ActionListener {

  /** calling panel */
  private DocsListener panel = null;

  /** our gedcom */
  private Gedcom gedcom = null;

  /** list of entities */
  Entity[] list;

  /** event type */
  private String eventTag;

  /** flag */
  private boolean busy = false;

  /** others */
  static private Font PF = new Font("", Font.PLAIN, 12);
  static private Font BF = new Font("", Font.BOLD, 12);
  private DataSet dataSet;
  private int FS = 10;
  public ChoiceWidget refLISTD = new ChoiceWidget();
  public DateWidget refDATED = new DateWidget();
  public TextFieldWidget refAGNCD = new TextFieldWidget("", FS);
  public ChoiceWidget refPLACD = new ChoiceWidget();

  /**
   * Constructor
   */
  public DatePanel(Gedcom gedcom, DocsListener panel, DataSet dataSet, String eventTag) {


    super();

    this.gedcom = gedcom;
    this.panel = panel;
    this.eventTag = eventTag;
    this.dataSet = dataSet;

    this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), panel.translate("Date_PanelTitle")));

    // define elements of panel
    JLabel refLISTL = new JLabel(panel.translate("AlreadyRef"));
    refLISTL.setFont(PF);
    refLISTD.setValues(eventTag.equals("FAM:MARR") ? dataSet.familiesStr : dataSet.indisStr);
    refLISTD.addActionListener(this);
    refLISTD.setIgnoreCase(true);
    JLabel refDATEL = new JLabel(panel.translate("Date"));
    refDATEL.setFont(PF);
    refDATED.setFont(BF);
    JLabel refAGNCL = new JLabel(panel.translate("Agent"));
    refAGNCL.setFont(PF);
    refAGNCD.setFont(BF);
    JLabel refPLACL = new JLabel(panel.translate("Place"));
    refPLACL.setFont(PF);
    refPLACD.setValues(dataSet.places);
    refPLACD.setIgnoreCase(true);

     // set grid
    this.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;

    // position elements
    c.insets = new Insets(0, 15, 0, 0);
    c.gridx = 0; c.gridy = 0; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refLISTL, c);
    c.gridx = 1; c.gridy = 0; c.gridwidth = 3;
    this.add(refLISTD, c);

    c.gridx = 0; c.gridy = 1; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refDATEL, c);
    c.gridx = 1; c.gridy = 1;
    this.add(refDATED, c);
    c.gridx = 2; c.gridy = 1;
    this.add(refAGNCL, c);
    c.gridx = 3; c.gridy = 1; c.ipadx = 100; c.gridwidth = GridBagConstraints.REMAINDER;
    this.add(refAGNCD, c);

    c.gridx = 0; c.gridy = 2;
    this.add(refPLACL, c);
    c.gridx = 1; c.gridy = 2; c.gridwidth = GridBagConstraints.REMAINDER;
    this.add(refPLACD, c);

    }

  /**
   * Process action performed
   */
  public void actionPerformed(ActionEvent e) {

     if (e.getSource() == refLISTD.getEditor().getEditorComponent() && !busy) {
         setEntity(getEntity(refLISTD.getText()));
     	}
     }


  /**
   * Populates fields upon user selecting an entity
   *
   */
  public void setEntity(Entity ent) {

     Property prop = null;
     String str = "";

     busy = true;

     // List box
     String existingText = refLISTD.getTextEditor().getText().trim();
     if (existingText.length() == 0 && ent != null) {
    	 refLISTD.setText(ent.toString());
     }

     // Date
     if (ent != null) prop = ent.getPropertyByPath(eventTag+":DATE");
     if (prop != null && prop instanceof PropertyDate) {
        PropertyDate pdate = (PropertyDate) prop;
        refDATED.setValue(pdate.getStart());
        }
     else refDATED.setValue(new PointInTime());

     // Agent
     if (ent != null) prop = ent.getPropertyByPath(eventTag+":AGNC");
     if (prop != null) str = prop.toString(); else str = "";

     refAGNCD.setText(str);

     // Place
     if (ent != null) prop = ent.getPropertyByPath(eventTag+":PLAC");
     if (prop != null) str = prop.toString(); else str = "";
     refPLACD.setText(str);
     refPLACD.getTextEditor().setCaretPosition(0);

     // Propagate
     panel.populateAll(ent);

     busy = false;

     }

  /**
   * Get the family
   */
  public Fam getFamily() {

    String str = refLISTD.getText();
    return (str == null ? null : (Fam) getEntity(str));
    }


  /**
   * Get the indi
   */
  public Indi getIndi() {

    String str = refLISTD.getText();
    return (str == null ? null : (Indi) getEntity(str));
    }



  /**
   * Get date
   */
  public PointInTime getDate() {
    PointInTime pit = refDATED.getValue();
    if (pit == null || !pit.isValid() || !pit.isComplete()) return null;
    return pit;
    }

  /**
   * Get place
   */
  public String getPlace() {
    String strDocPlace = refPLACD.getText();
    if (strDocPlace == null || strDocPlace.length() == 0) return null;
    return strDocPlace;
    }


  /**
   * Shortcuts of Helper methods
   */
  public Entity getEntity(String ref) {
    return HelperDocs.getEntity(gedcom, ref);
    }

  public boolean exists(String str) {
    return HelperDocs.exists(str);
    }

  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    Dimension size = super.getPreferredSize();
    return new Dimension(dataSet.panelWidth == 0 ? (int)size.getWidth() : dataSet.panelWidth, (int)size.getHeight());
  }


}

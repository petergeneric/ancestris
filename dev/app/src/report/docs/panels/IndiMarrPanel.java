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
package docs.panels;

import docs.HelperDocs;
import docs.DataSet;

import genj.gedcom.*;
import genj.util.Registry;
import genj.util.GridBagHelper;
import genj.util.swing.*;
import genj.view.ContextProvider;
import genj.view.ViewContext;
import genj.gedcom.time.PointInTime;
import genj.gedcom.time.Delta;

import java.awt.*;
import java.awt.event.*;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EtchedBorder;


/**
 * The panel for entering people of a marriage document
 */
public class IndiMarrPanel extends JPanel implements ItemListener {

  /** calling panel */
  private DocsListener panel = null;

  /** our gedcom */
  private Gedcom gedcom = null;

  /** individuals */
  Entity[] indis;

  /** formatting */
  private int FS = 10;
  private DataSet dataSet;
  static private Font PF = new Font("", Font.PLAIN, 12);
  static private Font BF = new Font("", Font.BOLD, 12);

  public ChoiceWidget refINDID = new ChoiceWidget();
  public TextFieldWidget refSURND = new TextFieldWidget("", FS);
  public TextFieldWidget refGIVND = new TextFieldWidget("", FS);
  public TextFieldWidget refAGED = new TextFieldWidget("", FS);
  public ChoiceWidget refPLACD = new ChoiceWidget();
  public DateWidget refDATED = new DateWidget();
  public ChoiceWidget refOCCUD = new ChoiceWidget();
  public ChoiceWidget refRESID = new ChoiceWidget();

  public ChoiceWidget refFINDID = new ChoiceWidget();
  public TextFieldWidget refFSURND = new TextFieldWidget("", FS);
  public TextFieldWidget refFGIVND = new TextFieldWidget("", FS);
  public TextFieldWidget refFAGED = new TextFieldWidget("", FS);
  public ChoiceWidget refFBPLACD = new ChoiceWidget();
  public DateWidget refFBDATED = new DateWidget();
  public ChoiceWidget refFOCCUD = new ChoiceWidget();
  public ChoiceWidget refFRESID = new ChoiceWidget();
  public ChoiceWidget refFDPLACD = new ChoiceWidget();
  public DateWidget refFDDATED = new DateWidget();

  public ChoiceWidget refMINDID = new ChoiceWidget();
  public TextFieldWidget refMSURND = new TextFieldWidget("", FS);
  public TextFieldWidget refMGIVND = new TextFieldWidget("", FS);
  public TextFieldWidget refMAGED = new TextFieldWidget("", FS);
  public ChoiceWidget refMBPLACD = new ChoiceWidget();
  public DateWidget refMBDATED = new DateWidget();
  public ChoiceWidget refMOCCUD = new ChoiceWidget();
  public ChoiceWidget refMRESID = new ChoiceWidget();
  public ChoiceWidget refMDPLACD = new ChoiceWidget();
  public DateWidget refMDDATED = new DateWidget();

  /**
   * Constructor
   */
  public IndiMarrPanel(Gedcom gedcom, DocsListener panel, boolean male, DataSet dataSet) {

    super();

    this.panel = panel;
    this.gedcom = gedcom;
    this.indis = dataSet.indis;
    this.dataSet = dataSet;
    this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), male ? panel.translate("mar_Groom") : panel.translate("mar_Bride")));

    // define elements of panel
    JLabel refINDIL = new JLabel(male ? panel.translate("mar_MaleRef") : panel.translate("mar_FemaleRef"));
    refINDIL.setFont(PF);
    refINDID.setValues(indis);
    refINDID.addItemListener(this);
    JLabel refSURNL = new JLabel(panel.translate("mar_Last"));
    refSURNL.setFont(PF);
    refSURND.setFont(BF);
    JLabel refGIVNL = new JLabel(panel.translate("mar_First"));
    refGIVNL.setFont(PF);
    refGIVND.setFont(BF);
    JLabel refAGEL = new JLabel(male ? panel.translate("mar_MaleAge") : panel.translate("mar_FemaleAge"));
    refAGEL.setFont(PF);
    refAGED.setFont(BF);
    JLabel refPLACL = new JLabel(male ? panel.translate("mar_MaleBorn") : panel.translate("mar_FemaleBorn"));
    refPLACL.setFont(PF);
    refPLACD.setValues(dataSet.places);
    JLabel refDATEL = new JLabel(panel.translate("mar_On"));
    refDATEL.setFont(PF);
    refDATED.setFont(BF);
    JLabel refOCCUL = new JLabel(panel.translate("mar_Occu"));
    refOCCUL.setFont(PF);
    refOCCUD.setValues(dataSet.occupations);
    JLabel refRESIL = new JLabel(panel.translate("mar_Place"));
    refRESIL.setFont(PF);
    refRESID.setValues(dataSet.places);

    JLabel refFATHERL = new JLabel(male ? panel.translate("mar_SonOf") : panel.translate("mar_DaughterOf"));
    JLabel refFINDIL = new JLabel(panel.translate("mar_MaleRef"));
    refFINDIL.setFont(PF);
    refFINDID.setValues(indis);
    refFINDID.addItemListener(this);
    JLabel refFSURNL = new JLabel(panel.translate("mar_Last"));
    refFSURNL.setFont(PF);
    refFSURND.setFont(BF);
    JLabel refFGIVNL = new JLabel(panel.translate("mar_First"));
    refFGIVNL.setFont(PF);
    refFGIVND.setFont(BF);
    JLabel refFAGEL = new JLabel(panel.translate("mar_MaleAge"));
    refFAGEL.setFont(PF);
    refFAGED.setFont(BF);
    JLabel refFBPLACL = new JLabel(panel.translate("mar_MaleBorn"));
    refFBPLACL.setFont(PF);
    refFBPLACD.setValues(dataSet.places);
    JLabel refFBDATEL = new JLabel(panel.translate("mar_On"));
    refFBDATEL.setFont(PF);
    refFBDATED.setFont(BF);
    JLabel refFOCCUL = new JLabel(panel.translate("mar_Occu"));
    refFOCCUL.setFont(PF);
    refFOCCUD.setValues(dataSet.occupations);
    JLabel refFRESIL = new JLabel(panel.translate("mar_Place"));
    refFRESIL.setFont(PF);
    refFRESID.setValues(dataSet.places);
    JLabel refFDPLACL = new JLabel(panel.translate("mar_MaleDied"));
    refFDPLACL.setFont(PF);
    refFDPLACD.setValues(dataSet.places);
    JLabel refFDDATEL = new JLabel(panel.translate("mar_On"));
    refFDDATEL.setFont(PF);
    refFDDATED.setFont(BF);

    JLabel refMOTHERL = new JLabel(panel.translate("mar_AndOf"));
    JLabel refMINDIL = new JLabel(panel.translate("mar_FemaleRef"));
    refMINDIL.setFont(PF);
    refMINDID.setValues(indis);
    refMINDID.addItemListener(this);
    JLabel refMSURNL = new JLabel(panel.translate("mar_Last"));
    refMSURNL.setFont(PF);
    refMSURND.setFont(BF);
    JLabel refMGIVNL = new JLabel(panel.translate("mar_First"));
    refMGIVNL.setFont(PF);
    refMGIVND.setFont(BF);
    JLabel refMAGEL = new JLabel(panel.translate("mar_FemaleAge"));
    refMAGEL.setFont(PF);
    refMAGED.setFont(BF);
    JLabel refMBPLACL = new JLabel(panel.translate("mar_FemaleBorn"));
    refMBPLACL.setFont(PF);
    refMBPLACD.setValues(dataSet.places);
    JLabel refMBDATEL = new JLabel(panel.translate("mar_On"));
    refMBDATEL.setFont(PF);
    refMBDATED.setFont(BF);
    JLabel refMOCCUL = new JLabel(panel.translate("mar_Occu"));
    refMOCCUL.setFont(PF);
    refMOCCUD.setValues(dataSet.occupations);
    JLabel refMRESIL = new JLabel(panel.translate("mar_Place"));
    refMRESIL.setFont(PF);
    refMRESID.setValues(dataSet.places);
    JLabel refMDPLACL = new JLabel(panel.translate("mar_FemaleDied"));
    refMDPLACL.setFont(PF);
    refMDPLACD.setValues(dataSet.places);
    JLabel refMDDATEL = new JLabel(panel.translate("mar_On"));
    refMDDATEL.setFont(PF);
    refMDDATED.setFont(BF);

     // set grid
    this.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;

    // position elements
    c.gridx = 0; c.gridy = 0; c.gridwidth = 1; c.weightx = 1.0; c.insets = new Insets(0, 15, 0, 0);
    this.add(refINDIL, c);
    c.gridx = 1; c.gridy = 0; c.gridwidth = 3;
    this.add(refINDID, c);

    c.gridx = 0; c.gridy = 1; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refSURNL, c);
    c.gridx = 1; c.gridy = 1;
    this.add(refSURND, c);
    c.gridx = 2; c.gridy = 1;
    this.add(refGIVNL, c);
    c.gridx = 3; c.gridy = 1; 
    this.add(refGIVND, c);
    c.gridx = 4; c.gridy = 1;
    this.add(refAGEL, c);
    c.gridx = 5; c.gridy = 1;
    this.add(refAGED, c);

    c.gridx = 0; c.gridy = 2; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refPLACL, c);
    c.gridx = 1; c.gridy = 2;
    this.add(refPLACD, c);
    c.gridx = 2; c.gridy = 2;
    this.add(refDATEL, c);
    c.gridx = 3; c.gridy = 2;
    this.add(refDATED, c);
    c.gridx = 4; c.gridy = 2;
    this.add(refOCCUL, c);
    c.gridx = 5; c.gridy = 2;
    this.add(refOCCUD, c);

    c.gridx = 0; c.gridy = 3; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refRESIL, c);
    c.gridx = 1; c.gridy = 3; c.gridwidth = 3;
    this.add(refRESID, c);
    c.gridx = 2; c.gridy = 3;



    c.gridx = 0; c.gridy = 4; c.gridwidth = 1; c.weightx = 1.0; c.insets = new Insets(10, 0, 0, 0);
    this.add(refFATHERL, c);

    c.insets = new Insets(0, 15, 0, 0);

    c.gridx = 0; c.gridy = 5; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refFINDIL, c);
    c.gridx = 1; c.gridy = 5; c.gridwidth = 3;
    this.add(refFINDID, c);

    c.gridx = 0; c.gridy = 6; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refFSURNL, c);
    c.gridx = 1; c.gridy = 6;
    this.add(refFSURND, c);
    c.gridx = 2; c.gridy = 6;
    this.add(refFGIVNL, c);
    c.gridx = 3; c.gridy = 6;
    this.add(refFGIVND, c);
    c.gridx = 4; c.gridy = 6;
    this.add(refFAGEL, c);
    c.gridx = 5; c.gridy = 6;
    this.add(refFAGED, c);

    c.gridx = 0; c.gridy = 7; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refFBPLACL, c);
    c.gridx = 1; c.gridy = 7;
    this.add(refFBPLACD, c);
    c.gridx = 2; c.gridy = 7;
    this.add(refFBDATEL, c);
    c.gridx = 3; c.gridy = 7;
    this.add(refFBDATED, c);
    c.gridx = 4; c.gridy = 7;
    this.add(refFOCCUL, c);
    c.gridx = 5; c.gridy = 7;
    this.add(refFOCCUD, c);

    c.gridx = 0; c.gridy = 8; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refFDPLACL, c);
    c.gridx = 1; c.gridy = 8;
    this.add(refFDPLACD, c);
    c.gridx = 2; c.gridy = 8;
    this.add(refFDDATEL, c);
    c.gridx = 3; c.gridy = 8;
    this.add(refFDDATED, c);
    c.gridx = 4; c.gridy = 8;
    this.add(refFRESIL, c);
    c.gridx = 5; c.gridy = 8;
    this.add(refFRESID, c);


    c.gridx = 0; c.gridy = 9; c.gridwidth = 1; c.weightx = 1.0; c.insets = new Insets(10, 0, 0, 0);
    this.add(refMOTHERL, c);

    c.insets = new Insets(0, 15, 0, 0);

    c.gridx = 0; c.gridy = 10; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refMINDIL, c);
    c.gridx = 1; c.gridy = 10; c.gridwidth = 3;
    this.add(refMINDID, c);

    c.gridx = 0; c.gridy = 11; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refMSURNL, c);
    c.gridx = 1; c.gridy = 11;
    this.add(refMSURND, c);
    c.gridx = 2; c.gridy = 11;
    this.add(refMGIVNL, c);
    c.gridx = 3; c.gridy = 11;
    this.add(refMGIVND, c);
    c.gridx = 4; c.gridy = 11;
    this.add(refMAGEL, c);
    c.gridx = 5; c.gridy = 11;
    this.add(refMAGED, c);

    c.gridx = 0; c.gridy = 12; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refMBPLACL, c);
    c.gridx = 1; c.gridy = 12;
    this.add(refMBPLACD, c);
    c.gridx = 2; c.gridy = 12;
    this.add(refMBDATEL, c);
    c.gridx = 3; c.gridy = 12;
    this.add(refMBDATED, c);
    c.gridx = 4; c.gridy = 12;
    this.add(refMOCCUL, c);
    c.gridx = 5; c.gridy = 12;
    this.add(refMOCCUD, c);

    c.gridx = 0; c.gridy = 13; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refMDPLACL, c);
    c.gridx = 1; c.gridy = 13;
    this.add(refMDPLACD, c);
    c.gridx = 2; c.gridy = 13;
    this.add(refMDDATEL, c);
    c.gridx = 3; c.gridy = 13;
    this.add(refMDDATED, c);
    c.gridx = 4; c.gridy = 13;
    this.add(refMRESIL, c);
    c.gridx = 5; c.gridy = 13;
    this.add(refMRESID, c);
}


  /**
   * Display individual given by indi
   */
  public void setIndi(Indi indi) {

    if (indi == null) {
       refINDID.setSelectedIndex(-1);
       return;
       }
    String str = indi.toString();
    for (int i = 0 ; i < indis.length ; i++) {
       if (indis[i].toString().equals(str)) {
          refINDID.setSelectedIndex(i);
          refINDID.getTextEditor().setCaretPosition(0);
          break;
          }
       }
    return;
    }

  /**
   * Display father of indi
   */
  public int getParent(ChoiceWidget refINDI, boolean father) {

    Indi indi = getIndi(refINDI);
    if (indi == null) return -1;
    Fam fam = indi.getFamilyWhereBiologicalChild();
    if (fam == null) return -1;
    Indi parent = father ? fam.getHusband() : fam.getWife();
    if (parent == null) return -1;
    String str = parent.toString();
    for (int i = 0 ; i < indis.length ; i++) {
       if (indis[i].toString().equals(str)) {
          return i;
          }
       }
    return -1;
    }

  /**
   * Selection list changed performed
   * --> populate indi: indi to fields
   */
  public void itemStateChanged(ItemEvent e) {

     if (e.getItemSelectable() == refINDID) {
        populateIndi(refINDID, refSURND, refGIVND, refAGED, refPLACD, refDATED, refOCCUD, refRESID, null, null);
        refFINDID.setSelectedIndex(getParent(refINDID, true));
        refFINDID.getTextEditor().setCaretPosition(0);
        refMINDID.setSelectedIndex(getParent(refINDID, false));
        refMINDID.getTextEditor().setCaretPosition(0);
        }
     if (e.getItemSelectable() == refFINDID) {
        populateIndi(refFINDID, refFSURND, refFGIVND, refFAGED, refFBPLACD, refFBDATED, refFOCCUD, refFRESID, refFDPLACD, refFDDATED);
        }
     if (e.getItemSelectable() == refMINDID) {
        populateIndi(refMINDID, refMSURND, refMGIVND, refMAGED, refMBPLACD, refMBDATED, refMOCCUD, refMRESID, refMDPLACD, refMDDATED);
        }
     }


  /**
   * Populates fields upon user selecting an individual in one of the list boxes
   *
   */
  private void populateIndi(ChoiceWidget refINDI, TextFieldWidget surn, TextFieldWidget givn, TextFieldWidget age, ChoiceWidget bplac, DateWidget bdate, ChoiceWidget occu, ChoiceWidget resi, ChoiceWidget dplac, DateWidget ddate) {

     Indi indi = getIndi(refINDI);
     Property prop = null;
     String str = "";

     // Surname
     if (indi != null) prop = indi.getPropertyByPath("INDI:NAME:SURN");
     if (prop != null) str = prop.toString(); else str = "";
     surn.setText(str);

     // Given names
     if (indi != null) prop = indi.getPropertyByPath("INDI:NAME:GIVN");
     if (prop != null) str = prop.toString(); else str = "";
     givn.setText(str);

     // Age depends on the so get it from parent panel
     if (indi != null && panel != null && indi.getBirthDate() != null) {
        String ageCalc = HelperDocs.calcAge(panel.getDate(), indi.getBirthDate().getStart());
        if (ageCalc != null) str = ageCalc; else str = "";
        }
     else str = "";
     age.setText(str);

     // Birth place
     if (indi != null) prop = indi.getPropertyByPath("INDI:BIRT:PLAC");
     if (prop != null) str = prop.toString(); else str = "";
     bplac.setText(str);
     bplac.getTextEditor().setCaretPosition(0);

     // Birth date
     if (indi != null) prop = indi.getPropertyByPath("INDI:BIRT:DATE");
     if (prop != null && prop instanceof PropertyDate) {
        PropertyDate pdate = (PropertyDate) prop;
        bdate.setValue(pdate.getStart());
        }
     else bdate.setValue(new PointInTime());

     // Occupation
     if (indi != null) prop = indi.getPropertyByPath("INDI:OCCU");
     if (prop != null) str = prop.toString(); else str = "";
     occu.setText(str);

     // Residence
     if (indi != null) prop = indi.getPropertyByPath("INDI:RESI:PLAC");
     if (prop != null) str = prop.toString(); else str = "";
     resi.setText(str);
     resi.getTextEditor().setCaretPosition(0);

     // Death place
     if (dplac != null) {
        if (indi != null) prop = indi.getPropertyByPath("INDI:DEAT:PLAC");
        if (prop != null) str = prop.toString(); else str = "";
        dplac.setText(str);
        dplac.getTextEditor().setCaretPosition(0);
        }

     // Death date
     if (ddate != null) {
        if (indi != null) prop = indi.getPropertyByPath("INDI:DEAT:DATE");
        if (prop != null && prop instanceof PropertyDate) {
           PropertyDate pdate = (PropertyDate) prop;
           ddate.setValue(pdate.getStart());
           }
        else ddate.setValue(new PointInTime());
        }

     }


  /**
   * Get individual represented in the main combobox
   */
  public Indi getIndi() {
    return getIndi(refINDID);
    }

  public Indi getIndi(ChoiceWidget refINDI) {
    String ref = refINDI.getText();
    return (Indi) HelperDocs.getEntity(gedcom, ref);
    }

  /**
   * Set age of main individual
   */
  public void setAge(String age) {
    refAGED.setText(age);
    }

  /**
   * Check for individual
   */
  public boolean isOK(int i) {
    if (i == 1) return isOK(refINDID, refSURND, refGIVND, refAGED, refPLACD, refDATED, refOCCUD, refRESID, null, null);
    if (i == 2) return isOK(refFINDID, refFSURND, refFGIVND, refFAGED, refFBPLACD, refFBDATED, refFOCCUD, refFRESID, refFDPLACD, refFDDATED);
    if (i == 3) return isOK(refMINDID, refMSURND, refMGIVND, refMAGED, refMBPLACD, refMBDATED, refMOCCUD, refMRESID, refMDPLACD, refMDDATED);
    return false;
    }

  private boolean isOK(ChoiceWidget refINDI, TextFieldWidget surn, TextFieldWidget givn, TextFieldWidget age, ChoiceWidget bplac, DateWidget bdate, ChoiceWidget occu, ChoiceWidget resi, ChoiceWidget dplac, DateWidget ddate) {
    String ref = refINDI.getText();
    if (exists(ref)) return true;
    boolean name = exists(surn.getText()) || exists(givn.getText());
    boolean info = exists(age.getText()) || exists(bplac.getText()) || exists(bdate.getValue()) || exists(occu.getText()) || exists(resi.getText());
    boolean info2 = (dplac == null ? true : exists(dplac.getText()) || exists(ddate.getValue()));
    if ((info || info2) && !name) return false;
    return (name || info);
    }

  /**
   * Create or update individual
   */
  public Indi upcreateIndi(boolean male, PointInTime pitDocDate, int i) throws GedcomException {
    if (i == 1) return HelperDocs.upcreateIndi(gedcom, male, pitDocDate, refINDID, refSURND, refGIVND, refAGED, refPLACD, refDATED, refOCCUD, refRESID, null, null);
    if (i == 2) return HelperDocs.upcreateIndi(gedcom, male, pitDocDate, refFINDID, refFSURND, refFGIVND, refFAGED, refFBPLACD, refFBDATED, refFOCCUD, refFRESID, refFDPLACD, refFDDATED);
    if (i == 3) return HelperDocs.upcreateIndi(gedcom, male, pitDocDate, refMINDID, refMSURND, refMGIVND, refMAGED, refMBPLACD, refMBDATED, refMOCCUD, refMRESID, refMDPLACD, refMDDATED);
    return null;
    }


  /**
   * Shortcuts of Helper methods
   */
  public boolean exists(String str) {
    return HelperDocs.exists(str);
    }
  private boolean exists(PointInTime pit) {
    return HelperDocs.exists(pit);
    }


  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    Dimension size = super.getPreferredSize();
    return new Dimension(dataSet.panelWidth == 0 ? (int)size.getWidth() : dataSet.panelWidth, (int)size.getHeight());
  }


}
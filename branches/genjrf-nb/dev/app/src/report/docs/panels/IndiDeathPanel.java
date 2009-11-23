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
import genj.util.swing.*;
import genj.gedcom.time.PointInTime;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;


/**
 * The panel for entering people of a death document
 */
public class IndiDeathPanel extends JPanel implements ActionListener {

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

  public TextFieldWidget refSURND = new TextFieldWidget("", FS);
  public TextFieldWidget refGIVND = new TextFieldWidget("", FS);
  public JCheckBox refMale;
  public TextFieldWidget refAGED = new TextFieldWidget("", FS);
  public ChoiceWidget refOCCUD = new ChoiceWidget();
  public ChoiceWidget refBPLACD = new ChoiceWidget();
  public DateWidget refBDATED = new DateWidget();

  public ChoiceWidget refSINDID = new ChoiceWidget();
  public TextFieldWidget refSSURND = new TextFieldWidget("", FS);
  public TextFieldWidget refSGIVND = new TextFieldWidget("", FS);
  public ChoiceWidget refSOCCUD = new ChoiceWidget();
  public ChoiceWidget refSDPLACD = new ChoiceWidget();
  public DateWidget refSDDATED = new DateWidget();
  public ChoiceWidget refSRESID = new ChoiceWidget();

  public ChoiceWidget refFINDID = new ChoiceWidget();
  public TextFieldWidget refFSURND = new TextFieldWidget("", FS);
  public TextFieldWidget refFGIVND = new TextFieldWidget("", FS);

  public ChoiceWidget refMINDID = new ChoiceWidget();
  public TextFieldWidget refMSURND = new TextFieldWidget("", FS);
  public TextFieldWidget refMGIVND = new TextFieldWidget("", FS);

  public DateWidget refDDATED = new DateWidget();
  public ChoiceWidget refRESID = new ChoiceWidget();


  /**
   * Constructor
   */
  public IndiDeathPanel(Gedcom gedcom, DocsListener panel, DataSet dataSet) {

    super();

    this.panel = panel;
    this.gedcom = gedcom;
    this.indis = dataSet.indis;
    this.dataSet = dataSet;

    this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), panel.translate("Dea_Declare")));

    // define elements of panel
    JLabel refINDIL = new JLabel(panel.translate("Dea_HaveDeclared"));
    JLabel refSURNL = new JLabel(panel.translate("Dea_Last"));
    refSURNL.setFont(PF);
    refSURND.setFont(BF);
    JLabel refGIVNL = new JLabel(panel.translate("Dea_First"));
    refGIVNL.setFont(PF);
    refGIVND.setFont(BF);
    refMale = new JCheckBox(panel.translate("Dea_Male"));
    refMale.setFont(PF);
    refMale.setSelected(true);
    JLabel refAGEL = new JLabel(panel.translate("Dea_AgeOf"));
    refAGEL.setFont(PF);
    refAGED.setFont(BF);
    JPanel refAge = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    refAge.add(refAGEL);
    refAge.add(refAGED);
    JLabel refOCCUL = new JLabel(panel.translate("Dea_Occu"));
    refOCCUL.setFont(PF);
    refOCCUD.setValues(dataSet.occupations);
    refOCCUD.setIgnoreCase(true);
    JLabel refBPLACL = new JLabel(panel.translate("Dea_BornAt"));
    refBPLACL.setFont(PF);
    refBPLACD.setValues(dataSet.places);
    refBPLACD.setIgnoreCase(true);
    JLabel refBDATEL = new JLabel(panel.translate("Dea_On"));
    refBDATEL.setFont(PF);
    refBDATED.setFont(BF);


    JLabel refSPOUSEL = new JLabel(panel.translate("Dea_SpouseOf"));
    JLabel refSINDIL = new JLabel(panel.translate("Dea_MaleRef"));
    refSINDIL.setFont(PF);
    refSINDID.setValues(dataSet.indisStr);
    refSINDID.addActionListener(this);
    refSINDID.setIgnoreCase(true);
    JLabel refSSURNL = new JLabel(panel.translate("Dea_Last"));
    refSSURNL.setFont(PF);
    refSSURND.setFont(BF);
    JLabel refSGIVNL = new JLabel(panel.translate("Dea_First"));
    refSGIVNL.setFont(PF);
    refSGIVND.setFont(BF);
    JLabel refSOCCUL = new JLabel(panel.translate("Dea_Occu"));
    refSOCCUL.setFont(PF);
    refSOCCUD.setValues(dataSet.occupations);
    refSOCCUD.setIgnoreCase(true);
    JLabel refSDPLACL = new JLabel(panel.translate("Dea_DiedAt"));
    refSDPLACL.setFont(PF);
    refSDPLACD.setValues(dataSet.places);
    refSDPLACD.setIgnoreCase(true);
    JLabel refSDDATEL = new JLabel(panel.translate("Dea_On"));
    refSDDATEL.setFont(PF);
    refSDDATED.setFont(BF);
    JLabel refSRESIL = new JLabel(panel.translate("Dea_Place"));
    refSRESIL.setFont(PF);
    refSRESID.setValues(dataSet.places);
    refSRESID.setIgnoreCase(true);

    JLabel refFATHERL = new JLabel(panel.translate("Dea_SonDauOf"));
    JLabel refFINDIL = new JLabel(panel.translate("Dea_MaleRef"));
    refFINDIL.setFont(PF);
    refFINDID.setValues(dataSet.indisStr);
    refFINDID.addActionListener(this);
    refFINDID.setIgnoreCase(true);
    JLabel refFSURNL = new JLabel(panel.translate("Dea_Last"));
    refFSURNL.setFont(PF);
    refFSURND.setFont(BF);
    JLabel refFGIVNL = new JLabel(panel.translate("Dea_First"));
    refFGIVNL.setFont(PF);
    refFGIVND.setFont(BF);

    JLabel refMOTHERL = new JLabel(panel.translate("Dea_AndOf"));
    JLabel refMINDIL = new JLabel(panel.translate("Dea_FemaleRef"));
    refMINDIL.setFont(PF);
    refMINDID.setValues(dataSet.indisStr);
    refMINDID.addActionListener(this);
    refMINDID.setIgnoreCase(true);
    JLabel refMSURNL = new JLabel(panel.translate("Dea_Last"));
    refMSURNL.setFont(PF);
    refMSURND.setFont(BF);
    JLabel refMGIVNL = new JLabel(panel.translate("Dea_First"));
    refMGIVNL.setFont(PF);
    refMGIVND.setFont(BF);

    JLabel refDEATHL = new JLabel(panel.translate("Dea_Died"));
    JLabel refDDATEL = new JLabel(panel.translate("Dea_On"));
    refDDATEL.setFont(PF);
    refDDATED.setFont(BF);
    JLabel refRESIL = new JLabel(panel.translate("Dea_Lived"));
    refRESIL.setFont(PF);
    refRESID.setValues(dataSet.places);
    refRESID.setIgnoreCase(true);

     // set grid
    this.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;


    // position elements
    c.gridx = 0; c.gridy = 0; c.gridwidth = 3; c.weightx = 1.0; c.insets = new Insets(10, 0, 0, 0);
    this.add(refINDIL, c);
    c.insets = new Insets(0, 15, 0, 0);
    c.gridx = 0; c.gridy++; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refSURNL, c);
    c.gridx = 1;
    this.add(refSURND, c);
    c.gridx = 2;
    this.add(refGIVNL, c);
    c.gridx = 3;
    this.add(refGIVND, c);

    c.gridx = 0; c.gridy++;
    this.add(refMale, c);
    c.gridx = 1;
    this.add(refAge, c);
    c.gridx = 2;c.weightx = 1.0; c.gridwidth = 1;
    this.add(refOCCUL, c);
    c.gridx = 3;
    this.add(refOCCUD, c);

    c.gridx = 0; c.gridy++;
    this.add(refBPLACL, c);
    c.gridx = 1;
    this.add(refBPLACD, c);
    c.gridx = 2;
    this.add(refBDATEL, c);
    c.gridx = 3;
    this.add(refBDATED, c);


    c.gridx = 0; c.gridy++; c.gridwidth = 3; c.weightx = 1.0; c.insets = new Insets(10, 0, 0, 0);
    this.add(refSPOUSEL, c);
    c.insets = new Insets(5, 15, 0, 0);
    c.gridx = 0; c.gridy++; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refSINDIL, c);
    c.gridx = 1; c.gridwidth = 3;
    this.add(refSINDID, c);

    c.insets = new Insets(0, 15, 0, 0);
    c.gridx = 0; c.gridy++; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refSSURNL, c);
    c.gridx = 1;
    this.add(refSSURND, c);
    c.gridx = 2;
    this.add(refSGIVNL, c);
    c.gridx = 3;
    this.add(refSGIVND, c);

    c.gridx = 0; c.gridy++; 
    this.add(refSRESIL, c);
    c.gridx = 1;
    this.add(refSRESID, c);
    c.gridx = 2;
    this.add(refSOCCUL, c);
    c.gridx = 3;
    this.add(refSOCCUD, c);

    c.gridx = 0; c.gridy++;
    this.add(refSDPLACL, c);
    c.gridx = 1;
    this.add(refSDPLACD, c);
    c.gridx = 2;
    this.add(refSDDATEL, c);
    c.gridx = 3;
    this.add(refSDDATED, c);


    c.gridx = 0; c.gridy++; c.gridwidth = 3; c.weightx = 1.0; c.insets = new Insets(10, 0, 0, 0);
    this.add(refFATHERL, c);
    c.insets = new Insets(5, 15, 0, 0);
    c.gridx = 0; c.gridy++; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refFINDIL, c);
    c.gridx = 1; c.gridwidth = 3;
    this.add(refFINDID, c);

    c.insets = new Insets(0, 15, 0, 0);
    c.gridx = 0; c.gridy++; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refFSURNL, c);
    c.gridx = 1;
    this.add(refFSURND, c);
    c.gridx = 2;
    this.add(refFGIVNL, c);
    c.gridx = 3;
    this.add(refFGIVND, c);


    c.gridx = 0; c.gridy++; c.gridwidth = 3; c.weightx = 1.0; c.insets = new Insets(10, 0, 0, 0);
    this.add(refMOTHERL, c);
    c.insets = new Insets(5, 15, 0, 0);
    c.gridx = 0; c.gridy++; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refMINDIL, c);
    c.gridx = 1; c.gridwidth = 3;
    this.add(refMINDID, c);

    c.insets = new Insets(0, 15, 0, 0);
    c.gridx = 0; c.gridy++; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refMSURNL, c);
    c.gridx = 1;
    this.add(refMSURND, c);
    c.gridx = 2;
    this.add(refMGIVNL, c);
    c.gridx = 3;
    this.add(refMGIVND, c);



    c.gridx = 0; c.gridy++; c.gridwidth = 3; c.weightx = 1.0; c.insets = new Insets(10, 0, 0, 0);
    this.add(refDEATHL, c);
    c.insets = new Insets(5, 15, 3, 0);
    c.gridx = 0; c.gridy++; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refDDATEL, c);
    c.gridx = 1;
    this.add(refDDATED, c);
    c.gridx = 2;
    this.add(refRESIL, c);
    c.gridx = 3;
    this.add(refRESID, c);

}


  /**
   * Display individual given by indi
   */
  public void setIndi(Indi deceased, Indi father, Indi mother, Indi spouse) {

    // Clear form is null
    if (deceased == null) {
       refSINDID.setText("");
       refFINDID.setText("");
       refMINDID.setText("");
       populateDeceased(null);
       populateSpouse();
       populateFather();
       populateMother();
       return;
       }

    // Populate child
    populateDeceased(deceased);

    // Get and populate spouse
    if (spouse != null) {
       String str = spouse.toString();
       for (int i = 0 ; i < indis.length ; i++) {
          if (indis[i].toString().equals(str)) {
             refSINDID.setSelectedIndex(i);
             refSINDID.getTextEditor().setCaretPosition(0);
             break;
             }
          }
       populateSpouse();
       }

    // Get and populate father
    if (father != null) {
       String str = father.toString();
       for (int i = 0 ; i < indis.length ; i++) {
          if (indis[i].toString().equals(str)) {
             refFINDID.setSelectedIndex(i);
             refFINDID.getTextEditor().setCaretPosition(0);
             break;
             }
          }
       populateFather();
       }

    // Get and populate mother
    if (mother != null) {
       String str = mother.toString();
       for (int i = 0 ; i < indis.length ; i++) {
          if (indis[i].toString().equals(str)) {
             refMINDID.setSelectedIndex(i);
             refMINDID.getTextEditor().setCaretPosition(0);
             break;
             }
          }
       populateMother();
       }

    return;
    }

  /**
   * Process action performed
   */
  public void actionPerformed(ActionEvent e) {

     if (e.getSource() == refSINDID.getEditor().getEditorComponent()) {
    	 populateSpouse();
     	}
     if (e.getSource() == refFINDID.getEditor().getEditorComponent()) {
    	 populateFather();
     	}
     if (e.getSource() == refMINDID.getEditor().getEditorComponent()) {
    	 populateMother();
     	}
     }


  /**
   * Populate father
   */
  public void populateFather() {
	  populateIndi(refFINDID, refFSURND, refFGIVND, null, null, null, null);
  }

  /**
   * Populate mother
   */
  public void populateMother() {
	  populateIndi(refMINDID, refMSURND, refMGIVND, null, null, null, null);
  }

  /**
   * Populate spouse
   */
  public void populateSpouse() {
	  populateIndi(refSINDID, refSSURND, refSGIVND, refSDPLACD, refSDDATED, refSOCCUD, refSRESID);
  }

  /**
   * Get individual represented in the main combobox
   */
  public Indi getIndi(ChoiceWidget refINDI) {
    String ref = refINDI.getText();
    return (Indi) HelperDocs.getEntity(gedcom, ref);
    }


  /**
   * Populates fields upon user selecting an individual in one of the list boxes
   *
   */
  private void populateIndi(ChoiceWidget refINDI, TextFieldWidget surn, TextFieldWidget givn, ChoiceWidget dplac, DateWidget ddate, ChoiceWidget occu, ChoiceWidget resi) {

     Indi indi = getIndi(refINDI);
     Property prop = null;
     String str = "";

     // Surname
     if (indi != null) prop = indi.getPropertyByPath("INDI:NAME:SURN");
     if (prop != null) str = prop.toString(); else str = (indi != null ? indi.getLastName() : "");
     surn.setText(str);

     // Given names
     if (indi != null) prop = indi.getPropertyByPath("INDI:NAME:GIVN");
     if (prop != null) str = prop.toString(); else str = (indi != null ? indi.getFirstName() : "");
     givn.setText(str);

     // Occupation
     if (occu != null) {
        if (indi != null) prop = indi.getPropertyByPath("INDI:OCCU");
        if (prop != null) str = prop.toString(); else str = "";
        occu.setText(str);
        }

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

     // Residence
     if (resi != null) {
        if (indi != null) prop = indi.getPropertyByPath("INDI:RESI:PLAC");
        if (prop != null) str = prop.toString(); else str = "";
        resi.setText(str);
        resi.getTextEditor().setCaretPosition(0);
        }

     }


  /**
   * Populate child fields
   */
  public void populateDeceased(Indi indi) {

     Property prop = null;
     String str = "";

     // Surname
     if (indi != null) prop = indi.getPropertyByPath("INDI:NAME:SURN");
     if (prop != null) str = prop.toString(); else str = (indi != null ? indi.getLastName() : "");
     refSURND.setText(str);

     // Given names
     if (indi != null) prop = indi.getPropertyByPath("INDI:NAME:GIVN");
     if (prop != null) str = prop.toString(); else str = (indi != null ? indi.getFirstName() : "");
     refGIVND.setText(str);

     // Sex
     if (indi != null) {
        refMale.setSelected(indi.getSex() == 1);
        }

     // Age depends on the so get it from parent panel
     if (indi != null && panel != null && indi.getBirthDate() != null) {
        String ageCalc = HelperDocs.calcAge(panel.getDate(), indi.getBirthDate().getStart());
        if (ageCalc != null) str = ageCalc; else str = "";
        }
     else str = "";
     refAGED.setText(str);

     // Occupation
     if (indi != null) prop = indi.getPropertyByPath("INDI:OCCU");
     if (prop != null) str = prop.toString(); else str = "";
     refOCCUD.setText(str);

     // Birth place
     if (indi != null) prop = indi.getPropertyByPath("INDI:BIRT:PLAC");
     if (prop != null) str = prop.toString(); else str = "";
     refBPLACD.setText(str);
     refBPLACD.getTextEditor().setCaretPosition(0);

     // Birth date
     if (indi != null) prop = indi.getPropertyByPath("INDI:BIRT:DATE");
     if (prop != null && prop instanceof PropertyDate) {
        PropertyDate pdate = (PropertyDate) prop;
        refBDATED.setValue(pdate.getStart());
        }
     else refBDATED.setValue(new PointInTime());

     // Death date
     if (indi != null) prop = indi.getPropertyByPath("INDI:DEAT:DATE");
     if (prop != null && prop instanceof PropertyDate) {
        PropertyDate pdate = (PropertyDate) prop;
        refDDATED.setValue(pdate.getStart());
        }
     else refDDATED.setValue(new PointInTime());

     // Residence
     if (indi != null) prop = indi.getPropertyByPath("INDI:RESI:PLAC");
     if (prop != null) str = prop.toString(); else str = "";
     refRESID.setText(str);
     refRESID.getTextEditor().setCaretPosition(0);

    }


  /**
   * Check for individual
   */
  public boolean isOK(int i) {
    if (i == 1) return isOK(null, refSURND, refGIVND);
    if (i == 2) return isOK(refSINDID, refSSURND, refSGIVND);
    if (i == 3) return isOK(refFINDID, refFSURND, refFGIVND);
    if (i == 4) return isOK(refMINDID, refMSURND, refMGIVND);
    return false;
    }

  private boolean isOK(ChoiceWidget refINDI, TextFieldWidget surn, TextFieldWidget givn) {
    if (refINDI != null) {
       String ref = refINDI.getText();
       if (exists(ref)) return true;
       }
    return (exists(surn.getText()) && exists(givn.getText()));
    }

  /**
   * Create or update individual
   */
  public Indi upcreateIndi(int i, ChoiceWidget refINDI, PointInTime pitDocDate, ChoiceWidget dplac, DateWidget ddate, String strAgent) throws GedcomException {

    // deceased
    if (i == 1) {
       Indi deceased = HelperDocs.upcreateIndi(gedcom, refMale.isSelected(), pitDocDate, refINDI, refSURND, refGIVND, refAGED, null, null, refOCCUD, refRESID, dplac, ddate);
       Property propEvent = deceased.getProperty("DEAT");
       if (exists(strAgent)) HelperDocs.upcreateProperty(propEvent, "AGNC", strAgent);
       return deceased;
       }

    // spouse
    if (i == 2) return HelperDocs.upcreateIndi(gedcom, !refMale.isSelected(), pitDocDate, refSINDID, refSSURND, refSGIVND, null, null, null, refSOCCUD, refSRESID, refSDPLACD, refSDDATED);

    // father
    if (i == 3) return HelperDocs.upcreateIndi(gedcom, true, pitDocDate, refFINDID, refFSURND, refFGIVND, null, null, null, null, null, null, null);

    // mother
    if (i == 4) return HelperDocs.upcreateIndi(gedcom, false, pitDocDate, refMINDID, refMSURND, refMGIVND, null, null, null, null, null, null, null);

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
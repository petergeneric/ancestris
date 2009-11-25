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
 * The panel for entering people of a birth document
 */
public class IndiBirthPanel extends JPanel implements ActionListener, FocusListener {

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

  public ChoiceWidget refFINDID = new ChoiceWidget();
  public TextFieldWidget refFSURND = new TextFieldWidget("", FS);
  public TextFieldWidget refFGIVND = new TextFieldWidget("", FS);
  public TextFieldWidget refFAGED = new TextFieldWidget("", FS);
  public ChoiceWidget refFBPLACD = new ChoiceWidget();
  public DateWidget refFBDATED = new DateWidget();
  public ChoiceWidget refFOCCUD = new ChoiceWidget();
  public ChoiceWidget refFRESID = new ChoiceWidget();

  public JRadioButton refMale;
  public JRadioButton refFemale;
  public ButtonGroup refSex;
  public DateWidget refDATED = new DateWidget();

  public ChoiceWidget refMINDID = new ChoiceWidget();
  public TextFieldWidget refMSURND = new TextFieldWidget("", FS);
  public TextFieldWidget refMGIVND = new TextFieldWidget("", FS);
  public TextFieldWidget refMAGED = new TextFieldWidget("", FS);
  public ChoiceWidget refMBPLACD = new ChoiceWidget();
  public DateWidget refMBDATED = new DateWidget();
  public ChoiceWidget refMOCCUD = new ChoiceWidget();
  public ChoiceWidget refMRESID = new ChoiceWidget();

  public TextFieldWidget refSURND = new TextFieldWidget("", FS);
  public TextFieldWidget refGIVND = new TextFieldWidget("", FS);

  /**
   * Constructor
   */
  public IndiBirthPanel(Gedcom gedcom, DocsListener panel, DataSet dataSet) {

    super();

    this.panel = panel;
    this.gedcom = gedcom;
    this.indis = dataSet.indis;
    this.dataSet = dataSet;

    this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), panel.translate("Bir_Declare")));

    // define elements of panel
    JLabel refFATHERL = new JLabel(panel.translate("Bir_Father"));
    JLabel refFINDIL = new JLabel(panel.translate("Bir_FatherRef"));
    refFINDIL.setFont(PF);
    refFINDID.setValues(dataSet.indisStr);
    refFINDID.addActionListener(this);
    refFINDID.setIgnoreCase(true);
    JLabel refFSURNL = new JLabel(panel.translate("Bir_FatherLast"));
    refFSURNL.setFont(PF);
    refFSURND.setFont(BF);
    refFSURND.addFocusListener(this);
    JLabel refFGIVNL = new JLabel(panel.translate("Bir_FatherFirst"));
    refFGIVNL.setFont(PF);
    refFGIVND.setFont(BF);
    JLabel refFAGEL = new JLabel(panel.translate("Bir_FatherAge"));
    refFAGEL.setFont(PF);
    refFAGED.setFont(BF);
    JLabel refFBPLACL = new JLabel(panel.translate("Bir_FatherBorn"));
    refFBPLACL.setFont(PF);
    refFBPLACD.setValues(dataSet.places);
    refFBPLACD.setIgnoreCase(true);
    JLabel refFBDATEL = new JLabel(panel.translate("Bir_FatherBornOn"));
    refFBDATEL.setFont(PF);
    refFBDATED.setFont(BF);
    JLabel refFOCCUL = new JLabel(panel.translate("Bir_FatherOccu"));
    refFOCCUL.setFont(PF);
    refFOCCUD.setValues(dataSet.occupations);
    refFOCCUD.setIgnoreCase(true);
    JLabel refFRESIL = new JLabel(panel.translate("Bir_FatherPlace"));
    refFRESIL.setFont(PF);
    refFRESID.setValues(dataSet.places);
    refFRESID.setIgnoreCase(true);

    JLabel refSEXL = new JLabel(panel.translate("Bir_WasPresented"));
    refMale = new JRadioButton(panel.translate("Bir_Male"));
    refMale.setFont(PF);
    refMale.setSelected(true);
    refFemale = new JRadioButton(panel.translate("Bir_Female"));
    refFemale.setFont(PF);
    refSex = new ButtonGroup();
    refSex.add(refMale);
    refSex.add(refFemale);
    JLabel refDATEL = new JLabel(panel.translate("Bir_Born"));
    refDATEL.setFont(PF);
    refDATED.setFont(BF);

    JLabel refMOTHERL = new JLabel(panel.translate("Bir_Mother"));
    JLabel refMINDIL = new JLabel(panel.translate("Bir_MotherRef"));
    refMINDIL.setFont(PF);
    refMINDID.setValues(dataSet.indisStr);
    refMINDID.addActionListener(this);
    refMINDID.setIgnoreCase(true);
    JLabel refMSURNL = new JLabel(panel.translate("Bir_MotherLast"));
    refMSURNL.setFont(PF);
    refMSURND.setFont(BF);
    JLabel refMGIVNL = new JLabel(panel.translate("Bir_MotherFirst"));
    refMGIVNL.setFont(PF);
    refMGIVND.setFont(BF);
    JLabel refMAGEL = new JLabel(panel.translate("Bir_MotherAge"));
    refMAGEL.setFont(PF);
    refMAGED.setFont(BF);
    JLabel refMBPLACL = new JLabel(panel.translate("Bir_MotherBorn"));
    refMBPLACL.setFont(PF);
    refMBPLACD.setValues(dataSet.places);
    refMBPLACD.setIgnoreCase(true);
    JLabel refMBDATEL = new JLabel(panel.translate("Bir_MotherBornOn"));
    refMBDATEL.setFont(PF);
    refMBDATED.setFont(BF);
    JLabel refMOCCUL = new JLabel(panel.translate("Bir_MotherOccu"));
    refMOCCUL.setFont(PF);
    refMOCCUD.setValues(dataSet.occupations);
    refMOCCUD.setIgnoreCase(true);
    JLabel refMRESIL = new JLabel(panel.translate("Bir_MotherPlace"));
    refMRESIL.setFont(PF);
    refMRESID.setValues(dataSet.places);
    refMRESID.setIgnoreCase(true);

    JLabel refCHILDL = new JLabel(panel.translate("Bir_Child"));
    JLabel refSURNL = new JLabel(panel.translate("Bir_ChildLast"));
    refSURNL.setFont(PF);
    refSURND.setFont(BF);
    JLabel refGIVNL = new JLabel(panel.translate("Bir_ChildFirst"));
    refGIVNL.setFont(PF);
    refGIVND.setFont(BF);


     // set grid
    this.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;

    // position elements
    c.gridx = 0; c.gridy = 0; c.gridwidth = 3; c.weightx = 1.0; c.insets = new Insets(10, 0, 0, 0);
    this.add(refFATHERL, c);
    c.insets = new Insets(5, 15, 0, 0);

    c.gridx = 0; c.gridy = 1; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refFINDIL, c);
    c.gridx = 1; c.gridy = 1; c.gridwidth = 3;
    this.add(refFINDID, c);

    c.insets = new Insets(0, 15, 0, 0);
    c.gridx = 0; c.gridy = 2; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refFSURNL, c);
    c.gridx = 1; c.gridy = 2; c.ipady = 6;
    this.add(refFSURND, c);
    c.gridx = 2; c.gridy = 2;
    this.add(refFGIVNL, c);
    c.gridx = 3; c.gridy = 2;
    this.add(refFGIVND, c);
    c.gridx = 4; c.gridy = 2;
    this.add(refFAGEL, c);
    c.gridx = 5; c.gridy = 2;
    this.add(refFAGED, c);

    c.gridx = 0; c.gridy = 3; c.weightx = 1.0; c.gridwidth = 1; c.ipady = 0;
    this.add(refFBPLACL, c);
    c.gridx = 1; c.gridy = 3;
    this.add(refFBPLACD, c);
    c.gridx = 2; c.gridy = 3;
    this.add(refFBDATEL, c);
    c.gridx = 3; c.gridy = 3;
    this.add(refFBDATED, c);
    c.gridx = 4; c.gridy = 3;
    this.add(refFOCCUL, c);
    c.gridx = 5; c.gridy = 3;
    this.add(refFOCCUD, c);

    c.gridx = 0; c.gridy = 4; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refFRESIL, c);
    c.gridx = 1; c.gridy = 4;  c.gridwidth = 3;
    this.add(refFRESID, c);


    c.gridx = 0; c.gridy = 5; c.gridwidth = 3; c.weightx = 1.0; c.insets = new Insets(10, 0, 0, 0);
    this.add(refSEXL, c);
    c.insets = new Insets(0, 15, 0, 0);
    c.gridx = 1; c.gridy = 6; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refMale, c);
    c.gridx = 1; c.gridy = 7;
    this.add(refFemale, c);
    c.gridx = 2; c.gridy = 6;
    this.add(refDATEL, c);
    c.gridx = 3; c.gridy = 6;
    this.add(refDATED, c);



    c.gridx = 0; c.gridy = 8; c.gridwidth = 3; c.weightx = 1.0; c.insets = new Insets(10, 0, 0, 0);
    this.add(refMOTHERL, c);
    c.insets = new Insets(5, 15, 0, 0);
    c.gridx = 0; c.gridy = 9; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refMINDIL, c);
    c.gridx = 1; c.gridy = 9; c.gridwidth = 3;
    this.add(refMINDID, c);

    c.insets = new Insets(0, 15, 0, 0);
    c.gridx = 0; c.gridy = 10; c.weightx = 1.0; c.gridwidth = 1; c.ipady = 6;
    this.add(refMSURNL, c);
    c.gridx = 1; c.gridy = 10;
    this.add(refMSURND, c);
    c.gridx = 2; c.gridy = 10;
    this.add(refMGIVNL, c);
    c.gridx = 3; c.gridy = 10;
    this.add(refMGIVND, c);
    c.gridx = 4; c.gridy = 10;
    this.add(refMAGEL, c); c.ipady = 0;
    c.gridx = 5; c.gridy = 10;
    this.add(refMAGED, c);

    c.gridx = 0; c.gridy = 11; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refMBPLACL, c);
    c.gridx = 1; c.gridy = 11;
    this.add(refMBPLACD, c);
    c.gridx = 2; c.gridy = 11;
    this.add(refMBDATEL, c);
    c.gridx = 3; c.gridy = 11;
    this.add(refMBDATED, c);
    c.gridx = 4; c.gridy = 11;
    this.add(refMOCCUL, c);
    c.gridx = 5; c.gridy = 11;
    this.add(refMOCCUD, c);

    c.gridx = 0; c.gridy = 12; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refMRESIL, c);
    c.gridx = 1; c.gridy = 12; c.gridwidth = 3;
    this.add(refMRESID, c);


    c.gridx = 0; c.gridy = 13; c.gridwidth = 3; c.weightx = 1.0; c.insets = new Insets(10, 0, 0, 0);
    this.add(refCHILDL, c);
    c.insets = new Insets(5, 15, 3, 0);
    c.gridx = 0; c.gridy = 14; c.weightx = 1.0; c.gridwidth = 1; c.ipady = 6;
    this.add(refSURNL, c);
    c.gridx = 1; c.gridy = 14;
    this.add(refSURND, c);
    c.gridx = 2; c.gridy = 14;
    this.add(refGIVNL, c);
    c.gridx = 3; c.gridy = 14;
    this.add(refGIVND, c);

}


  /**
   * Display individual given by indi
   */
  public void setIndi(Indi child, Indi father, Indi mother) {

    // Clear form if null
    if (child == null) {
       refFINDID.setText("");
       refMINDID.setText("");
       populateChild(null);
       populateFather();
       populateMother();
       return;
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

    // Populate child
    populateChild(child);

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

     if (e.getSource() == refFINDID.getEditor().getEditorComponent()) {
  	    populateFather();
     	}
     if (e.getSource() == refMINDID.getEditor().getEditorComponent()) {
    	populateMother();
     	}
     }


  /**
   * Action performed on text fields
   */
  public void focusGained(FocusEvent e) {
    return;
    }

  public void focusLost(FocusEvent e) {
    if (e.getSource() == refFSURND && exists(refFSURND.getText()) && !exists(refSURND.getText())) {
       refSURND.setText(refFSURND.getText());
       }
    }



  /**
   * Get individual represented in the main combobox
   */
  public Indi getIndi(ChoiceWidget refINDI) {
    String ref = refINDI.getText();
    return (Indi) HelperDocs.getEntity(gedcom, ref);
    }

  
  /**
   * Populate father
   */
  public void populateFather() {
	  populateIndi(refFINDID, refFSURND, refFGIVND, refFAGED, refFBPLACD, refFBDATED, refFOCCUD, refFRESID);
  }

  /**
   * Populate mother
   */
  public void populateMother() {
	  populateIndi(refMINDID, refMSURND, refMGIVND, refMAGED, refMBPLACD, refMBDATED, refMOCCUD, refMRESID);
  }

  
  
  /**
   * Populates fields upon user selecting an individual in one of the list boxes
   *
   */
  private void populateIndi(ChoiceWidget refINDI, TextFieldWidget surn, TextFieldWidget givn, TextFieldWidget age, ChoiceWidget bplac, DateWidget bdate, ChoiceWidget occu, ChoiceWidget resi) {

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

     }


  /**
   * Populate child fields
   */
  public void populateChild(Indi indi) {

     Property prop = null;
     String str = "";

     // Sex
     if (indi != null) {
        if (indi.getSex() == 2) refFemale.setSelected(true); else refMale.setSelected(true);
        }

     // Birth date
     if (indi != null) prop = indi.getPropertyByPath("INDI:BIRT:DATE");
     if (prop != null && prop instanceof PropertyDate) {
        PropertyDate pdate = (PropertyDate) prop;
        refDATED.setValue(pdate.getStart());
        }
     else refDATED.setValue(new PointInTime());

     // Surname
     if (indi != null) prop = indi.getPropertyByPath("INDI:NAME:SURN");
     if (prop != null) str = prop.toString(); else str = "";
     refSURND.setText(str);

     // Given names
     if (indi != null) prop = indi.getPropertyByPath("INDI:NAME:GIVN");
     if (prop != null) str = prop.toString(); else str = "";
     refGIVND.setText(str);

    }


  /**
   * Check for individual
   */
  public boolean isOK(int i) {
    if (i == 1) return isOK(null, refSURND, refGIVND);
    if (i == 2) return isOK(refFINDID, refFSURND, refFGIVND);
    if (i == 3) return isOK(refMINDID, refMSURND, refMGIVND);
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
  public Indi upcreateIndi(int i, ChoiceWidget refINDI, PointInTime pitDocDate, ChoiceWidget bplac, DateWidget bdate, String strAgent) throws GedcomException {

    if (i == 1) {
       Indi child = HelperDocs.upcreateIndi(gedcom, refMale.isSelected(), pitDocDate, refINDI, refSURND, refGIVND, null, bplac, bdate, null, null, null, null);
       Property propEvent = child.getProperty("BIRT");
       if (exists(strAgent)) HelperDocs.upcreateProperty(propEvent, "AGNC", strAgent);
       return child;
       }
    if (i == 2) return HelperDocs.upcreateIndi(gedcom, true, pitDocDate, refFINDID, refFSURND, refFGIVND, refFAGED, refFBPLACD, refFBDATED, refFOCCUD, refFRESID, null, null);
    if (i == 3) return HelperDocs.upcreateIndi(gedcom, false, pitDocDate, refMINDID, refMSURND, refMGIVND, refMAGED, refMBPLACD, refMBDATED, refMOCCUD, refMRESID, null, null);

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
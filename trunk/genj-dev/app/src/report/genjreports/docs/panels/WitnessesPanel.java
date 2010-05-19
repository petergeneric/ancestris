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
 * The panel for entering a number of witnesses
 */
public class WitnessesPanel extends JPanel implements ActionListener {

  /** calling panel */
  private DocsListener panel = null;

  /** our gedcom */
  private Gedcom gedcom = null;

  /** individuals */
  Entity[] indis;

  /** event type */
  private String eventTag;

  private int numberWit;
  public Witness[] witnesses;
  private DataSet dataSet;
  static private Font PF = new Font("", Font.PLAIN, 12);
  static private Font BF = new Font("", Font.BOLD, 12);

  /**
   * Constructor
   */
  public WitnessesPanel(Gedcom gedcom, DocsListener panel, int numberWit, DataSet dataSet, String eventTag, String title) {

    super();
    this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), title));

    this.gedcom = gedcom;
    this.panel = panel;
    this.numberWit = numberWit;
    this.indis = dataSet.indis;
    this.eventTag = eventTag;
    this.dataSet = dataSet;


    // define elements of panel
    witnesses = new Witness[numberWit];

    // set grid
    this.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH; c.ipadx = 0;  c.ipady = 0;
    c.insets = new Insets(0, 15, 0, 0); c.weightx = 1.0; c.gridwidth = 1;

    // position elements
    for (int i=0; i < numberWit ; i++) {

       witnesses[i] = new Witness(dataSet);
       witnesses[i].refCREED.setSelected(true);
       witnesses[i].refTypeD.add(witnesses[i].refCREED);
       witnesses[i].refTypeD.add(witnesses[i].refNOTED);

       c.insets = new Insets(8, 10, 0, 0);
       c.gridx = 0; c.gridy = i*3;
       JLabel lab = new JLabel((i+1)+"° "+panel.translate("Witness"));
       lab.setFont(PF);
       this.add(lab, c);
       c.gridx = 1; c.gridy = i*3;
       witnesses[i].refINDID.setValues(dataSet.indisStr);
       witnesses[i].refINDID.addActionListener(this);
       witnesses[i].refINDID.setIgnoreCase(true);
       this.add(witnesses[i].refINDID, c);
       c.gridx = 2; c.gridy = i*3;
       this.add(witnesses[i].refSURNL, c);
       c.gridx = 3; c.gridy = i*3;
       this.add(witnesses[i].refSURND, c);
       c.gridx = 4; c.gridy = i*3;
       this.add(witnesses[i].refGIVNL, c);
       c.gridx = 5; c.gridy = i*3;
       this.add(witnesses[i].refGIVND, c);

       c.insets = new Insets(0, 10, 0, 0);
       c.gridx = 1; c.gridy = i*3+1; 
       this.add(witnesses[i].refCREED, c);
       c.gridx = 2; c.gridy = i*3+1;
       this.add(witnesses[i].refMALE, c);
       c.gridx = 3; c.gridy = i*3+1;
       this.add(witnesses[i].refAGE, c);
       c.gridx = 4; c.gridy = i*3+1;
       this.add(witnesses[i].refOCCUL, c);
       c.gridx = 5; c.gridy = i*3+1;
       this.add(witnesses[i].refOCCUD, c);

       c.gridx = 1; c.gridy = i*3+2; 
       this.add(witnesses[i].refNOTED, c);
       c.gridx = 2; c.gridy = i*3+2;
       this.add(witnesses[i].refRESIL, c);
       c.gridx = 3; c.gridy = i*3+2;
       this.add(witnesses[i].refRESID, c);
       c.gridx = 4; c.gridy = i*3+2;
       this.add(witnesses[i].refRELAL, c);
       c.gridx = 5; c.gridy = i*3+2;
       this.add(witnesses[i].refRELAD, c);
       }
    }

  public class Witness {

   private int FS = 10;

   public ChoiceWidget refINDID;
   public JLabel refSURNL;
   public TextFieldWidget refSURND;
   public JLabel refGIVNL;
   public TextFieldWidget refGIVND;
   public JLabel refAGEL;
   public TextFieldWidget refAGED;
   public JPanel refAGE;
   public JCheckBox refMALE;

   public JLabel refOCCUL;
   public ChoiceWidget refOCCUD;
   public JLabel refRESIL;
   public ChoiceWidget refRESID;
   public JLabel refRELAL;
   public ChoiceWidget refRELAD;

   public JRadioButton refCREED;
   public JRadioButton refNOTED;
   public ButtonGroup refTypeD;


   public Witness(DataSet dataSet) {
      refINDID = new ChoiceWidget();
      refSURNL = new JLabel(panel.translate("Wit_Lastname"));
      refSURNL.setFont(PF);
      refSURND = new TextFieldWidget("", FS);
      refSURND.setFont(BF);
      refGIVNL = new JLabel(panel.translate("Wit_Firstname"));
      refGIVNL.setFont(PF);
      refGIVND = new TextFieldWidget("", FS);
      refGIVND.setFont(BF);
      refAGEL = new JLabel(panel.translate("Wit_Age"));
      refAGEL.setFont(PF);
      refAGED = new TextFieldWidget("", FS);
      refAGED.setFont(BF);
      refMALE = new JCheckBox(panel.translate("Wit_Male"));
      refMALE.setFont(PF);
      refAGE = new JPanel(new FlowLayout(FlowLayout.LEFT));
      refAGE.add(refAGEL);
      refAGE.add(refAGED);
      refOCCUL = new JLabel(panel.translate("Wit_Occu"));
      refOCCUL.setFont(PF);
      refOCCUD = new ChoiceWidget();
      refOCCUD.setValues(dataSet.occupations);
      refOCCUD.setIgnoreCase(true);
      refRESIL = new JLabel(panel.translate("Wit_Place"));
      refRESIL.setFont(PF);
      refRESID = new ChoiceWidget();
      refRESID.setValues(dataSet.places);
      refRESID.setIgnoreCase(true);
      refRELAL = new JLabel(panel.translate("Wit_Relation"));
      refRELAL.setFont(PF);
      refRELAD = new ChoiceWidget();
      refRELAD.setValues(dataSet.relations);
      refRELAD.setIgnoreCase(true);
      refCREED = new JRadioButton(panel.translate("Wit_AsPerson"));
      refCREED.setFont(PF);
      refNOTED = new JRadioButton(panel.translate("Wit_AsNote"));
      refNOTED.setFont(PF);
      refTypeD = new ButtonGroup();
      }
   }

  /**
   * Display witnesses from entity
   */
  public void setWitnesses(Entity ent) {

    for (int i = 0 ; i < numberWit ; i++) {
       witnesses[i].refINDID.setText("");
       populateIndi(i);
       }
    if (ent == null) return;

    Property prop = null;
    String str = "";
    int iWit = 0;
    String tag = "@"+eventTag;

    // Look for individuals that have ASSO @xxxx@ in their properties
    // For all of the "event" ones, fill in witnesses incrementally
    Entity[] individuals = gedcom.getEntities(Gedcom.INDI, "INDI:NAME");
    for (int i = 0 ; i < individuals.length && iWit < numberWit ; i++) {
       Property[] props = individuals[i].getProperties("ASSO", true);
       for (int j=0; j < props.length ; j++) {
          PropertyAssociation propAsso = (PropertyAssociation)props[j];
          if (propAsso.getTargetEntity() == ent) {
             prop = propAsso.getProperty("RELA");
             String value = props[j].getPropertyValue("RELA");
             if (prop != null && value.indexOf(tag) > -1) {
                setWitness(iWit, individuals[i]);
                iWit++;
                }
             }
          }
       }

    return;
    }

  /**
   * Display a witness
   */
  public void setWitness(int iWit, Entity indi) {

    if (indi == null || iWit >= numberWit) return;
    String str = indi.toString();
    for (int i = 0 ; i < indis.length ; i++) {
       if (indis[i].toString().equals(str)) {
          witnesses[iWit].refINDID.setSelectedIndex(i);
          witnesses[iWit].refINDID.getTextEditor().setCaretPosition(0);
          break;
          }
       }
    populateIndi(iWit);
    
    return;
    }


  /**
   * Process action performed
   */
  public void actionPerformed(ActionEvent e) {

	    for (int i=0; i < numberWit ; i++) {
	        if (e.getSource() == witnesses[i].refINDID.getEditor().getEditorComponent()) {
	           populateIndi(i);
	           }
	       }
     }


 

  /**
   * Populates fields upon user selecting an individual in one of the list boxes
   */
  private void populateIndi(int i) {

     Indi indi = getIndi(i);
     Property prop = null;
     String str = "";

     // Surname
     if (indi != null) prop = indi.getPropertyByPath("INDI:NAME:SURN");
     if (prop != null) str = prop.toString(); else str = (indi != null ? indi.getLastName() : "");
     witnesses[i].refSURND.setText(str);

     // Given names
     if (indi != null) prop = indi.getPropertyByPath("INDI:NAME:GIVN");
     if (prop != null) str = prop.toString(); else str = (indi != null ? indi.getFirstName() : "");
     witnesses[i].refGIVND.setText(str);

     // Sex (check box ON if male)
     if (indi != null) witnesses[i].refMALE.setSelected(indi.getSex() == 1);

     // Age depends on the current date
     if (indi != null && panel != null && indi.getBirthDate() != null) {
        String age = HelperDocs.calcAge(panel.getDate(), indi.getBirthDate().getStart());
        if (age != null) str = age; else str = "";
        }
     else str = "";
     witnesses[i].refAGED.setText(str);

     // Occupation
     if (indi != null) prop = indi.getPropertyByPath("INDI:OCCU");
     if (prop != null) str = prop.toString(); else str = "";
     witnesses[i].refOCCUD.setText(str);

     // Residence
     if (indi != null) prop = indi.getPropertyByPath("INDI:RESI:PLAC");
     if (prop != null) str = prop.toString(); else str = "";
     witnesses[i].refRESID.setText(str);
     witnesses[i].refRESID.getTextEditor().setCaretPosition(0);

     // Relation : populate ensuring proper family and relation for that witness
     // - ASSO should have the proper @FXXX@ (only parent panel would know), and RELA should have proper string@FAM:MARR
     populateAsso(indi, i);
     }

  /**
   * Get individual represented in the main combobox
   */
  public Indi getIndi(int i) {
    String ref = witnesses[i].refINDID.getText();
    return (Indi) HelperDocs.getEntity(gedcom, ref);
    }

  /**
   * Populates asso field
   */
  private void populateAsso(Indi indi, int i) {

    if (indi == null) {
       witnesses[i].refRELAD.setText("");
       return;
       }

    String tag = "@"+eventTag;

    Entity ent = panel.getEntity();
    Property prop = null;
    String str = "";
    if (ent != null) {
       Property props[] = indi.getProperties("ASSO");
       for (int j=0; j < props.length ; j++) {
          PropertyAssociation propAsso = (PropertyAssociation)props[j];
          if (propAsso.getTargetEntity() == ent) {
             prop = propAsso.getProperty("RELA");
             String value = props[j].getPropertyValue("RELA");
             if (prop != null && value.indexOf(tag) > -1) {
                str = prop.toString();
                break;
                }
             }
          }
       }
    witnesses[i].refRELAD.setText(str);
    }

  /**
   * Populates all asso witnesses
   */
  public void populateAsso() {
    for (int i=0; i < numberWit ; i++) {
       populateAsso(getIndi(i), i);
       }
    }

  /**
   * Update or create contract
   */
  public void upcreateWitnesses(Property propEvent, PointInTime pitDocDate) throws GedcomException {

   for (int i=0; i < numberWit ; i++) {

      // Continue if witness is blank
      if (!exists(witnesses[i].refINDID.getText()) && !exists(witnesses[i].refSURND.getText()) && !exists(witnesses[i].refGIVND.getText())) continue;

      // Update or create individual if flag set
      if (witnesses[i].refCREED.isSelected()) {
         Indi indi = HelperDocs.upcreateIndi(gedcom, witnesses[i].refMALE.isSelected(), pitDocDate, witnesses[i].refINDID, witnesses[i].refSURND, witnesses[i].refGIVND, witnesses[i].refAGED, null, null, witnesses[i].refOCCUD, witnesses[i].refRESID, null, null);

         // Asso part
         Property asso = upcreateProperty(indi, "ASSO", "@"+ propEvent.getEntity().getId() + "@");
         if (!exists(witnesses[i].refRELAD.getText())) witnesses[i].refRELAD.setText("Témoin");
         String tag = propEvent.getEntity().getTag();
         upcreateProperty(asso, "RELA", witnesses[i].refRELAD.getText()+"@"+tag+":"+propEvent.getTag());
         upcreateProperty(asso, "TYPE", tag);
         try {
            ((PropertyXRef)asso).link();
            } catch (Exception e) {
               if (e.getMessage().indexOf("Already linked") == -1) e.printStackTrace();
            }

         }

      // Else create individual as a note in event
      else if (propEvent != null) {
         String key = panel.translate("Wit_WitnessMark", ""+(i+1));
         String note = ""+key+" : ";
         note += witnesses[i].refMALE.isSelected() ? panel.translate("Wit_Sir") : panel.translate("Wit_Mrs");
         note += " " + witnesses[i].refSURND.getText()+", " + witnesses[i].refGIVND.getText()+", ";
         note += panel.translate("Wit_AgeOf", witnesses[i].refAGED.getText())+", ";
         note += panel.translate("Wit_OccuOf", witnesses[i].refOCCUD.getText())+", ";
         note += panel.translate("Wit_PlaceOf", witnesses[i].refRESID.getText());
         Property[] pNotes = propEvent.getProperties("NOTE");
         boolean found = false;
         for (int n = 0 ; n < pNotes.length ; n++) {
            if (pNotes[n].toString().substring(0,10).equals(key)) { 
               String existingValue = pNotes[n].getPropertyValue("NOTE");
               if (!note.equals(existingValue)) pNotes[n].setValue(note);
               found = true;
               break;
               }
            }
         if (!found) {
            propEvent.addProperty("NOTE", note);
            }
         }

      }

   return;
   }



  /**
   * Shortcuts of Helper methods
   */
  private Property upcreateProperty(Property prop, String tag, String value) {
    return HelperDocs.upcreateProperty(prop, tag, value);
    }

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

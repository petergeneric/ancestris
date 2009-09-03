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
import docs.ImagesDocs;
import docs.DataSet;

import genj.gedcom.*;
import genj.util.Registry;
import genj.util.GridBagHelper;
import genj.util.swing.*;
import genj.view.ContextProvider;
import genj.view.ViewContext;
import genj.io.FileAssociation;


import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EtchedBorder;


/**
 * The panel for entering source documents
 */
public class ActPanel extends JPanel implements ItemListener {

  /** calling panel */
  private DocsListener panel = null;

  /** our gedcom */
  private Gedcom gedcom = null;

  /** sources and repositories */
  Entity[] sources;
  Entity[] repos;

  /** registry */
  private Registry registry;

  /** event type */
  private String eventTag;
  private DataSet dataSet;
  static private Font PF = new Font("", Font.PLAIN, 12);
  static private Font BF = new Font("", Font.BOLD, 12);

  private int FS = 10;
  public ChoiceWidget refSOURD = new ChoiceWidget();
  public JRadioButton refTypeAct;
  public JRadioButton refTypeReg;
  public ButtonGroup refTypeD = new ButtonGroup();
  public ChoiceWidget refABBRD = new ChoiceWidget();
  public TextFieldWidget refAUTHD = new TextFieldWidget("", FS);
  public TextFieldWidget refAUTH2D = new TextFieldWidget("", FS);
  public TextFieldWidget refAUTH3D = new TextFieldWidget("", FS);
  public TextFieldWidget refAUTH4D = new TextFieldWidget("", FS);
  public TextFieldWidget refCALND = new TextFieldWidget("", FS);
  public TextFieldWidget refMEDID = new TextFieldWidget("", FS);
  public TextFieldWidget refPAGED = new TextFieldWidget("", FS);
  public ChoiceWidget refQUAYD = new ChoiceWidget();
  public FileChooserWidget refFileD = new FileChooserWidget();
  private ViewFileButton viewButton = null;
  public TextFieldWidget refTITLD = new TextFieldWidget("", FS);
  public TextAreaWidget refTEXTD = new TextAreaWidget("", 1, FS, true, true);
  public ChoiceWidget refREPOD = new ChoiceWidget();
  public TextFieldWidget refNAMED = new TextFieldWidget("", FS);
  public TextFieldWidget refADDRD = new TextFieldWidget("", FS);
  public TextFieldWidget refPOSTD = new TextFieldWidget("", FS);
  public TextFieldWidget refCITYD = new TextFieldWidget("", FS);
  public TextFieldWidget refCTRYD = new TextFieldWidget("", FS);
  public TextFieldWidget refEMAILD = new TextFieldWidget("", FS);
  public TextFieldWidget refPHONED = new TextFieldWidget("", FS);
  public TextFieldWidget refFAXD = new TextFieldWidget("", FS);
  public TextFieldWidget refWWWD = new TextFieldWidget("", FS);


  /**
   * Constructor
   */
  public ActPanel(Gedcom gedcom, DocsListener panel, DataSet dataSet, Registry registry, String eventTag, String title) {

    super();

    this.gedcom = gedcom;
    this.panel = panel;
    this.sources = dataSet.sources;
    this.repos = dataSet.repos;
    this.registry = registry;
    this.eventTag = eventTag;
    this.dataSet = dataSet;


    // get directory that will be used for files
    String directory = gedcom.getOrigin().getFile().getParent();
    refFileD.setDirectory(directory);

    // define elements of panel
    JLabel refSOURL = new JLabel(panel.translate("AlreadyRef"));
    refSOURL.setFont(PF);
    refSOURD.setValues(sources);
    refSOURD.addItemListener(this);
    JLabel refTypeL = new JLabel(panel.translate("Sour_Type"));
    refTypeL.setFont(PF);
    refTypeAct = new JRadioButton(panel.translate("Sour_Is_Doc"));
    refTypeReg = new JRadioButton(panel.translate("Sour_Is_Register"));
    refTypeAct.setFont(PF);
    refTypeReg.setFont(PF);
    if (getSavedType()) 
       refTypeAct.setSelected(true);
    else 
       refTypeReg.setSelected(true);
    refTypeAct.addItemListener(this);
    refTypeReg.addItemListener(this);
    refTypeD.add(refTypeAct);
    refTypeD.add(refTypeReg);
    JLabel refABBRL = new JLabel(panel.translate("Sour_Abbr"));
    refABBRL.setFont(PF);
    refABBRD.setValues(getAbbr());
    JLabel refAUTHL = new JLabel(panel.translate("Sour_Titl"));
    refAUTHL.setFont(PF);
    refAUTHD.setFont(BF);
    JLabel refAUTH2L = new JLabel(panel.translate("Sour_Even"));
    refAUTH2L.setFont(PF);
    refAUTH2D.setFont(BF);
    JLabel refAUTH3L = new JLabel(panel.translate("Sour_Period"));
    refAUTH3L.setFont(PF);
    refAUTH3D.setFont(BF);
    JLabel refAUTH4L = new JLabel(panel.translate("Sour_Places"));
    refAUTH4L.setFont(PF);
    refAUTH4D.setFont(BF);
    JLabel refCALNL = new JLabel(panel.translate("Sour_Caln"));
    refCALNL.setFont(PF);
    refCALND.setFont(BF);
    JLabel refMEDIL = new JLabel(panel.translate("Sour_Medi"));
    refMEDIL.setFont(PF);
    refMEDID.setFont(BF);
    JLabel refPAGEL = new JLabel(panel.translate("Sour_Page"));
    refPAGEL.setFont(PF);
    refPAGED.setFont(BF);
    refPAGEL.setEnabled(eventTag != null);
    refPAGED.setEnabled(eventTag != null);
    JLabel refQUAYL = new JLabel(panel.translate("Sour_Quay"));
    refQUAYL.setFont(PF);
    refQUAYD.setFont(BF);
    refQUAYD.setValues(new String[] { 
       panel.translate("Sour_Quay0"),
       panel.translate("Sour_Quay1"),
       panel.translate("Sour_Quay2"),
       panel.translate("Sour_Quay3")
       });
    refQUAYL.setEnabled(eventTag != null);
    refQUAYD.setEnabled(eventTag != null);
    JLabel refFileL = new JLabel(panel.translate("Sour_File"));
    refFileL.setFont(PF);
    refFileD.setFont(BF);
    JPanel button = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
    viewButton = new ViewFileButton();
    ButtonHelper bh = new ButtonHelper().setContainer(button);
    bh.setInsets(0);
    bh.create(viewButton);
    button.setEnabled(false);

    JLabel refTITLL = new JLabel(panel.translate("Sour_Titl2"));
    refTITLL.setFont(PF);
    refTITLD.setFont(BF);
    JLabel refTEXTL = new JLabel(panel.translate("Sour_Text"));
    refTEXTL.setFont(PF);
    refTEXTD.setFont(PF);
    refTEXTD.setBorder(BorderFactory.createLineBorder(Color.gray));

    JLabel refREPOL = new JLabel(panel.translate("AlreadyRef"));
    refREPOL.setFont(PF);
    refREPOD.setValues(repos);
    refREPOD.addItemListener(this);
    JLabel refNAMEL = new JLabel(panel.translate("Repo_Nom"));
    refNAMEL.setFont(PF);
    refNAMED.setFont(BF);
    JLabel refADDRL = new JLabel(panel.translate("Repo_Addr"));
    refADDRL.setFont(PF);
    refADDRD.setFont(BF);
    JLabel refPOSTL = new JLabel(panel.translate("Repo_Post"));
    refPOSTL.setFont(PF);
    refPOSTD.setFont(BF);
    JLabel refCITYL = new JLabel(panel.translate("Repo_City"));
    refCITYL.setFont(PF);
    refCITYD.setFont(BF);
    JLabel refCTRYL = new JLabel(panel.translate("Repo_Ctry"));
    refCTRYL.setFont(PF);
    refCTRYD.setFont(BF);
    JLabel refEMAILL = new JLabel(panel.translate("Repo_Email"));
    refEMAILL.setFont(PF);
    refEMAILD.setFont(BF);
    JLabel refPHONEL = new JLabel(panel.translate("Repo_Phone"));
    refPHONEL.setFont(PF);
    refPHONED.setFont(BF);
    JLabel refFAXL = new JLabel(panel.translate("Repo_Fax"));
    refFAXL.setFont(PF);
    refFAXD.setFont(BF);
    JLabel refWWWL = new JLabel(panel.translate("Repo_Web"));
    refWWWL.setFont(PF);
    refWWWD.setFont(BF);

     // set grid
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel sourPanel = new JPanel();
    sourPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), panel.translate("Sour_Section")));
    sourPanel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;

    // position elements
    c.gridx = 0; c.gridy = 0; c.weightx = 1.0; c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = new Insets(0, 15, 0, 0);

    c.gridx = 0; c.gridy = 1; c.gridwidth = 1;
    sourPanel.add(refSOURL, c);
    c.gridx = 1; c.gridy = 1; c.gridwidth = GridBagConstraints.REMAINDER;
    sourPanel.add(refSOURD, c);

    c.gridx = 0; c.gridy = 2; c.gridwidth = 1;
    sourPanel.add(refTypeL, c);
    c.gridx = 1; c.gridy = 2; c.gridwidth = GridBagConstraints.REMAINDER;
    sourPanel.add(refTypeAct, c);
    c.gridx = 1; c.gridy = 3; c.gridwidth = GridBagConstraints.REMAINDER;
    sourPanel.add(refTypeReg, c);

    c.gridx = 0; c.gridy = 4; c.gridwidth = 1;
    sourPanel.add(refABBRL, c);
    c.gridx = 1; c.gridy = 4; c.gridwidth = GridBagConstraints.REMAINDER;
    sourPanel.add(refABBRD, c);

    c.gridx = 0; c.gridy = 5; c.gridwidth = 1;
    sourPanel.add(refTITLL, c);
    c.gridx = 1; c.gridy = 5; c.gridwidth = GridBagConstraints.REMAINDER;
    sourPanel.add(refTITLD, c);

    c.gridx = 0; c.gridy = 6; c.gridwidth = 1;
    sourPanel.add(refAUTHL, c);
    c.gridx = 1; c.gridy = 6; c.gridwidth = GridBagConstraints.REMAINDER;
    sourPanel.add(refAUTHD, c);

    c.gridx = 0; c.gridy = 7; c.gridwidth = 1;
    sourPanel.add(refAUTH2L, c);
    c.gridx = 1; c.gridy = 7;
    sourPanel.add(refAUTH2D, c);
    c.gridx = 2; c.gridy = 7;
    sourPanel.add(refAUTH3L, c);
    c.gridx = 3; c.gridy = 7;
    sourPanel.add(refAUTH3D, c);
    c.gridx = 4; c.gridy = 7;
    sourPanel.add(refAUTH4L, c);
    c.gridx = 5; c.gridy = 7; c.gridwidth = GridBagConstraints.REMAINDER;
    sourPanel.add(refAUTH4D, c);

    c.gridx = 0; c.gridy = 8; c.gridwidth = 1;
    sourPanel.add(refCALNL, c);
    c.gridx = 1; c.gridy = 8;
    sourPanel.add(refCALND, c);
    c.gridx = 2; c.gridy = 8;
    sourPanel.add(refMEDIL, c);
    c.gridx = 3; c.gridy = 8;
    sourPanel.add(refMEDID, c);

    c.gridx = 0; c.gridy = 9; c.gridwidth = 1;
    sourPanel.add(refPAGEL, c);
    c.gridx = 1; c.gridy = 9;
    sourPanel.add(refPAGED, c);
    c.gridx = 2; c.gridy = 9;
    sourPanel.add(refQUAYL, c);
    c.gridx = 3; c.gridy = 9;
    sourPanel.add(refQUAYD, c);

    c.gridx = 0; c.gridy = 10; c.gridwidth = 1;
    sourPanel.add(refFileL, c);
    c.gridx = 1; c.gridy = 10; c.gridwidth = 4;
    sourPanel.add(refFileD, c);
    c.gridx = 5; c.gridy = 10; c.gridwidth = GridBagConstraints.REMAINDER;
    sourPanel.add(button, c);

    c.gridx = 0; c.gridy = 11; c.gridwidth = 1;
    sourPanel.add(refTEXTL, c);
    c.gridx = 1; c.gridy = 11; c.gridwidth = GridBagConstraints.REMAINDER;
    sourPanel.add(refTEXTD, c);
    this.add(sourPanel);
    this.add(new JLabel("  "));


    JPanel repoPanel = new JPanel();
    repoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), panel.translate("Repo_Section")));
    repoPanel.setLayout(new GridBagLayout());
    c.insets = new Insets(0, 15, 0, 0);

    c.gridx = 0; c.gridy = 0; c.weightx = 1.0; c.gridwidth = 1; c.gridheight = 1;
    repoPanel.add(refREPOL, c);
    c.gridx = 1; c.gridy = 0; c.gridwidth = GridBagConstraints.REMAINDER;
    repoPanel.add(refREPOD, c);

    c.gridx = 0; c.gridy = 1; c.gridwidth = 1;
    repoPanel.add(refNAMEL, c);
    c.gridx = 1; c.gridy = 1; c.gridwidth = GridBagConstraints.REMAINDER;
    repoPanel.add(refNAMED, c);

    c.gridx = 0; c.gridy = 2;
    repoPanel.add(refADDRL, c);
    c.gridx = 1; c.gridy = 2; c.gridwidth = GridBagConstraints.REMAINDER;
    repoPanel.add(refADDRD, c);

    c.gridx = 0; c.gridy = 3; c.gridwidth = 1;
    repoPanel.add(refPOSTL, c);
    c.gridx = 1; c.gridy = 3;
    repoPanel.add(refPOSTD, c);
    c.gridx = 2; c.gridy = 3;
    repoPanel.add(refCITYL, c);
    c.gridx = 3; c.gridy = 3;
    repoPanel.add(refCITYD, c);
    c.gridx = 4; c.gridy = 3;
    repoPanel.add(refCTRYL, c);
    c.gridx = 5; c.gridy = 3; c.gridwidth = GridBagConstraints.REMAINDER;
    repoPanel.add(refCTRYD, c);

    c.gridx = 0; c.gridy = 4; c.gridwidth = 1;
    repoPanel.add(refEMAILL, c);
    c.gridx = 1; c.gridy = 4;
    repoPanel.add(refEMAILD, c);
    c.gridx = 2; c.gridy = 4;
    repoPanel.add(refPHONEL, c);
    c.gridx = 3; c.gridy = 4;
    repoPanel.add(refPHONED, c);
    c.gridx = 4; c.gridy = 4;
    repoPanel.add(refFAXL, c);
    c.gridx = 5; c.gridy = 4; c.gridwidth = GridBagConstraints.REMAINDER;
    repoPanel.add(refFAXD, c);

    c.gridx = 0; c.gridy = 5; c.gridwidth = 1;
    repoPanel.add(refWWWL, c);
    c.gridx = 1; c.gridy = 5; c.gridwidth = GridBagConstraints.REMAINDER;
    repoPanel.add(refWWWD, c);
    this.add(repoPanel);

    }

  /**
   * Get saved reference type for sources : act (0 or true) or register (1 or false)
   */
  public boolean getSavedType() {
     return registry.get("type", "0").equals("0");
     }

  /**
   * Save reference type for sources (act(0) or register(1))
   */
  public void saveType() {
     registry.put("type", refTypeAct.isSelected() ? "0" : "1");
     Registry.persist();
     return;
     }


  /**
   * Display source given by string
   */
  public void setSource(Entity ent) {

    if (ent == null) {
       refSOURD.setSelectedIndex(-1);
       refREPOD.setSelectedIndex(-1);
       return;
       }

    String source = "";
    if (eventTag == null || eventTag.length() == 0) {
       source = ent.toString();
       }
    else {
       Property prop = ent.getPropertyByPath(eventTag+":SOUR");
       if (prop != null) source = prop.toString();
       }

    if (!exists(source)) {
       refSOURD.setSelectedIndex(-1);
       refREPOD.setSelectedIndex(-1);
       return;
       }

    for (int i = 0 ; i < sources.length ; i++) {
       if (sources[i].toString().equals(source)) {
          refSOURD.setSelectedIndex(i);
          break;
          }
       }
    return;
    }

  /**
   * Display repository given by string
   */
  public void setRepository(Entity ent) {

    if (ent == null) {
       refSOURD.setSelectedIndex(-1);
       refREPOD.setSelectedIndex(-1);
       return;
       }

    String repo = ent.toString();

    if (!exists(repo)) {
       refSOURD.setSelectedIndex(-1);
       refREPOD.setSelectedIndex(-1);
       return;
       }

    for (int i = 0 ; i < repos.length ; i++) {
       if (repos[i].toString().equals(repo)) {
          refREPOD.setSelectedIndex(i);
          break;
          }
       }
    return;
    }


  /**
   * Selection list changed performed
   * --> populate fields
   */
  public void itemStateChanged(ItemEvent e) {

     if (e.getItemSelectable() == refSOURD || e.getItemSelectable() == refTypeAct || e.getItemSelectable() == refTypeReg) {
        populateSource();
        saveType();
        }
     if (e.getItemSelectable() == refREPOD) {
        populateRepo();
        }
     }

  /**
   * Populates fields upon user selecting a source
   */
  private String[] getAbbr() {

     java.util.List<String> abbrs = new ArrayList();
     for (int i = 0; i < sources.length; i++) {
        Entity ent = (Entity) sources[i];
        Property prop = ent.getPropertyByPath("SOUR:ABBR");
        String abbr = (prop != null) ? prop.toString() : "";
        if (!(abbr.length() == 0) && !abbrs.contains(abbr)) {
           abbrs.add(abbr);
           }
        }

     Collections.sort(abbrs);
     return (String[]) (abbrs.toArray(new String[abbrs.size()]));
     }




  /**
   * Populates fields upon user selecting a source
   *
   */
  public void populateSource() {

     Entity ent = getEntity(refSOURD.getText());
     Entity entUp = panel.getEntity();
     Property prop = null;
     String str = "";

     // Abbreviation
     if (ent != null) prop = ent.getPropertyByPath("SOUR:ABBR");
     if (prop != null) str = prop.toString(); else str = "";
     refABBRD.setText(str);

     // Register title
     if (ent != null) prop = isAct() ? ent.getPropertyByPath("SOUR:AUTH") : ent.getPropertyByPath("SOUR:TITL");
     if (prop != null) str = prop.toString(); else str = "";
     refAUTHD.setText(str);

     // Register events
     if (ent != null) prop = isAct() ? null : ent.getPropertyByPath("SOUR:DATA:EVEN");
     if (prop != null) str = prop.toString(); else str = "";
     refAUTH2D.setText(str);

     // Register period
     if (ent != null) prop = isAct() ? null : ent.getPropertyByPath("SOUR:DATA:EVEN:DATE");
     if (prop != null) str = prop.toString(); else str = "";
     refAUTH3D.setText(str);

     // Register places 
     if (ent != null) prop = isAct() ? null : ent.getPropertyByPath("SOUR:DATA:EVEN:PLAC");
     if (prop != null) str = prop.toString(); else str = "";
     refAUTH4D.setText(str);

     // Caln
     if (ent != null) prop = ent.getPropertyByPath("SOUR:REPO:CALN");
     if (prop != null) str = prop.toString(); else str = "";
     refCALND.setText(str);

     // Medi
     if (ent != null) prop = ent.getPropertyByPath("SOUR:REPO:CALN:MEDI");
     if (prop != null) str = prop.toString(); else str = "";
     refMEDID.setText(str);

     // Page in the register
     if (ent != null) prop = ((entUp == null || eventTag == null) ? null : entUp.getPropertyByPath(eventTag+":SOUR:PAGE"));
     if (prop != null) str = prop.toString(); else str = "";
     refPAGED.setText(str);

     // Quality of the source
     if (ent != null) prop = ((entUp == null || eventTag == null) ? null : entUp.getPropertyByPath(eventTag+":SOUR:QUAY"));
     if (prop != null) str = prop.toString().substring(0,1); else str = "";
     refQUAYD.setText(str);

     // File
     if (ent != null) prop = isAct() ? ent.getPropertyByPath("SOUR:OBJE:FILE") : ((entUp == null || eventTag == null) ? null : entUp.getPropertyByPath(eventTag+":SOUR:OBJE:FILE"));
     if (prop != null) {
        str = prop.toString();
        File file = new File(gedcom.getOrigin().getFile().getParent()+File.separator+str);
        refFileD.setFile(file);
        refFileD.setDirectory(file.getParent());
        viewButton.setFile(file);
        }
     else {
        refFileD.setDirectory(gedcom.getOrigin().getFile().getParent());
        refFileD.setFile("");
        viewButton.setFile(null);
        }

     // Act title
     if (isAct()) {
        if (ent != null) prop = ent.getPropertyByPath("SOUR:TITL");
        if (prop != null) str = prop.toString(); else str = "";
        }
     else {
        Property note = (entUp == null || eventTag == null) ? null : entUp.getPropertyByPath(eventTag+":SOUR:NOTE");
        str = (note != null && note.toString().indexOf("TITL:") == 0) ? note.toString().substring(5) : "";
        }
     refTITLD.setText(str);

     // Act text
     if (ent != null) prop = isAct() ? ent.getPropertyByPath("SOUR:TEXT") : ((entUp == null || eventTag == null) ? null : entUp.getPropertyByPath(eventTag+":SOUR:DATA:TEXT"));
     if (prop != null) str = prop.toString(); else str = "";
     refTEXTD.setText(str);

      // Repo
     if (ent != null) prop = ent.getPropertyByPath("SOUR:REPO");
     if (prop != null) {
        for (int r = 0 ; r < repos.length ; r++) {
           if (repos[r].toString().equals(prop.toString())) { 
              refREPOD.setSelectedIndex(r);
              break;
              }
           }
        }
     if (ent == null || prop == null) {
        refREPOD.setText("");
        }

     }

   /**
   * Populates fields upon user selecting a source
   *
   */
  private void populateRepo() {

     Entity ent = getEntity(refREPOD.getText());
     Property prop = null;
     String str = "";

     // Name
     if (ent != null) prop = ent.getPropertyByPath("REPO:NAME");
     if (prop != null) str = prop.toString(); else str = "";
     refNAMED.setText(str);

     // Address
     if (ent != null) prop = ent.getPropertyByPath("REPO:ADDR");
     if (prop != null) str = prop.toString(); else str = "";
     refADDRD.setText(str);

     // Post code
     if (ent != null) prop = ent.getPropertyByPath("REPO:ADDR:POST");
     if (prop != null) str = prop.toString(); else str = "";
     refPOSTD.setText(str);

     // City
     if (ent != null) prop = ent.getPropertyByPath("REPO:ADDR:CITY");
     if (prop != null) str = prop.toString(); else str = "";
     refCITYD.setText(str);

     // Country
     if (ent != null) prop = ent.getPropertyByPath("REPO:ADDR:CTRY");
     if (prop != null) str = prop.toString(); else str = "";
     refCTRYD.setText(str);

     // Email
     if (ent != null) prop = ent.getPropertyByPath("REPO:_EMAIL");
     if (prop != null) str = prop.toString(); else str = "";
     refEMAILD.setText(str);

     // Phone
     if (ent != null) prop = ent.getPropertyByPath("REPO:PHON");
     if (prop != null) str = prop.toString(); else str = "";
     refPHONED.setText(str);

     // Fax
     if (ent != null) prop = ent.getPropertyByPath("REPO:_FAX");
     if (prop != null) str = prop.toString(); else str = "";
     refFAXD.setText(str);

     // Web address
     if (ent != null) prop = ent.getPropertyByPath("REPO:_WWW");
     if (prop != null) str = prop.toString(); else str = "";
     refWWWD.setText(str);

     }


 /**
   * Get entity represented in the referenced combo box
   */
  public Entity getEntity(String ref) {
    return HelperDocs.getEntity(gedcom, ref);
    }

  /**
   * Get attachement type (isAct = true when type is refTypeAct) 
   */
  public boolean isAct() {
    return refTypeAct.isSelected();
    }

  /**
   * Check non empty repository
   */
  public boolean existsRepositoryField() {
    return (exists(refNAMED.getText()) || exists(refADDRD.getText()) || exists(refPOSTD.getText()) || exists(refCITYD.getText()) || exists(refCTRYD.getText()) || exists(refEMAILD.getText()) || exists(refPHONED.getText()) || exists(refFAXD.getText()) || exists(refWWWD.getText()));
    }

  /**
   * Check non empty source
   */
  public boolean existsSourceField() {
    return (exists(refABBRD.getText()) || exists(refAUTHD.getText()) || exists(refAUTH2D.getText()) || exists(refAUTH3D.getText()) || exists(refAUTH4D.getText()) || exists(refCALND.getText()) || exists(refMEDID.getText()) || exists(refPAGED.getText()) || exists(refQUAYD.getText()) || exists(refFileD) || exists(refTITLD.getText()) || exists(refTEXTD.getText()));
    }


  /**
   * Update or create act
   */
  public Entity upcreateAct(Property propEvent) throws GedcomException {

    // Check if repository is to be created
   Entity repository = getEntity(refREPOD.getText());
   if (repository == null && existsRepositoryField()) {
      repository = gedcom.createEntity(Gedcom.REPO);
      }

   if (repository != null) {
      // Name
      if (exists(refNAMED.getText())) upcreateProperty(repository, "NAME", refNAMED.getText());

      // Postal address
      if (exists(refPOSTD.getText()) || exists(refCITYD.getText()) || exists(refCTRYD.getText())) {
         Property prop = upcreateProperty(repository, "ADDR", refADDRD.getText());
         if (exists(refPOSTD.getText())) upcreateProperty(prop, "POST", refPOSTD.getText());
         if (exists(refCITYD.getText())) upcreateProperty(prop, "CITY", refCITYD.getText());
         if (exists(refCTRYD.getText())) upcreateProperty(prop, "CTRY", refCTRYD.getText());
         }

      // Remote contact details
      if (exists(refEMAILD.getText())) upcreateProperty(repository, "_EMAIL", refEMAILD.getText());
      if (exists(refPHONED.getText())) upcreateProperty(repository, "PHON", refPHONED.getText());
      if (exists(refFAXD.getText())) upcreateProperty(repository, "_FAX", refFAXD.getText());
      if (exists(refWWWD.getText())) upcreateProperty(repository, "_WWW", refWWWD.getText());
      }


   // Update source (source optional)
   Entity source = getEntity(refSOURD.getText());
   if (source == null && existsSourceField()) {
      source = gedcom.createEntity(Gedcom.SOUR);
      }

   if (source != null) {
      // Abbreviation
      if (exists(refABBRD.getText())) upcreateProperty(source, "ABBR", refABBRD.getText());

      // Repository
      if (repository != null) {
         Property prop = upcreateProperty(source, "REPO", "@"+repository.getId()+"@");
         try {
            ((PropertyXRef)prop).link();
            } catch (Exception e) {
               if (e.getMessage().indexOf("Already linked") == -1) e.printStackTrace();
            }
         if (exists(refCALND.getText())) {
            prop = upcreateProperty(prop, "CALN", refCALND.getText());
            if (exists(refMEDID.getText())) {
               upcreateProperty(prop, "MEDI", refMEDID.getText());
               }
            }

         }

      // Author
      if (refTypeAct.isSelected()) { // source is the act
         String strAuth = "";
         if (exists(refAUTHD.getText()))  strAuth += refAUTHD.getText();
         if (exists(refAUTH2D.getText())) strAuth += ((strAuth.length() > 0) ? " - " : "") + refAUTH2D.getText();
         if (exists(refAUTH3D.getText())) strAuth += ((strAuth.length() > 0) ? " - " : "") + refAUTH3D.getText();
         if (exists(refAUTH4D.getText())) strAuth += ((strAuth.length() > 0) ? " - " : "") + refAUTH4D.getText();
         if (strAuth.length() > 0) upcreateProperty(source, "AUTH", strAuth);
         }
      else { // source is the register
         if (exists(refAUTHD.getText())) upcreateProperty(source, "TITL", refAUTHD.getText());
         if (exists(refAUTH2D.getText())) {
            Property prop = upcreateProperty(source, "DATA", "");
            prop = upcreateProperty(prop, "EVEN", refAUTH2D.getText());
            if (exists(refAUTH3D.getText())) upcreateProperty(prop, "DATE", refAUTH3D.getText());
            if (exists(refAUTH4D.getText())) upcreateProperty(prop, "PLAC", refAUTH4D.getText());
            }
         }

      // Title and Text
      if (refTypeAct.isSelected()) { // source is the act
         if (exists(refTITLD.getText())) upcreateProperty(source, "TITL", refTITLD.getText());
         if (exists(refTEXTD.getText())) upcreateProperty(source, "TEXT", refTEXTD.getText());
         }

      // File
      if (exists(refFileD) && refTypeAct.isSelected()) {
         Property prop = upcreateProperty(source, "OBJE", "");
         String directory = gedcom.getOrigin().getFile().getParent();
         String filePath = refFileD.getFile().getAbsolutePath();
         upcreateProperty(prop, "FILE", filePath);
         upcreateProperty(prop, "FORM", "jpg");
         }



      // Link event to quality and page if an event exists
      if (propEvent != null) {
         Property pEventSour = upcreateProperty(propEvent, "SOUR", "@"+source.getId()+"@");
         try {
            ((PropertyXRef)pEventSour).link();
            } catch (Exception e) {
               if (e.getMessage().indexOf("Already linked") == -1) e.printStackTrace();
            }
         if (exists(refQUAYD.getText())) upcreateProperty(pEventSour, "QUAY", refQUAYD.getText());
         if (exists(refPAGED.getText())) upcreateProperty(pEventSour, "PAGE", refPAGED.getText());
         // Title and Text
         if (!refTypeAct.isSelected()) { // source is not the act
            if (exists(refTITLD.getText())) upcreateProperty(pEventSour, "NOTE", "TITL:"+refTITLD.getText());
            if (exists(refTEXTD.getText())) {
               Property prop = upcreateProperty(pEventSour, "DATA", "");
               upcreateProperty(prop, "TEXT", refTEXTD.getText());
               }
            }
         // File
         if (exists(refFileD) && !refTypeAct.isSelected()) {
            Property prop = upcreateProperty(pEventSour, "OBJE", "");
            String directory = gedcom.getOrigin().getFile().getParent();
            String filePath = refFileD.getFile().getAbsolutePath();
            upcreateProperty(prop, "FILE", filePath);
            upcreateProperty(prop, "FORM", "jpg");
            }
         }



      }

    if (source == null) return repository; else return source;
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

  private boolean exists(FileChooserWidget fcw) {
    return HelperDocs.exists(fcw);
    }


  /**
   * Action - view file
   */
  private class ViewFileButton extends Action2 {
    /** the wrapped association */
    private FileAssociation association;

    /** the wrapped file */
    private File file;

    /** constructor */
    protected ViewFileButton() {
      setImage(ImagesDocs.imgFile);
      setTip(panel.translate("Sour_File_Button"));
    }
    /** accessor */
    public void setFile(File f) {
      file = f;
    }
    /** run */
    protected void execute() {
     if (refFileD.isEmpty()) file = null;
     else file = refFileD.getFile();
     if (file==null)
       return;
     if (association==null)
       association = FileAssociation.get(file, "View", getTarget());
     if (association!=null)
       association.execute(file);
     }
  }

  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    Dimension size = super.getPreferredSize();
    return new Dimension(dataSet.panelWidth == 0 ? (int)size.getWidth() : dataSet.panelWidth, (int)size.getHeight());
  }



}
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

import docs.EditDocsPanel;
import docs.HelperDocs;
import docs.ImagesDocs;

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
import java.util.List;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EtchedBorder;


/**
 * The panel for entering source documents
 */
public class CopyPanel extends JPanel implements ActionListener  {

  /** our gedcoms */
  public Gedcom gedcom1 = null;
  public Gedcom gedcom2 = null;

  /** panel */
  private EditDocsPanel view;

  /** event type */
  static private Font PF = new Font("", Font.PLAIN, 12);
  static private Font BF = new Font("", Font.BOLD, 12);
  private int FS = 10;

  /** panel items */
  private ChoiceWidget refSOURD = new ChoiceWidget();
  private JLabel refFOUND = null;
  private JLabel refSTR2L = null;
  private Action2 runMatch = new Action2();

  /** entities */
  private int numberEntities;
  public Match[] matches = null;


  /**
   * Constructor
   */
  public CopyPanel(Gedcom gedcom, EditDocsPanel view, List<Entity> entities) {

    super();

    this.gedcom1 = gedcom;
    this.view = view;
    this.numberEntities = entities.size();

    setLayout(new BorderLayout());

    if (numberEntities == 0) {
       this.add(new JLabel(view.translate("nothingToCopy")));
       return;
       }

    // place gedcom panel element
    JPanel file = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel refSOURL = new JLabel(view.translate("targetGedcom"));
    refSOURL.setFont(PF);
    file.add(refSOURL);
    refSOURD.setValues(GedcomDirectory.getInstance().getGedcoms());
    file.add(refSOURD);
    JButton refRUN = new JButton(view.translate("matchButton"));
    refRUN.addActionListener(this);
    file.add(refRUN);
    refFOUND = new JLabel();
    file.add(refFOUND);
    file.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), ""));
    this.add(file, "North");

    // place list elements
    JPanel list = new JPanel();
    JLabel refSTR1L = new JLabel(view.translate("originGedcom",gedcom1.getName()));
    refSTR2L = new JLabel(view.translate("targetGedcom"));
    JLabel refCREAL = new JLabel(view.translate("createFlag"));
    refSTR1L.setFont(PF);
    refSTR2L.setFont(PF);
    refCREAL.setFont(PF);

    list.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0; c.gridy = 0; c.weightx = 1.0; c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = new Insets(0, 0, 0, 0);

    c.gridx = 0; c.gridy = 1; c.gridwidth = 2;
    list.add(refSTR1L, c);
    c.gridx = 2; c.gridy = 1; c.gridwidth = 1;
    list.add(refSTR2L, c);
    c.gridx = 3; c.gridy = 1;
    list.add(refCREAL, c);
    c.gridwidth = 1;

    // define elements of panel
    matches = new Match[numberEntities];
    int i = 0;
    for (Iterator it = entities.iterator(); it.hasNext();) {
       Entity ent = (Entity) it.next();
       matches[i] = new Match(gedcom1, null, ent);

       c.gridx = 0; c.gridy = 2+i;
       list.add(matches[i].refSTR1D, c);

       c.gridx = 1; c.gridy = 2+i;
       list.add(new JLabel("   =>   "), c);

       c.gridx = 2; c.gridy = 2+i; c.ipadx = 250;
       list.add(matches[i].refSTR2D, c);
       c.gridx = 3; c.gridy = 2+i; c.ipadx = 0;
       list.add(matches[i].refCHKD, c);

       i++;
       }

    list.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), ""));
    this.add(list);

    }



  /**
   * Class to display and assess matches
   */
  public class Match {

   public Entity ent1 = null;
   public Entity ent2 = null;

   public JLabel refSTR1D;
   public ChoiceWidget refSTR2D;
   public JCheckBox refCHKD;

   public Match(Gedcom gedcom1, Gedcom gedcom2, Entity entity1) {

      this.ent1 = entity1;
      refSTR1D = new JLabel(entity1.toString());
      refSTR2D = new ChoiceWidget();
      refCHKD = new JCheckBox("");
      }
   }

  /**
   * Button clicked
   */
   public void actionPerformed(ActionEvent e) {

    // Get and assign second gedcom (the target)
    String name = refSOURD.getText();
    if (name.trim().length() == 0) return;
    gedcom2 = getGedcom(name);
    if (gedcom2 == null) return;

    refSTR2L.setText(view.translate("targetGedcom0", gedcom2.getName()));

    // Fill in list boxes with second gedcom entities
    // and tries to match entities from first gedcom to entities of the second one
    Entity[] indis2 = gedcom2.getEntities(Gedcom.INDI, "INDI:NAME");
    Entity[] fams2 = gedcom2.getEntities(Gedcom.FAM, "FAM");
    Entity[] sources2 = gedcom2.getEntities(Gedcom.SOUR, "SOUR");
    Entity[] repos2 = gedcom2.getEntities(Gedcom.REPO, "REPO");

    int found = 0;

    for (int i = 0 ; i < numberEntities ; i++) {

       String tag = matches[i].ent1.getTag();
       if (tag.equals(Gedcom.INDI)) {
          matches[i].refSTR2D.setValues(indis2);
          matches[i].ent2 = findEntity(tag, matches[i].ent1, indis2);
          }
       else if (tag.equals(Gedcom.FAM)) {
          matches[i].refSTR2D.setValues(fams2);
          matches[i].ent2 = findEntity(tag, matches[i].ent1, fams2);
          }
       else if (tag.equals(Gedcom.SOUR)) {
          matches[i].refSTR2D.setValues(sources2);
          matches[i].ent2 = findEntity(tag, matches[i].ent1, sources2);
          }
       else if (tag.equals(Gedcom.REPO)) {
          matches[i].refSTR2D.setValues(repos2);
          matches[i].ent2 = findEntity(tag, matches[i].ent1, repos2);
          }

       if (matches[i].ent2 != null) { 
          matches[i].refSTR2D.setText(matches[i].ent2.toString());
          matches[i].refCHKD.setSelected(false);
          found++; 
          }
       else {
          matches[i].refCHKD.setSelected(true);
          }

       }
    refFOUND.setText("   " + view.translate("foundEntities", ""+found));
    }

  /**
   * Get gedcom instance
   */
  private Gedcom getGedcom(String name) {

   for (Gedcom g : GedcomDirectory.getInstance().getGedcoms()) {
     if (g.toString().equals(name)) return g;
     }
   return null;
   }

  /**
   * Find entity
   */
  private Entity findEntity(String tag, Entity ent1, Entity[] entities) {

    boolean found = false;

    for (int j=0 ; j < entities.length ; j++) {

       if (tag.equals(Gedcom.INDI)) {
          Indi indi1 = (Indi)ent1;
          Indi indi2 = (Indi)entities[j];
          found = indi1 != null && indi2 != null && indi1.getLastName().equals(indi2.getLastName()) && indi1.getFirstName().equals(indi2.getFirstName());
          }
       else if (tag.equals(Gedcom.FAM)) { 
          Fam fam1 = (Fam)ent1;
          Fam fam2 = (Fam)entities[j];
          found = fam1 != null && fam2 != null && fam1.getHusband() != null && fam2.getHusband() != null && fam1.getHusband().getLastName().equals(fam2.getHusband().getLastName())  && fam1.getHusband().getFirstName().equals(fam2.getHusband().getFirstName()) && fam1.getWife() != null && fam2.getWife() != null && fam1.getWife().getLastName().equals(fam2.getWife().getLastName()) && fam1.getWife().getFirstName().equals(fam2.getWife().getFirstName());
          }
       else if (tag.equals(Gedcom.SOUR)) { 
          Source source1 = (Source)ent1;
          Source source2 = (Source)entities[j];
          found = ((source1.getTitle().equals(source2.getTitle()) && source1.getTitle().trim().length() !=0) || (source1.getText().equals(source2.getText())  && source1.getText().trim().length() !=0));
          }
       else if (tag.equals(Gedcom.REPO)) { 
          Repository repo1 = (Repository)ent1;
          Repository repo2 = (Repository)entities[j];
          found = repo1.getProperty("NAME") != null && repo2.getProperty("NAME") != null && repo1.getProperty("NAME").toString().equals(repo2.getProperty("NAME").toString());
          }

       if (found) {
          return entities[j];
          }
       }

   return null;
   }



}
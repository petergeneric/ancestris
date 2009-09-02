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

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;


/**
 * The panel for entering a contract event
 */
public class ContractPanel extends JPanel {

  static private Font PF = new Font("", Font.PLAIN, 12);
  static private Font BF = new Font("", Font.BOLD, 12);

  private DataSet dataSet;
  private int FS = 10;
  public DateWidget refDATED = new DateWidget();
  public TextFieldWidget refAGNCD = new TextFieldWidget("", FS);
  public ChoiceWidget refPLACD = new ChoiceWidget();

  public ContractPanel(DocsListener panel, DataSet dataSet) {


    super();
    this.dataSet = dataSet;
    this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), panel.translate("Contract")));

    // define elements of panel
    JLabel refDATEL = new JLabel(panel.translate("Contract_Signed"));
    refDATEL.setFont(PF);
    refDATED.setFont(BF);
    JLabel refAGNCL = new JLabel(panel.translate("Contract_Solicitor"));
    refAGNCL.setFont(PF);
    refAGNCD.setFont(BF);
    JLabel refPLACL = new JLabel(panel.translate("Contract_Place"));
    refPLACL.setFont(PF);
    refPLACD.setValues(dataSet.places);

     // set grid
    this.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;

    // position elements
    c.insets = new Insets(0, 15, 0, 0);
    c.gridx = 0; c.gridy = 0; c.weightx = 1.0; c.gridwidth = 1;
    this.add(refDATEL, c);
    c.gridx = 1; c.gridy = 0;
    this.add(refDATED, c);
    c.gridx = 2; c.gridy = 0;
    this.add(refAGNCL, c);
    c.gridx = 3; c.gridy = 0; c.ipadx = 150; c.gridwidth = GridBagConstraints.REMAINDER;
    this.add(refAGNCD, c);

    c.gridx = 0; c.gridy = 1;
    this.add(refPLACL, c);
    c.gridx = 1; c.gridy = 1; c.gridwidth = GridBagConstraints.REMAINDER;
    this.add(refPLACD, c);

    }


  /**
   * Display source given by string
   */
  public void setContract(Entity ent) {

    Property prop = null;
    String str = "";

    // Date
    if (ent != null) prop = ent.getPropertyByPath("FAM:MARC:DATE");
    if (prop != null && prop instanceof PropertyDate) {
       PropertyDate pdate = (PropertyDate) prop;
       refDATED.setValue(pdate.getStart());
       }
    else refDATED.setValue(new PointInTime());

    // Agent
    if (ent != null) prop = ent.getPropertyByPath("FAM:MARC:AGNC");
    if (prop != null) str = prop.toString(); else str = "";
    refAGNCD.setText(str);

    // Place
    if (ent != null) prop = ent.getPropertyByPath("FAM:MARC:PLAC");
    if (prop != null) str = prop.toString(); else str = "";
    refPLACD.setText(str);
    refPLACD.getTextEditor().setCaretPosition(0);

    return;
    }

  /**
   * Update or create contract
   */
  public Property upcreateContract(Fam fam) throws GedcomException {

   Property contract = null;
   if (fam != null) {
      contract = HelperDocs.upcreateProperty(fam, "MARC", "");
      if (exists(refDATED.getValue())) HelperDocs.upcreateProperty(contract, "DATE", refDATED.getValue().getValue());
      if (exists(refAGNCD.getText())) HelperDocs.upcreateProperty(contract, "AGNC", refAGNCD.getText());
      if (exists(refPLACD.getText())) HelperDocs.upcreateProperty(contract, "PLAC", refPLACD.getText());
      }

    return contract;
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

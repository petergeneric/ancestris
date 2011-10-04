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

import genj.edit.Options;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyName;
import genj.util.swing.Action2;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.DialogHelper;
import genj.util.swing.TextFieldWidget;
import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : NAME
 */
public class FullNameBean extends PropertyBean {

//  private final static NestedBlockLayout LAYOUT = new NestedBlockLayout(
//      "<table>"+
//       "<row><l/><row><v wx=\"1\"/><check pad=\"0\"/></row></row>"+
//       "<row><l/><v wx=\"1\"/></row>"+
//       "<row><l/><v/></row>"+
//       "<row><l/><v/></row>"+
//      "</table>"
//  );
//
  /** our components */
  private Property[] sameLastNames = new Property[0];
  private ChoiceWidget cLast, cFirst;
  private JCheckBox cAll;
  private TextFieldWidget tSuff, tNick;

  // Extended Panel
  private javax.swing.JPanel extPanel;
  private TextFieldWidget tNPfx, tNSfx, tSPfx;



  /**
   * Calculate message for replace all last names
   */
  private String getReplaceAllMsg() {
    if (sameLastNames.length<2)
      return null;
    // we're using getDisplayValue() here
    // because like in PropertyRelationship's case there might be more
    // in the gedcom value than what we want to display (witness@INDI:BIRT)
    return RESOURCES.getString("choice.global.confirm", ""+sameLastNames.length, ((PropertyName)getProperty()).getLastName(), cLast.getText());
  }
  
  public FullNameBean() {
// Layout, Column and Row constraints as arguments.
MigLayout layout = new MigLayout(
     new LC().fillX().hideMode(2),
	 new AC().align("right").gap("rel").grow().fill()
//         ,new AC().gap("10")
         );

    setLayout(layout);

    cLast  = new ChoiceWidget();
    cLast.addChangeListener(changeSupport);
    cLast.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        String msg = getReplaceAllMsg();
        if (msg!=null) {
          cAll.setVisible(true);
          cAll.setToolTipText(msg);
        }
      }
    });
    cLast.setIgnoreCase(true);

    cFirst = new ChoiceWidget();
    cFirst.addChangeListener(changeSupport);
    cFirst.setIgnoreCase(true);

    tSuff  = new TextFieldWidget("", 10); 
    tSuff.addChangeListener(changeSupport);

    tNick = new TextFieldWidget("", 10); 
    tNick.addChangeListener(changeSupport);

    tNPfx  = new TextFieldWidget("", 10);
    JLabel lNPfx = new JLabel(Gedcom.getName("NPFX"));
    lNPfx.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
    tNPfx.addChangeListener(changeSupport);

    tNSfx  = new TextFieldWidget("", 10);
    JLabel lNSfx = new JLabel(Gedcom.getName("NSFX"));
    lNSfx.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
    tNSfx.addChangeListener(changeSupport);

    tSPfx  = new TextFieldWidget("", 10);
    JLabel lSPfx = new JLabel(Gedcom.getName("SPFX"));
    lSPfx.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
    tSPfx.addChangeListener(changeSupport);

    cAll = new JCheckBox();
    cAll.setBorder(new EmptyBorder(1,1,1,1));
    cAll.setVisible(false);
    cAll.setRequestFocusEnabled(false);
    // listen to selection of global and ask for confirmation
    cAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String msg = getReplaceAllMsg();
        if (msg!=null&&cAll.isSelected()) {
          int rc = DialogHelper.openDialog(RESOURCES.getString("choice.global.enable"), DialogHelper.QUESTION_MESSAGE, msg, Action2.yesNo(), FullNameBean.this);
          cAll.setSelected(rc==0);
        }
      }
    });

        final JCheckBox jCheckBox1 = new JCheckBox();

        jCheckBox1.setFont(new Font("DejaVu Sans", 0, 10)); // NOI18N
        jCheckBox1.setText(org.openide.util.NbBundle.getMessage(FullNameBean.class, "FullNameBean.jCheckBox1.text")); // NOI18N
        jCheckBox1.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extPanel.setVisible(jCheckBox1.isSelected());
            }
        });

    // Layout the bean
    add(new JLabel(PropertyName.getLabelForLastName()));
    add(cLast,new CC().split().growX().gapRight("0"));
    add(cAll,new CC().wrap());

    add(new JLabel(PropertyName.getLabelForFirstName()));
    add(cFirst,new CC().wrap());

    add(new JLabel(PropertyName.getLabelForSuffix()));
    add(tSuff,new CC().wrap());

    add(new JLabel(Gedcom.getName("NICK")));
    add(tNick,new CC().wrap());

    extPanel = new JPanel(new MigLayout(
            new LC().fillX(),
            new AC().align("right").gap("rel").grow().fill()
            ));
    extPanel.setVisible(jCheckBox1.isSelected());
    extPanel.add(lNPfx);
    extPanel.add(tNPfx,new CC().wrap());

    extPanel.add(lSPfx);
    extPanel.add(tSPfx,new CC().wrap());

    extPanel.add(lNSfx);
    extPanel.add(tNSfx,new CC().wrap());

    add(jCheckBox1,new CC().wrap());
    add(extPanel,new CC().grow().spanX().gapBefore("20").wrap());

    
    // we're done aside from declaring the default focus
    defaultFocus = cFirst;

  }

  /**
   * Finish editing a property through proxy
   */
  @Override
  @SuppressWarnings("fallthrough")
  protected void commitImpl(Property property) {

    PropertyName p = (PropertyName) property;
    
    // ... calc texts
    String first = cFirst.getText().trim();
    String last  = cLast .getText().trim();
    String suff  = tSuff .getText().trim();
    String nick  = tNick .getText().trim();
    
    Gedcom ged = p.getGedcom();
    if (ged!=null) {
      switch (Options.getInstance().correctName) {
      // John DOE
      case 2:
        last = last.toUpperCase(ged.getLocale());
        cLast.setText(last);
      // John Doe
      case 1:
        if (first.length()>0) {
          first = Character.toString(first.charAt(0)).toUpperCase(ged.getLocale()) + first.substring(1);
          cFirst.setText(first);
        }
      }
    }

    // ... store changed value
    p.setName( first, last, suff, cAll.isSelected());
    p.setNick( nick );
    
    // start fresh
    setPropertyImpl(p);

    // Done
  }

  /**
   * Set context to edit
   */
  public void setPropertyImpl(Property prop) {
    
    PropertyName name = (PropertyName)prop;
    if (name==null) {
      sameLastNames = new Property[0];
      cLast.setValues(PropertyName.getLastNames(getRoot().getGedcom(), true));
      cLast.setText("");
      cFirst.setValues(PropertyName.getFirstNames(getRoot().getGedcom(), true));
      cFirst.setText("");
      tSuff.setText("");
      tNick.setText("");
    } else {
      // keep track of who has the same last name
      sameLastNames = name.getSameLastNames();
      // first, last, suff
      cLast.setValues(name.getLastNames(true));
      cLast.setText(name.getLastName());
      cFirst.setValues(name.getFirstNames(true));
      cFirst.setText(name.getFirstName()); 
      tSuff.setText(name.getSuffix()); 
      tNick.setText(name.getNick());
    }
    
    cAll.setVisible(false);
    cAll.setSelected(false);

    // done
  }

} //ProxyName

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
package ancestris.core.modules.nav;

import ancestris.view.SelectionSink;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Indi;
import genj.gedcom.PropertySex;
import genj.util.GridBagHelper;
import ancestris.core.actions.AbstractAncestrisAction;
import genj.util.swing.ImageIcon;
import genj.util.swing.PopupWidget;
import genj.view.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import org.openide.util.NbBundle;

import spin.Spin;

/**
 * A navigator with buttons to easily navigate through Gedcom data
 */
public class NavigatorView extends View {
  
  private final static String 
    FATHER   = "father",
    MOTHER   = "mother",
    YSIBLING = "ysibling",
    OSIBLING = "osibling",
    PARTNER  = "partner",
    CHILD    = "child";

  private final static ImageIcon
    imgYSiblings = new ImageIcon(NavigatorView.class,"YSiblings"),
    imgOSiblings = new ImageIcon(NavigatorView.class,"OSiblings"),
    imgChildren  = new ImageIcon(NavigatorView.class,"Children"),
    imgFather    = new ImageIcon(NavigatorView.class,"Father"),
    imgMother    = new ImageIcon(NavigatorView.class,"Mother"),
    imgMPartner  = Indi.IMG_MALE,
    imgFPartner  = Indi.IMG_FEMALE;


  private GedcomListener callback = (GedcomListener)Spin.over(new GedcomListenerAdapter() {
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      if (context!=null&&context.getEntity()==entity)
        setContext(new Context(gedcom), true);
    }
  });
  
  /** components */
  private JLabel labelCurrent, labelSelf;
  private Map<String, PopupWidget> key2popup = new HashMap<String, PopupWidget>();
  private JPanel popupPanel = createPopupPanel();
  
  /** the current context */
  private Context context = new Context();

  /**
   * Constructor
   */
  public NavigatorView() {
    
    // layout    
    setLayout(new BorderLayout());

    labelCurrent = new JLabel();
    labelCurrent.setBorder(BorderFactory.createTitledBorder(Gedcom.getName(Gedcom.INDI,false)));
    add(labelCurrent,BorderLayout.NORTH);
    add(popupPanel,BorderLayout.CENTER);
    
//    // setup key bindings
//    new Shortcut(FATHER  );
//    new Shortcut(MOTHER  );
//    new Shortcut(YSIBLING);
//    new Shortcut(OSIBLING);
//    new Shortcut(PARTNER );
//    new Shortcut(CHILD   );

    // done    

  }

//  /**
//   * A shortcut
//   */
//  private class Shortcut extends AbstractAction {
//    /** relative key */
//    String relative;
//    /** constructor */
//    Shortcut(String relative) {
//      
//      // remember shortcut's relative 
//      this.relative = relative;
//
//      // identify mnemonic
//      MnemonicAndText mat = new MnemonicAndText(resources.getString(relative));
//      
//      KeyStroke keystroke = KeyStroke.getKeyStroke("control "+mat.getMnemonic());
//      if (keystroke!=null) {
//        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(keystroke, relative);
//        //getInputMap(WHEN_FOCUSED).put(keystroke, relative);
//        getActionMap().put(relative, this);
//      }
//    }
//    /** performed */
//    public void actionPerformed(ActionEvent e) {
//      getPopup(relative).doClick();
//    }
//  } //Shortcut

  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(140,200);
  }

  /**
   * context changer
   */
  @Override
  public void setContext(Context newContext, boolean isActionPerformed) {
    
    // disconnect from old
    if (context.getGedcom()!=null && context.getGedcom()!=newContext.getGedcom()) {
      context.getGedcom().removeGedcomListener(callback);
      
      // connect to new
      if (newContext.getGedcom()!=null)
        // connect to new
        newContext.getGedcom().addGedcomListener(callback);
    }


    // stay as is?
    Indi old = (Indi)context.getEntity();
    if (old!=null && context.getGedcom().contains(old) && !(newContext.getEntity() instanceof Indi || newContext.getEntity() instanceof Fam) )
      return;

    Indi newIndi = null;
    if (newContext.getEntity() instanceof Indi) {
        newIndi = (Indi)newContext.getEntity();
    } else if (newContext.getEntity() instanceof Fam){
        Fam theFam = (Fam)newContext.getEntity();
        if (theFam != null && theFam.getWife() != null)
            newIndi = theFam.getWife();
        if (theFam != null && theFam.getHusband() != null)
            newIndi = theFam.getHusband();
    }

    // entity to take?
    if (newIndi != null) {
      
      context = new Context(newIndi);

      for (Component c : popupPanel.getComponents())
        c.setEnabled(true);
      
      // jumps
      Indi current = (Indi)context.getEntity();
      setJump (FATHER  , current.getBiologicalFather());
      setJump (MOTHER  , current.getBiologicalMother());
      setJumps(OSIBLING, current.getOlderSiblings());
      setJumps(PARTNER , current.getPartners());
      setJumps(YSIBLING, current.getYoungerSiblings());
      setJumps(CHILD   , current.getChildren());
      // update label
      labelCurrent.setText(current.toString());
      labelCurrent.setIcon(current.getImage(false));

      // update the self label/partner popup images
      PopupWidget partner = getPopup(PARTNER);
      switch (current.getSex()) {
        case PropertySex.FEMALE:
          labelSelf.setIcon(imgFPartner);
          partner.setIcon(imgMPartner);
          break;
        case PropertySex.MALE:
          labelSelf.setIcon(imgMPartner);
          partner.setIcon(imgFPartner);
          break;
      }
      
    } else {

      context = new Context(newContext.getGedcom());

      for (Component c : popupPanel.getComponents())
        c.setEnabled(false);
      
      // no jumps
      setJump(FATHER  , null);
      setJump(MOTHER  , null);
      setJump(OSIBLING, null);
      setJumps(PARTNER , null);
      setJump(YSIBLING, null);
      setJumps(CHILD   , null);
      
      // update label
      labelCurrent.setText("n/a");
      labelCurrent.setIcon(null);
    }

    // done
  }
  
  /**
   * Return popup by key
   */
  private PopupWidget getPopup(String key) {
    return key2popup.get(key);  
  }

  /**
   * remember a jump to individual
   */
  private void setJump(String key, Indi i) {
    setJumps(key, i==null ? new Indi[0] : new Indi[]{ i });
  }
  
  /**
   * remember jumps to individuals
   */
  private void setJumps(String key, Indi[] is) {
    // lookup popup
    PopupWidget popup = getPopup(key);
    popup.removeItems();
    // no jumps?
    if (is==null||is.length==0) {
      popup.setEnabled(false);
    } else {
      popup.setEnabled(true);
      for (int i=0;i<is.length;i++) {
        popup.addItem(new Jump(is[i]));
      }
    }
    // done
  }
    
  /**
   * Creates a button
   */
  private JComponent createPopup(String key, ImageIcon i) {
    
    // create result
    PopupWidget result = new PopupWidget();
    result.setIcon(i);
    result.setFocusPainted(false);
    result.setFireOnClickWithin(500);
    result.setFocusable(false);
    result.setEnabled(false);
//    result.setToolTipText(new MnemonicAndText(resources.getString(key)).getText("Ctrl-"));
    result.setToolTipText(NbBundle.getMessage(NavigatorView.class, "tip."+key));

    // remember    
    key2popup.put(key, result);
    
    // done
    return result;
  }

  /**
   * Creates the panel
   */
  private JPanel createPopupPanel() {    
    
    final TitledBorder border = BorderFactory.createTitledBorder(NbBundle.getMessage(NavigatorView.class, "nav.navigate.title"));
    final JPanel result = new PopupPanel();
    result.setBorder(border);
    GridBagHelper gh = new GridBagHelper(result);
    
    // add the buttons
    JComponent
      popFather   = createPopup(FATHER,   imgFather),
      popMother   = createPopup(MOTHER,   imgMother),
      popOSibling = createPopup(OSIBLING, imgOSiblings),
      popPartner  = createPopup(PARTNER,  imgMPartner),
      popYSibling = createPopup(YSIBLING, imgYSiblings),
      popChildren = createPopup(CHILD,    imgChildren); 

    labelSelf = new JLabel(Gedcom.getEntityImage(Gedcom.INDI),SwingConstants.CENTER);

    popPartner.setPreferredSize(popOSibling.getPreferredSize());
    popFather .setPreferredSize(popOSibling.getPreferredSize());
    popMother .setPreferredSize(popOSibling.getPreferredSize());
    labelSelf.setPreferredSize(popOSibling.getPreferredSize());
    
    gh.add(popFather  ,4,1,1,1);
    gh.add(popMother  ,5,1,1,1);
    gh.add(popOSibling,1,2,2,1,0,new Insets(12,0,12,12));
    gh.add(labelSelf  ,4,2,1,1);
    gh.add(popPartner ,5,2,1,1);
    gh.add(popYSibling,7,2,2,1,0,new Insets(12,12,12,0));
    gh.add(popChildren,4,3,2,1);

    // done
    return result;
  }
  
  /**
   * A panel for the popup buttons that connects 'em with lines
   */
  private class PopupPanel extends JPanel {
    /**
     * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
     */
    protected void paintChildren(Graphics g) {
    
      // paint lines
      g.setColor(Color.lightGray);
      
      line(g,getPopup(MOTHER), getPopup(OSIBLING));
      line(g,getPopup(MOTHER), getPopup(YSIBLING));
      line(g,getPopup(MOTHER), labelSelf);
      line(g,getPopup(PARTNER), getPopup(CHILD));
          
      // now paint popup buttons
      super.paintChildren(g);
    
      // done
    }
    
    /**
     * connect components (lower/left - top/center)
     */
    private void line(Graphics g, JComponent c1, JComponent c2) {
      Rectangle
        a = c1.getBounds(),
        b = c2.getBounds();
      int y = (a.y+a.height+b.y)/2;
      int x = a.x;
      g.drawLine(x,a.y+a.height,x,y);
      x = b.x+b.width/2;
      g.drawLine(x,y,x,b.y);
      g.drawLine(a.x,y,x,y);
//      g.drawLine(a.x,a.y+a.height,b.x+b.width/2,b.y);
    }
  
  } //PopupPanel

  /**
   * Jump to another indi
   */
  private class Jump extends AbstractAncestrisAction {
    /** the target */
    private Indi target;
    /** constructor */
    private Jump(Indi taRget) {
      // remember
      target = taRget;
      // our looks
      setText(target.toString());
      setImage(target.getImage(false));
    }
    /** do it */
    public void actionPerformed(ActionEvent event) {
      // propagate to others (do this before event.getSource() gets disconnected)
      SelectionSink.Dispatcher.fireSelection(event, new Context(target));
      // follow immediately
      setContext(new Context(target),true);
    }
  } //Jump

} ///NavigatorView

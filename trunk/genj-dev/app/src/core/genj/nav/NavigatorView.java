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
package genj.nav;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Indi;
import genj.gedcom.PropertySex;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ImageIcon;
import genj.util.swing.PopupWidget;
import genj.view.ContextSelectionEvent;
import genj.view.ViewContext;
import genj.window.WindowBroadcastEvent;
import genj.window.WindowBroadcastListener;
import genj.window.WindowManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import spin.Spin;

/**
 * A navigator with buttons to easily navigate through Gedcom data
 */
public class NavigatorView extends JPanel implements WindowBroadcastListener {
  
  private static Resources resources = Resources.get(NavigatorView.class);

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


  private GedcomListener callback = new GedcomListenerAdapter() {
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      if (current == entity) {
        setCurrentEntity(gedcom.getFirstEntity(Gedcom.INDI));
      } else {
        setCurrentEntity(current);
      }
    }
  };
  
  /** the label holding information about the current individual */
  private JLabel labelCurrent, labelSelf;
  
  /** the current individual */
  private Indi current;
  
  /** jumps per key */
  private Map key2jumps = new HashMap();
  
  /** popups per key */
  private Map key2popup = new HashMap();
  
  /** the gedcom */
  private Gedcom gedcom;
  
  private Registry registry;
  
  /**
   * Constructor
   */
  public NavigatorView(String title, Gedcom gedcom, Registry registry) {
    
    // remember
    this.gedcom = gedcom;
    this.registry = registry;
    
    // layout    
    setLayout(new BorderLayout());

    labelCurrent = new JLabel();
    labelCurrent.setBorder(BorderFactory.createTitledBorder(Gedcom.getName(Gedcom.INDI,false)));
    add(labelCurrent,BorderLayout.NORTH);
    add(new JScrollPane(createPopupPanel()),BorderLayout.CENTER);
    
    // Check if we can preset something to show
    Entity entity = gedcom.getEntity(registry.get("entity", (String)null));
    if (entity==null) entity = gedcom.getFirstEntity(Gedcom.INDI);
    if (entity!=null) 
      setCurrentEntity(entity);
    
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

  public void addNotify() {
    // continue
    super.addNotify();
    // listen
    gedcom.addGedcomListener((GedcomListener)Spin.over(callback));
  }
  
  /**
   * @see javax.swing.JComponent#removeNotify()
   */
  public void removeNotify() {
    // stop listening
    gedcom.removeGedcomListener((GedcomListener)Spin.over(callback));
    // remember
    if (current!=null)
      registry.put("entity", current.getId());
    
    // continue
    super.removeNotify();
  }

  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(140,200);
  }

  /**
   * Context listener callback
   */  
  public boolean handleBroadcastEvent(WindowBroadcastEvent event) {
    ContextSelectionEvent cse = ContextSelectionEvent.narrow(event, gedcom);
    if (cse!=null)
      setCurrentEntity(cse.getContext().getEntity());
    return true;
  }
  
  /**
   * Set the current entity
   */
  public void setCurrentEntity(Entity e) {
    
    // only individuals - and not already current
    if (e==current || (e!=null&&!(e instanceof Indi)) ) 
      return;
    
    // forget jumps
    key2jumps.clear();
    
    // and current
    current = (Indi)e;

    // nothing?
    if (current == null) {
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
    } else {
      // jumps
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

    }
          
    // done
  }
  
  /**
   * Return popup by key
   */
  private PopupWidget getPopup(String key) {
    return (PopupWidget)key2popup.get(key);  
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
    ArrayList jumps = new ArrayList();
    // no jumps?
    if (is==null||is.length==0) {
      popup.setEnabled(false);
    } else {
      popup.setEnabled(true);
      for (int i=0;i<is.length;i++) 
        jumps.add(new Jump(is[i]));
    }
    // done
    popup.setActions(jumps);
  }
    
  /**
   * Creates a button
   */
  private JComponent createPopup(String key, ImageIcon i) {
    
    // create result
    PopupWidget result = new PopupWidget();
    result.setIcon(i);
    result.setFocusPainted(false);
    result.setFireOnClick(true);
    result.setFocusable(false);
    result.setEnabled(false);
//    result.setToolTipText(new MnemonicAndText(resources.getString(key)).getText("Ctrl-"));
    result.setToolTipText(resources.getString("tip."+key));

    // remember    
    key2popup.put(key, result);
    
    // done
    return result;
  }

  /**
   * Creates the panel
   */
  private JPanel createPopupPanel() {    
    
    final String title = resources.getString("nav.navigate.title");
    final TitledBorder border = BorderFactory.createTitledBorder(title);
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
  private class Jump extends Action2 {
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
    protected void execute() {
      // follow immediately
      setCurrentEntity(target);
      // propagate to others
      WindowManager.broadcast(new ContextSelectionEvent(new ViewContext(target), NavigatorView.this, true));
    }
  } //Jump

} ///NavigatorView

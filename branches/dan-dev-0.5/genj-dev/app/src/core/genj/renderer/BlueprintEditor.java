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
package genj.renderer;

import genj.common.PathTreeWidget;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Grammar;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertySimpleReadOnly;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * An editor component for changing a rendering scheme */
public class BlueprintEditor extends JSplitPane {
  
  /** faked values */
  private final static Map<String,String> SAMPLES = new HashMap<String, String>();
  
  static {
    SAMPLES.put("NAME", "John /Doe/");
    SAMPLES.put("SEX" , "M");
    SAMPLES.put("DATE", "01 JAN 1900");
    SAMPLES.put("PLAC", "Nice Place");
    SAMPLES.put("ADDR", "Long Address");
    SAMPLES.put("CITY", "Big City");
    SAMPLES.put("POST", "12345");
  }

  /** the text are for the html */
  private JTextArea source;
  
  /** the preview */
  private Preview preview;
  
  /** resources */
  private final static Resources resources = Resources.get(BlueprintEditor.class);
  
  /** the grammar we're looking at*/
  private Grammar grammar = Grammar.V55;
  
  /** the current scheme */
  private Blueprint blueprint;

  /** the insert button */
  private AbstractButton bInsert;
  
  /** an example entity we use */
  private Entity example; 
  
  /** whether we've changed */
  private boolean isChanged = false;
  
  /** the blueprint manager */
  private BlueprintManager blueprintManager = BlueprintManager.getInstance();
    
  /**
   * Constructor   */
  public BlueprintEditor(Entity recipient) { 
    example = recipient;
    grammar = recipient.getGedcom().getGrammar();
    // preview
    preview = new Preview();
    preview.setBorder(BorderFactory.createTitledBorder(resources.getString("blueprint.preview")));
    // edit
    JPanel edit = new JPanel(new BorderLayout());
      // html
      source = new JTextArea(3,32);
      source.setFont(new Font("Monospaced", Font.PLAIN, 12));
      JScrollPane scroll = new JScrollPane(source);
      scroll.setBorder(BorderFactory.createTitledBorder("HTML"));
      // buttons
      bInsert = new JButton(new Insert());
    edit.setMinimumSize(new Dimension(0,0));
    edit.add(scroll, BorderLayout.CENTER);
    edit.add(bInsert, BorderLayout.SOUTH);
    // layout
    setLeftComponent(preview);
    setRightComponent(edit);
    setDividerLocation(Integer.MAX_VALUE);
    setOrientation(JSplitPane.VERTICAL_SPLIT);
    setOneTouchExpandable(true);
    // event listening
    source.getDocument().addDocumentListener(preview);
    // intial set
    set(null);
    // done
  }
  
  /**
   * @see javax.swing.JSplitPane#getLastDividerLocation()
   */
  public int getLastDividerLocation() {
    return getSize().height/2;
  }
  
  /**
   * Set Gedcom, Blueprint
   */
  public void set(Blueprint setBlueprint) {
    // resolve buttons and html
    if (setBlueprint==null) {
      blueprint = null;
      source.setText("");
    } else {
      blueprint = setBlueprint;
      source.setText(blueprint.getHTML());
      source.setCaretPosition(0);
    }
    boolean edit = blueprint!=null&&!blueprint.isReadOnly();
    bInsert.setEnabled(edit);
    source.setEditable(edit);
    source.setToolTipText(blueprint!=null&&blueprint.isReadOnly() ? resources.getString("blueprint.readonly", blueprint.getName()) : null);
    if (edit)
      setSourceVisible(true);
    // mark unchanged
    isChanged = false;
    // make sure that changes
    preview.repaint();
    // done    
  }
  
  /**
   * Commits changes   */
  public void commit() {
    if (blueprint!=null&&isChanged) {
      blueprint.setSource(source.getText());
      try {
        blueprintManager.saveBlueprint(blueprint);
        // mark unchanged
        isChanged = false;
      } catch (IOException e) {
        Logger.getLogger("genj.renderer").log(Level.WARNING, "can't save blueprint", e);
      }
    }
  }
  
  /**
   * Make sure html is visible
   */
  public void setSourceVisible(boolean v) {
    // this doesn't work if component isn't "correctly realized"
    SwingUtilities.invokeLater(new ShowHTML(v));
  }
  
  private class ShowHTML implements Runnable {
    private boolean visible;
    public ShowHTML(boolean visible) {
      this.visible = visible;
    }
    public void run() {
      setDividerLocation( visible ? 0.5D : 1.0D);
    }
  }
  
  /**
   * The preview   */
  private class Preview extends JComponent implements DocumentListener {
    /**
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    public void changedUpdate(DocumentEvent e) {
      isChanged = true;
      repaint();
    }
    /**
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) {
      isChanged = true;
      repaint();
    }
    /**
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    public void removeUpdate(DocumentEvent e) {
      isChanged = true;
      repaint();
    }
    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) {
      
      ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      
      // no html doing nothing
      if (source.getText().length()==0) return; 
      // fix bounds (border changes insets)
      Rectangle bounds = getBounds();
      Insets insets = getInsets();
      bounds.x += insets.left;
      bounds.y += insets.top ;
      bounds.width -= insets.left+insets.right;
      bounds.height-= insets.top +insets.bottom;
      // clear background
      g.setColor(Color.white);
      g.fillRect(bounds.x,bounds.y,bounds.width,bounds.height);
      // render content
      g.setFont(getFont());
      BlueprintRenderer renderer = new BlueprintRenderer(new Blueprint(source.getText())) {
        @Override
        protected Property getProperty(Entity entity, TagPath path) {
          
          // try to lookup from entity
          Property result = super.getProperty(entity, path);
          if (result!=null) 
            return result;

          // generate a sample value
          String sample = SAMPLES.get(path.getLast());
          if (sample==null)
            sample = Gedcom.getName(path.getLast());
            
          MetaProperty meta = grammar.getMeta(path, false);
          if (PropertyXRef.class.isAssignableFrom(meta.getType()))
            sample = "@...@";
          try {
            return meta.create(sample);
          } catch (GedcomException e) {
            return new PropertySimpleReadOnly(path.getLast(), sample);
          }
        }
      };
      renderer.setDebug(true);
      renderer.render(g, example, bounds);
      // done
    }
  } //Preview

  /**
   * Insert a property   */
  private class Insert extends Action2 {
    /** constructor */
    private Insert() {
      super.setText(resources.getString("prop.insert"));
      super.setTip(resources.getString("prop.insert.tip"));
    }
    /** @see genj.util.swing.Action2#execute() */
    public void actionPerformed(ActionEvent event) {
      // create a tree of available TagPaths
      PathTreeWidget tree = new PathTreeWidget();
      TagPath[] paths = grammar.getAllPaths(blueprint.getTag(), Property.class);
      tree.setPaths(paths, new TagPath[0]);
      // Recheck with the user
      int option = DialogHelper.openDialog(resources.getString("prop.insert.tip"),DialogHelper.QUESTION_MESSAGE,tree,Action2.okCancel(),BlueprintEditor.this);        
      // .. OK?
      if (option!=0) return;
      // add those properties
      paths = tree.getSelection();
      for (int p=0;p<paths.length; p++) {
        source.insert(
          "<prop path="+paths[p].toString()+">"+(p==paths.length-1?"":"\n"), 
          source.getCaretPosition()
        );
      }
      // request focus
      source.requestFocusInWindow();
      // done
    }
  } //ActionInsert
  
} //RenderingSchemeEditor

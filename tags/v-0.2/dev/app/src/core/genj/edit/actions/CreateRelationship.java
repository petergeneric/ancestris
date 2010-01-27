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
package genj.edit.actions;

import genj.common.SelectEntityWidget;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.util.WordBuffer;
import genj.util.swing.NestedBlockLayout;
import genj.view.ContextSelectionEvent;
import genj.view.ViewContext;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Base type for all relationships we create - it always uses the same pattern
 * <il>
 *  <li>ask the user for a new or existing entity based on the target type of the relationship and
 *  <li>explain what is going to happen
 *  <li>perform the necessary actions in a concrete implementation
 * </il>
 */
public abstract class CreateRelationship extends AbstractChange {

  /** the referenced entity */
  private Entity existing;

  /** check for forcing id */
  private JCheckBox checkID;
  
  /** text field for entering id */
  private JTextField requestID;
  
  /** the target type of the relationship (where it points to) */
  protected String targetType;
  
  /**
   * Constructor
   */
  public CreateRelationship(String name, Gedcom gedcom, String targetType, ViewManager manager) {
    super(gedcom, Gedcom.getEntityImage(targetType).getOverLayed(imgNew), resources.getString("link", name), manager);
    this.targetType = targetType;
  }

  /**
   * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
   */
  protected String getConfirmMessage() {

    WordBuffer result = new WordBuffer("\n");
    
    // You are about to create a {0} in {1}! / You are about to reference {0} in {1}!
    // This {0} will be {1}.
    result.append( existing==null ?
      resources.getString("confirm.new", new Object[]{ Gedcom.getName(targetType,false), gedcom}) :
      resources.getString("confirm.use", new Object[]{ existing.getId(), gedcom})
    );
    
    // relationship detail
    result.append( resources.getString("confirm.new.related", getDescription()) );

    // A warning already?
    String warning = getWarning(existing);
    if (warning!=null) 
      result.append( "**Note**: " + warning );

    // combine
    return result.toString();
  }
  
  /**
   * Provide a description
   */
  public abstract String getDescription();

  /**
   * Provide a warning for given existing target (default none)
   */
  public String getWarning(Entity target) {
    return null;
  }

  /**
   * Override content components to show to user 
   */
  protected JPanel getDialogContent() {
    
    JPanel result = new JPanel(new NestedBlockLayout("<col><row><select wx=\"1\"/></row><row><text wx=\"1\" wy=\"1\"/></row><row><check/><text/></row></col>"));

    // create selector
    final SelectEntityWidget select = new SelectEntityWidget(gedcom, targetType, resources.getString("select.new"));
 
    // prepare id checkbox and textfield
    requestID = new JTextField(gedcom.getNextAvailableID(targetType), 8);
    requestID.setEditable(false);
    
    checkID = new JCheckBox(resources.getString("assign_id"));
    checkID.getModel().addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        requestID.setEditable(checkID.isSelected());
        if (checkID.isSelected())  requestID.requestFocusInWindow();
      }
    });
    
    // wrap it up
    result.add(select);
    result.add(getConfirmComponent());
    result.add(checkID);
    result.add(requestID);

    // add listening
    select.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // grab current selection (might be null)
        existing = select.getSelection();
        // can the user force an id now?
        if (existing!=null) checkID.setSelected(false);
        checkID.setEnabled(existing==null);
        refresh();
      }
    });
    
    // preselect something (for anything but indi and fam)?
    if (!(targetType.equals(Gedcom.INDI)||targetType.equals(Gedcom.FAM)))
      select.setSelection(gedcom.getEntity(ViewManager.getRegistry(gedcom).get("select."+targetType, (String)null)));
    
    // done
    return result;
  }

  /**
   * @see genj.edit.EditViewFactory.Change#change()
   */
  public void perform(Gedcom gedcom) throws GedcomException {
    // create the entity if necessary
    Entity change;
    if (existing!=null) {
      change = existing;
    } else {
      // check id
      String id = null;
      if (requestID.isEditable()) {
        id = requestID.getText();
        if (gedcom.getEntity(targetType, id)!=null)
          throw new GedcomException(resources.getString("assign_id_error", id));
      }
      // focus always changes to new that we create now
      change = gedcom.createEntity(targetType, id);
      change .addDefaultProperties();
    }
    
    // perform the change
    Property focus = change(change, change!=existing);
    
    // remember selection
    ViewManager.getRegistry(gedcom).put("select."+targetType, change.getId());
    
    // select
    WindowManager.broadcast(new ContextSelectionEvent(new ViewContext(focus), getTarget(), false));
    
    // done
  }
  
  /**
   * Apply the relationship
   * @param target the entity that the resulting relationship has to point to
   * @param targetIsNew whether the target was newly created for this relationship
   * @return the property that should receive focus after this action
   */
  protected abstract Property change(Entity target, boolean targetIsNew) throws GedcomException;

}

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
package genj.edit;

import ancestris.core.actions.SubMenuAction;
import ancestris.core.pluginservice.AncestrisPlugin;
import genj.edit.actions.CreateAssociation;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.gedcom.TagPath;



/**
 * our editing plugin
 */
public class EditPlugin
//implements ActionProvider 
{
  
//  private final static Resources RESOURCES = Resources.get(EditPlugin.class);
//  private final static Logger LOG = Logger.getLogger("ancestris.edit");
//  
//  private String ACC_UNDO = "ctrl Z", ACC_REDO = "ctrl Y";
  
  /**
   * Constructor
   */
  /*package*/ EditPlugin() {
    AncestrisPlugin.register(this);
  }
  
//  @Override
//  public void gedcomOpened(final Gedcom gedcom) {
//
//      return;
//    // check if there's any individuals
//    if (!gedcom.getEntities(Gedcom.INDI).isEmpty())
//      return;
//
//    if (0!=DialogHelper.openDialog(RESOURCES.getString("wizard.first", gedcom.getName()), DialogHelper.QUESTION_MESSAGE, RESOURCES.getString("wizard.empty", gedcom.getName()), Action2.yesNo(), workbench))
//      return;
//
//    try {
//      wizardFirst(workbench, gedcom);
//    } catch (Throwable t) {
//      LOG.log(Level.WARNING, "problem in wizard", t);
//    }
//
//  }
//
//    @Override
//    public void commitRequested(Context context) {
//    }
//
//    @Override
//    public void gedcomClosed(Gedcom gedcom) {
//    }
//  
  /**
   * @see genj.view.ActionProvider#createActions(Entity[], ViewManager)
   */
//  private void createActions(List<? extends Property> properties, Action2.Group group) {
//    
//    // Toggle "Private"
//    if (Enigma.isAvailable())
//      group.add(new TogglePrivate(properties.get(0).getGedcom(), properties));
//    
//    // Delete
//    group.add(new DelProperty(properties));
//    
//    // done
//  }

  /**
   * @see genj.view.ContextSupport#createActions(Property)
   */
  private void createActions(Property property, SubMenuAction group) {
    
//    // Place format for PropertyFile
//    if (property instanceof PropertyPlace)  
//      group.add(new SetPlaceHierarchy((PropertyPlace)property)); 
      
//    // Check what xrefs can be added
//    MetaProperty[] subs = property.getNestedMetaProperties(0);
//    for (int s=0;s<subs.length;s++) {
//      // NOTE REPO SOUR SUBM (BIRT|ADOP)FAMC
//      Class<? extends Property> type = subs[s].getType();
//      if (type==PropertyNote.class||
//          type==PropertyRepository.class||
//          type==PropertySource.class||
//          type==PropertySubmitter.class||
//          type==PropertyFamilyChild.class||
//          type==PropertyMedia.class 
//        ) {
//        // .. make sure @@ forces a non-substitute!
//        group.add(new CreateXReference(property,subs[s].getTag()));
//        // continue
//        continue;
//      }
//    }
    
    // Add Association to events if property is contained in individual
    // or ASSO allows types
    if ( property instanceof PropertyEvent
        && ( (property.getEntity() instanceof Indi)
            || property.getGedcom().getGrammar().getMeta(new TagPath("INDI:ASSO")).allows("TYPE"))  )
      group.addAction(new CreateAssociation(property));
    
//    // Toggle "Private"
//    if (Enigma.isAvailable())
//      group.add(new TogglePrivate(property.getGedcom(), Collections.singletonList(property)));
//    
//    // Delete
//    if (!property.isTransient()) 
//      group.add(new DelProperty(property));
  
    // done
  }
  
  /**
   * Actions for context
   */
  public void createActions(Context context,SubMenuAction result) {

    // take out shortcuts
//    Action2.uninstall(workbench, ACC_REDO);
//    Action2.uninstall(workbench, ACC_UNDO);
    
    // nothing without gedcom
    if (context.getGedcom()==null)
      return;
    
    
            // XXX: recurse for all parent properties. In ancestris we don't do that ATM
            // It could probably be difficult to do with NB logic
            // and not very user friendly
          Property cursor = context.getProperty();
          while (!(cursor instanceof Entity)) {
            SubMenuAction group = new SubMenuAction(
                    Property.LABEL+" '"+TagPath.get(cursor).getName() + '\'', 
                    cursor.getImage(false));
            createActions(cursor, group);
            if (!group.getActions().isEmpty())
              result.addAction(group);
            cursor = cursor.getParent();
          }
    
    // done
  }

}


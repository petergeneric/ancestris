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
package genj.gedcom;

import genj.util.WordBuffer;

/**
 * Gedcom Property : ASSO.
 * Property wrapping the condition of a property having an association 
 * to another entity
 * There are two forms for association tag:
 * <ul><li> the first is the default used by Ancestris and is fully compliant to 
 *   the gedcom standard. In this form, the ASSO tag is allowed only under an INDI ta g
 *   and the RELA contain a tag place to identify the event involved in this association.
 *   this tag path is an Ancestris feature and not use by any other genealogy software
 *   but remains gedcom compliant as there is no specification for RELA tag value.
 * <li> the second is not fully compliant to the standard but seems to be widely used.
 *   In this form, the ASSO tag is put under the related event and points to the INDI
 *   involved in this association.
 * </ul>
 * this class can understand both form
 */
public class PropertyAssociation extends PropertyXRef {
  
  /**
   * need tag-argument constructor for all properties
   */
  /*package*/ PropertyAssociation(String tag) {
    super(tag);
    assertTag("ASSO");
  }

  private boolean tagAssoIsInEvent(){
    if (!(getParent() instanceof Indi))
        return true;

    Property rela = getProperty("RELA");
    if (rela!=null&&rela.getValue().contains("@"))
            return false;
    return true;
}
  /**
   * We're trying to give a bit more information than the
   * default display value (target.getEntity().toString())
   * For example:
   *  Birth Meier, Nils (I008) 25 May 1970 Rendsburg
   * @see genj.gedcom.PropertyXRef#getDisplayValue()
   */
    @Override
  public String getDisplayValue() {
      if (tagAssoIsInEvent()){
          return getRelaDisplayValue(getTargetEntity());
      } else {
        // find target
        PropertyXRef target = getTarget();
        Property parent = null;
        if (target!=null)
            parent = target.getParent();
          return getRelaInEventDisplayValue(parent);
      }
  }
//  rela for this property is indi
//  
    private String getRelaInEventDisplayValue(Property parent){
//    // find target
//    PropertyXRef target = getTarget();
//    if (target==null)
//      return super.getDisplayValue();
//    
//    // check its parent
//    Property parent = target.getParent();
    if (parent==null)
      return super.getDisplayValue();
    
    // collect some info e.g.
    //  Meier, Nils (I008) - Birth - 25 May 1970 - Rendsburg
    WordBuffer result = new WordBuffer(" - ");

    Property relaProp = getProperty("RELA");
    String rela = "";
    if (rela!=null&&relaProp.getDisplayValue().length()>0) 
        rela = " ("+relaProp.getDisplayValue() + ")";

    result.append(parent.getEntity()+rela);

    result.append(Gedcom.getName(parent.getTag()));
    
    Property date = parent.getProperty("DATE");
    if (date!=null)
      result.append(date);
    
    Property place = parent.getProperty("PLAC");
    if (place!=null)
      result.append(place);
    
    
    // done
    return result.toString();
  }
  
  /**
   * @see genj.gedcom.PropertyXRef#getForeignDisplayValue()
   */
    @Override
  protected String getForeignDisplayValue() {
      if (tagAssoIsInEvent()){
          return getRelaInEventDisplayValue(getParent());
      } else {
          return getRelaDisplayValue(getEntity());
      }
  }
  /**
   * when an INDI is associated to a property (which can be an event, an INDI 
   * or a FAM, a RELAtion is establish between the two.
   */
    
    /**
     * Display 
     * indi is rela in event
     * target = indi associated to this event
     */    
    private String getRelaDisplayValue(Entity target){
        // do we know a relationship?
        Property rela = getProperty("RELA");
        if (rela!=null&&rela.getDisplayValue().length()>0) 
        return rela.getDisplayValue() + ": " + target.toString();
        // fallback
        return super.getForeignDisplayValue();
    }
  
  /**
   * Returns a warning string that describes what happens when this
   * property would be deleted
   * @return warning as <code>String</code>, <code>null</code> when no warning
   */
    @Override
  public String getDeleteVeto() {
    // warn if linked
    if (getTargetEntity()==null) 
      return null;
    return resources.getString("prop.asso.veto");
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when property has no parent property,
   * or a double husband/wife situation would be the result
   */
  public void link() throws GedcomException {

     // Try to find entity
    Entity ent = getCandidate();

    // Create Backlink using RELA
    PropertyForeignXRef fxref = new PropertyForeignXRef();
    try {
      PropertyRelationship rela = (PropertyRelationship)getProperty("RELA");
      ent.getProperty(rela.getAnchor()).addProperty(fxref);
    } catch (Throwable t) {
      ent.addProperty(fxref);
    }

    // ... and point
    link(fxref);

    // .. update type
    Property type = getProperty("TYPE");
    if (type==null) type = addProperty(new PropertySimpleValue("TYPE"));
    type.setValue(ent.getTag());

    // Done
  }

  /**
   * The expected referenced type
   */
  public String getTargetType() {
    // only pointing to individuals?
    if (!getMetaProperty().allows("TYPE"))
      return Gedcom.INDI;
    // check type
    Property type = getProperty("TYPE");
    if (type!=null)
      return type.getValue();
    // match one up by id we have
    String prefix = getValue().substring(1,2);
    for (int i = 0; i < Gedcom.ENTITIES.length; i++) {
      if (Gedcom.getEntityPrefix(Gedcom.ENTITIES[i]).startsWith(prefix))
        return Gedcom.ENTITIES[i];
    }
    // grrr, too bad
    return Gedcom.INDI;
  }
  
} //PropertyAssociation

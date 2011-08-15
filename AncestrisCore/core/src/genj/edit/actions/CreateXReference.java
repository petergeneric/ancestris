/**
 * 
 */
package genj.edit.actions;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Knows how to create cross-references to other entities, namely NOTE, OBJE, SUBM, SOUR, ... and
 * even FAM (it can be added as a simple cross-reference to BIRThs)
 */
public class CreateXReference extends CreateRelationship {
  
  private Property source;
  private String sourceTag;
  private PropertyXRef xref;
  
  /** Constructor */
  public CreateXReference(Property source, String sourceTag) {
    super(getName(source, sourceTag),source.getGedcom(), getTargetType(source, sourceTag));
    this.source = source;
    this.sourceTag = sourceTag;
  }
  
  /** figure out target type for given source+tag */
  private static String getTargetType(Property source, String sourceTag) {
    // ask a sample
    try {
      PropertyXRef sample = (PropertyXRef)source.getMetaProperty().getNested(sourceTag, false).create("@@");
      return sample.getTargetType();
    } catch (GedcomException e) {
      Logger.getLogger("genj.edit.actions").log(Level.SEVERE, "couldn't determine target type", e);
      throw new RuntimeException("Couldn't determine target type for source tag "+sourceTag);
    }
  }
  
  /** figoure out our name - done once */
  private static String getName(Property source, String sourceTag) {
    String targetType = getTargetType(source, sourceTag);
    if (targetType.equals(sourceTag))
      return Gedcom.getName(targetType);
    return Gedcom.getName(targetType) + " (" + Gedcom.getName(sourceTag) + ")";    
  }
  
  /** more about what we do */
  public String getDescription() {
    return resources.getString("create.xref.desc", Gedcom.getName(targetType), source.getEntity().toString());
  }

  /** do the change */
  protected Property change(Entity target, boolean targetIsNew) throws GedcomException {
    
    // create a the source link
    xref = (PropertyXRef)source.addProperty(sourceTag, '@'+target.getId()+'@');
    
    try {
      xref.link();
      xref.addDefaultProperties();
    } catch (GedcomException e) {
      source.delProperty(xref);
      xref = null;
      throw e;
    }
    
    //  focus stays with owner
    return targetIsNew ? xref.getTarget() : xref;
    
  }

  /**
   * The created reference
   */
  public PropertyXRef getReference() {
    return xref;
  }
  
}

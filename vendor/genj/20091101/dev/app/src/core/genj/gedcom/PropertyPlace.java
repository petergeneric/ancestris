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

import genj.util.DirectAccessTokenizer;
import genj.util.ReferenceSet;
import genj.util.swing.ImageIcon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * PLAC a choice value with brains for understanding sub-property FORM
 */
public class PropertyPlace extends PropertyChoiceValue {

  private final static boolean USE_SPACES = Options.getInstance().isUseSpacedPlaces;

  public final static ImageIcon
    IMAGE = Grammar.V55.getMeta(new TagPath("INDI:BIRT:PLAC")).getImage();

  public final static String
    JURISDICTION_SEPARATOR = ",";

  private final static String
    JURISDICTION_RESOURCE_PREFIX = "prop.plac.jurisdiction.";

  public final static String
    TAG = "PLAC",
    FORM = "FORM";

  /**
   * Overridden - special trim
   */
  protected String trim(String value) {

    /*
     20051212 at some point we switched to trimming values on places
     here, making sure that the separator only is between jurisdictions.
     Peter asked me to add spaces as well for readability:
       2 PLAC Hamburg, Schleswig Holstein, Deutschland
     instead of
       2 PLAC Hamburg,Schleswig Holstein,Deutschland

     But Francois reminded me that we didn't want to have spaces in
     the Gedcom file - the spec doesn't explicitly disallow it but especially
     in Francois' way of keeping place information
       2 PLAC ,Allanche,,Cantal,Auvergne,
     adding spaces doesn't look good
       2 PLAC , Allanche, , Cantal, Auvergne,

     We played with the idea of using space-comma in getDisplayValue()
     and comma-only in getValue()/trim() - problem is that it takes mem
     to cache or runtime performance to calculate that. It's also problematic
     that the display value would be different from the choices remembered
     (one with space the other without)

     So finally we decided to put in a global option that lets the user
     make the choice - internally getValue()-wize we handle this uniformly then
    */

    // trim each jurisdiction separately
    StringBuffer buf = new StringBuffer(value.length());
    DirectAccessTokenizer jurisdictions = new DirectAccessTokenizer(value, JURISDICTION_SEPARATOR);
    for (int i=0; ; i++) {
      String jurisdiction = jurisdictions.get(i, true);
      if (jurisdiction==null) break;
      if (i>0) {
        buf.append(JURISDICTION_SEPARATOR);
        if (USE_SPACES) buf.append(' ');
      }
      buf.append(jurisdiction);
    }
    return buf.toString().intern();
  }

  /**
   * Remember a jurisdiction's value
   */
  protected boolean remember( String theOld, String theNew) {

    // let super do its stuff
    if (!super.remember(theOld, theNew))
      return false;
    Gedcom gedcom = getGedcom();

    // forget old jurisdictions
    DirectAccessTokenizer jurisdictions = new DirectAccessTokenizer(theOld, JURISDICTION_SEPARATOR);
    for (int i=0;;i++) {
      String jurisdiction = jurisdictions.get(i, true);
      if (jurisdiction==null) break;
      // forget PLAC.n
      if (jurisdiction.length()>0)
        gedcom.getReferenceSet(TAG+"."+i).remove(jurisdiction, this);
      // next
    }

    // remember new jurisdictions
    jurisdictions = new DirectAccessTokenizer(theNew, JURISDICTION_SEPARATOR);
    for (int i=0;;i++) {
      String jurisdiction = jurisdictions.get(i, true);
      if (jurisdiction==null) break;
      // remember PLAC.n
      if (jurisdiction.length()>0)
        gedcom.getReferenceSet(TAG+"."+i).add(jurisdiction.intern(), this);
      // next
    }

    // done
    return true;
  }

  /**
   * Accessor - format
   */
  public String[] getFormat() {
    return toJurisdictions(getFormatAsString());
  }

  private String[] toJurisdictions(String value) {
    ArrayList result = new ArrayList(10);
    String lastToken = JURISDICTION_SEPARATOR;
    for (StringTokenizer tokens = new StringTokenizer( value, ",", true); tokens.hasMoreTokens(); ) {
      String token = tokens.nextToken();
      if (!JURISDICTION_SEPARATOR.equals(token))
        result.add(token);
      else if (JURISDICTION_SEPARATOR.equals(lastToken))
        result.add("");
      lastToken = token;
    }
    if (JURISDICTION_SEPARATOR.equals(lastToken))
      result.add("");
    return (String[])result.toArray(new String[result.size()]);
  }  
  
  /**
   * Accessor - the format of this place's value (non localized)
   */
  public String getFormatAsString() {
    // look it up
    String result = "";
    Property pformat = getProperty(FORM);
    if (pformat!=null)
      result = pformat.getValue();
    else {
      Gedcom ged = getGedcom();
      if (ged!=null)
        result = ged.getPlaceFormat();
    }
    // done
    return result;
  }

  /**
   * Accessor - the hierarchy of this place's value (non localized)
   */
  public void setFormatAsString(boolean global, String format) {
    if (!global)
      throw new IllegalArgumentException("non-global n/a");
    // propagate
    getGedcom().setPlaceFormat(format);
    // mark changed
    propagatePropertyChanged(this, getValue());
  }

  /**
   * Accessor - all places with the same jurisdiction for given hierarchy level
   */
  public PropertyPlace[] getSameChoices(int hierarchyLevel) {
    String jurisdiction = getJurisdiction(hierarchyLevel);
    if (jurisdiction==null)
      return null;
    Collection places = getGedcom().getReferenceSet(TAG+"."+hierarchyLevel).getReferences(jurisdiction);
    return (PropertyPlace[])places.toArray(new PropertyPlace[places.size()]);
  }

  /**
   * Accessor - all jurisdictions of given level in same gedcom file
   */
  public String[] getAllJurisdictions(int hierarchyLevel, boolean sort) {
    Gedcom gedcom = getGedcom();
    if (gedcom==null)
      return new String[0];
    return getAllJurisdictions(gedcom, hierarchyLevel, sort);
  }

  /**
   * Accessor - all jurisdictions of given level in gedcom
   * @param hierarchyLevel either a zero-based level or -1 for whole place values
   */
  public static String[] getAllJurisdictions(Gedcom gedcom, int hierarchyLevel, boolean sort) {
    ReferenceSet refset = gedcom.getReferenceSet( hierarchyLevel<0 ? TAG : TAG+"."+hierarchyLevel);
    Collection jurisdictions = refset.getKeys(sort ? gedcom.getCollator() : null);
    return (String[])jurisdictions.toArray(new String[jurisdictions.size()]);
  }

  /**
   * Accessor - first non-empty jurisdiction
   * @return jurisdiction of zero+ length
   */
  public String getFirstAvailableJurisdiction() {
    DirectAccessTokenizer jurisdictions = new DirectAccessTokenizer(getValue(), JURISDICTION_SEPARATOR);
    String result = "";
    for (int i=0; result.length()==0 ;i++) {
      result = jurisdictions.get(i, true);
      if (result==null)
        return "";
    }
    return result;
  }

  /**
   * Accessor - jurisdiction of given level
   * @return jurisdiction of zero+ length or null if n/a
   */
  public String getJurisdiction(int hierarchyLevel) {
    return new DirectAccessTokenizer(getValue(), JURISDICTION_SEPARATOR).get(hierarchyLevel, true);
  }
  
  /**
   * Accessor - jurisdictions
   */
  public String[] getJurisdictions() {
    return toJurisdictions(getValue());
  }

  /**
   * Accessor - jurisdictions that is the city
   */
  public String getCity() {
    int cityIndex = getCityIndex();
    if (cityIndex<0)
      return getFirstAvailableJurisdiction();
    String city = new DirectAccessTokenizer(getValue(), JURISDICTION_SEPARATOR).get(cityIndex, true);
    return city!=null ? city : "";
  }

  /**
   * Accessor - all jurisdictions starting with city
   */
  public String getValueStartingWithCity() {
    // grab result
    String result = getValue();
    // check city index - we assume it start with the first if n/a
    int cityIndex = getCityIndex();
    if (cityIndex<=0)
      return result;
    // grab sub
    return new DirectAccessTokenizer(result, JURISDICTION_SEPARATOR).getSubstring(cityIndex);
  }

  /**
   * Derive index of city value in the list of jurisdictions in this place
   * @return zero based index or -1 if not determined
   */
  private int getCityIndex() {

    // try to get a place format
    if (getFormatAsString().length()==0)
      return -1;

    // look for a city key in the hierarchy
    Set cityKeys = Options.getInstance().placeHierarchyCityKeys;
    String[] format = getFormat();
    for (int i=0; i<format.length;i++) {
      if (cityKeys.contains(format[i].toLowerCase()))
        return i;
    }

    // don't know
    return -1;
  }

} //PropertyPlace

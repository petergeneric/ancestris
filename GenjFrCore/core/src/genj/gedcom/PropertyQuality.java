/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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

/**
 * QUAY
 */
public class PropertyQuality extends Property {
  
  private String value = "";

  public final static String[] QUALITIES = Gedcom.resources.getString("QUAY.vals",false).split(",");

  public PropertyQuality(String tag) {
    super(tag);
  }
  
  public PropertyQuality() {
    this("QUAY");
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }
  
  public void setQuality(int quality) {
    if (quality<-1||quality>3)
      throw new IllegalArgumentException("invalid quality value "+quality);
    String old = getValue();
    value = quality<0 ? "" : String.valueOf(quality);
    super.propagatePropertyChanged(this, old);
  }

  public int getQuality() {
    try {
      int result = Integer.parseInt(value);
      if (result>=0&&result<=3)
        return result;
    } catch (NumberFormatException e) {
    }
    return -1;
  }
  
}

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

package genj.fo;

import java.io.OutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

/** 
 * Format for XLSFO - a simple identity transformation 
 */
public class XSLFOFormat extends Format {
  
  /**
   * Constructor
   */
  public XSLFOFormat() {
    super("XSL-FO", "xml", true);
  }
  
  /**
   * Formatting logic 
   */
  protected void formatImpl(Document doc, OutputStream out) throws Throwable {
    
    // grab xsl transformer (identity)
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    
    // transformation to xslfo
    transformer.transform(doc.getDOMSource(), new StreamResult(out));
    
    // done
  }
  
}
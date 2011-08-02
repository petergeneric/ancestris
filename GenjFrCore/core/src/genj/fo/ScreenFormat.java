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
import javax.xml.transform.sax.SAXResult;

/**
 * Format  for rendering on screen - using FOP
 */
public class ScreenFormat extends Format {

  /**
   * Constructor
   */
  public ScreenFormat() {
    super("Screen", null, false);
  }

  /**
   * our format logic
   */
  protected void formatImpl(Document doc, OutputStream out) throws Throwable {

    // create FOP tree builder that handles the document content and generates out
    org.xml.sax.ContentHandler handler = new org.apache.fop.fo.FOTreeBuilder("application/X-fop-awt-preview", new org.apache.fop.apps.FOUserAgent(), out);

    // grab xsl transformer
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    
    // do the transformation
    transformer.transform(doc.getDOMSource(), new SAXResult(handler));

    // done
  }
  
}

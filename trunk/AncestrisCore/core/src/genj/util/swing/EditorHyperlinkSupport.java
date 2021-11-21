/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2009 Nils Meier <nils@meiers.net>
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
package genj.util.swing;

import genj.io.FileAssociation;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.openide.util.Exceptions;

/**
 * A Hyperlink Follow Action
 */
public class EditorHyperlinkSupport implements HyperlinkListener {

  private final static Logger LOG = Logger.getLogger("ancestris.log");
  
  private JEditorPane editor;
  
  /** constructor */
  public EditorHyperlinkSupport(JEditorPane editor) {
    this.editor = editor;
  }

  /** callback - link clicked */
  public void hyperlinkUpdate(HyperlinkEvent e) {
    // need activate
    if (e.getEventType()!=HyperlinkEvent.EventType.ACTIVATED)
      return;
    // internal?
    if (e.getDescription().startsWith("#")) 
        editor.scrollToReference(e.getDescription().substring(1));
    else {
      try {
        handleHyperlink(e.getDescription());
      } catch (Throwable t) {
        LOG.log(Level.INFO, "can't open browser for "+e.getDescription());
      }
    }          
    // done
  }
  
  protected void handleHyperlink(String link) throws IOException, URISyntaxException {
    try {
        FileAssociation.getDefault().execute(new URL(link.replaceAll(" ", "%20")));
    } catch (MalformedURLException ex) {
        Exceptions.printStackTrace(ex);
    }
  }

}
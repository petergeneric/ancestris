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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * A Hyperlink Follow Action
 */
public class EditorHyperlinkSupport implements HyperlinkListener {

  private final static Logger LOG = Logger.getLogger("genj.log");
  
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
      Desktop.getDesktop().browse(new URI(link));
    } catch (Throwable t) {
      LOG.log(Level.INFO, "can't browse link "+link, t);
    }
  }

}
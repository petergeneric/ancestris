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
package genj.app;

import genj.util.EnvironmentChecker;
import genj.util.Resources;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;

import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * A bridge to javax Help System
 */
class HelpWidget extends JPanel {
  
  private final static Resources RESOURCES = Resources.get(HelpWidget.class);

  /**
   * Constructor
   */
  public HelpWidget() {
    
    // simple layout
    super(new BorderLayout());
    
    // create center component
    JComponent pCenter = getContent();
    if (pCenter==null) {
      pCenter = new JLabel(RESOURCES.getString("cc.help.help_file_missing", Locale.getDefault().getLanguage().toLowerCase()), SwingConstants.CENTER);
      pCenter.setBorder(new EmptyBorder(16,16,16,16));
    }
    
    // layout
    add(pCenter, BorderLayout.CENTER);    
    
    // done
  }
  
  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return new Dimension(480,480);
  }
  
  /**
   * Initialization of help
   */
  private JComponent getContent() {
    
    // Open the Help Set        
    String file = calcHelpBase() + "/helpset.xml";
    App.LOG.info("Trying to use help in " + file );
    // safety check
    if (!new File(file).exists()) {
      App.LOG.log(Level.WARNING, "No help found in "+file);
      return null;
    }

    // Load and init through bridge
    try {
      // without jumping through these hoops I'm getting java.lang.NoClassDefFoundError: javax/help/JHelp
      // if HelpWidget.class is loaded :(
      HelpSet set = (HelpSet)HelpSet.class.getConstructor(new Class[]{ClassLoader.class, URL.class})
        .newInstance(new Object[]{null,new URL("file","", file)});
      return (JComponent)JHelp.class.getConstructor(new Class[]{set.getClass()}).newInstance(new Object[]{set});
    } catch (Throwable t) {
      App.LOG.log(Level.WARNING, "Problem reading help", t);
    }
    
    // default - none
    return null;
  }

  /**
   * Calculate help-directory location 'help'
   */
  private String calcHelpBase() {
    
    // First we look in "genj.help.dir"
    String dir = EnvironmentChecker.getProperty(
      this,
      new String[]{ "genj.help.dir", "user.dir/help"},
      ".",
      "read help"
    );
    
    // Then we check for local language
    String local = dir+"/"+Locale.getDefault().getLanguage();
    if (new File(local).exists()) {
      return local;
    }
    
    // ... otherwise fallback to 'en' language
    return dir+"/en";
    
  }

} //HelpWidget

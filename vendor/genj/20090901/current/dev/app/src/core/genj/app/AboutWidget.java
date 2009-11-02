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
 *
 *
 * AboutDialog class
 * This class creates the content of AboutDialog application
 * $Header: /cvsroot/genj/dev/src/core/genj/app/AboutDialog.java,v 1.11 2002/08/15 07:17:23 island1 Exp $
 * @author Francois Massonneau <frmas@free.fr>
 * @version 1.0
 *
 */
 
package genj.app;

import genj.Version;
import genj.io.FileAssociation;
import genj.util.EnvironmentChecker;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.view.ViewManager;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * AboutDialog 
 */
public class AboutWidget extends JPanel{
  
  private final static int DEFAULT_ROWS = 16, DEFAULT_COLS = 40;
  
  /** the view manager */
  private ViewManager viewManager;
  
  /** the resources we're using */
  private Resources resources = Resources.get(AboutWidget.class);
  
  /**
   * Constructor
   */
  public AboutWidget(ViewManager setManager) {

    // remember    
    viewManager = setManager;
    
    // create a north panel
    JLabel pNorth = new JLabel(resources.getString("cc.about.dialog.northpanel.label"), null, JLabel.CENTER);
    
    // create a center panel
    JTabbedPane pCenter = new JTabbedPane(SwingConstants.TOP);
    pCenter.addTab(resources.getString("cc.about.dialog.tab1.title"), null, new WelcomePanel());
    pCenter.addTab(resources.getString("cc.about.dialog.tab2.title"), null, new AuthorsPanel());
    pCenter.addTab(resources.getString("cc.about.dialog.tab3.title"), null, new CopyrightPanel());

    // create the main panel    
    setLayout(new BorderLayout());
    add(pNorth , BorderLayout.NORTH );
    add(pCenter, BorderLayout.CENTER);
    
    // done    
  }
  
  /**
   * Helper to read text from a file
   */
  protected void readTextFile(JTextArea ta, String file, String fallback) {
    try {
      FileInputStream fin = new FileInputStream(file);
      Reader in = new InputStreamReader(fin);
      ta.read(in,null);
      fin.close();
    }
    catch (Throwable t) {
      ta.setText(fallback);
    }
  }

  /**
   * Panel - Authors
   */  
  private class AuthorsPanel extends JScrollPane {

    /** 
     * Constructor
     */  
    protected AuthorsPanel() {

      // create contained text area
      JTextArea text = new JTextArea(DEFAULT_ROWS,DEFAULT_COLS);
      text.setLineWrap(false);
      text.setWrapStyleWord(true);
      text.setEditable(false);

      String dir = EnvironmentChecker.getProperty(
        this,
        new String[]{ "user.dir" },
        ".",
        "get authors.txt"
      );
      
      String path = dir + File.separatorChar + "doc" + File.separatorChar + "authors.txt";
      
      readTextFile(text, path, resources.getString("cc.about.file_missing.text") + path);

      // setup looks
      setViewportView(text);      
      
      setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      // done
      
    }      
  } // AuthorsPanel

  /**
   * Panel - Copyright
   */  
  private class CopyrightPanel extends JPanel {

    /** 
     * Constructor
     */  
    protected CopyrightPanel()  {
      
      super(new BorderLayout());
      
      add(getNorth(), BorderLayout.NORTH);
      add(getCenter(), BorderLayout.CENTER);
    
    }
    
    /**
     * Create the north 
     */
    private JComponent getNorth() {
      
      JTextArea text = new JTextArea(resources.getString("cc.about.tab3.text1"),3,DEFAULT_COLS);
      text.setLineWrap(true);
      text.setWrapStyleWord(true);
      text.setEditable(false);
      
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder(resources.getString("cc.about.tab3.text1.title")),
        new EmptyBorder(3, 3, 3, 3)
      ));
      panel.add(text, BorderLayout.CENTER);
      
      return panel;
    }

    /**
     * Create the center
     */
    private JComponent getCenter() {
          
      // the text    
      JTextArea text = new JTextArea(DEFAULT_ROWS,DEFAULT_COLS);
      String dir = EnvironmentChecker.getProperty(
        this,
        new String[]{ "user.dir" },
        ".",
        "read gpl.txt"
      );
      
      String path = dir + File.separatorChar + "doc" + File.separatorChar + "gpl.txt";
      readTextFile(text, path, resources.getString("cc.about.file_missing.text") + path);
      text.setLineWrap(false);
      text.setEditable(false);
      text.setBorder(new EmptyBorder(3, 3, 3, 3));
      
      // a scroller
      JScrollPane scroll = new JScrollPane(text);
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scroll.setBorder(BorderFactory.createTitledBorder(resources.getString("cc.about.tab3.text2.title")));
      
      // done
      return scroll;
    }
  
  } // CopyrightPanel

  /**
   * Panel - Welcome
   */  
  private class WelcomePanel extends JPanel  {
  
    /**
     * Constructor
     */
    protected WelcomePanel() {
      
      super(new BorderLayout());
      
      String msg = resources.getString("cc.about.tab1.text1", Version.getInstance().getVersionString());
        
      // the text
      JTextArea text = new JTextArea(msg,DEFAULT_ROWS,DEFAULT_COLS);
      text.setBorder(new EmptyBorder(3, 3, 3, 3));    
      text.setLineWrap(true);
      text.setWrapStyleWord(true);
      text.setEditable(false);
    
      // looks
      add(text, BorderLayout.CENTER);
      add(new JButton(new Log()), BorderLayout.SOUTH);
      
    }
    
  }
  
  private class Log extends Action2 {
    Log() {
      setText("Log");
    }
    protected void execute() {
      FileAssociation asso = FileAssociation.get(App.LOGFILE, "GenJ Log", AboutWidget.this);
      if (asso!=null)
        asso.execute(App.LOGFILE);
    }
  }
  
  
} //AboutWidget

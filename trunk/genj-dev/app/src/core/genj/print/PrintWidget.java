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
package genj.print;

import genj.option.Option;
import genj.option.OptionListener;
import genj.option.OptionsWidget;
import genj.option.PropertyOption;
import genj.renderer.Options;
import genj.util.Dimension2d;
import genj.util.swing.Action2;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.UnitGraphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.print.PrintService;
import javax.print.ServiceUI;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * A PrintDialog */
public class PrintWidget extends JTabbedPane implements OptionListener {
  
  /** task */
  private PrintTask task;
  
  /** services to choose from */
  private ChoiceWidget services;

  /** a preview */
  private Preview preview;

  /**
   * Constructor   */
  public PrintWidget(PrintTask task) {
    
    // remember 
    this.task = task;

    add(PrintTask.RESOURCES.getString("printer" ), createFirstPage());
    add(PrintTask.RESOURCES.getString("settings"), createSecondPage());
    
    // done    
  }
  
  private JPanel createFirstPage() {
    
    String LAYOUT_TEMPLATE = 
      "<col>"+
      "<row><lprinter/><printers wx=\"1\"/><settings/></row>"+
      "<row><lpreview/></row>"+
      "<row><preview wx=\"1\" wy=\"1\"/></row>"+
      "</col>";
    
    // setup layout
    JPanel page = new JPanel(new NestedBlockLayout(LAYOUT_TEMPLATE));
    
    // 'printer'
    page.add("lprinter", new JLabel(PrintTask.RESOURCES.getString("printer")));
    
    // choose service
    services = new ChoiceWidget(task.getServices(), task.getService());
    services.setEditable(false);
    services.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        // only selection is interesting
        if (e.getStateChange()!=ItemEvent.SELECTED) 
          // change service
          task.setService((PrintService)services.getSelectedItem());
      }
    });
    page.add("printers", services);

    // settings
    page.add("settings", new JButton(new Settings()));
    
    // 'preview'
    page.add("lpreview", new JLabel(PrintTask.RESOURCES.getString("preview")));
    
    // the actual preview
    preview = new Preview();
    
    page.add("preview", new JScrollPane(preview));
    
    // done
    return page;    
  }
  
  private JComponent createSecondPage() {
    List options = PropertyOption.introspect(task.getRenderer());
    for (int i = 0; i < options.size(); i++) 
      ((Option)options.get(i)).addOptionListener(this);
    return new OptionsWidget(PrintTask.RESOURCES.getString("printer"), options);
  }
  
  /**
   * option change callback
   */
  public void optionChanged(Option option) {
    task.invalidate();
  }
  
  /**
   * The preview
   */
  private class Preview extends JComponent implements Scrollable {
    
    private float 
      padd = 0.1F, // inch
      zoom = 0.25F; // 25%

    private Point dpiScreen = Options.getInstance().getDPI();
    
    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public Dimension getPreferredSize() {
      // calculate
      Dimension pages = task.getPages(); 
      Rectangle2D page = task.getPage(pages.width-1,pages.height-1, padd);
      return new Dimension(
        (int)((page.getMaxX())*dpiScreen.x*zoom),
        (int)((page.getMaxY())*dpiScreen.y*zoom)
      );
    }

    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) {
      
      // fill background
      g.setColor(Color.gray);
      g.fillRect(0,0,getWidth(),getHeight());
      g.setColor(Color.white);
      
      // render pages in app's dpi space
      Printer renderer = task.getRenderer();
      Dimension pages = task.getPages(); 
      UnitGraphics ug = new UnitGraphics(g, dpiScreen.x*zoom, dpiScreen.y*zoom);
      Rectangle2D clip = ug.getClip();
      for (int y=0;y<pages.height;y++) {
        for (int x=0;x<pages.width;x++) {
          // calculate layout
          Rectangle2D 
            page = task.getPage(x,y, padd), 
            imageable = task.getPrintable(page);
          // visible?
          if (!clip.intersects(page))
            continue;
          // draw page
          ug.setColor(Color.white);
          ug.draw(page, 0, 0, true);
          // draw number
          ug.setColor(Color.gray);
          ug.draw(String.valueOf(x+y*pages.width+1),page.getCenterX(),page.getCenterY(),0.5D,0.5D);
          ug.pushTransformation();
          ug.pushClip(imageable);
          ug.translate(imageable.getMinX(), imageable.getMinY());
          ug.getGraphics().scale(zoom,zoom);
          renderer.renderPage(ug.getGraphics(), new Point(x,y), new Dimension2d(imageable), dpiScreen, true);
          ug.popTransformation();
          ug.popClip();
          // next   
        }
      }
      // done
    }

    public boolean getScrollableTracksViewportHeight() {
      return false;
    }

    public boolean getScrollableTracksViewportWidth() {
      return false;
    }

    public Dimension getPreferredScrollableViewportSize() {
      return new Dimension(0,0);
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
      return orientation==SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
      return 1;
    }

  } //Preview

  /**
   * Action : printer settings
   */
  private class Settings extends Action2 {

    /** constructor */
    private Settings() {
      super.setText(PrintTask.RESOURCES.getString("settings"));
      super.setTarget(PrintWidget.this);
    }

    /** run */
    protected void execute() {
      // show settings
      Point pos = task.getOwner().getLocationOnScreen();
      PrintService choice = ServiceUI.printDialog(null, pos.x, pos.y, task.getServices(), task.getService(), null, task.getAttributes());
      if (choice!=null) {
        services.setSelectedItem(choice);
        task.invalidate();
      }

      // update preview
      preview.revalidate();
      preview.repaint();
      
    }
    
  } //Settings
  
} //PrintWidget

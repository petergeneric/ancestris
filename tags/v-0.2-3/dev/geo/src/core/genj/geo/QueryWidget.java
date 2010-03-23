/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2005 Nils Meier <nils@meiers.net>
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
package genj.geo;

import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.TextFieldWidget;
import genj.window.WindowManager;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

/**
 * A widget for dynamically searching for database information about locations
 */
public class QueryWidget extends JPanel {
  
  private final static Resources RESOURCES = Resources.get(QueryWidget.class);

  private static final String
    TXT_LOCATION = RESOURCES.getString("location"),
    TXT_LATLON = RESOURCES.getString("location.latlon"),
    TXT_QUERYING = RESOURCES.getString("query.querying");
  
  private final static NestedBlockLayout LAYOUT = new NestedBlockLayout(
      "<col>" +
      "<row><label/></row>" +
      "<row><label/><city wx=\"1\"/></row>" +
      "<row><label/><lat wx=\"1\"/><lon wx=\"1\"/></row>" +
      "<row><label/></row>" +
      "<row><hits wx=\"1\" wy=\"1\"/></row>" +
      "</col>"
      );
  
  /** our match model */
  private Model model;
  
  /** view */
  private GeoView view;
  
  /** components */
  private TextFieldWidget city, lat, lon;
  private JTable hits;
  private JLabel status;
  
  /** isChanging flag for our own changes that trigger events to ignore */
  private boolean isChanging = false;
  
  /**
   * Constructor
   */
  public QueryWidget(GeoLocation setLocation, GeoView setView) {
    super(LAYOUT.copy());
    
    // init state
    view = setView;
    model = new Model();
    
    // prepare our components
    city = new TextFieldWidget(setLocation.getCity());
    lat = new TextFieldWidget(setLocation.isValid() ? ""+setLocation.getCoordinate().y : "");
    lon = new TextFieldWidget(setLocation.isValid() ? ""+setLocation.getCoordinate().x : "");
    
    city.setToolTipText(RESOURCES.getString("query.city.tip"));
    lat.setToolTipText(RESOURCES.getString("query.lat.tip"));
    lon.setToolTipText(RESOURCES.getString("query.lon.tip"));
    
    hits = new JTable(model);
    hits.setPreferredScrollableViewportSize(new Dimension(64,64));
    
    status = new JLabel();
    
    add(new JLabel(RESOURCES.getString("query.instruction"))); 
    add(new JLabel(RESOURCES.getString("query.city"))); add(city);
    add(new JLabel(RESOURCES.getString("query.latlon"))); add(lat); add(lon);
    add(status);
    add(new JScrollPane(hits));
    
    // listen to changes
    final Timer timer = new Timer(500, new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        String sCity = city.getText().trim();
        int len =  sCity.length();
        if (sCity.endsWith("*")) len--;
        if (len<3) return;
        model.setLocation(new GeoLocation(sCity, null, null));
      }
    });
    timer.setRepeats(false);
    timer.start();
    
    city.addChangeListener( new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        // restart query time
        if (!isChanging) timer.restart();
      }
    });

    ChangeListener cl = new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (!isChanging) 
          view.setSelection(getGeoLocation());
      }
    };
    lat.addChangeListener(cl);
    lon.addChangeListener(cl);
    
    hits.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        // anything selected?
        int row = hits.getSelectedRow();
        if (row<0)
          return;
        GeoLocation loc = model.getLocation(row);
        // show it on the map
        view.setSelection(loc);
        // update the values we're showing
        isChanging = true;
        city.setText(loc.getCity());
        lat.setText(""+loc.getCoordinate().y);
        lon.setText(""+loc.getCoordinate().x);
        isChanging = false;
        // done for now
      }
    });
    
    // done
  }
  
  /**
   * Lifecycle callback
   */
  public void addNotify() {
    // continue
    super.addNotify();
    // start async querying
    model.start();
  }
  
  /**
   * Lifecycle callback
   */
  public void removeNotify() {
    model.stop();
    // clear current view selection
    view.setSelection(Collections.EMPTY_LIST);
    // continue
    super.removeNotify();
  }
  
  /**
   * Selected Location
   */
  public GeoLocation getGeoLocation() {
    try {
      GeoLocation loc = new GeoLocation(city.getText(), null, null);
      loc.setCoordinate(Double.parseDouble(lat.getText()), Double.parseDouble(lon.getText()));
      return loc;
    } catch (Throwable t) {
      return null;
    }
  }
  
  /**
   * Our asynchronous model
   */
  private class Model extends AbstractTableModel implements Runnable {
    
    private Thread thread = null;
    private boolean running = false;
    private GeoLocation query = null;
    private List locations = new ArrayList();
    
    /** table callback - column name */
    public String getColumnName(int col) {
      switch (col) {
      default: case 0: return TXT_LOCATION;
      case 1: return TXT_LATLON;
    }
    }
    
    /** table callback - columns */
    public int getColumnCount() {
      return 2;
    }
    
    /** table callback - rows */
    public int getRowCount() {
      return locations.size();
    }
    
    /** table callback - cell  */
    public Object getValueAt(int row, int col) {
      GeoLocation loc = (GeoLocation)locations.get(row);
      switch (col) {
        default: case 0: return loc.toString();
        case 1: return loc.getCoordinateAsString();
      }
    }
    
    /** returns location by row */
    public GeoLocation getLocation(int row) {
      return (GeoLocation)locations.get(row);
    }
    
    /** set current location to query */
    public void setLocation(GeoLocation set) {
      synchronized (this) {
        query = set;
        notify();
      }
    }

    /** our async thread */
    public void run() {
      // loop while running
      while (running) {
        // wait for something to do
        GeoLocation todo;
        synchronized (this) { 
          try { this.wait(250); } catch (InterruptedException ie) {} 
          todo = query;
          query = null;
        }
        // a task?
        if (running&&todo!=null) {
          synchronized (this) {
            locations = Collections.EMPTY_LIST;
            fireTableDataChanged();
            status.setText(TXT_QUERYING);
          }
          try {
            List found  = GeoService.getInstance().query(todo);
            synchronized (this) {
              locations = found;
              fireTableDataChanged();
              status.setText(RESOURCES.getString("query.matches", String.valueOf(found.size())));
            }
          } catch (final GeoServiceException e) {
            GeoView.LOG.log(Level.WARNING, "exception while querying", e);
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                if (QueryWidget.this.isVisible())
                  WindowManager.getInstance(QueryWidget.this).openDialog(null, TXT_QUERYING, WindowManager.INFORMATION_MESSAGE, e.getMessage(), Action2.okOnly(), QueryWidget.this);
              }
            });
          }
        }
      }
      // exit
    }
    
    /** start our thread */
    private synchronized void start() {
      stop();
      running = true;
      thread = new Thread(this);
      thread.start();
    }
    
    /** stop our thread */
    private void stop() {
      running = false;
      synchronized (this) {
        notify();
        if (thread!=null) thread.interrupt();
      }
      //while (thread!=null) try {  thread.join(); thread = null; } catch (InterruptedException ie) {} 
      
    }
    
  } //Query
  
}

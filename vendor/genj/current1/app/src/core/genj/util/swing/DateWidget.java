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
package genj.util.swing;

import genj.gedcom.GedcomException;
import genj.gedcom.MetaProperty;
import genj.gedcom.time.Calendar;
import genj.gedcom.time.PointInTime;
import genj.util.ChangeSupport;
import genj.util.WordBuffer;
import genj.window.WindowManager;

import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

/**
 * Generic component for editing dates
 * @author Tomas Dahlqvist fix for US, European and ISO handling of Date
 */
public class DateWidget extends JPanel {
  
  private final static 
   NestedBlockLayout LAYOUT = new NestedBlockLayout("<row><x/><x/><x/><x/></row>");
  
  /** components */
  private PopupWidget widgetCalendar; 
  private TextFieldWidget widgetDay,widgetYear;
  private ChoiceWidget widgetMonth;
  
  /** current calendar */
  private Calendar calendar; 
  
  /** change support */
  private ChangeSupport changeSupport = new ChangeSupport(this) {
    protected void fireChangeEvent(Object source) {
      // update our status
      updateStatus();
      // continue
      super.fireChangeEvent(source);
    }
  };
    
  /**
   * Constructor
   */
  public DateWidget() {
    this(new PointInTime());
  }
  
  /**
   * Constructor
   */
  public DateWidget(PointInTime pit) {

    calendar = pit.getCalendar();
        
    // create calendar switches
    ArrayList switches = new ArrayList(PointInTime.CALENDARS.length+1);
    for (int s=0;s<PointInTime.CALENDARS.length;s++)
      switches.add(new SwitchCalendar(PointInTime.CALENDARS[s]));
    
    // initialize Sub-components
    widgetCalendar = new PopupWidget(); 
    widgetCalendar.setActions(switches);
    
    widgetYear  = new TextFieldWidget("",5+1);
    widgetYear.setSelectAllOnFocus(true);
    widgetYear.addChangeListener(changeSupport);
    
    widgetMonth = new ChoiceWidget(new Object[0], null);
    widgetMonth.setIgnoreCase(true);
    widgetMonth.setSelectAllOnFocus(true);
    widgetMonth.addChangeListener(changeSupport);

    widgetDay   = new TextFieldWidget("",2+1);
    widgetDay.setSelectAllOnFocus(true);
    widgetDay.addChangeListener(changeSupport);
    
    // Setup Layout
    setLayout(LAYOUT.copy()); // reuse a copy of layout 
    
    add(widgetCalendar);
    
    String format;
    switch (new SimpleDateFormat().toPattern().charAt(0)) {
	    case 'm': case 'M':
        format = "mmm/dd/yyyy"; 
        add(widgetMonth); 
        add(widgetDay) ; 
        add(widgetYear); 
        break;
	    case 'd': case 'D':
	      format = "dd.mmm.yyyy"; 
        add(widgetDay) ; 
        add(widgetMonth); 
        add(widgetYear); 
	      break;
	    default: 
	      format = "yyyy-mmm-dd"; 
	      add(widgetYear); 
	      add(widgetMonth); 
	      add(widgetDay) ; 
	      break;
    }
    
    widgetDay.setToolTipText(format);
    widgetMonth.setToolTipText(format);
    widgetYear.setToolTipText(format);
    
    // Status
    setValue(pit);
    updateStatus();

    // Done
  }
  
  /**
   * Add change listener
   */
  public void addChangeListener(ChangeListener l) {
    changeSupport.addChangeListener(l);
  }
  
  /**
   * Remove change listener
   */
  public void removeChangeListener(ChangeListener l) {
    changeSupport.removeChangeListener(l);
  }
  
  /**
   * Set current value
   */
  public void setValue(PointInTime pit) {

    // keep calendar    
    calendar = pit.getCalendar();

    // update tooltip
    widgetCalendar.setToolTipText(calendar.getName());
    
    // update year widget
    widgetYear.setText(calendar.getDisplayYear(pit.getYear ()));

    // update day widget
    widgetDay.setText(calendar.getDay(pit.getDay()));

    // update month widget
    String[] months = calendar.getMonths(true);
    widgetMonth.setValues(Arrays.asList(months));
    try {
      widgetMonth.setSelectedItem(null);
      widgetMonth.setSelectedItem(months[pit.getMonth()]);
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    
    // update our visible status
    updateStatus();
    
    // focus
    getComponent(1).requestFocusInWindow();
    
    // done
  }
  
  /**
   * Get current value
   */
  public PointInTime getValue() {

    int 
      u = PointInTime.UNKNOWN,
      d = u,
      m = u,
      y = u;
      
    // analyze day
    String day = widgetDay.getText().trim();
    if (day.length()>0) {
      try {
        d = Integer.parseInt(day) - 1;
      } catch (NumberFormatException e) {
        return null; 
      }
    }
    // analyze year
    String year = widgetYear.getText().trim();
    if (year.length()>0) {
      try {
        y = calendar.getYear(year);
      } catch (GedcomException e) {
        return null; 
      }
    }
    // analyze month
    String month = widgetMonth.getText();
    if (month.length()>0) {
      try {
        m = Integer.parseInt(month) - 1;
      } catch (NumberFormatException e) {
        String[] months = calendar.getMonths(true);
        for (m=0;m<months.length;m++)
          if (month.equalsIgnoreCase(months[m])) break;
        if (m==months.length) 
          return null;
      }
    }
    
    // generate result
    PointInTime result = new PointInTime(d, m, y, calendar);
    
    // is it valid?
    if ((d==u&&m==u&&y==u)||result.isValid())
      return result;
    
    // done 
    return null;
  }

  /**
   * Update the status icon
   */
  private void updateStatus() {
    // check whether valid
    PointInTime value = getValue();
    if (value==null) {
      // show 'X' on disabled button
      widgetCalendar.setEnabled(false);
      widgetCalendar.setIcon(MetaProperty.IMG_ERROR);
    } else {
      // show current calendar on enabled button
      widgetCalendar.setEnabled(true);
      widgetCalendar.setIcon(calendar.getImage());
    }
  }

  /**
   * Return the maximum size this component should be sized to
   */
  public Dimension getMaximumSize() {
    return new Dimension(super.getMaximumSize().width, super.getPreferredSize().height);
  }

  /**
   * @see javax.swing.JComponent#requestFocus()
   */
  public void requestFocus() {
    getComponent(1).requestFocus();
  }
  
  /**
   * @see javax.swing.JComponent#requestFocusInWindow()
   */
  public boolean requestFocusInWindow() {
    return getComponent(1).requestFocusInWindow();
  }
  
  /**
   * Action to switch calendar
   */
  private class SwitchCalendar extends Action2 {
    /** the calendar to switch to */
    private Calendar newCalendar;
    /**
     * Constructor
     */
    private SwitchCalendar(Calendar cal) {
      newCalendar = cal;
      setImage(newCalendar.getImage());
    }
    /**
     * @see genj.util.swing.Action2#getText()
     */
    public String getText() {
      WordBuffer result = new WordBuffer();
      result.append(newCalendar.getName());
      result.setFiller(" - ");
      try {
        PointInTime pit = DateWidget.this.getValue().getPointInTime(newCalendar); 
        result.append(pit.getDayOfWeek(true));
        result.append(pit);
      } catch (Throwable t) {
      }
      return result.toString();
    }
    /**
     * @see genj.util.swing.Action2#execute()
     */
    protected void execute() {
      PointInTime pit = DateWidget.this.getValue();
      if (pit!=null) {
        try {
          pit.set(newCalendar);
        } catch (GedcomException e) {
          WindowManager wm = WindowManager.getInstance(DateWidget.this);
          if (wm==null) {
            Logger.getLogger("genj.util.swing").info(e.getMessage());
          } else {
            Action[] actions = { Action2.ok(),  new Action2(Calendar.TXT_CALENDAR_RESET) };
            int rc = wm.openDialog(null, Calendar.TXT_CALENDAR_SWITCH, WindowManager.ERROR_MESSAGE, e.getMessage(), actions, DateWidget.this);
            if (rc==0) 
              return;
          }
          pit = new PointInTime(newCalendar);
        }
        // change
        setValue(pit);
      }
      // update current status
      updateStatus();
    }
  } // SwitchCalendar
  
} //DateEntry

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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Generic component for editing dates
 * 
 * @author Tomas Dahlqvist fix for US, European and ISO handling of Date
 */
public class DateWidget extends JPanel {

  private final static NestedBlockLayout LAYOUT = new NestedBlockLayout("<row><x pad=\"0\"/><x/><x/><x pad=\"0\"/></row>");

  /** components */
  private PopupWidget widgetCalendar;
  private TextFieldWidget widgetDay, widgetYear;
  private ChoiceWidget widgetMonth;

  /** current calendar */
  private Calendar calendar;
  private List<SwitchCalendar> switches;

  /** change support */
  private ChangeSupport changeSupport = new ChangeSupport(this) {
    @Override
    public void fireChangeEvent(javax.swing.event.ChangeEvent event) {
      // update our status
      updateStatus();
      // continue
      super.fireChangeEvent(event);
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

    setOpaque(false);

    calendar = pit.getCalendar();

    // create calendar switches
    switches = new ArrayList<SwitchCalendar>(PointInTime.CALENDARS.length + 1);
    for (int s = 0; s < PointInTime.CALENDARS.length; s++)
      switches.add(new SwitchCalendar(PointInTime.CALENDARS[s]));

    // initialize Sub-components
    widgetYear = new TextFieldWidget("", 4);
    widgetYear.setSelectAllOnFocus(true);
    widgetYear.addChangeListener(changeSupport);

    widgetMonth = new ChoiceWidget(new Object[0], null);
    widgetMonth.setIgnoreCase(true);
    widgetMonth.setSelectAllOnFocus(true);
    widgetMonth.addChangeListener(changeSupport);

    widgetDay = new TextFieldWidget("", 2);
    widgetDay.setSelectAllOnFocus(true);
    widgetDay.addChangeListener(changeSupport);

    widgetCalendar = new PopupWidget();
    widgetCalendar.addItems(switches);

    // Setup Layout
    setLayout(LAYOUT.copy()); // reuse a copy of layout

    String format;
    switch (new SimpleDateFormat().toPattern().charAt(0)) {
    case 'm':
    case 'M':
      format = "mmm/dd/yyyy";
      add(widgetMonth);
      add(widgetDay);
      add(widgetYear);
      
      widgetMonth.getTextEditor().setDocument(new TabbingDoc(widgetDay));
      widgetDay.setDocument(new TabbingDoc(widgetYear));
      break;
    case 'd':
    case 'D':
      format = "dd.mmm.yyyy";
      add(widgetDay);
      add(widgetMonth);
      add(widgetYear);

      widgetDay.setDocument(new TabbingDoc(widgetMonth));
      widgetMonth.getTextEditor().setDocument(new TabbingDoc(widgetYear));
      break;
    default:
      format = "yyyy-mmm-dd";
      add(widgetYear);
      add(widgetMonth);
      add(widgetDay);
      
      widgetYear.setDocument(new TabbingDoc(widgetMonth));
      widgetMonth.getTextEditor().setDocument(new TabbingDoc(widgetDay));
      break;
    }

    add(widgetCalendar);

    widgetDay.setToolTipText(format);
    widgetMonth.setToolTipText(format);
    widgetYear.setToolTipText(format);

    // Status
    setValue(pit);
    updateStatus();

    // Done
  }

  private class TabbingDoc extends PlainDocument {
    private JComponent next;

    public TabbingDoc(JComponent next) {
      this.next = next;
    }

    private boolean tab(String str) {
      if (str.length()!=1)
        return false;
      char c = str.charAt(0);
      if (c!='.'&&c!='/'&&c!='-')
        return false;
      if (next != null)
        next.requestFocusInWindow();
      return true;
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
      if (tab(str))
        return;
      super.insertString(offs, str, a);
    }

    @Override
    public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
      if (tab(text))
        return;
      super.replace(offset, length, text, attrs);
    }
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
    widgetYear.setText(calendar.getDisplayYear(pit.getYear()));

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

    // done
  }

  /**
   * Get current value
   */
  public PointInTime getValue() {

    int u = PointInTime.UNKNOWN, d = u, m = u, y = u;

    // analyze day
    String day = widgetDay.getText().trim();
    if (day.length() > 0) {
      try {
        d = Integer.parseInt(day) - 1;
      } catch (NumberFormatException e) {
        return null;
      }
    }
    // analyze year
    String year = widgetYear.getText().trim();
    if (year.length() > 0) {
      try {
        y = calendar.getYear(year);
      } catch (GedcomException e) {
        return null;
      }
    }
    // analyze month
    String month = widgetMonth.getText();
    if (month.length() > 0) {
      try {
        m = Integer.parseInt(month) - 1;
      } catch (NumberFormatException e) {
        String[] months = calendar.getMonths(true);
        for (m = 0; m < months.length; m++)
          if (month.equalsIgnoreCase(months[m]))
            break;
        if (m == months.length)
          return null;
      }
    }

    // generate result
    PointInTime result = new PointInTime(d, m, y, calendar);

    // is it valid?
    if ((d == u && m == u && y == u) || result.isValid())
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
    if (value == null) {
      // show 'X' on disabled button
      widgetCalendar.setEnabled(false);
      widgetCalendar.setDisabledIcon(MetaProperty.IMG_ERROR);
    } else {
      // show current calendar on enabled button
      widgetCalendar.setEnabled(true);
      widgetCalendar.setIcon(calendar.getImage());
    }
    for (SwitchCalendar switcher : switches) 
      switcher.preview();
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
    getComponent(0).requestFocus();
  }

  /**
   * @see javax.swing.JComponent#requestFocusInWindow()
   */
  public boolean requestFocusInWindow() {
    return getComponent(0).requestFocusInWindow();
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
      setText(cal.getName());
    }

    /**
     * set a preview to see
     */
    public void preview() {
      WordBuffer result = new WordBuffer();
      result.append(newCalendar.getName());
      result.setFiller(" - ");
      try {
        PointInTime pit = DateWidget.this.getValue().getPointInTime(newCalendar);
        result.append(pit.getDayOfWeek(true));
        result.append(pit);
      } catch (Throwable t) {
      }
      setText(result.toString());
    }

    /**
     * @see genj.util.swing.Action2#execute()
     */
    public void actionPerformed(ActionEvent event) {
      PointInTime pit = DateWidget.this.getValue();
      if (pit != null) {
        try {
          pit.set(newCalendar);
        } catch (GedcomException e) {
          Action[] actions = { Action2.ok(), new Action2(Calendar.TXT_CALENDAR_RESET) };
          int rc = DialogHelper.openDialog(Calendar.TXT_CALENDAR_SWITCH, DialogHelper.ERROR_MESSAGE, e.getMessage(), actions, DateWidget.this);
          if (rc == 0)
            return;
          pit = new PointInTime(newCalendar);
        }
        // change
        setValue(pit);
      }
      // update current status
      updateStatus();
    }
  } // SwitchCalendar

} // DateEntry

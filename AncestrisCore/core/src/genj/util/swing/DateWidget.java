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

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.util.swing.DialogManager;
import genj.gedcom.GedcomException;
import genj.gedcom.MetaProperty;
import genj.gedcom.time.Calendar;
import genj.gedcom.time.PointInTime;
import genj.util.ChangeSupport;
import genj.util.Resources;
import genj.util.WordBuffer;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
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

  private final static NestedBlockLayout LAYOUT = new NestedBlockLayout("<row><x pad=\"0\"/><x/><x/><x pad=\"0\"/><x pad=\"0,10,0,0\"/></row>");

  private Resources resources = Resources.get(Resources.class);
  
  /** components */
  private PopupWidget widgetCalendar;
  private TextFieldWidget widgetDay, widgetYear;
  private ChoiceWidget widgetMonth;
  private JLabel altDisplay;

  /** current calendar */
  private Calendar calendar;
  private List<SwitchCalendar> switches;
  private Calendar preferedCalendar;
  private Calendar alternateCalendar;

  /** change support */
  private ChangeSupport changeSupport = new ChangeSupport(this) {
    @Override
    public void fireChangeEvent(javax.swing.event.ChangeEvent event) {
          // update our status
          if (!SwingUtilities.isEventDispatchThread()) {
              SwingUtilities.invokeLater(new Runnable() {
                  @Override
                  public void run() {
                      updateStatus();
                  }
              });
          } else {
              updateStatus();
          }
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
      switches.add(new SwitchCalendar(PointInTime.CALENDARS[s], changeSupport));

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
    widgetCalendar.setPreferredSize(new Dimension(26,20));

    altDisplay = new JLabel("");

    // Setup Layout
    setLayout(LAYOUT.copy()); // reuse a copy of layout

    add(widgetCalendar);

    String format;
    switch (new SimpleDateFormat().toPattern().charAt(0)) {
    case 'm':
    case 'M':
      format = resources.getString("dateformat.tip.month"); // "mm/dd/yyyy"
      add(widgetMonth);
      add(widgetDay);
      add(widgetYear);
      
      widgetMonth.getTextEditor().setDocument(new TabbingDoc(widgetDay));
      widgetDay.setDocument(new TabbingDoc(widgetYear));
      break;
    case 'd':
    case 'D':
      format = resources.getString("dateformat.tip.day"); // "dd.mm.yyyy"
      add(widgetDay);
      add(widgetMonth);
      add(widgetYear);

      widgetDay.setDocument(new TabbingDoc(widgetMonth));
      widgetMonth.getTextEditor().setDocument(new TabbingDoc(widgetYear));
      break;
    default:
      format = resources.getString("dateformat.tip.year"); // "yyyy-mm-dd"
      add(widgetYear);
      add(widgetMonth);
      add(widgetDay);
      
      widgetYear.setDocument(new TabbingDoc(widgetMonth));
      widgetMonth.getTextEditor().setDocument(new TabbingDoc(widgetDay));
      break;
    }

    add(altDisplay);

    widgetDay.setToolTipText(format);
    widgetMonth.setToolTipText(format);
    widgetYear.setToolTipText(format);

    // Status
    setValue(pit);
    updateStatus();

    // Done
  }
/**
 * positionne les calendiers preferes. Ceux-ci, s'il sont non nuls, seront utilises pour afficher
 * un equivalence de la date a cote du widget:
 *
 * @param prefered dans le cas ou le calendrier courant et different de prefered, alors prefered (si non nul) est utilise
 * @param alternate si le calendrier courant est egal a prefered, alors alternate est utilise
 */
  public void setPreferedCalendar(Calendar prefered, Calendar alternate){
      alternateCalendar = alternate;
      preferedCalendar = prefered;
  }

  private static class TabbingDoc extends PlainDocument {
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
   * Add action listener
   */
  public void addActionListener(ActionListener a) {
    widgetYear.addActionListener(a);
    widgetMonth.addActionListener(a);
    widgetDay.addActionListener(a);
  }

  /**
   * Remove action listener
   */
  public void removeActionListener(ActionListener a) {
    widgetYear.removeActionListener(a);
    widgetMonth.removeActionListener(a);
    widgetDay.removeActionListener(a);
  }

  /**
   * Set current value
   */
  public final void setValue(PointInTime pit) {

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
     * Get current selected calendar
     *
     * @return
     */
    public Calendar getCalendar() {
        return calendar;
    }
    
    /**
     * Get current value
     *
     * @return
     */
    public PointInTime getValue() {
        return getValue(true);
    }
    
    /**
     * Get current value
     *
     * @return
     */
    public PointInTime getValue(boolean forceValid) {

        Integer u = PointInTime.UNKNOWN, d = u, m = u, y = u;

        // analyze day
        d = getDay();
        if (d == null) {
            return null;
        }

        // analyze year
        y = getYear();
        if (y == null) {
            return null;
        }

        // analyze month
        m = getMonth();
        if (m == null) {
            return null;
        }

        // generate result
        PointInTime result = new PointInTime(d, m, y, calendar);

        // is it valid?
        if ((d.equals(u) && m.equals(u) && y.equals(u)) || result.isValid() || !forceValid) {
            return result;
        }

        // done
        return null;
    }

  /**
   * Get current integer value
   */
  public Integer getDay() {
      Integer d = PointInTime.UNKNOWN;
      String day = widgetDay.getText().trim();
      if (day.length() > 0) {
          try {
              d = Integer.parseInt(day) - 1;
          } catch (NumberFormatException e) {
              return null;
          }
      }
      return d;
  }
  
  public Integer getMonth() {
      Integer m = PointInTime.UNKNOWN;
      String month = widgetMonth.getText();
      if (month.length() > 0) {
          try {
              m = Integer.parseInt(month) - 1;
          } catch (NumberFormatException e) {
              String[] months = calendar.getMonths(true);
              for (m = 0; m < months.length; m++) {
                  if (month.equalsIgnoreCase(months[m])) {
                      break;
                  }
              }
              if (m == months.length) {
                  return null;
              }
          }
      }
      return m;
  }

  public Integer getYear() {
      Integer y = PointInTime.UNKNOWN;
      String year = widgetYear.getText().trim();
      if (year.length() > 0) {
          try {
              y = calendar.getYear(year);
          } catch (GedcomException e) {
              return null;
          }
      }
      return y;
  }
  
  
  /**
   * Update the status icon
   */
  private void updateStatus() {
      // check whether valid
      PointInTime value = getValue();
      Calendar helpCalendar = null;
      if (value == null) {
          // show 'X' on disabled button
          widgetCalendar.setEnabled(false);
          widgetCalendar.setDisabledIcon(MetaProperty.IMG_ERROR);
      } else {
          // show current calendar on enabled button
          widgetCalendar.setVisible(true);
          widgetCalendar.setEnabled(true);
          widgetCalendar.setIcon(calendar.getImage());
          if (value.getCalendar() == preferedCalendar) {
              helpCalendar = alternateCalendar;
          } else {
              helpCalendar = preferedCalendar;
          }
      }

      if (helpCalendar == null) {
          altDisplay.setVisible(false);
          altDisplay.setText("");
      } else {
          altDisplay.setVisible(true);
          try {
              String dispValue = value.getPointInTime(helpCalendar).toString();
              if ("?".equals(dispValue)) {
                  dispValue = "";
              }
              altDisplay.setText(dispValue);

          } catch (GedcomException ex) {
          }
      }

      for (SwitchCalendar switcher : switches) {
          switcher.preview();
      }
  }
  
    private boolean areValidDaysMonths() {
        // test day validity
        if (!widgetDay.getText().isEmpty()) {
            Integer d = getDay();
            if (d == null) {
                return false;
            }
            PointInTime result = new PointInTime(d, 0, 2016, calendar);
            if (!result.isValid()) {
                return false;
            }
        }
        // test day validity
        if (!widgetMonth.getText().isEmpty()) {
            Integer m = getMonth();
            if (m == null) {
                return false;
            }
            PointInTime result = new PointInTime(0, m, 2016, calendar);
            if (!result.isValid()) {
                return false;
            }
        }
        return true;
    }

  

  /**
   * Return the maximum size this component should be sized to
   */
    @Override
  public Dimension getMaximumSize() {
    return new Dimension(super.getMaximumSize().width, super.getPreferredSize().height);
  }

  /**
   * @see javax.swing.JComponent#requestFocus()
   */
    @Override
  public void requestFocus() {
    getComponent(1).requestFocus();
  }

  /**
   * @see javax.swing.JComponent#requestFocusInWindow()
   */
    @Override
  public boolean requestFocusInWindow() {
    return getComponent(0).requestFocusInWindow();
  }

  /**
   * Action to switch calendar
   */
  private class SwitchCalendar extends AbstractAncestrisAction {
    /** the calendar to switch to */
    private Calendar newCalendar;
    private ChangeSupport widgetChangeSupport;

    /**
     * Constructor
     */
    private SwitchCalendar(Calendar cal, ChangeSupport cs) {
      this.widgetChangeSupport = cs;
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
      PointInTime p = DateWidget.this.getValue();
      if (p.isComplete()) {
            PointInTime pit = p.getPointInTime(newCalendar);
            result.append(pit.getDayOfWeek(true));
            result.append(pit);
      } else {
          PointInTime from,to;
          if (p.getMonth() == PointInTime.UNKNOWN){
              from = new PointInTime(PointInTime.UNKNOWN,0,p.getYear(),p.getCalendar());
              to = (new PointInTime());
              to.set(from);
              to.add(0,-1,1);
              from.set(PointInTime.UNKNOWN,from.getMonth(),from.getYear());
              to.set(PointInTime.UNKNOWN,to.getMonth(),to.getYear());
          } else {
              from = new PointInTime(0,p.getMonth(),p.getYear(),p.getCalendar());
              to = new PointInTime();
              to.set(from);
              to.add(-1,1,0);
          }
              result.append(from.getPointInTime(newCalendar));
              result.append(to.getPointInTime(newCalendar));
      }
      } catch (Throwable t) {
      }
      setText(result.toString());
    }

    /**
     * @see genj.util.swing.AbstractAncestrisAction#execute()
     */
        @Override
    public void actionPerformed(ActionEvent event) {
      PointInTime pit = DateWidget.this.getValue();
      if (pit != null) {
        try {
          pit.set(newCalendar);
        } catch (GedcomException e) {
            String reset = Calendar.TXT_CALENDAR_RESET;
          if (DialogManager.create(Calendar.TXT_CALENDAR_SWITCH, e.getMessage())
                  .setMessageType(DialogManager.ERROR_MESSAGE)
                  .setOptions(new Object[]{DialogManager.OK_OPTION,reset})
                  .show() == DialogManager.OK_OPTION){
            return;
        }
          pit = new PointInTime(newCalendar);
        }
        // change
        setValue(pit);
      }
      // update current status
      widgetChangeSupport.fireChangeEvent(event);
    }
  } // SwitchCalendar

} // DateEntry

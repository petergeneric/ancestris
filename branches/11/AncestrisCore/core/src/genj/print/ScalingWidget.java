/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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

import genj.util.ChangeSupport;
import genj.util.Resources;
import genj.util.swing.ChoiceWidget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Widget for selecting zoom factor
 */
public class ScalingWidget extends JPanel {

  private final static Pattern
    PERCENT = Pattern.compile("([0-9]{1,3})%"), // 10%,20%,100%,...
    DIM     = Pattern.compile("([0-9]{1,2})x([0-9]{1,2})"); // 1x1,1x2,...
  
  private ChoiceWidget choice;
  private ChangeSupport changeSupport = new ChangeSupport(this);

  /**
   * Constructor
   */
  public ScalingWidget() {
    super(new BorderLayout());
    
    choice = new ChoiceWidget(new String[]{ "1x1", "50%", "75%", "100%"}, "100%" );
    choice.addChangeListener(new Validate());
    
    add(new JLabel(Resources.get(this).getString("scaling")), BorderLayout.WEST);
    add(choice, BorderLayout.CENTER);
  }
  
  /**
   * Selected value
   * @return Dimension (pages) or Double (zoom 0.0-1.0) or null if not set
   */
  public Object getValue() {
    
    String t = choice.getText().trim();
    
    Matcher p = PERCENT.matcher(t);
    if (p.find())
      return Integer.parseInt(p.group(1))*0.01D;
    
    Matcher d = DIM.matcher(t);
    if (d.find())
      return new Dimension(Integer.parseInt(d.group(1)), Integer.parseInt(d.group(2)));

    return null;
  }
  
  private class Validate implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      Object value = getValue();
      Color c = value!=null ? null : Color.RED;
      choice.getEditor().getEditorComponent().setForeground(c);
      if (value!=null) changeSupport.fireChangeEvent();
    }
  }
  
  void addChangeListener(ChangeListener listener) {
    changeSupport.addChangeListener(listener);
  }
  
  void removeChangeListener(ChangeListener listener) {
    changeSupport.removeChangeListener(listener);
  }
  
}

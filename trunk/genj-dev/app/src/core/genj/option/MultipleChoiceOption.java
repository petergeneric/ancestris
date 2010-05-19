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
package genj.option;

import genj.util.Registry;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;

/**
 * A multiple choice Option
 */
public abstract class MultipleChoiceOption extends PropertyOption {

  /** wrapped option */
  private PropertyOption option;

  /** constructor */
  protected MultipleChoiceOption(PropertyOption option) {
    super(option.instance, option.getProperty());
    this.option = option;
  }

  /** restore */
  public void restore(Registry registry) {
    option.restore(registry);
  }

  /** persist */
  public void persist(Registry registry) {
    option.persist(registry);
  }

  /** name */
  public String getName() {
    return option.getName();
  }

  /** name */
  public void setName(String set) {
    option.setName(set);
  }

  /** tool tip */
  public String getToolTip() {
    return option.getToolTip();
  }

  /** tool tip */
  public void setToolTip(String set) {
    option.setToolTip(set);
  }

  /** value */
  public Object getValue() {
    return option.getValue();
  }

  /** value */
  public void setValue(Object set) {
    option.setValue(set);
  }

  /** ui access */
  public OptionUI getUI(OptionsWidget widget) {
    return new UI();
  }


  /** getter for index */
  protected int getIndex() {
    return ((Integer)option.getValue()).intValue();
  }

  /** setter for index */
  protected void setIndex(int i) {
    option.setValue(new Integer(i));
  }

  /** getter for choice */
  protected Object getChoice() {
    Object[] choices = getChoices();
    int i = getIndex();
    return i<0||i>choices.length-1 ? null : choices[i];
  }

  /** accessor choices */
  public final Object[] getChoices() {
    try  {
      return getChoicesImpl();
    } catch (Throwable t) {
      return new Object[0];
    }
  }

  /** accessor impl */
  protected abstract Object[] getChoicesImpl() throws Throwable;

  /**
   * our UI
   */
  public class UI extends JComboBox implements OptionUI {

    /** constructor */
    private UI() {
      Object[] choices = getChoices();
      setModel(new DefaultComboBoxModel(choices));
      int index = getIndex();
      if (index<0||index>choices.length-1)
        index = -1;
      setSelectedIndex(index);
    }

    /** component representation */
    public JComponent getComponentRepresentation() {
      return this;
    }

    /** text representation */
    public String getTextRepresentation() {
      Object result = getChoice();
      return result!=null ? result.toString() : "";
    }

    /** commit */
    public void endRepresentation() {
      setIndex(getSelectedIndex());
    }

  } //UI

} //MultipleChoiceOption
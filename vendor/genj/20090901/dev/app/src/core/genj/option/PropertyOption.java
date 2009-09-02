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
import genj.util.Resources;
import genj.util.swing.FileChooserWidget;
import genj.util.swing.FontChooser;
import genj.util.swing.TextFieldWidget;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

/**
 * An option based on a simple accessible value
 */
public abstract class PropertyOption extends Option {

  /** property */
  protected String property;

  /** option is for instance */
  protected Object instance;

  /**
   * Get options for given instance
   */
  public static List introspect(Object instance) {

    // prepare result
    List result = new ArrayList();
    Set beanattrs = new HashSet();

    // loop over bean properties of instance
    try {
      BeanInfo info = Introspector.getBeanInfo(instance.getClass());
      PropertyDescriptor[] properties = info.getPropertyDescriptors();
      for (int p=0; p<properties.length; p++) {

        PropertyDescriptor property = properties[p];

        try {
          // has to have getter & setter
          if (property.getReadMethod()==null||property.getWriteMethod()==null)
            continue;

          // int, boolean, String?
          if (!Impl.isSupportedArgument(property.getPropertyType()))
            continue;

          // try a read
          property.getReadMethod().invoke(instance, (Object[])null);

          // create
          Option option = BeanPropertyImpl.create(instance, property);

          // and keep the option
          result.add(option);

          // remember name
          beanattrs.add(property.getName());
        } catch (Throwable t) {
        }
      }
    } catch (IntrospectionException e) {
    }

    // loop over fields of instance
    Field[] fields = instance.getClass().getFields();
    for (int f=0;f<fields.length;f++) {

      Field field = fields[f];
      Class type = field.getType();

      // won't address name of property again
      if (beanattrs.contains(field.getName()))
        continue;

      // has to be public, non-static, non-final
      int mod = field.getModifiers();
      if (Modifier.isFinal(mod) || Modifier.isStatic(mod))
        continue;
      try {
        field.get(instance);
      } catch (Throwable t) {
        continue;
      }

      // int, boolean, String?
      if (!Impl.isSupportedArgument(type))
        continue;

      // create
      Option option = FieldImpl.create(instance, field);

      // and keep the option
      result.add(option);

      // next
    }

    // done
    return result;
  }

  /**
   * Constructor
   */
  protected PropertyOption(Object instance, String property) {
    this.instance = instance;
    this.property = property;
  }

  /**
   * Accessor - option value
   */
  public abstract Object getValue();

  /**
   * Accessor - option value
   */
  public abstract void setValue(Object set);

  /**
   * Setter - name
   */
  public abstract void setName(String set);

  /**
   * Setter - tool tip
   */
  public abstract void setToolTip(String set);

  /**
   * Accessor - a unique key for this option
   */
  public String getProperty() {
    return property;
  }

  /**
   * Accessor - category of this option
   */
  public String getCategory() {
    String result = super.getCategory();
    if (result==null) {
      // try to localize?
      Resources resources = Resources.get(instance);
      result = resources.getString("options", false);
      if (result!=null)
        super.setCategory(result);
    }
    return result;
  }

  /**
   * A UI for a font
   */
  protected static class FontUI implements OptionUI {

    /** widgets */
    private FontChooser chooser = new FontChooser();

    /** option */
    private PropertyOption option;

    /** constructor */
    public FontUI(PropertyOption option) {
      this.option = option;
    }

    /** callback - text representation = none */
    public String getTextRepresentation() {
      Font font = (Font)option.getValue();
      return font==null ? "..." : font.getFamily() + "," + font.getSize();
    }

    /** callback - component representation */
    public JComponent getComponentRepresentation() {
      chooser.setSelectedFont((Font)option.getValue());
      return chooser;
    }

    /** commit - noop */
    public void endRepresentation() {
      option.setValue(chooser.getSelectedFont());
    }

  } //FontUI

  /**
   * A UI for a file
   */
  protected static class FileUI implements OptionUI {

    /** file chooser */
    private FileChooserWidget chooser = new FileChooserWidget();

    /** option */
    private PropertyOption option;

    /** constructor */
    public FileUI(PropertyOption option) {
      this.option = option;
      chooser.setFile((File)option.getValue());
    }

    /** text is file name */
    public String getTextRepresentation() {
      return chooser.getFile().toString();
    }

    /** component is the chooser */
    public JComponent getComponentRepresentation() {
      return chooser;
    }

    /** end and commit change */
    public void endRepresentation() {
      option.setValue(chooser.getFile());
    }

  } //FileUI

  /**
   * A UI for a boolean
   */
  protected static class BooleanUI extends JCheckBox implements OptionUI {
    /** option */
    private PropertyOption option;

    /** constructor */
    public BooleanUI(PropertyOption option) {
      this.option = option;
      setOpaque(false);
      setHorizontalAlignment(JCheckBox.LEFT);
      Boolean value = (Boolean)option.getValue();
      if (value.booleanValue())
        setSelected(true);
    }
    /** no text ui */
    public String getTextRepresentation() {
      return null;
    }
    /** component */
    public JComponent getComponentRepresentation() {
      return this;
    }
    /** commit */
    public void endRepresentation() {
      option.setValue(isSelected()?Boolean.TRUE : Boolean.FALSE);
    }
  } //BooleanUI

  /**
   * A UI for text, numbers, etc.
   */
  protected static class SimpleUI extends TextFieldWidget implements OptionUI {
    /** option */
    private PropertyOption option;

    /** constructor */
    public SimpleUI(PropertyOption option) {
      this.option = option;
      Object value = option.getValue();
      setText(value!=null?value.toString():"");
      setSelectAllOnFocus(true);
      setColumns(12);
    }
    /** no text ui */
    public String getTextRepresentation() {
      return getText();
    }
    /** component */
    public JComponent getComponentRepresentation() {
      return this;
    }
    /** commit */
    public void endRepresentation() {
      option.setValue(getText());
    }
  } //BooleanUI

  /**
   * Impl base type
   */
  private static abstract class Impl extends PropertyOption {

    /** type */
    protected Class type;

    /** a user readable name */
    private String name;

    /** a user readable tool tip */
    private String toolTip;

    /** mapper */
    private Mapper mapper;

    /**
     * Constructor
     */
    protected Impl(Object instance, String property, Class type) {
      super(instance, property);
      this.type     = type;

      // TODO Options - hardcoded mapper for fonts
      this.mapper   = type==Font.class ? new FontMapper() : new Mapper();
    }

    /**
     * Accessor - name of this option
     */
    public String getName() {
      if (name==null) {
        // can localize?
        Resources resources = Resources.get(instance);
        name = resources.getString("option."+property, false);
        if (name==null) {
          name = resources.getString(property, false);
          if (name==null)
            name = property;
        }
      }
      // done
      return name;
    }

    /**
     * Accessor - name of this option
     */
    public void setName(String set) {
      name = set;
    }

    /**
     * Accessor - tool tip for this option
     */
    public String getToolTip() {
      if (toolTip==null) {
        // can localize?
        Resources resources = Resources.get(instance);
        toolTip = resources.getString("option." + property + ".tip", false);
        if (toolTip==null) {
          toolTip = resources.getString(property + ".tip", false);
        }
      }
      // done
      return toolTip;
    }

    /**
     * Accessor - tool tip for this option
     */
    public void setToolTip(String set) {
      toolTip = set;
    }

    /**
     * Restore option values from registry
     */
    public void restore(Registry registry) {
      String value = registry.get(instance.getClass().getName() + '.' + getProperty(), (String)null);
      if (value!=null)
        setValue(value);
    }

    /**
     * Persist option values to registry
     */
    public void persist(Registry registry) {
      Object value = getValue();
      if (value!=null)
        registry.put(instance.getClass().getName() + '.' + getProperty(), value.toString());
    }

    /**
     * Provider a UI for this option
     */
    public OptionUI getUI(OptionsWidget widget) {
      // TODO Options - hardcoded UI
      // a font?
      if (Font.class.isAssignableFrom(type))
        return new FontUI(this);
      // a boolean?
      if (type==Boolean.TYPE)
        return new BooleanUI(this);
      // a file?
      if (type==File.class)
        return new FileUI(this);
      // all else
      return new SimpleUI(this);
    }

    /**
     * Accessor - current value of this option
     */
    public final Object getValue() {
      try {
        // get it
        return getValueImpl();
      } catch (Throwable t) {
        return null;
      }
    }

    /**
     * Accessor - implementation
     */
    protected abstract Object getValueImpl() throws Throwable;

    /**
     * Accessor - current value of this option
     */
    public final void setValue(Object value) {

      // a change in value?
      try {
        Object old = getValueImpl();
        if (old==value)
          return;
        if (old!=null&&value!=null&&old.equals(value))
          return;

        setValueImpl(mapper.toObject(value, type));

      } catch (Throwable t) {
        // not much we can do about that - ignored
      }
      // notify
      fireChangeNotification();
    }

    /**
     * Accessor - implementation
     */
    protected abstract void setValueImpl(Object value) throws Throwable;

    /**
     * Test for supported option types
     */
    private static boolean isSupportedArgument(Class type) {
      return
        Font.class.isAssignableFrom(type)   ||
        File.class.isAssignableFrom(type)   ||
        String.class.isAssignableFrom(type) ||
        Float.TYPE.isAssignableFrom(type) ||
        Double.TYPE.isAssignableFrom(type) ||
        Long.TYPE.isAssignableFrom(type) ||
        Integer.TYPE.isAssignableFrom(type) ||
        Boolean.TYPE.isAssignableFrom(type);
    }

  } //Impl

  /**
   * A field Option
   */
  private static class FieldImpl extends Impl {

    /** field */
    protected Field field;

    /** factory */
    protected static Option create(final Object instance, Field field) {
      // create one
      PropertyOption result = new FieldImpl(instance, field);
      // is it an Integer field with matching multiple choice field?
      if (field.getType()==Integer.TYPE) try {
        final Field choices = instance.getClass().getField(field.getName()+"s");
        if (choices.getType().isArray())
          // wrap in multiple choice
          return new MultipleChoiceOption(result) {
            public Object[] getChoicesImpl() throws Throwable {
              return (Object[])choices.get(instance);
            }
          };
      } catch (Throwable t) {
      }
      // done
      return result;
    }

    /** Constructor */
    private FieldImpl(Object instance, Field field) {
      super(instance, field.getName(), field.getType());
      this.field = field;
    }

    /** accessor */
    protected Object getValueImpl() throws Throwable {
      return field.get(instance);
    }

    /** accessor */
    protected void setValueImpl(Object value) throws Throwable {
      field.set(instance, value);
    }

  } //Field

  /**
   * A bean property Option
   */
  private static class BeanPropertyImpl extends Impl {

    /** descriptor */
    PropertyDescriptor descriptor;

    /** factory */
    protected static Option create(final Object instance, PropertyDescriptor descriptor) {
      // create one
      PropertyOption result = new BeanPropertyImpl(instance, descriptor);
      // is it an Integer field with matching multiple choice field?
      if (descriptor.getPropertyType()==Integer.TYPE) try {
        final Method choices = instance.getClass().getMethod(descriptor.getReadMethod().getName()+"s", (Class[])null);
        if (choices.getReturnType().isArray())
          // wrap in multiple choice
          return new MultipleChoiceOption(result) {
            public Object[] getChoicesImpl() throws Throwable {
              return (Object[])choices.invoke(instance, (Object[])null);
            }
          };
      } catch (Throwable t) {
      }
      // done
      return result;
    }

    /** Constructor */
    private BeanPropertyImpl(Object instance, PropertyDescriptor property) {
      super(instance, property.getName(), property.getPropertyType());
      this.descriptor = property;
    }

    /** accessor */
    protected Object getValueImpl() throws Throwable {
      return descriptor.getReadMethod().invoke(instance, (Object[])null);
    }

    /** accessor */
    protected void setValueImpl(Object value) throws Throwable {
      descriptor.getWriteMethod().invoke(instance, new Object[]{value} );
    }

  } //BeanProperty

  /**
   * A mapper
   */
  private static class Mapper {

    /**
     * box type making sure no primitive types are returned
     */
    private static Class box(Class type) {
      if (type == boolean.class) return Boolean.class;
      if (type == byte.class) return Byte.class;
      if (type == char.class) return Character.class;
      if (type == short.class) return Short.class;
      if (type == int.class) return Integer.class;
      if (type == long.class) return Long.class;
      if (type == float.class) return Float.class;
      if (type == double.class) return Double.class;
      return type;
    }


    protected String toString(Object object) {
      return object!=null ? object.toString() : "";
    }

    protected Object toObject(Object object, Class expected) {
      // make sure expected is not a primitive type
      expected = box(expected);
      // already ok?
      if (object==null||object.getClass()==expected)
        return object;
      // map it
      try {
        return expected.getConstructor(new Class[]{object.getClass()})
          .newInstance(new Object[]{ object });
      } catch (Throwable t) {
        throw new IllegalArgumentException("can't map "+object+" to expected");
      }
    }
  } // Mapper

  /**
   * A mapper - Font
   */
  private static class FontMapper extends Mapper{

    private final static String
    FAMILY = "family=",
    STYLE  = "style=",
    SIZE   = "size=";

    /** font from string representation */
    protected Object toObject(Object object, Class expected) {

      if (expected!=Font.class||object==null||object.getClass()!=String.class)
        return super.toObject(object, expected);
      String string = (String)object;

      // check what we've got
      Map map = new HashMap();

      String family = getAttribute(string, FAMILY);
      if (family==null)
        family = "SansSerif";
      map.put(TextAttribute.FAMILY, family);

      try {
        map.put(TextAttribute.SIZE, new Float(getAttribute(string, SIZE)));
      } catch (Throwable t) {
        map.put(TextAttribute.SIZE, new Float(11F));
      }

      // done
      return new Font(map);
    }

    protected String getAttribute(String string, String key) {

      int i = string.indexOf(key);
      if (i<0)
        return null;
      i += key.length();

      int j = i;
      for (;j<string.length();j++) {
        char c = string.charAt(j);
        if (!(Character.isLetterOrDigit(c)||Character.isWhitespace(c)))
          break;
      }

      return j<i ? null : string.substring(i, j);
    }

  }

} //ValueOption

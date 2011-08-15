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
package genj.renderer;

import genj.option.CustomOption;
import genj.option.Option;
import genj.option.OptionProvider;
import genj.option.PropertyOption;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ScreenResolutionScale;

import java.awt.Font;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 * Blueprint/Renderer Options
 */
public class Options extends OptionProvider {

  private final static Resources RESOURCES = Resources.get(Options.class);

  /** singleton */
  private final static Options instance = new Options();

  /** the default font */
  private Font defaultFont = new Font("SansSerif", 0, 11);

  /** the current screen resolution */
  private DPI dpi = new DPI(
    Toolkit.getDefaultToolkit().getScreenResolution(),
    Toolkit.getDefaultToolkit().getScreenResolution()
  );

  /**
   * singleton access
   */
  public static Options getInstance() {
    return instance;
  }

  /**
   * Accessor - font
   */
  public Font getDefaultFont() {
    return defaultFont;
  }

  /**
   * Accessor - font
   */
  public void setDefaultFont(Font set) {
    defaultFont = set;
  }

  /**
   * Access to our options (one)
   */
  public List<? extends Option> getOptions() {
    List<Option> result = new ArrayList<Option>(PropertyOption.introspect(getInstance()));
    result.add(new ScreenResolutionOption());
    return result;
  }

  /**
   * Accessor - DPI
   */
  public DPI getDPI() {
    return dpi;
  }

  /**
   * Option for Screen Resolution
   */
  private class ScreenResolutionOption extends CustomOption {

    /** callback - user readble name */
    public String getName() {
      return RESOURCES.getString("option.screenresolution");
    }

    /** callback - user readble tool tip */
    public String getToolTip() {
      return RESOURCES.getString("option.screenresolution.tip", false);
    }

    /** callback - persist */
    public void persist() {
      Registry.get(this).put("dpi.h", dpi.horizontal());
      Registry.get(this).put("dpi.v", dpi.vertical());
    }

    /** callback - restore */
    public void restore() {
      int h = Registry.get(this).get("dpi.h", 0);
      int v = Registry.get(this).get("dpi.v", 0);
      if (h>0&&v>0)
        dpi = new DPI(h,v);
    }

    /** callback - edit option */
    @Override
    protected JComponent getEditor() {
      return new ScreenResolutionScale(dpi);
    }
    
    @Override
    protected void commit(JComponent editor) {
      dpi = ((ScreenResolutionScale)editor).getDPI();
    }

  } //ScreenResolutionOption

} //Options

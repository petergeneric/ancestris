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

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.JToolBar;

/**
 * A slider that knows about our other components
 */
public class SliderWidget extends JSlider {

  /**
   * 
   */
  public SliderWidget() {
    super();
  }

  /**
   * @param orientation
   */
  public SliderWidget(int orientation) {
    super(orientation);
  }

  /**
   * @param min
   * @param max
   */
  public SliderWidget(int min, int max) {
    super(min, max);
  }

  /**
   * @param min
   * @param max
   * @param value
   */
  public SliderWidget(int min, int max, int value) {
    super(min, max, value);
  }

  /**
   * @param orientation
   * @param min
   * @param max
   * @param value
   */
  public SliderWidget(int orientation, int min, int max, int value) {
    super(orientation, min, max, value);
  }

  /**
   * @param brm
   */
  public SliderWidget(BoundedRangeModel brm) {
    super(brm);
  }

  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    // check if we're in a toolbar
    if (getParent() instanceof JToolBar) {
      int orientation = ((JToolBar)getParent()).getOrientation();
      setOrientation(orientation);
    }
    setMaximumSize(getPreferredSize());
    setAlignmentX(0);
    // delegate
    super.addNotify();
  }
} //SliderWidget

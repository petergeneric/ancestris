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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.plaf.ToolBarUI;
import javax.swing.plaf.UIResource;
import net.miginfocom.swing.MigLayout;
import org.openide.util.actions.Presenter;

/**
 * JToolbar with MigLayout.
 * DAN 20140610: this is the only combination which allowed
 * a dynamic fill of a JPanel component based on its actual size.
 * This is used in ATable class for shortcuts display.
 * Most of the source here come from JToolBar source adapted
 * for MigLayout
 */
public class ToolBar extends JToolBar {
    
    AtomicBoolean notEmpty = new AtomicBoolean(false);

    /**
     * Creates a new tool bar; orientation defaults to <code>HORIZONTAL</code>.
     */
    public ToolBar() {
        this(HORIZONTAL);
    }

    /**
     * Creates a new tool bar with the specified <code>orientation</code>.
     * The <code>orientation</code> must be either <code>HORIZONTAL</code>
     * or <code>VERTICAL</code>.
     *
     * @param orientation the orientation desired
     */
    public ToolBar(int orientation) {
        this(null, orientation);
    }

    /**
     * Creates a new tool bar with the specified <code>name</code>. The
     * name is used as the title of the undocked tool bar. The default
     * orientation is <code>HORIZONTAL</code>.
     *
     * @param name the name of the tool bar
     *
     * @since 1.3
     */
    public ToolBar(String name) {
        this(name, HORIZONTAL);
    }

    /**
     * Creates a new tool bar with a specified <code>name</code> and
     * <code>orientation</code>.
     * All other constructors call this constructor.
     * If <code>orientation</code> is an invalid value, an exception will
     * be thrown.
     *
     * @param name        the name of the tool bar
     * @param orientation the initial orientation -- it must be
     *                    either <code>HORIZONTAL</code> or <code>VERTICAL</code>
     *
     * @exception IllegalArgumentException if orientation is neither
     *                                     <code>HORIZONTAL</code> nor <code>VERTICAL</code>
     * @since 1.3
     */
    public ToolBar(String name, int orientation) {
        super(name, orientation);
        ToolBarLayout layout = new ToolBarLayout(orientation);
        setLayout(layout);
        
        addPropertyChangeListener(layout);

//        updateUI();
    }

    /**
     * Notification from the <code>UIFactory</code> that the L&F has changed.
     * Called to replace the UI with the latest version from the
     * <code>UIFactory</code>.
     *
     * @see JComponent#updateUI
     */
    @Override
    public void updateUI() {
        setUI((ToolBarUI) UIManager.getUI(this));
        // GTKLookAndFeel installs a different LayoutManager, and sets it
        // to null after changing the look and feel, so, install the default
        // if the LayoutManager is null.
        if (getLayout() == null) {
            setLayout(new ToolBarLayout(getOrientation()));
        }
        invalidate();
    }
    
    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        super.addImpl(comp, constraints, index);
        setVisible(true);
        notEmpty.set(true);
        
    }
    
    public void add(JComponent component) {
        super.add(component);
        setVisible(true);
        component.setFocusable(false);
        notEmpty.set(true);
    }
    
    public void add(Presenter.Toolbar component) {
        if (component != null) {
            add(component.getToolbarPresenter());
        }
    }
    
    public JToolBar getToolBar() {
        return (notEmpty.get()) ? this : null;
    }
    
    public void beginUpdate() {
        notEmpty.set(false);
        removeAll();
        setVisible(false);
//      bar.validate();
    }
    
    public void endUpdate() {
    }
    
    public void addGlue() {
        add(Box.createGlue());
    }
    
    @Override
    public void addSeparator() {
        addSeparator(new Dimension(3, 32));
    }
    
    private class ToolBarLayout
            implements LayoutManager2, Serializable, PropertyChangeListener, UIResource {
        
        MigLayout lm;
        
        ToolBarLayout(int orientation) {
            setLm(orientation);
        }
        
        private void setLm(int orientation) {
            if (orientation == JToolBar.VERTICAL) {
                lm = new MigLayout("insets 10 0 10 0, gap 0, flowy");
            } else {
                lm = new MigLayout("insets 0 10 0 10, gap 0, flowx");
            }
        }
        
        @Override
        public void addLayoutComponent(String name, Component comp) {
            lm.addLayoutComponent(name, comp);
        }
        
        @Override
        public void addLayoutComponent(Component comp, Object constraints) {
            lm.addLayoutComponent(comp, constraints);
        }
        
        @Override
        public void removeLayoutComponent(Component comp) {
            lm.removeLayoutComponent(comp);
        }
        
        @Override
        public Dimension preferredLayoutSize(Container target) {
            return lm.preferredLayoutSize(target);
        }
        
        @Override
        public Dimension minimumLayoutSize(Container target) {
            return lm.minimumLayoutSize(target);
        }
        
        @Override
        public Dimension maximumLayoutSize(Container target) {
            return lm.maximumLayoutSize(target);
        }
        
        @Override
        public void layoutContainer(Container target) {
            lm.layoutContainer(target);
        }
        
        @Override
        public float getLayoutAlignmentX(Container target) {
            return lm.getLayoutAlignmentX(target);
        }
        
        @Override
        public float getLayoutAlignmentY(Container target) {
            return lm.getLayoutAlignmentY(target);
        }
        
        @Override
        public void invalidateLayout(Container target) {
            lm.invalidateLayout(target);
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if (name.equals("orientation")) {
                int o = ((Integer) e.getNewValue()).intValue();
                setLm(o);
            }
        }
    }
}

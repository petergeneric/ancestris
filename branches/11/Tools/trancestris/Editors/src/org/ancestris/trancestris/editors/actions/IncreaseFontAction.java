/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.editors.actions;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.ancestris.trancestris.editors.resourceeditor.ResourceEditorTopComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class IncreaseFontAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        TopComponent tc = WindowManager.getDefault().findTopComponent("ResourceEditorTopComponent");
        Font font = ((ResourceEditorTopComponent) tc).getFont();
        ((ResourceEditorTopComponent) tc).setFont(new Font(font.getFontName(), font.getStyle(), font.getSize() + 2));
    }
}

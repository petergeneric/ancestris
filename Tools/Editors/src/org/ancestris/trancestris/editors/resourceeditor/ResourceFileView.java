package org.ancestris.trancestris.editors.resourceeditor;

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ResourceFileView.java
import java.awt.*;
import javax.swing.*;
import javax.swing.event.ListDataListener;
import org.ancestris.trancestris.resources.ResourceFile;

public class ResourceFileView extends JList {

    private class LRenderer extends JTextArea implements ListCellRenderer {

        private JTextArea cellRenderer;

        @Override
        public Component getListCellRendererComponent(JList jlist, Object obj, int index, boolean isSelected, boolean cellHasFocus) {
            if (cellRenderer == null) {
                cellRenderer = new JTextArea();
                cellRenderer.setFont(jlist.getFont());
                cellRenderer.setOpaque(true);
            }

            String s = file.getLineTranslation(index);
            Color color;
            switch (file.getLineState(index)) {
                case -1:
                    color = Color.blue;
                    break;

                case 0: // '\0'
                    color = Color.red;
                    break;

                case 1: // '\001'
                default:
                    color = jlist.getForeground();
                    break;
            }
            if (isSelected) {
                cellRenderer.setForeground(Color.white);
                cellRenderer.setBackground(Color.black);
            } else {
                cellRenderer.setForeground(color);
                cellRenderer.setBackground(jlist.getBackground());
            }

            cellRenderer.setText(obj.toString());
            return cellRenderer;
        }

        private LRenderer() {
        }

        LRenderer(LRenderer lrenderer) {
            this();
        }
    }
    private ResourceFile file = null;

    public ResourceFileView() {
        setCellRenderer(new LRenderer(null));
    }

    public ResourceFile getResourceFile() {
        return file;
    }

    public void incSelection() {
        int i = getSelectedIndex();
        if (i < 0) {
            return;
        }
        while (i + 1 < getModel().getSize()) {
            i++;
            if (file.getLineState(i) == 0) {
                setSelectedIndex(i);
                ensureIndexIsVisible(i);
                break;
            }
        }
    }

    public void setResourceFile(ResourceFile resourcefile) {
        file = resourcefile;
        if (file == null) {
            setModel(new DefaultListModel());
            return;
        }
        ListModel listmodel = new ListModel() {

            @Override
            public void addListDataListener(ListDataListener listdatalistener1) {
            }

            @Override
            public void removeListDataListener(ListDataListener listdatalistener1) {
            }

            @Override
            public Object getElementAt(int i) {
                return file.getLine(i);
            }

            @Override
            public int getSize() {
                return file.getLineCount();
            }
        };
        setModel(listmodel);
        java.awt.Container container = getParent();
        if (container instanceof JViewport) {
            ((JViewport) container).setViewPosition(new Point(0, 0));
        }
    }
}

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

    private class LRenderer extends JLabel implements ListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList jlist, Object obj, int i, boolean flag, boolean flag1) {
            if (label == null) {
                label = new JLabel();
                label.setFont(jlist.getFont());
                label.setOpaque(true);
            }
            String s = file.getLineTranslation(i);
            Color color;
            switch (file.getLineState(i)) {
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
            label.setBackground(flag ? jlist.getSelectionBackground() : jlist.getBackground());
            label.setForeground(flag ? jlist.getSelectionForeground() : color);
            label.setText(obj.toString());
            return label;
        }
        private JLabel label;

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

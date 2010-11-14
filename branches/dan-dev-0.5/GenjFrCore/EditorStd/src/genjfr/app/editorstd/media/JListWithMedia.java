/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.editorstd.media;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author frederic
 */
public class JListWithMedia extends JList {

    MediaWrapper[] mediaList;

    public JListWithMedia(MediaWrapper[] mediaList) {
        this.mediaList = mediaList;
        setListData(mediaList);
        //setBorder(new LineBorder(Color.gray));
        setCellRenderer(new CustomCellRenderer());
    }

    class CustomCellRenderer implements ListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (list == null || value == null) {
                return new JLabel("no value");
            }
            Component component = ((MediaWrapper) value).getComponent();
            component.setBackground(isSelected ? Color.LIGHT_GRAY : Color.white);
            component.setForeground(isSelected ? Color.white : Color.LIGHT_GRAY);
            return component;
        }
    }
}

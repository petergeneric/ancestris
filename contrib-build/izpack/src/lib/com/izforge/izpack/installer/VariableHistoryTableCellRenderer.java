/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.installer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Map;


/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: $
 */
public class VariableHistoryTableCellRenderer extends DefaultTableCellRenderer
{
    private static final long serialVersionUID = 6779914244548965230L;
    private Map<String, VariableHistory> variablehistory;

    public VariableHistoryTableCellRenderer(Map<String, VariableHistory> variablehistory)
    {
        this.variablehistory = variablehistory;
    }


    /* (non-Javadoc)
    * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
    */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column)
    {
        JComponent comp = null;

        VariableHistory vh = (VariableHistory) value;

        JLabel label = new JLabel();
        label.setAutoscrolls(true);
        comp = label;

        label.setText(vh.getLastValue());

        comp.setOpaque(true);
        if (vh.isNewvariable())
        {
            comp.setBackground(Color.green);
        }
        else if (vh.isChanged())
        {
            comp.setBackground(Color.yellow);
        }
        return comp;
    }

    public void clearState()
    {
        for (String s : variablehistory.keySet())
        {
            VariableHistory vh = variablehistory.get(s);
            vh.clearState();
        }
    }
}


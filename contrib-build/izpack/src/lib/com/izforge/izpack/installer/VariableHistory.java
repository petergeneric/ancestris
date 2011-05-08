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

import java.util.ArrayList;
import java.util.List;


/**
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 * @version $Id: $
 */
public class VariableHistory
{
    private String name;
    private List<String[]> values;
    private boolean newvariable;
    private boolean changed;


    public VariableHistory(String variable)
    {
        name = variable;
        values = new ArrayList<String[]>();
    }


    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }


    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public void addValue(String value, String comment)
    {
        String[] valuecomment = new String[2];
        valuecomment[0] = value;
        valuecomment[1] = comment;
        values.add(valuecomment);
        if (values.size() == 1)
        {
            newvariable = true;
            changed = true;
        }
        else
        {
            changed = true;
        }
    }

    public String[] getValueComment(int index)
    {
        return values.get(index);
    }

    public int getValuesCount()
    {
        return values.size();
    }

    public String getLastValue()
    {
        if (values.size() > 0)
        {
            String[] valuecomment = values.get(values.size() - 1);
            return valuecomment[0];
        }
        else
        {
            return "";
        }
    }

    /**
     * @return the newvariable
     */
    public boolean isNewvariable()
    {
        return this.newvariable;
    }


    /**
     * @return the changed
     */
    public boolean isChanged()
    {
        return this.changed;
    }


    /**
     * @param changed the changed to set
     */
    public void setChanged(boolean changed)
    {
        this.changed = changed;
    }

    public void clearState()
    {
        newvariable = false;
        changed = false;
    }

    public String getValueHistoryDetails()
    {
        StringBuffer details = new StringBuffer();
        details.append("<html><body>");
        details.append("<h3>Details of <b>");
        details.append(this.name);
        details.append("</b></h3>");
        for (int i = values.size() - 1; i >= 0; i--)
        {
            String[] valuecomment = values.get(i);
            details.append(i + 1);
            details.append(". ");
            details.append(valuecomment[0]);
            details.append(" (");
            details.append(valuecomment[1]);
            details.append(")<br>");
        }
        details.append("</body></html>");
        return details.toString();
    }

    public String toString()
    {
        return this.getLastValue();
    }
}

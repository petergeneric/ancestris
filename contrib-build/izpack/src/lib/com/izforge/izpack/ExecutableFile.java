/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2001,2002 Olexij Tkatchenko
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

package com.izforge.izpack;

import com.izforge.izpack.util.OsConstraint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Encloses information about a executable file. This class abstracts the way to do a system
 * dependent postprocessing of installation.
 *
 * @author Olexij Tkatchenko <ot@parcs.de>
 */

public class ExecutableFile implements Serializable
{

    static final long serialVersionUID = 4175489415984990405L;

    /**
     * when to execute this file
     */
    public final static int POSTINSTALL = 0;

    public final static int NEVER = 1;

    public final static int UNINSTALL = 2;

    /**
     * type of a file
     */
    public final static int BIN = 0;

    public final static int JAR = 1;

    /**
     * what to do if execution fails
     */
    public final static int ABORT = 0;

    public final static int WARN = 1;

    public final static int ASK = 2;

    public final static int IGNORE = 3;

    /**
     * The file path
     */
    public String path;

    /**
     * Execution stage (NEVER, POSTINSTALL, UNINSTALL)
     */
    public int executionStage;

    /**
     * Main class of jar file
     */
    public String mainClass;

    /**
     * type (BIN|JAR)
     */
    public int type;

    /**
     * Failure handling (ABORT, WARN, ASK)
     */
    public int onFailure;

    /**
     * List of arguments
     */
    public List<String> argList = null;

    /**
     * List of operating systems to run on
     */
    public List<OsConstraint> osList = null;

    /**
     * Indicates the file should be kept after executing. Default is false for backward
     * compatibility.
     */
    public boolean keepFile;

    /**
     * condition for this executable
     */
    private String condition = null;

    /**
     * Constructs a new uninitialized instance.
     */
    public ExecutableFile()
    {
        this.path = null;
        executionStage = NEVER;
        mainClass = null;
        type = BIN;
        onFailure = ASK;
        osList = new ArrayList<OsConstraint>();
        argList = new ArrayList<String>();
        keepFile = false;
    }

    /**
     * Constructs and initializes a new instance.
     *
     * @param path           the file path
     * @param executionStage when to execute
     * @param onFailure      what to do if execution fails
     * @param osList         list of operating systems to run on
     */
    public ExecutableFile(String path, int executionStage, int onFailure, List<OsConstraint> osList,
                          boolean keepFile)
    {
        this.path = path;
        this.executionStage = executionStage;
        this.onFailure = onFailure;
        this.osList = osList;
        this.keepFile = keepFile;
    }

    public ExecutableFile(String path, int type, String mainClass, int executionStage,
                          int onFailure, List<String> argList, List<OsConstraint> osList, boolean keepFile)
    {
        this.path = path;
        this.mainClass = mainClass;
        this.type = type;
        this.executionStage = executionStage;
        this.onFailure = onFailure;
        this.argList = argList;
        this.osList = osList;
        this.keepFile = keepFile;
    }

    public String toString()
    {
        StringBuffer retval = new StringBuffer();
        retval.append("path = ").append(path);
        retval.append("\n");
        retval.append("mainClass = ").append(mainClass);
        retval.append("\n");
        retval.append("type = ").append(type);
        retval.append("\n");
        retval.append("executionStage = ").append(executionStage);
        retval.append("\n");
        retval.append("onFailure = ").append(onFailure);
        retval.append("\n");
        retval.append("argList: ").append(argList);
        retval.append("\n");
        if (argList != null)
        {
            for (String anArgList : argList)
            {
                retval.append("\targ: ").append(anArgList);
                retval.append("\n");
            }
        }
        retval.append("\n");
        retval.append("osList = ").append(osList);
        retval.append("\n");
        if (osList != null)
        {
            for (OsConstraint anOsList : osList)
            {
                retval.append("\tos: ").append(anOsList);
                retval.append("\n");
            }
        }
        retval.append("keepFile = ").append(keepFile);
        retval.append("\n");
        return retval.toString();
    }


    /**
     * @return the condition
     */
    public String getCondition()
    {
        return this.condition;
    }


    /**
     * @param condition the condition to set
     */
    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public boolean hasCondition()
    {
        return this.condition != null;
    }
}

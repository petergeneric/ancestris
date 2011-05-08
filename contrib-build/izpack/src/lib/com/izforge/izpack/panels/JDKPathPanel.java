/*
 * $Id: JDKPathPanel.java 2784 2009-05-29 16:45:56Z jponge $
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2004 Klaus Bartz
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

package com.izforge.izpack.panels;

import com.coi.tools.os.win.MSWinConstants;
import com.coi.tools.os.win.NativeLibException;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.os.RegistryDefaultHandler;
import com.izforge.izpack.util.os.RegistryHandler;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Panel which asks for the JDK path.
 *
 * @author Klaus Bartz
 */
public class JDKPathPanel extends PathInputPanel
{

    private static final long serialVersionUID = 3257006553327810104L;

    public static final String[] testFiles = new String[]{"lib" + File.separator + "tools.jar"};

    public static final String JDK_ROOT_KEY = "Software\\JavaSoft\\Java Development Kit";

    public static final String JDK_VALUE_NAME = "JavaHome";

    public static final String OSX_JDK_HOME = "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home/";

    private static final int OK = 0;
    private static final int BAD_VERSION = 1;
    private static final int BAD_REAL_PATH = 2;
    private static final int BAD_REG_PATH = 3;

    private String detectedVersion;

    private String minVersion = null;

    private String maxVersion = null;

    private String variableName;

    private Set<String> badRegEntries = null;


    /**
     * The constructor.
     *
     * @param parent The parent window.
     * @param idata  The installation data.
     */
    public JDKPathPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);
        setMustExist(true);
        if (!OsVersion.IS_OSX)
        {
            setExistFiles(JDKPathPanel.testFiles);
        }
        setMinVersion(idata.getVariable("JDKPathPanel.minVersion"));
        setMaxVersion(idata.getVariable("JDKPathPanel.maxVersion"));
        setVariableName("JDKPath");
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return Wether the panel has been validated or not.
     */
    public boolean isValidated()
    {
        boolean retval = false;
        if (super.isValidated())
        {
            switch (verifyVersionEx())
            {
                case OK:
                    idata.setVariable(getVariableName(), pathSelectionPanel.getPath());
                    retval = true;
                    break;
                case BAD_REG_PATH:
                    if (askQuestion(parent.langpack.getString("installer.warning"), parent.langpack.getString("JDKPathPanel.nonValidPathInReg"),
                            AbstractUIHandler.CHOICES_YES_NO, AbstractUIHandler.ANSWER_NO) == AbstractUIHandler.ANSWER_YES)
                    {
                        idata.setVariable(getVariableName(), pathSelectionPanel.getPath());
                        retval = true;
                    }
                    break;
                case BAD_REAL_PATH:
                    break;
                case BAD_VERSION:
                    String min = getMinVersion();
                    String max = getMaxVersion();
                    StringBuffer message = new StringBuffer();
                    message.append(parent.langpack.getString("JDKPathPanel.badVersion1")).append(
                            getDetectedVersion()).append(
                            parent.langpack.getString("JDKPathPanel.badVersion2"));
                    if (min != null && max != null)
                    {
                        message.append(min).append(" - ").append(max);
                    }
                    else if (min != null)
                    {
                        message.append(" >= ").append(min);
                    }
                    else if (max != null)
                    {
                        message.append(" <= ").append(max);
                    }

                    message.append(parent.langpack.getString("JDKPathPanel.badVersion3"));
                    if (askQuestion(parent.langpack.getString("installer.warning"), message.toString(),
                            AbstractUIHandler.CHOICES_YES_NO, AbstractUIHandler.ANSWER_NO) == AbstractUIHandler.ANSWER_YES)
                    {
                        idata.setVariable(getVariableName(), pathSelectionPanel.getPath());
                        retval = true;
                    }
                    break;
                default:
                    throw new RuntimeException("Internal error: unknown result of version verification.");

            }
        }
        return (retval);
    }

    /**
     * Called when the panel becomes active.
     */
    public void panelActivate()
    {
        // Resolve the default for chosenPath
        super.panelActivate();
        String chosenPath;
        // The variable will be exist if we enter this panel
        // second time. We would maintain the previos
        // selected path.
        if (idata.getVariable(getVariableName()) != null)
        {
            chosenPath = idata.getVariable(getVariableName());
        }
        else
        {
            if (OsVersion.IS_OSX)
            {
                chosenPath = OSX_JDK_HOME;
            }
            else
            {
                // Try the JAVA_HOME as child dir of the jdk path
                chosenPath = (new File(idata.getVariable("JAVA_HOME"))).getParent();
            }
        }
        // Set the path for method pathIsValid ...
        pathSelectionPanel.setPath(chosenPath);

        if (!pathIsValid() || !verifyVersion())
        {
            chosenPath = resolveInRegistry();
            if (!pathIsValid() || !verifyVersion())
            {
                chosenPath = "";
            }
        }
        // Set the default to the path selection panel.
        pathSelectionPanel.setPath(chosenPath);
        String var = idata.getVariable("JDKPathPanel.skipIfValid");
        // Should we skip this panel?
        if (chosenPath.length() > 0 && var != null && "yes".equalsIgnoreCase(var))
        {
            idata.setVariable(getVariableName(), chosenPath);
            parent.skipPanel();
        }

    }

    /**
     * Returns the path to the needed JDK if found in the registry. If there are more than one JDKs
     * registered, that one with the highest allowd version will be returned. Works only on windows.
     * On Unix an empty string returns.
     *
     * @return the path to the needed JDK if found in the windows registry
     */
    private String resolveInRegistry()
    {
        String retval = "";
        int oldVal = 0;
        RegistryHandler rh = null;
        badRegEntries = new HashSet<String>();
        try
        {
            // Get the default registry handler.
            rh = RegistryDefaultHandler.getInstance();
            if (rh == null)
            // We are on a os which has no registry or the
            // needed dll was not bound to this installation. In
            // both cases we forget the try to get the JDK path from registry.
            {
                return (retval);
            }
            rh.verify(idata);
            oldVal = rh.getRoot(); // Only for security...
            rh.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);
            String[] keys = rh.getSubkeys(JDK_ROOT_KEY);
            if (keys == null || keys.length == 0)
            {
                return (retval);
            }
            Arrays.sort(keys);
            int i = keys.length - 1;
            String min = getMinVersion();
            String max = getMaxVersion();
            // We search for the highest allowd version, therefore retrograde
            while (i > 0)
            {
                if (compareVersions(keys[i], max, false, 4, 4, "__NO_NOT_IDENTIFIER_"))
                { // First allowd version found, now we have to test that the min value
                    // also allows this version.
                    if (compareVersions(keys[i], min, true, 4, 4, "__NO_NOT_IDENTIFIER_"))
                    {
                        String cv = JDK_ROOT_KEY + "\\" + keys[i];
                        String path = rh.getValue(cv, JDK_VALUE_NAME).getStringData();
                        // Use it only if the path is valid.
                        // Set the path for method pathIsValid ...
                        pathSelectionPanel.setPath(path);
                        if (!pathIsValid())
                        {
                            badRegEntries.add(keys[i]);
                        }
                        else if ("".equals(retval))
                        {
                            retval = path;
                        }
                        pathSelectionPanel.setPath(retval);
                    }
                }
                i--;
            }
        }
        catch (Exception e)
        { // Will only be happen if registry handler is good, but an
            // exception at performing was thrown. This is an error...
            e.printStackTrace();
        }
        finally
        {
            if (rh != null && oldVal != 0)
            {
                try
                {
                    rh.setRoot(MSWinConstants.HKEY_LOCAL_MACHINE);
                }
                catch (NativeLibException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return (retval);
    }

    private int verifyVersionEx()
    {
        String min = getMinVersion();
        String max = getMaxVersion();
        int retval = OK;
        // No min and max, version always ok.
        if (min == null && max == null)
        {
            return (OK);
        }

        if (!pathIsValid())
        {
            return (BAD_REAL_PATH);
        }
        // No get the version ...
        // We cannot look to the version of this vm because we should
        // test the given JDK VM.
        String[] params;
        if ( System.getProperty("os.name").indexOf("Windows") >= 0 ) {
            String[] paramsp = {
                "cmd",
                "/c",
                pathSelectionPanel.getPath() + File.separator + "bin" + File.separator + "java",
                "-version"
            };
            params=paramsp;
        } else {
            String[] paramsp = {
                pathSelectionPanel.getPath() + File.separator + "bin" + File.separator + "java",
                "-version"
            };
            params=paramsp;
        }
        String[] output = new String[2];
        FileExecutor fe = new FileExecutor();
        fe.executeCommand(params, output);
        // "My" VM writes the version on stderr :-(
        String vs = (output[0].length() > 0) ? output[0] : output[1];
        if (min != null)
        {
            if (!compareVersions(vs, min, true, 4, 4, "__NO_NOT_IDENTIFIER_"))
            {
                retval = BAD_VERSION;
            }
        }
        if (max != null)
        {
            if (!compareVersions(vs, max, false, 4, 4, "__NO_NOT_IDENTIFIER_"))
            {
                retval = BAD_VERSION;
            }
        }
        if (retval == OK && badRegEntries != null && badRegEntries.size() > 0)
        {   // Test for bad registry entry.
            if (badRegEntries.contains(getDetectedVersion()))
            {
                retval = BAD_REG_PATH;
            }
        }
        return (retval);

    }

    private boolean verifyVersion()
    {
        return (verifyVersionEx() <= 0);
    }

    private boolean compareVersions(String in, String template, boolean isMin,
            int assumedPlace, int halfRange, String useNotIdentifier)
            {
        StringTokenizer st = new StringTokenizer(in, " \t\n\r\f\"");
        int i;
        int currentRange = 0;
        String[] interestedEntries = new String[halfRange + halfRange];
        for (i = 0; i < assumedPlace - halfRange; ++i)
        {
            if (st.hasMoreTokens())
            {
                st.nextToken(); // Forget this entries.
            }
        }

        for (i = 0; i < halfRange + halfRange; ++i)
        { // Put the interesting Strings into an intermediaer array.
            if (st.hasMoreTokens())
            {
                interestedEntries[i] = st.nextToken();
                currentRange++;
            }
        }

        for (i = 0; i < currentRange; ++i)
        {
            if (useNotIdentifier != null && interestedEntries[i].indexOf(useNotIdentifier) > -1)
            {
                continue;
            }
            if (Character.getType(interestedEntries[i].charAt(0)) != Character.DECIMAL_DIGIT_NUMBER)
            {
                continue;
            }
            break;
        }
        if (i == currentRange)
        {
            detectedVersion = "<not found>";
            return (false);
        }
        detectedVersion = interestedEntries[i];
        StringTokenizer current = new StringTokenizer(interestedEntries[i], "._-");
        StringTokenizer needed = new StringTokenizer(template, "._-");
        while (needed.hasMoreTokens())
        {
            // Current can have no more tokens if needed has more
            // and if a privious token was not accepted as good version.
            // e.g. 1.4.2_02 needed, 1.4.2 current. The false return
            // will be right here. Only if e.g. needed is 1.4.2_00 the
            // return value will be false, but zero should not b e used
            // at the last version part.
            if (!current.hasMoreTokens())
            {
                return (false);
            }
            String cur = current.nextToken();
            String nee = needed.nextToken();
            int curVal = 0;
            int neededVal = 0;
            try
            {
                curVal = Integer.parseInt(cur);
                neededVal = Integer.parseInt(nee);
            }
            catch (NumberFormatException nfe)
            { // A number format exception will be raised if
                // there is a non numeric part in the version,
                // e.g. 1.5.0_beta. The verification runs only into
                // this deep area of version number (fourth sub place)
                // if all other are equal to the given limit. Then
                // it is right to return false because e.g.
                // the minimal needed version will be 1.5.0.2.
                return (false);
            }
            if (curVal < neededVal)
            {
                if (isMin)
                {
                    return (false);
                }
                return (true);
            }
            if (curVal > neededVal)
            {
                if (isMin)
                {
                    return (true);
                }
                return (false);
            }
        }
        return (true);
    }

    /**
     * Returns the current detected version.
     *
     * @return the current detected version
     */
    public String getDetectedVersion()
    {
        return detectedVersion;
    }

    /**
     * Returns the current used maximum version.
     *
     * @return the current used maximum version
     */
    public String getMaxVersion()
    {
        return maxVersion;
    }

    /**
     * Returns the current used minimum version.
     *
     * @return the current used minimum version
     */
    public String getMinVersion()
    {
        return minVersion;
    }

    /**
     * Sets the given value as current detected version.
     *
     * @param string version string to be used as detected version
     */
    protected void setDetectedVersion(String string)
    {
        detectedVersion = string;
    }

    /**
     * Sets the given value as maximum for version control.
     *
     * @param string version string to be used as maximum
     */
    protected void setMaxVersion(String string)
    {
        if (string != null && string.length() > 0)
        {
            maxVersion = string;
        }
        else
        {
            maxVersion = "99.0.0";
        }
    }

    /**
     * Sets the given value as minimum for version control.
     *
     * @param string version string to be used as minimum
     */
    protected void setMinVersion(String string)
    {
        if (string != null && string.length() > 0)
        {
            minVersion = string;
        }
        else
        {
            minVersion = "1.0.0";
        }
    }

    /**
     * Returns the name of the variable which should be used for the path.
     *
     * @return the name of the variable which should be used for the path
     */
    public String getVariableName()
    {
        return variableName;
    }

    /**
     * Sets the name for the variable which should be set with the path.
     *
     * @param string variable name to be used
     */
    public void setVariableName(String string)
    {
        variableName = string;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
     */
    public String getSummaryBody()
    {
        return (idata.getVariable(getVariableName()));
    }
}

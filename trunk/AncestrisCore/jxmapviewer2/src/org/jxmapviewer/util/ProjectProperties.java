package org.jxmapviewer.util;

/**
 * Project properties.
 *
 * @author Primoz K.
 */
public enum ProjectProperties {

    /**
     * The only instance of this class
     */
    INSTANCE;

    private ProjectProperties() {
    }
    /***************************************************************
     ********************* PROPERTIES GETTERS **********************
     ***************************************************************/

    /**
     * @return Project version.
     */
    public String getVersion() {
        return "11.0";
    }

    /**
     * @return Project name.
     */
    public String getName() {
        return "Ancestris";
    }

}

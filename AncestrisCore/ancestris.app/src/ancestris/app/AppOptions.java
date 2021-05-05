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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/*
 * XXX:This class has been moved from ancestris.app to core but the package is still ancestris.app
 * this is to help keeping preferences set from ancestris version 0.6 valid for upcomming version 0.7.
 * this will be refactore for version 0.8
 */
package ancestris.app;

import genj.util.AncestrisPreferences;
import genj.util.Registry;


/**
 * Application options
 */
public class AppOptions {

    /** singleton */
        //XXX: preference path must be defined in core options namespace
    private final static AncestrisPreferences APP_OPTIONS = Registry.get(AppOptions.class);

    /**
     * Instance access
     */
    public static AncestrisPreferences getInstance() {
        return APP_OPTIONS;
    }

    /**
     * Getter - maximum log size
     */
    public static int getMaxLogSizeKB() {
        int size = APP_OPTIONS.get("maxLogSizeKB", 2048);
        if (size > 16384) {
            return 16384;
        }
        return size;
    }

    /**
     * Setter - maximum log size
     */
    public static void setMaxLogSizeKB(int set) {
        if (set > 16384) {
            set = 16384;
        }
        APP_OPTIONS.put("maxLogSizeKB", Math.max(128, set));
    }

    public static boolean isWriteBOM() {
        return APP_OPTIONS.get("isWriteBOM", true);
    }

    public static void setWriteBOM(boolean isWriteBOM) {
        APP_OPTIONS.put("isWriteBOM", isWriteBOM);
    }

    public static boolean isRestoreViews() {
        return APP_OPTIONS.get("isRestoreViews", true);
    }

    public static void setRestoreViews(boolean isRestoreViews) {
        APP_OPTIONS.put("isRestoreViews", isRestoreViews);
    }

    public static String getFontSize() {
        return APP_OPTIONS.get("fontsize", "12");
    }

    public static void setFontSize(String fontsize) {
        APP_OPTIONS.put("fontsize", fontsize);
    }

} 

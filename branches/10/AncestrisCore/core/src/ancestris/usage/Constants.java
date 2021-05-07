/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.usage;

/**
 *
 * @author frederic
 */
public interface Constants {

    static String COMM_SERVER = "serv01.ancestris.org";
    static String COMM_PROTOCOL = "http://";
    static String COMM_CREDENTIALS = "user=ancestrishare&pw=2fQB";

    static String CMD_PUT = "/usagelogPut.php?";
    static String PARAM_ID = "ID";
    static String PARAM_ACTION = "ACT";
    static String ACTION_ON = "ON";
    static String ACTION_OFF = "OFF";
    static String ACTION_SAVE = "SAVE";
    static String PARAM_VERSION = "VER";
    static String PARAM_OS = "OS";
    static int MAX_LENGTH = 30;

    static String CMD_GET = "/usagelogGet.php?";
    static String PARAM_TYPE = "TYPE";
    static String PARAM_VALUE = "VALUE";
    static String TAG_LINE = "l";
    static String TAG_PERIOD = "p";
    static String TAG_VALUE = "v";

    static String CSTMP = "FC7E90DB1F7AH2H3";
    
}

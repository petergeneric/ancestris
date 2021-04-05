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

import static ancestris.usage.Constants.CMD_REGKEY;
import static ancestris.usage.Constants.COMM_PROTOCOL;
import static ancestris.usage.Constants.COMM_SERVER;
import static ancestris.usage.Constants.CSTMP;
import static ancestris.usage.Constants.PARAM_ID;
import static ancestris.usage.Constants.PARAM_KEY;
import static ancestris.usage.Constants.PARAM_OS;
import static ancestris.usage.Constants.PARAM_VERSION;
import genj.util.EnvironmentChecker;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Module to manage usage log
 *
 * @author frederic
 */
public class UsageManager implements Constants {
    
    private static String id = "";
    private static String version = "";
    private static String OS = "";

   private static void setActiveUsage() {
        // Active login
        id = truncate(setField(EnvironmentChecker.getProperty("user.home.ancestris", "", ""), CSTMP));
        version = truncate(EnvironmentChecker.getAncestrisVersion());
        OS = truncate(EnvironmentChecker.getProperty("os.name", "", "") + " " + EnvironmentChecker.getProperty("os.version", "", ""));
    }

    public static boolean writeUsage(String action) {
        if (id.isEmpty()) {
            setActiveUsage();
        }
        String key1 = "&" + PARAM_ID + "=" + id;
        String key2 = "&" + PARAM_ACTION + "=" + action;
        String key3 = "&" + PARAM_VERSION + "=" + version;
        String key4 = "&" + PARAM_OS + "=" + OS;
        String outputString = query(COMM_PROTOCOL + COMM_SERVER + CMD_PUT + COMM_CREDENTIALS + key1 + key2 + key3 + key4);
        return (outputString.isEmpty());
    }

    public static List<UsageDataSet> getPeriodUsage() {

        List<UsageDataSet> ret = new ArrayList<UsageDataSet>();

        String outputString = query(COMM_PROTOCOL + COMM_SERVER + CMD_PUT + COMM_CREDENTIALS);
        if (outputString.isEmpty()) {
            return ret;
        }

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(outputString));
            Document doc = db.parse(is);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName(TAG_LINE);

            // Collect list of Ancestris friends (registered name and access details)
            for (int temp = 0; temp < nodeList.getLength(); temp++) {
                Node node = nodeList.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element member = (Element) node;
                    String period = member.getElementsByTagName(TAG_PERIOD).item(0).getTextContent();
                    String value = member.getElementsByTagName(TAG_VALUE).item(0).getTextContent();
                    ret.add(new UsageDataSet(period, value));
                }
            }

        } catch (Exception ex) {
            //Exceptions.printStackTrace(ex);
            return ret;
        }

        return ret;
    }
    
    public static String getKey(String key) {
        if (id.isEmpty() || version.isEmpty() || OS.isEmpty()) {
            return null;
        }

        String ret = "";
        String key1 = "&" + PARAM_ID + "=" + id;
        String key2 = "&" + PARAM_VERSION + "=" + version;
        String key3 = "&" + PARAM_OS + "=" + OS;
        String key4 = "&" + PARAM_KEY + "=" + key;
        String outputString = query(COMM_PROTOCOL + COMM_SERVER + CMD_REGKEY + key1 + key2 + key3 + key4);

        // outputString looks like : 
        // <register>
        //      <key>
        //          <value1>this is value1</value1>
        //          <value2>this is value 2</value2>
        //      </key>
        // </register>
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(outputString));
            Document doc = db.parse(is);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName(key);
            Element node = (Element) nodeList.item(0);
            ret += StringEscapeUtils.unescapeHtml(node.getElementsByTagName("value1").item(0).getTextContent());
            ret += ":";
            ret += StringEscapeUtils.unescapeHtml(node.getElementsByTagName("value2").item(0).getTextContent());

        } catch (Exception ex) {
            //Exceptions.printStackTrace(ex);
        }

        return ret;
    }

    private static String query(String url) {

        String ret = "";

        try {
            String responseString = "";
            URL data = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) data.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((responseString = in.readLine()) != null) {
                ret = ret + responseString;
            }
            in.close();
            connection.disconnect();
        } catch (Exception ex) {
            ret = "";
        }

        return ret;
    }

    private static String truncate(String text) {
        if (text == null) {
            return text;
        }
        return text.substring(0, Math.min(text.length(), MAX_LENGTH)).replace(" ", "_");
    }

    private static String setField(String text, String cst) {
        try {
            if (text == null || cst == null) {
                return null;
            }

            char[] keys = cst.toCharArray();
            char[] mesg = text.toCharArray();

            int ml = mesg.length;
            int kl = keys.length;
            char[] newmsg = new char[ml];

            for (int i = 0; i < ml; i++) {
                char c = (char) (mesg[i] ^ keys[i % kl]);
                newmsg[i] = toHex(c % 16);
            }

            return new String(newmsg);
        } catch (Exception e) {
            return "UNK";
        }
    }

    private static char toHex(int nybble) {
        if (nybble < 0 || nybble > 15) {
            throw new IllegalArgumentException();
        }
        return "0123456789ABCDEF".charAt(nybble);
    }

}

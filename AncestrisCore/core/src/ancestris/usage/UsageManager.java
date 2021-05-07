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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Module to manage usage log
 *
 * @author frederic
 */
public class UsageManager implements Constants {

    public static boolean writeUsage(String id, String action, String version, String OS) {
        String key1 = "&" + PARAM_ID + "=" + truncate(setField(id, CSTMP));
        String key2 = "&" + PARAM_ACTION + "=" + action;
        String key3 = "&" + PARAM_VERSION + "=" + truncate(version);
        String key4 = "&" + PARAM_OS + "=" + truncate(OS);
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

    private static String query(String url) {

        String ret = "";

        try {
            String responseString = "";
            URL data = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) data.openConnection();
			connection.setConnectTimeout(5000);
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

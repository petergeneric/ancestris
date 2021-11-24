/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dominique
 */
public class ResourceParser {

    private static final Logger logger = Logger.getLogger(ResourceParser.class.getName());
    /** PropertiesFileEntry for which source is this parser created. */
    InputStream inputStream;
    /** Properties file reader. Input stream. */
    ResourceReader resourceReader;
    /** Flag if parsing should be stopped. */
    private boolean stop = false;

    /**
     * Creates parser. Has to be {@link init} afterwards.
     * @param pfe FileEntry where the properties file is stored.
     */
    public ResourceParser(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /** Inits parser.
     * @exception IOException if any i/o problem occured during reading */
    void initParser() throws IOException {
        resourceReader = createReader();
    }

    /** Creates new input stream from the file object.
     * Finds the properties data object, checks if the document is loaded,
     * if not is loaded and created a stream from the document.
     * @exception IOException if any i/o problem occured during reading
     */
    private ResourceReader createReader() throws IOException {
        return new ResourceReader(new InputStreamReader(inputStream));
    }

    /** Parses .properties file specified by <code>pfe</code> and resets its properties
     * structure.
     * @return new properties structure or null if parsing failed
     */
    public ResourceStructure parseFile() {
        try {
            ResourceStructure resourceStructure = parseFileMain();
            return resourceStructure;
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, null, ioe);
            return null;
        }
    }

    /** Stops parsing. */
    public void stop() {
        stop = true;
        clean();
    }

    /** Provides clean up after finish parsing. */
    public void clean() {
        if (resourceReader != null) {
            try {
                resourceReader.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            resourceReader = null;
        }
    }

    /** Parses .properties file and creates <code>PropertiesStruture</code>. */
    private ResourceStructure parseFileMain() throws IOException {

        LinkedHashMap<String, ResourceItem.ResourceLine> resourceLines = new LinkedHashMap<String, ResourceItem.ResourceLine>(25, 1.0F);

        ResourceReader reader = null;

        while (true) {
            if (stop) {
                // Parsing stopped -> return immediatelly.
                return null;
            }

            reader = resourceReader;
            if (reader == null) {
                // Parsing was stopped.
                return null;
            }
            ResourceItem.ResourceLine element = readNextElem(reader);

            if (element == null) {
                break;
            } else {
                // add at the end of the list
                resourceLines.put(element.getKey(), element);
            }
        }

        return new ResourceStructure(resourceLines);
    }

    /**
     * Reads next element from input stream.
     * @return next element or null if the end of the stream occurred */
    private ResourceItem.ResourceLine readNextElem(ResourceReader in) throws IOException {
        ResourceItem.PropertyComment commE = null;
        ResourceItem.PropertyKey keyE = null;
        ResourceItem.PropertyValue valueE = null;

        int begPos = in.position;

        // read the comment
        int keyPos = begPos;
        FlaggedLine fl = in.readLineExpectComment();
        StringBuilder comment = new StringBuilder();
        boolean firstNull = true;
        while (fl != null) {
            firstNull = false;
            if (fl.flag) {
                //part of the comment
                comment.append(trimComment(fl.line));
                comment.append(fl.lineSep);
                keyPos = in.position;
            } else { // not a part of a comment
                break;
            }
            fl = in.readLineExpectComment();
        }

        // exit completely if null is returned the very first time
        if (firstNull) {
            return null;
        }

        String comHelp;
        comHelp = comment.toString();
        if (comment.length() > 0) {
            if (comment.charAt(comment.length() - 1) == '\n') {
                comHelp = comment.substring(0, comment.length() - 1);
            }

            commE = new ResourceItem.PropertyComment(UtilConvert.loadConvert(comHelp));
            // fl now contains the line after the comment or  null if none exists
        }

        if (fl != null) {
            // read the key and the value
            // list of
            ArrayList<FlaggedLine> lines = new ArrayList<FlaggedLine>(2);
            fl.startPosition = keyPos;
            fl.stringValue = fl.line.toString();
            lines.add(fl);
            int nowPos;
            while (isPartialLine(fl.line)) {
                // do something with the previous line
                fl.stringValue = fl.stringValue.substring(0, fl.stringValue/*fix: was: line*/.length() - 1);
                // now the new line
                nowPos = in.position;
                fl = in.readLineNoFrills();
                if (fl == null) {
                    break;
                }
                // delete the leading whitespaces
                int startIndex = 0;
                for (startIndex = 0; startIndex < fl.line.length(); startIndex++) {
                    if (UtilConvert.whiteSpaceChars.indexOf(fl.line.charAt(startIndex)) == -1) {
                        break;
                    }
                }
                fl.stringValue = fl.line.substring(startIndex, fl.line.length());
                fl.startPosition = nowPos + startIndex;
                lines.add(fl);
            }
            // now I have an ArrayList with strings representing lines and positions of the first non-whitespace character

            PositionMap positionMap = new PositionMap(lines);
            String line = positionMap.getString();

            // Find start of key
            int len = line.length();
            int keyStart;
            for (keyStart = 0; keyStart < len; keyStart++) {
                if (UtilConvert.whiteSpaceChars.indexOf(line.charAt(keyStart)) == -1) {
                    break;
                }
            }

            // Find separation between key and value
            int separatorIndex;
            for (separatorIndex = keyStart; separatorIndex < len; separatorIndex++) {
                char currentChar = line.charAt(separatorIndex);
                if (currentChar == '\\') {
                    separatorIndex++;
                } else if (UtilConvert.keyValueSeparators.indexOf(currentChar) != -1) {
                    break;
                }
            }

            // Skip over whitespace after key if any
            int valueIndex;
            for (valueIndex = separatorIndex; valueIndex < len; valueIndex++) {
                if (UtilConvert.whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1) {
                    break;
                }
            }

            // Skip over one non whitespace key value separators if any
            if (valueIndex < len) {
                if (UtilConvert.strictKeyValueSeparators.indexOf(line.charAt(valueIndex)) != -1) {
                    valueIndex++;
                }
            }

            // Skip over white space after other separators if any
            while (valueIndex < len) {
                if (UtilConvert.whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1) {
                    break;
                }
                valueIndex++;
            }
            String key = line.substring(keyStart, separatorIndex);
            String value = (separatorIndex < len) ? line.substring(valueIndex, len) : ""; // NOI18N

//            if (key == null) // PENDING - should join with the next comment
//            ;

            if (key.equals("OpenIDE-Module-Name") && value.equals("Age Calculation")) {
                String debug = "";
            }

            int currentPos = in.position;
            int valuePosFile = 0;

            try {
                valuePosFile = positionMap.getFilePosition(valueIndex);
            } catch (ArrayIndexOutOfBoundsException e) {
                valuePosFile = currentPos;
            }

            keyE = new ResourceItem.PropertyKey(UtilConvert.loadConvert(key));
            valueE = new ResourceItem.PropertyValue(UtilConvert.loadConvert(value));
        }

        return ((keyE == null || valueE == null) ? null : new ResourceItem.ResourceLine(keyE, valueE, commE));
    }

    /** Remove leading comment markers. */
    private StringBuffer trimComment(StringBuffer line) {
        while (line.length() > 0) {
            char lead = line.charAt(0);
            if (lead == '#' || lead == '!') {
                line.deleteCharAt(0);
            } else {
                break;
            }
        }
        return line;
    }

    /** Utility method. Computes the real offset from the long value representing position in the parser.
     * @return the offset
     */
    private static int position(long p) {
        return (int) (p & 0xFFFFFFFFL);
    }

    /**
     * Properties reader which allows reading from an input stream or from a string and remembers
     * its position in the document.
     */
    private static class ResourceReader extends BufferedReader {

        /** Name constant of line separator system property. */
        private static final String LINE_SEPARATOR = "line.separator"; // NOI18N
        /** The character that someone peeked. */
        private int peekChar = -1;
        /** Position after the last character read. */
        public int position = 0;

        /** Creates <code>PropertiesReader</code> from buffer. */
        private ResourceReader(String buffer) {
            super(new StringReader(buffer));
        }

        /** Creates <code>PropertiesReader</code> from another reader. */
        private ResourceReader(Reader reader) {
            super(reader);
        }

        /** Read one character from the stream and increases the position.
         * @return the character or -1 if the end of the stream has been reached
         */
        @Override
        public int read() throws IOException {
            int character = peek();
            peekChar = -1;
            if (character != -1) {
                position++;
            }

            return character;
        }

        /** Returns the next character without increasing the position. Subsequent calls
         * to peek() and read() will return the same character.
         * @return the character or -1 if the end of the stream has been reached
         */
        private int peek() throws IOException {
            if (peekChar == -1) {
                peekChar = super.read();
            }

            return peekChar;
        }

        /** Reads the next line and returns the flag as true if the line is a comment line.
         *  If the input is empty returns null
         *  Flag in the result is true if the line is a comment line
         */
        public FlaggedLine readLineExpectComment() throws IOException {
            int charRead = read();
            if (charRead == -1) { // end of the reader reached

                return null;
            }

            boolean decided = false;
            FlaggedLine fl = new FlaggedLine();
            while (charRead != -1 && charRead != (int) '\n' && charRead != (int) '\r') {
                if (!decided) {
                    if (UtilConvert.whiteSpaceChars.indexOf((char) charRead) == -1) {
                        // not a whitespace - decide now
                        fl.flag = (((char) charRead == '!') || ((char) charRead == '#'));
                        decided = true;
                    }
                }
                fl.line.append((char) charRead);
                charRead = read();
            }

            if (!decided) { // all were whitespaces
                fl.flag = true;
            }

            // set the line separator
            if (charRead == (int) '\r') {
                if (peek() == (int) '\n') {
                    charRead = read();
                    fl.lineSep = "\r\n"; // NOI18N
                } else {
                    fl.lineSep = "\r"; // NOI18N
                }
            } else if (charRead == (int) '\n') {
                fl.lineSep = "\n"; // NOI18N
            } else {
                fl.lineSep = System.getProperty(LINE_SEPARATOR);
            }

            return fl;
        }

        /** Reads the next line.
         * @return <code>FlaggedLine</code> or null if the input is empty */
        public FlaggedLine readLineNoFrills() throws IOException {
            int charRead = read();
            if (charRead == -1) // end of the reader reached
            {
                return null;
            }

            FlaggedLine fl = new FlaggedLine();
            while (charRead != -1 && charRead != (int) '\n' && charRead != (int) '\r') {
                fl.line.append((char) charRead);
                charRead = read();
            }

            // set the line separator
            if (charRead == (int) '\r') {
                if (peek() == (int) '\n') {
                    charRead = read();
                    fl.lineSep = "\r\n"; // NOI18N
                } else {
                    fl.lineSep = "\r"; // NOI18N
                }
            } else if (charRead == (int) '\n') { // NOI18N
                fl.lineSep = "\n"; // NOI18N
            } else {
                fl.lineSep = System.getProperty(LINE_SEPARATOR);
            }

            return fl;
        }
    } // End of nested class PropertiesReader.

    /**
     * Returns true if the given line is a line that must
     * be appended to the next line
     */
    private static boolean isPartialLine(StringBuffer line) {
        int slashCount = 0;
        int index = line.length() - 1;
        while ((index >= 0) && (line.charAt(index--) == '\\')) {
            slashCount++;
        }
        return (slashCount % 2 == 1);
    }

    /** Nested class which maps positions in a string to positions in the underlying file.
     * @see FlaggedLine */
    private static class PositionMap {

        /** List of <code>FlaggedLine</code>'s. */
        private List<FlaggedLine> list;

        /** Constructor - expects a list of FlaggedLine */
        PositionMap(List<FlaggedLine> lines) {
            list = lines;
        }

        /** Returns the string represented by the object */
        public String getString() {
            String allLines = list.get(0).stringValue;
            for (int part = 1; part < list.size(); part++) {
                allLines += list.get(part).stringValue;
            }
            return allLines;
        }

        /** Returns position in the file for a position in a string
         * @param posString position in the string to find file position for
         * @return position in the file
         * @exception ArrayIndexOutOfBoundsException if the requested position is outside
         * the area represented by this object
         */
        public int getFilePosition(int posString) throws ArrayIndexOutOfBoundsException {
            // get the part
            int part;
            int lengthSoFar = 0;
            int lastLengthSoFar = 0;
            for (part = 0; part < list.size(); part++) {
                lastLengthSoFar = lengthSoFar;
                lengthSoFar += list.get(part).stringValue.length();
                // brute patch - last (cr)lf should not be the part of the thing, other should
                if (part == list.size() - 1) {
                    if (lengthSoFar >= posString) {
                        break;
                    }
                } else {
                    if (lengthSoFar > posString) {
                        break;
                    }
                }
            }
            if (posString > lengthSoFar) {
                throw new ArrayIndexOutOfBoundsException("not in scope"); // NOI18N
            }
            return list.get(part).startPosition + posString - lastLengthSoFar;
        }
    } // End of nested class PositionMap.

    /** Helper nested class. */
    private static class FlaggedLine {

        /** Line buffer. */
        StringBuffer line;
        /** Flag. */
        boolean flag;
        /** Line separator. */
        String lineSep;
        /** Start position. */
        int startPosition;
        /** Value. */
        String stringValue;

        /** Constructor. */
        FlaggedLine() {
            line = new StringBuffer();
            flag = false;
            lineSep = "\n"; // NOI18N
            startPosition = 0;
        }
    } // End of nested class FlaggedLine.
}

/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2017 Ancestris
 * 
 * Author: Daniel André (daniel@ancestris.org) & Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 *
 * @author Daniel & Frederic
 */
public class GedcomFileWriter extends BufferedWriter {

    String EOL = System.getProperty("line.separator");
    private int levelShift = 0;
    private int shiftedLevel = -1;

    public GedcomFileWriter(File fileOut, Charset charset, String eol) throws UnsupportedEncodingException, FileNotFoundException {
        super(new OutputStreamWriter(new FileOutputStream(fileOut), charset));
        EOL = eol;
    }

    public String writeLine(int level, String tag, String value) throws IOException {
        return writeLine(level, null, tag, value);
    }

    public String writeLine(GedcomFileReader input) throws IOException {
        return writeLine(input.getLevel(), input.getXref(), input.getTag(),
                input.getValue());
    }

    public String writeLine(int level, String xref, String tag, String value)
            throws IOException {

        if (level <= shiftedLevel) {
            shiftedLevel = -1;
            levelShift = 0;
        }
        String result = Integer.toString(level + levelShift) + " ";

        if (xref != null && xref.length() > 0) {
            result += "@" + xref + "@ ";
        }
        result += tag;

        // Value
        if (value != null && value.length() > 0) {
            result += " " + value;
        }
        write(result + EOL);
        return result;
    }

    public String shiftLine(GedcomFileReader input) throws IOException {
        return shiftLine(input.getLevel(), input.getXref(), input.getTag(),
                input.getValue());
    }

    public String shiftLine(int level, String xref, String tag, String value) throws IOException {
        if (levelShift == 0) {
            String result = writeLine(level + 1, xref, tag, value);
            levelShift = 1;
            shiftedLevel = level;
            return result;
        }
        return null;
    }

}

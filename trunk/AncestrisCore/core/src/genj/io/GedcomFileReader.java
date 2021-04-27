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

import genj.gedcom.TagPath;
import static genj.io.PropertyReader.RESOURCES;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import org.openide.util.Exceptions;

/**
 *
 * @author Daniel & Frederic
 */
public class GedcomFileReader extends PropertyReader {

    private String theLine = "";
    private TagPath path = null;
    private Charset charset = null;

    public TagPath getPath() {
        return path;
    }

    public static GedcomFileReader create(File fileIn) {
        GedcomEncodingSniffer sniffer;
        Charset charset;
        try {
            sniffer = new GedcomEncodingSniffer(new BufferedInputStream(new FileInputStream(fileIn)));
            charset = sniffer.getCharset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        GedcomFileReader reader = new GedcomFileReader(new InputStreamReader(sniffer, charset));
        reader.setCharset(charset);
        return reader;
    }

    private GedcomFileReader(Reader in) {
        super(in, null, false);
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public String getValue() {
        return value;
    }

    public String getXref() {
        return xref;
    }

    public int getLevel() {
        return level;
    }

    public String getNextLine(boolean consume) throws IOException {
        boolean ret = readLine(false, true);
        theLine = line;
        if (!ret && tag == null) {   // empty line, return 
            throw new GedcomFormatException(RESOURCES.getString("read.error.norecord"), 0);
        }
        if (level <= 0) {
            path = new TagPath(tag);
        } else {
            try {
                path = new TagPath(new TagPath(path, level), tag);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new GedcomFormatException(RESOURCES.getString("read.warn.badlevel", level), lines);
            }
        }
        if (consume) {
            line = null;
        }
        return theLine;
    }

    public void close() throws IOException {
        in.close();
    }

    public String getTag() {
        return tag;
    }

    public String getLine() {
        return theLine;
    }

    
    
    /**
     * Import needs a separate simpler read function with the ability to read incorrect Gedcom format lines
     * @return line 
     */
    public String getRawLine() {
        
        // Grab raw line even if for empty lines
        while (line == null) {
            try {
                line = in.readLine();
            } catch (IOException ex) {
                return null;
            }
            if (line == null) {
                return null;
            }
            lines++;
        }

        
        // Split line
        String[] splitLine = line.split("\\s", -1);
        int current_token = 0;

        
        
        // Get level
        try {
            level = Integer.parseInt(splitLine[current_token], 10);
            current_token++;
        } catch (NumberFormatException nfe) {
            level = -1;
        }

        
        
        // Get tag
        if (splitLine.length > 0) {
            // try to get a tag if there is multiple spaces.
            while (current_token < splitLine.length) {
                tag = splitLine[current_token];
                current_token++;
                if (tag != null && !tag.isEmpty()) {
                    break;
                }
            }
        } else {
            tag = "_TAG";
        }

        
        
        // Get xref
        if (level == 0 && tag.startsWith("@")) {

            // .. valid ?
            if (!tag.endsWith("@") || tag.length() <= 2) {
                while (current_token < splitLine.length && !tag.endsWith("@")) {
                    tag += " " + splitLine[current_token];
                    current_token++;
                }
            }
            if (!tag.endsWith("@") || tag.length() <= 2) {
                xref = "";
            } else {
                xref = tag.substring(1, tag.length() - 1);
            }
            tag = splitLine[current_token];
            current_token++;

        } else {
            xref = "";
        }
        tag = tag.intern();

        
        
        // Get value
        if (current_token < splitLine.length) {
            value = splitLine[current_token];
            current_token++;
            while (current_token < splitLine.length) {
                value += " " + splitLine[current_token];
                current_token++;
            }

        } else {
            value = "";
        }

        // Get path
        if (level == 0) {
            path = new TagPath(tag);
        } else if (level > 0) {
            try {
                path = new TagPath(new TagPath(path, level), tag);
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        }

        theLine = line;
        line = null;
        
        return theLine;
    }


    
    
}

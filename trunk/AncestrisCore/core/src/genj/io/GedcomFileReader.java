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
            path = new TagPath(new TagPath(path, level), tag);
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

}

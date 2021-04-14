/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.io.input;

import ancestris.util.swing.FileChooserBuilder;
import genj.io.InputSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper for File management in Ancetris.
 *
 * @author Zurga
 */
public class FileInput extends InputSource {

    private File file;

    public FileInput(File file) {
        this(file.getName(), file);

    }

    public FileInput(String name, File file) {
        super(name);
        this.file = file;
        this.setLocation(file.getAbsolutePath());
    }

    public File getFile() {
        return file;
    }

    @Override
    public InputStream open() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FileInput)) {
            return false;
        }
        FileInput that = (FileInput) obj;
        return that.file.equals(this.file) && that.getName().equals(this.getName());
    }

    @Override
    public int hashCode() {
        return file.hashCode() * getName().hashCode();
    }

    @Override
    public String toString() {
        return "file name=" + getName() + " file=" + file.toString();
    }
    
    @Override
    public String getExtension() {
        return FileChooserBuilder.getExtension(file.getName());
    }

}

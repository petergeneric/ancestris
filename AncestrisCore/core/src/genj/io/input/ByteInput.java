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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Wrapper for Bytes Management in ancestris.
 * @author Zurga
 */
 public class ByteInput extends InputSource {
    
    private final byte[] bytes;

    public ByteInput(String name, byte[] bytes) {
      super(name);
      this.bytes = bytes;
      // Prevent null pointer on location.
      setLocation("");
    }
    
    @Override
    public InputStream open() {
      return new ByteArrayInputStream(bytes);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ByteInput)) {
            return false;
        }
      return Arrays.equals(this.bytes, ((ByteInput)obj).bytes);
    }
    
    @Override
    public int hashCode() {
      return Arrays.hashCode(bytes);
    }
    
    @Override
    public String toString() {
      return "byte array size="+bytes.length+" name="+getName();
    }
    
    @Override
    public String getExtension() {
        return FileChooserBuilder.getExtension(getName());
    }

  }

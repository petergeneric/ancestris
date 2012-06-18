/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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
package genj.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Source of Input
 */
public abstract class InputSource {
  
  private String name;
  
  protected InputSource(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public abstract InputStream open() throws IOException;
  
  public static InputSource get(File file) {
    return get(file.getName(), file);
  }

  public static InputSource get(String name, File file) {
    return new FileInput(name, file);
  }
  
  public static InputSource get(String name, byte[] bytes) {
    return new ByteInput(name, bytes);
  }
  
  public static class FileInput extends InputSource {
    
    private File file;

    public FileInput(File file) {
      this(file.getName(), file);
    }
    public FileInput(String name, File file) {
      super(name);
      this.file = file;
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
      if (!(obj instanceof FileInput))
        return false;
      FileInput that = (FileInput)obj;
      return that.file.equals(this.file) && that.getName().equals(this.getName());
    }
    
    @Override
    public int hashCode() {
      return file.hashCode();
    }
    
    @Override
    public String toString() {
      return "file name="+getName()+" file="+file.toString();
    }
    
  }
  
  public static class ByteInput extends InputSource {
    
    private byte[] bytes;

    public ByteInput(String name, byte[] bytes) {
      super(name);
      this.bytes = bytes;
    }
    
    @Override
    public InputStream open() {
      return new ByteArrayInputStream(bytes);
    }
    
    @Override
    public boolean equals(Object obj) {
      return obj instanceof ByteInput && ((ByteInput)obj).bytes.equals(bytes);
    }
    
    @Override
    public int hashCode() {
      return bytes.hashCode();
    }
    
    @Override
    public String toString() {
      return "byte array size="+bytes.length+" name="+getName();
    }
    
  }
  
}

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

import genj.io.input.ByteInput;
import genj.io.input.FileInput;
import genj.io.input.URLInput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

/**
 * Source of Input
 */
public abstract class InputSource {
  
  private final String name;
  private String location;
  
  protected InputSource(String name) {
    this.name = name;
  }
  
  public final String getName() {
    return name;
  }
  
  public final String getLocation() {
      return location;
  }
  
  protected final void setLocation(String loc) {
      this.location = loc;
  }
  
  public abstract InputStream open() throws IOException;
  
  public abstract String getExtension();
  
  public static Optional<InputSource> get(File file) {
      if (file == null) {
          return Optional.empty();
      }
    return get(file.getName(), file);
  }

  public static Optional<InputSource> get(String name, File file) {
      if (file == null) {
          return Optional.empty();
      }
    return Optional.of(new FileInput(name, file));
  }
  
  public static Optional<InputSource> get(String name, byte[] bytes) {
      if (bytes == null) {
          return Optional.empty();
      }
    return Optional.of(new ByteInput(name, bytes));
  }
  
  public static Optional<InputSource> get(URL url) {
      if (url == null) {
          return Optional.empty();
      }
      return get(url.getFile(), url);
  }
  
  public static Optional<InputSource> get(String name, URL url) {
      if (url == null) {
          return Optional.empty();
      }
      return Optional.of(new URLInput(name, url));
  }
  
}

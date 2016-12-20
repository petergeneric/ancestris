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
package genj.util;

import genj.renderer.DPI;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A utility class that offers image meta-information
 */
public class ImageSniffer {

  /** sniffed suffix */
  private String suffix = null;

  /** bytes read */
  private int read = 0;

  /** sniffed values */  
  protected Dimension dimension;
  protected DPI dpi = new DPI(72,72);
    
  /**
   * Constructor
   */
  public ImageSniffer(File file) {
    try {
      FileInputStream in = new FileInputStream(file);
      init(in);
      in.close();
    } catch (IOException e) {
    }
  }
  
  /**
   * Constructor
   */
  public ImageSniffer(InputStream in) {
    init(in);
  }

  private void init(InputStream in) {
    
    try {
  
      // check first 2 bytes for type
      int tag = (read(in)&0xff) << 8 | (read(in)&0xff);
  
      switch (tag) {
        case 0x4749: sniffGif(in);break;
        case 0x8950: sniffPng(in);break;
        case 0xffd8: sniffJpg(in);break;
        case 0x424d: sniffBmp(in);break;
        default:
      }    
      
    } catch (IOException e) {
    }

    // validate what we've got
    if (dpi!=null&&(dpi.horizontal()<=0||dpi.vertical()<=0))
      dpi = null;
    if (dimension!=null&&(dimension.width<1||dimension.height<1))
      dimension = null;
    
    // done    
  }
  
  /**
   * Accessor - suffix
   */
  public String getSuffix() {
    return suffix;
  }
  
  /**
   * Accessor - resolution (dpi)
   * @return null if unknown
   */
  public DPI getDPI() {
    return dpi;
  }

  /**
   * Accessor - dimension in points
   * @return null if unknown
   */
  public Dimension getDimension() {
    return dimension;
  }

  /**
   * Accessor - dimension
   * @return size in inches or null if not known
   */
  public Dimension2D getDimensionInInches() {
    // check whether we have a resolution
    if (dpi==null||dimension==null) 
      return null;
    return new Dimension2d(
      (double)dimension.width/dpi.horizontal(), 
      (double)dimension.height/dpi.vertical()
    );
  }
  
  /**
   * sniffer - gif
   */
  private void sniffGif(InputStream in) throws IOException {
    
    // two possible magic headers
    final int
      F89A = string2int("F89a"),
      F87A = string2int("F87a");
      
    // sniff rest of magic
    int magic = sniffIntBigEndian(in);
    if (magic!=F89A&&magic!=F87A)
      return;

    // width & height
    dimension = new Dimension(
    	sniffShortLittleEndian(in),
    	sniffShortLittleEndian(in)
    );

    // no resolution
          
    //  int flags = read();
    //  bitsPerPixel = ((flags >> 4) & 0x07) + 1;

    // done
    suffix = "gif";
  }

  /**
   * sniffer - png
   * 
   * @see http://www.libpng.org/pub/png/spec
   */
  private void sniffPng(InputStream in) throws IOException {

    // prepare chunk type identifies
    final int
      IHDR = string2int("IHDR"),
      IDAT = string2int("IDAT"),
      IEND = string2int("IEND"),
      PHYS = string2int("pHYs");
            
    // sniff rest of magic
    if (!sniff(in, new byte[]{0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a}))
      return;

    // len and type
    int len = sniffIntBigEndian(in);
    int type = sniffIntBigEndian(in);
    if (IHDR!=type)
      return;

    // width & height
    dimension = new Dimension(
      sniffIntBigEndian(in),
      sniffIntBigEndian(in)
    );
    
    // skip rest + crc
    skip(len-8+4, in);

    // look for pHYs
    while (true) {
      
      // len and type
      len = sniffIntBigEndian(in);
      type = sniffIntBigEndian(in);

      // chunk : dat?
      if (IDAT==type) break;

      // chunk : end?
      if (IEND==type) break;

      // chunk : pHYs?      
      if (PHYS==type) {
        int
          x = sniffIntBigEndian(in),
          y = sniffIntBigEndian(in);
        if (read(in)==1) { //meter
          dpi = new DPI(
          	(int)Math.round(2.54D*x/100),
          	(int)Math.round(2.54D*y/100)
          );
        }
        break;
      }
      
      // skip data + crc
      skip(len+4, in);
      
    }
    
    // set
    suffix = "png";
  }

  /**
   * sniffer - tiff
   * 
   * @see http://www.media.mit.edu/pia/Research/deepview/exif.html
   */
  private boolean sniffTiff(InputStream in) throws IOException {
    
    int start = read;

    // analyze TIFF header
    boolean intel;
    switch (sniffShortLittleEndian(in)) {
      case 0x4949: //II = intel
        intel = true;
        break;
      case 0x4d4d: //MM = motorola
        intel = false; 
        break;
      default:
        return false;
    }
    
    // skip 0x002a
    skip(2, in);
    
    // jump to IFD (Image File Directory)
    skip(sniffInt(in, intel)-(read-start), in);
    
    // loop directory looking for x/y resolution
    int xres = 0, yres = 0;
    for (int i=0,j=sniffShort(in,intel);i<j;i++) {
      // directory image information entry - 12 bytes
      int tag = sniffShort(in,intel),
          format = sniffShort(in,intel),
          components = sniffInt(in, intel),
          value = sniffInt(in, intel);
      switch (tag) {
      	case 0x011a: //x-resolution
      	  xres = value;
      		break;
      	case 0x011b: //y-resolution
      	  yres = value;
      		break;
      }
      // check next
    }
    
    // did we get resolution offsets that still work?
    if (xres<(read-start)||yres<(read-start)) 
      return false;
    
    // lookup resolution values
    if (xres<yres) {
      skip(xres-(read-start), in);
      xres = sniffInt(in, intel) / sniffInt(in, intel);
      skip(yres-(read-start), in);
      yres = sniffInt(in, intel) / sniffInt(in, intel);
    } else {
      skip(yres-(read-start), in);
      yres = sniffInt(in, intel) / sniffInt(in, intel);
      skip(xres-(read-start), in);
      xres = sniffInt(in, intel) / sniffInt(in, intel);
    }
    dpi = new DPI(xres, yres);
    
    // done
    return true;
    
  }
  
  /**
   * sniffer - jpg
   * 
   * @see http://www.dcs.ed.ac.uk/home/mxr/gfx/2d/JPEG.txt
   */
  private void sniffJpg(InputStream in) throws IOException {

    final byte[] 
      JFIF = "JFIF".getBytes(),
      EXIF = "Exif".getBytes();

    // loop chunks
    chunks: while (true) {
    
      // marker and size
      int 
        marker = sniffShortBigEndian(in),
        size = sniffShortBigEndian(in) - 2, // without 'size' itself
        start = read; 
        
      // marker?
      switch (marker) {
        case 0xffe1: // EXIF
          // looking for Exif and trailing short
          if (!sniff(in, EXIF))
            break;
          skip(2, in);
          // tiff from here
          sniffTiff(in);
          break;
        case 0xffe0: // jpeg APPx
          // looking for JFIF
          if (sniff(in, JFIF)) {
            // skip '0'(1) and version(2)
            skip(3, in);
            // check units
            switch (read(in)) {
              case 1: // dots per inch
                dpi = new DPI(sniffShortBigEndian(in), sniffShortBigEndian(in));
                break;
              case 2: // dots per cm
                dpi = new DPI(
	                (int)(sniffShortBigEndian(in) * 2.54f),
	                (int)(sniffShortBigEndian(in) * 2.54f)
                );
                break;
              }
          }
          break;
        case 0xffc0: // SOFn  ffc0 - ffcf (without 4&8)
        case 0xffc1:
        case 0xffc2:
        case 0xffc3:
        //case 0xffc4:
        case 0xffc5:
        case 0xffc6:
        case 0xffc7:
        //case 0xffc8:
        case 0xffc9:
        case 0xffca:
        case 0xffcb:
        case 0xffcc:
        case 0xffcd:
        case 0xffce:
        case 0xffcf:
          // bitsPerPixel = a * b
          read(in); //a
          dimension = new Dimension(
	          sniffShortBigEndian(in),
	          sniffShortBigEndian(in)
          );
          read(in); //b
          break;
        case 0xffd9:
          // EOI
          break chunks;
        default:
          if ((marker & 0xff00) != 0xff00)
            return; // not a valid marker
      }
      
      // skip rest of chunk
      skip(size-(read-start), in);
    }    
    
    // done
    suffix = "jpg";
  }

  /**
   * sniffer - bmp
   */
  private void sniffBmp(InputStream in) throws IOException {

    // skip some stuff
    skip(16, in);
    
    // width & height    
    dimension = new Dimension(
	    sniffIntLittleEndian(in),
	    sniffIntLittleEndian(in)
    );
    
    // skip short
    skip(2, in);

    // bits per pixel    
    int bitsPerPixel = sniffShortLittleEndian(in);
    if (bitsPerPixel != 1 && bitsPerPixel != 4 &&
        bitsPerPixel != 8 && bitsPerPixel != 16 &&
        bitsPerPixel != 24 && bitsPerPixel != 32) {
        return;
    }
    
    // skip two longs
    skip(8, in);
    
    // resolution
    dpi = new DPI(
      (int)Math.round(2.54D*sniffIntLittleEndian(in)/100), // dots per meter
      (int)Math.round(2.54D*sniffIntLittleEndian(in)/100)  // dots per meter
    );

    // done
    suffix = "bmp";
  }

  /**
   * Read one byte 
   */
  private int read(InputStream in) throws IOException {
    read++;
    return in.read();
  }
  
  /**
   * Skip bytes
   */
  private void skip(int num, InputStream in) throws IOException {
    read += num;
    if (num!=in.skip(num))
      throw new IOException("cannot skip");
  }

  /**
   * Sniffer - check magic
   */
  private boolean sniff(InputStream in, byte[] magic) throws IOException {
    for (int m=0;m<magic.length;m++) {
      int i = read(in);
      if (i==-1||i!=magic[m]) return false;
    }
    return true;
  }

  /**
   * Sniffer - check magic
   */
  private boolean sniff(InputStream in, String magic) throws IOException {
    return sniff(in, magic.getBytes());
  }
  
  /**
   * Sniffer - int as intel or motorola
   */
  private int sniffInt(InputStream in, boolean intel) throws IOException {
    return intel ? sniffIntLittleEndian(in) : sniffIntBigEndian(in);  
  }
  
  /**
   * Sniffer - int big endian
   */
  private int sniffIntBigEndian(InputStream in) throws IOException {
    return
      (read(in) & 0xff) << 24 | 
      (read(in) & 0xff) << 16 | 
      (read(in) & 0xff) <<  8 | 
      (read(in) & 0xff)       ;
  }
  
  /**
   * Sniffer - int little endian
   */
  private int sniffIntLittleEndian(InputStream in) throws IOException {
    return
      (read(in) & 0xff)       | 
      (read(in) & 0xff) <<  8 | 
      (read(in) & 0xff) << 16 | 
      (read(in) & 0xff) << 24 ;
  }

  /**
   * Sniffer - short as intel or motorola
   */
  private int sniffShort(InputStream in, boolean intel) throws IOException {
    return intel ? sniffShortLittleEndian(in) : sniffShortBigEndian(in);  
  }
  
  /**
   * Sniffer - short big endian
   */  
  private int sniffShortBigEndian(InputStream in) throws IOException {
    return
      (read(in) & 0xff) << 8 | 
      (read(in) & 0xff)      ;
  }
  
  /**
   * Sniffer - short big endian
   */
  private int sniffShortLittleEndian(InputStream in) throws IOException {
    return 
      (read(in) & 0xff)      | 
      (read(in) & 0xff) << 8 ;
  }
  
  /**
   * transform a string to int (4 bytes)
   */
  private int string2int(String s) {
    if (s.length()!=4) throw new IllegalArgumentException();
    return
      (s.charAt(0) & 0xff) << 24 | 
      (s.charAt(1) & 0xff) << 16 | 
      (s.charAt(2) & 0xff) <<  8 | 
      (s.charAt(3) & 0xff)       ;
  }
  
} //ImageSniffer

/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.renderer;

import genj.gedcom.Entity;
import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyXRef;
import genj.io.InputSource;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * A renderer of media - it can find suitable media information to render,
 * offers quick size access and best offer scaling results with caching
 */
public class MediaRenderer {

    private final static Logger LOG = Logger.getLogger("ancestris.renderer");

    private final static Map<String, CacheEntry> CACHE = new WeakHashMap<>();
    
    // Keep the last getImage to avoid to retrieve again.
    private final static Map<String, BufferedImage> CACHE_IMAGE = new WeakHashMap<>(1);

    /**
     * Get Size
     *
     * @param root Property
     * @return Image dimensions
     */
    public static Dimension getSize(Property root) {

        // check cache against newly resolved source
        InputSource source = getSource(root);
        if (source == null) {
            return new Dimension();
        }

        return getSize(source);
    }

    public static Dimension getSize(InputSource source) {

        CacheEntry cached = CACHE.get(source.getName());
        if (cached != null) {
            return cached.size;
        } else {
            cached = new CacheEntry();
        }
        cached.source = source;
        cached.size = new Dimension();
        CACHE.put(source.getName(), cached);

        // read new
        try (InputStream in = source.open()) {
            if (in != null) {
                LOG.finer("Reading size from " + source);
                ImageInputStream iin = ImageIO.createImageInputStream(in);
                Iterator<ImageReader> iter = ImageIO.getImageReaders(iin);
                if (iter.hasNext()) {
                    ImageReader reader = iter.next();
                    try {
                        reader.setInput(iin, false, false);
                        cached.size.setSize(reader.getWidth(0), reader.getHeight(0));
                    } finally {
                        reader.dispose();
                    }
                }
            }
        } catch (IOException ioe) {
            LOG.log(Level.FINER, "Can't get image dimension for " + source.getName(), ioe);
        }

        return cached.size;
    }

    public static InputSource getSource(Property prop) {

        final Optional<InputSource> ois = getInternalSource(prop);
        if (ois.isPresent()) {
            return ois.get();
        }
        // nothing found
        return null;
    }

    private static Optional<InputSource> getInternalSource(Property prop) {
        // a filep?
        if (prop instanceof PropertyFile) {
            return ((PropertyFile) prop).getInput();
        }

        // a blob?
        if (prop instanceof PropertyBlob) {
            return ((PropertyBlob) prop).getInput();
        }
        // contained OBJE?
        for (int i = 0; i < prop.getNoOfProperties(); i++) {
            Property child = prop.getProperty(i);

            // OBJE > OBJE?
            if (child instanceof PropertyXRef) {
                Entity e = ((PropertyXRef) child).getTargetEntity();
                if (e instanceof Media) {
                    Media m = (Media) e;
                    PropertyBlob blob = m.getBlob();
                    if (blob != null) {
                        return blob.getInput();
                    }
                    return Optional.ofNullable(m.getFile());
                }
            }

            // OBJE|file?
            if ("OBJE".equals(child.getTag())) {
                Property filep = child.getProperty("FILE");
                if (filep instanceof PropertyFile) {
                    PropertyFile file = ((PropertyFile) filep);
                    return file.getInput();
                }
            }
        }

        // nothing found
        return Optional.empty();
    }

    /**
     * Render graphics.
     *
     * @param g Graphics target
     * @param bounds Rectangle limits
     * @param root Property to display
     */
    public static void render(Graphics g, Rectangle bounds, Property root) {
        Optional<BufferedImage> oImage = getScaleImage(root, bounds.width, bounds.height);

        // render what we have
        if (oImage.isPresent()) {
            Image image = oImage.get();
            g.drawImage(image,
                    bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
                    0, 0, image.getWidth(null), image.getHeight(null),
                    null
            );
        }
        // done
    }

    /**
     * Get Image limited to bounds.
     *
     * @param prop Property of the Image
     * @param x Width
     * @param y Height
     * @return Optional of image.
     */
    public static Optional<BufferedImage> getScaleImage(Property prop, int x, int y) {
        InputSource source = getSource(prop);
        if (source == null) {
            return Optional.empty();
        }

        return getScaleImage(source, x, y);
    }

    /**
     * Get Image from InputSource
     *
     * @param source Source
     * @param x destination width
     * @param y destination height
     * @return Image scaled.
     */
    public static Optional<BufferedImage> getScaleImage(InputSource source, int x, int y) {
        Dimension render = new Dimension(x, y);
        // load image
        BufferedImage image = null;
        try (InputStream in = source.open()) {
            if (in != null) {
                LOG.log(Level.FINE, "Reading image from {0} for {1} and {2}", new Object[]{source, x, y});
                try (ImageInputStream iin = ImageIO.createImageInputStream(in)) {

                    Iterator<ImageReader> iter = ImageIO.getImageReaders(iin);
                    if (iter.hasNext() && x > 0 && y > 0) {
                        ImageReader reader = iter.next();
                        try {
                            reader.setInput(iin, false, false);
                            Dimension size = new Dimension(reader.getWidth(0), reader.getHeight(0));
                            ImageReadParam param = reader.getDefaultReadParam();

                            param.setSourceSubsampling(
                                    Math.max(1, (int) Math.floor(size.width / x)),
                                    Math.max(1, (int) Math.floor(size.height / y)),
                                    0, 0);
                            image = reader.read(0, param);

                        } finally {
                            reader.dispose();
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            LOG.log(Level.FINER, "Can't get image for " + source, ioe);
        }

        return Optional.ofNullable(image);
    }

    public static Optional<BufferedImage> getImage(InputSource inputSource) {
        if (inputSource == null) {
            return Optional.empty();
        }
        
        if (CACHE_IMAGE.containsKey(inputSource.getName())) {
            return Optional.ofNullable(CACHE_IMAGE.get(inputSource.getName()));
        }

        BufferedImage image = null;
        try (InputStream in = inputSource.open()) {
            if (in != null) {
                try (ImageInputStream iin = ImageIO.createImageInputStream(in)) {
                    image = ImageIO.read(iin);                                            // TODO: This might take a while for certain pictures. Try to optimize.
                }
            }
        } catch (IOException ioe) {
            LOG.log(Level.FINER, "Can't get image for " + inputSource, ioe);
        }
        
        // Keep only one to prévent bloat memory.
        CACHE_IMAGE.clear();
        CACHE_IMAGE.put(inputSource.getName(), image);

        return Optional.ofNullable(image);
    }

    private static Dimension fit(Dimension a, Dimension b) {
        float scale = Math.min(b.width / (float) a.width, b.height / (float) a.height);
        return new Dimension((int) (a.width * scale), (int) (a.height * scale));
    }

    private static class CacheEntry {

        InputSource source;
        Dimension size;
    }
}

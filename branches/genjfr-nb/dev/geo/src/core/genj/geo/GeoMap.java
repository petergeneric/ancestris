/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2005 Nils Meier <nils@meiers.net>
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
package genj.geo;

import genj.util.Origin;
import genj.util.Resources;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.NoninvertibleTransformException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;

import org.geotools.shapefile.Shapefile;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;

/**
 * An available Map
 */
public class GeoMap {
  
  private final static String 
    SUFFIX_SHP = ".shp",
    PROPERTIES = "geo.properties";
  
  /** origin of map */
  private Origin origin;
  
  /** resources */
  private Resources resources;
  
  /** name */
  private String name;
  
  /** background color */
  private Color background = Color.WHITE;
  
  /** constructor */
  /*package*/ GeoMap(File fileOrDir) throws IOException {
    
    // no file at this point
    origin = Origin.create(fileOrDir.toURL());

    // load properties
    loadProperties();
    
    // done
  }
  
  /** load properties */
  private void loadProperties() {
    
    // load properties
    try {
      resources = new Resources(origin.open(PROPERTIES));
    } catch (IOException e) {
    }
    
    // init name&color
    name = translate("name", origin.getName());
    try {
      background =  new Color(Integer.decode(translate("color.background", "")).intValue());
    } catch (Throwable t) {
      background = new Color(0xccffff);
    }
    
    
  }
  
  /** a key */
  public String getKey() {
    return origin.getName();
  }
  
  /** resource access */
  private String translate(String key, String fallback) {
    // no resource?
    if (resources==null)
      return fallback;
    
    // try current language
    String result = resources.getString(key+"."+Locale.getDefault().getLanguage().toLowerCase(), false);
    if (result==null) 
      result = resources.getString(key, false);
    return result!=null ? result : fallback;
  }
  
  /** name */
  public String getName() {
    return name;
  }
  
  /** background color */
  public Color getBackground() {
    return background;
  }
 
  private final static Color[] PALETTE = {
    new Color(0xfbb3ad), new Color(0xb2cce2), new Color(0xccebc5), new Color(0xdecbe4), new Color(0xfed9a5), new Color(0xffffcc), new Color(0xe4d7bc), new Color(0xfddaec), new Color(0xf2f2f2)
  };
  
  /** 
   * load all feature collections for this geo map into LayerManager  
   */
  void load(LayerManager manager) throws IOException {
    
    // reload properties
    loadProperties();

    // load shapes files
    String[] shapes = origin.list();
    Arrays.sort(shapes);
    for (int i=0;i<shapes.length;i++) {
      
      // shape file?
      String shape = shapes[i];
      if (!shape.endsWith(SUFFIX_SHP)) 
        continue;
      String name = shape.substring(0, shape.length()-SUFFIX_SHP.length());
      
      // load it
      FeatureCollection fc = load(origin.open(shape));
      
      // create layer
      Layer layer = manager.addLayer(getName(), name, fc);
      
      // check for parameters
      if (Character.isDigit(name.charAt(0))) name = name.substring(1);
      String color = translate("color."+name, "");

      // set color
      try {
        Color c = new Color(Integer.decode(color).intValue());
        BasicStyle style = layer.getBasicStyle();
        style.setFillColor(c);
        style.setAlpha(255);
        style.setLineColor(Layer.defaultLineColor(c));
      } catch (NumberFormatException nfe) {
        
        // add a cycling color style
        layer.removeStyle(layer.getBasicStyle());
        layer.addStyle(new BasicStyle() {
          public void paint(Feature feature, Graphics2D graphics2d, Viewport viewport) throws NoninvertibleTransformException {
            Color c = PALETTE[feature.getGeometry().getNumPoints()%PALETTE.length];
            setFillColor(c);
            setLineColor(Layer.defaultLineColor(c));
            super.paint(feature, graphics2d, viewport);
          }
        });
        
      }
      
      // next
    }

    // done
  }
  
  /** load a feature collection for given shape file into layer manager */
  private FeatureCollection load(InputStream in) throws IOException {

    // read geometric shapes from file
    GeometryCollection gc;
    try {
      gc = new Shapefile(in).read(new GeometryFactory());
    } catch (Throwable t) {
      if (t instanceof IOException)
        throw (IOException)t;
      throw new IOException(t.getMessage());
    } finally {
      if (in!=null) in.close();
    }

    // pack into FeatureCollection
    FeatureSchema schema = new FeatureSchema();
    schema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
    
    FeatureDataset result = new FeatureDataset(schema);
    
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      Feature feature = new BasicFeature(schema);
      Geometry geo = gc.getGeometryN(i);
      feature.setGeometry(geo);
      result.add(feature);
    }
    
    return result;
    
  }
  
}//GeoMap
